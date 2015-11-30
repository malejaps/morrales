/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package morral;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;

public class Modelo2 {

    LpSolve solver;
    int numeroCajas = 0;
    double[] volumenesCajas = null;
    double[] pesosCajas = null;
    double volumenMorral = 0;
    double pesoMorral = 0;
    String[] resultadoFinal = null;
    double[] resultadoVariables = null;
    String[] resultadoVariablesAMostrar = null;
    double resultadoFuncionObjetivo = 0;
    double resultadoTiempoEjecucion = 0;
    double[] row = null;//almacena la fila que se la manda por restriccion o por funcion objetivo
    double[] row2 = null;//usada para segunda parte de restriccion de valor absoluto
    int Ncol = 0;//numero en total de variables incluyendo las de la funcion objetivo y las de las restricciones

    int abs = 0;
    int numeroMorrales = 0;
    int[] arregloabs = null;
    int cantidadabs = 0;

    //----Ya
    public Modelo2(int numeroCajas, double[] volumenesCajas, double[] pesosCajas, double volumenMorral, double pesoMorral, double resultadoModelo1, int[] arregloabs) {

        this.numeroCajas = numeroCajas;
        this.volumenesCajas = volumenesCajas;
        this.pesosCajas = pesosCajas;
        this.volumenMorral = volumenMorral;
        this.pesoMorral = pesoMorral;
        this.numeroMorrales = (int) resultadoModelo1;

        //Por cada abs mayor a 0  hacer una restriccion de valor absoluto
        for (int i = 0; i < arregloabs.length; i++) {
            if (arregloabs[i] > 0) {
                cantidadabs++;

            }
        }

        this.Ncol = 1 + (numeroCajas * numeroMorrales);
        this.row = new double[Ncol + 1];
        //this.row2 = new double[Ncol + 1];
        this.numeroMorrales = (int) resultadoModelo1;
        this.arregloabs = arregloabs;

    }

    public String[] modelar() throws LpSolveException {

        solver = LpSolve.makeLp(0, Ncol);

        this.nombrarVariables();
        this.declararVariables();
        this.armarFuncionObjetivo();
        this.armarRestriccionDistribucion();
        this.armarPrimeraRestriccion();
        this.armarSegundaRestriccion();
        this.armarTerceraRestriccion();

        /*Pruebas BB*/
        solver.setBbRule(LpSolve.NODE_FIRSTSELECT);
        //solver.setBbRule(LpSolve.NODE_GAPSELECT);
        //solver.setBbRule(LpSolve.NODE_RANGESELECT);
        //solver.setBbRule(LpSolve.NODE_FRACTIONSELECT);
        //solver.setBbRule(LpSolve.NODE_PSEUDOCOSTSELECT);
        //solver.setBbRule(LpSolve.NODE_PSEUDONONINTSELECT);
        //solver.setBbRule(LpSolve.NODE_PSEUDORATIOSELECT);
        //solver.setBbRule(LpSolve.NODE_USERSELECT);
        /*Fin Pruebas BB*/

        /*Calcular tiempo de ejecucion de lpsolve*/
        double inicioTiempo = System.currentTimeMillis();
        solver.solve();
        resultadoTiempoEjecucion = System.currentTimeMillis() - inicioTiempo;
        /*Fin calcular tiempo de ejecucion de lpsolve*/

        solver.writeLp("src/morral/modelo2Morral.lp");
        resultadoFuncionObjetivo = solver.getObjective();
        resultadoVariables = solver.getPtrVariables();

        for (int i = 0; i < resultadoVariables.length; i++) {
            System.out.println("Variables modelo 2 " + resultadoVariables[i] + " nombre " + solver.getColName(i + 1));
        }

        //Enviar resultado final a interfaz
        resultadoVariablesAMostrar = this.clasificarCajas(resultadoVariables);
        for (int i = 0; i < resultadoVariables.length; i++) {
            System.out.println("Variables Resultado 2 "
                    + resultadoVariables[i] + " nombre " + solver.getColName(i + 1));
        }
        resultadoFinal = new String[2 + resultadoVariablesAMostrar.length];
        resultadoFinal[0] = Integer.toString(resultadoFinal.length);
        resultadoFinal[1] = Double.toString(resultadoFuncionObjetivo);

        for (int i = 0; i < resultadoVariablesAMostrar.length; i++) {
            resultadoFinal[i + 2] = resultadoVariablesAMostrar[i];
        }

        solver.printObjective();
        solver.printLp();
        solver.deleteLp();

        return resultadoFinal;

    }

    public double getResultadoTiempoEjecucion() {
        return resultadoTiempoEjecucion;
    }

    public void declararVariables() throws LpSolveException {

        solver.setInt(1, true);
        for (int i = 2; i <= Ncol; i++) {
            try {
                solver.setBinary(i, true);
            } catch (LpSolveException e) {
                e.printStackTrace();
            }
        }
    }

    //Se encarga de armar un arreglo indicando los morrales de cada caja
    public String[] clasificarCajas(double variables[]) throws LpSolveException {

        String morralesCaja[] = new String[variables.length];
        int valorinicial = 0;

        for (int i = 0; i < morralesCaja.length; i++) {
            morralesCaja[i] = "";

        }

        int caja = 1;

        for (int i = 0; i < numeroMorrales; i++) {

            valorinicial = 1 + i;
            for (int j = valorinicial; j < variables.length; j = j + numeroMorrales) {
                if (variables[j] == 1) {
                    morralesCaja[i] += "Caja " + caja + ";";
                }
                caja++;

            }

            caja = 1;
            //}
        }

        return morralesCaja;
    }

    public void nombrarVariables() throws LpSolveException {

        solver.setColName(1, "maxd");

        System.out.println("Numero de cajas " + numeroCajas);
        System.out.println("Numero de morrales " + numeroMorrales);

        int numcol = 0;

        for (int i = 1; i <= numeroCajas; i++) {
            for (int j = 1; j <= numeroMorrales; j++) {

                solver.setColName(2 + numcol, "x" + i + j);
                numcol++;
            }
        }

    }

    public void armarFuncionObjetivo() throws LpSolveException {

        row[1] = 1;
        solver.setObjFn(row);
    }

    //Priemra restriccion que garantiza el valor absoluto
    public void armarRestriccionDistribucion() throws LpSolveException {
        int m = 0;
        int r = 0;
        int restriccionContador = 1;
        int cantidadRestricciones = 0;

        for (int valorinicial = 2; valorinicial <= numeroMorrales; valorinicial++) {

            cantidadRestricciones = numeroMorrales - restriccionContador;

            //numero de filas correspondientes a cada restriccion  
            for (int i = 1; i <= cantidadRestricciones; i++) {
                row = new double[Ncol + 1];
                row2 = new double[Ncol + 1];

                m = 0;//contador para recorrer pesos
                row[1] = -1;//corresponde al -abs de cada restriccion
                row2[1] = -1;

                //numero de columnas correspondientes a cada columna del cuadro de restricciones
                for (int j = valorinicial; j < Ncol; j = j + numeroMorrales) {
                    //System.out.println("VARIABLE i " + i);
                    //System.out.println("VARIABLE j " + (j));
                    //System.out.println("VARIABLE j+i " + (j + i));
                    row[j] = pesosCajas[m];
                    row[j + i] = -pesosCajas[m];
                    row2[j] = -pesosCajas[m];
                    row2[j + i] = pesosCajas[m];
                    m++;
                }
                solver.addConstraint(row, LpSolve.LE, 0);
                solver.addConstraint(row2, LpSolve.LE, 0);

            }

            restriccionContador++;
        }

    }


    //Armar restriccion de sumas igual a 1
    public void armarPrimeraRestriccion() throws LpSolveException {
        int valorinicial = 0;
        int limite = 0;

        valorinicial = 2;
        limite = valorinicial + numeroMorrales;
        for (int i = 0; i < numeroCajas; i++) {
            row = new double[Ncol + 1];
            for (int j = valorinicial; j <= limite - 1; j++) {
                row[j] = 1;

            }
            valorinicial = limite;
            limite = valorinicial + numeroMorrales;
            solver.addConstraint(row, LpSolve.EQ, 1);
        }
    }

    //Armar restriccion del peso
    public void armarSegundaRestriccion() throws LpSolveException {

        int m = 0;
        int valorinicial = 0;

        for (int i = 0; i < numeroMorrales; i++) {
            row = new double[Ncol + 1];

            valorinicial = 2 + i;
            m = 0;
            for (int j = valorinicial; j <= Ncol; j = j + numeroMorrales) {

                row[j] = pesosCajas[m];
                m++;

            }

            solver.addConstraint(row, LpSolve.LE, pesoMorral);

        }
    }

    //Armar restriccion del volumen
    public void armarTerceraRestriccion() throws LpSolveException {

        int m = 0;
        int valorinicial = 0;

        for (int i = 0; i < numeroMorrales; i++) {
            row = new double[Ncol + 1];
            valorinicial = 2 + i;
            m = 0;
            for (int j = valorinicial; j <= Ncol; j = j + numeroMorrales) {

                row[j] = volumenesCajas[m];
                m++;
            }
            solver.addConstraint(row, LpSolve.LE, volumenMorral);
        }
    }
}
