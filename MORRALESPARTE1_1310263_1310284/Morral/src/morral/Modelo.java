/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package morral;

import javax.swing.JOptionPane;
import lpsolve.LpSolve;
import lpsolve.LpSolveException;

public class Modelo {

    LpSolve solver;
    double M = 0;
    int numeroCajas = 0;
    double[] volumenesCajas = null;
    double[] pesosCajas = null;
    double volumenMorral = 0;
    double pesoMorral = 0;
    String[] resultadoFinal = null;
    double[] resultadoVariables = null;
    String[] resultadoVariablesAMostrar = null;
    double resultadoFuncionObjetivo = 0;
    double[] row = null;
    int Ncol = 0;

    public Modelo(int numeroCajas, double[] volumenesCajas, double[] pesosCajas, double volumenMorral, double pesoMorral) {

        this.numeroCajas = numeroCajas;
        this.volumenesCajas = volumenesCajas;
        this.pesosCajas = pesosCajas;
        this.volumenMorral = volumenMorral;
        this.pesoMorral = pesoMorral;
        this.Ncol = numeroCajas + (numeroCajas * numeroCajas);//numero de columnas en total=numero de variables.        
        this.row = new double[Ncol + 1];

    }

    public String[] modelar() throws LpSolveException {

        solver = LpSolve.makeLp(0, Ncol + 1);
        M = definirM(pesosCajas, volumenesCajas);
        System.out.println("M definida " + M);

        this.nombrarVariables();
        this.declararBinarias();
        this.armarFuncionObjetivo();
        this.armarPrimeraRestriccion();
        this.armarSegundaRestriccion();
        this.armarTerceraRestriccion();
        this.armarCuartaRestriccion();
        //solver.setBbRule(LpSolve.NODE_FIRSTSELECT);
        //solver.setBbRule(LpSolve.NODE_GAPSELECT);
        solver.setBbRule(LpSolve.NODE_RANGESELECT);
        //solver.setBbRule(LpSolve.NODE_FRACTIONSELECT);
        //solver.setBbRule(LpSolve.NODE_PSEUDOCOSTSELECT);
        //solver.setBbRule(LpSolve.NODE_PSEUDONONINTSELECT);
        //solver.setBbRule(LpSolve.NODE_PSEUDORATIOSELECT);
        //solver.setBbRule(LpSolve.NODE_USERSELECT);
        solver.solve();
        solver.writeLp("src/morral/modeloMorral.lp");
        resultadoFuncionObjetivo = solver.getObjective();
        resultadoVariables = solver.getPtrVariables();
        for (int i = 0; i < resultadoVariables.length - 1; i++) {
            System.out.println("Variables " + resultadoVariables[i] + " nombre " + solver.getColName(i + 1));
        }
        System.out.println("FUNCION OBJETIVO " + resultadoFuncionObjetivo);

        if ((resultadoFuncionObjetivo >= 1) && (resultadoFuncionObjetivo <= numeroCajas)) {

            //Enviar resultado final a interfaz
            resultadoVariablesAMostrar = this.clasificarCajas(resultadoVariables, resultadoFuncionObjetivo);
            //System.out.println("Tamaño Arreglo "+ resultadoVariablesAMostrar.length);
            resultadoFinal = new String[2 + resultadoVariablesAMostrar.length];
            resultadoFinal[0] = Integer.toString(resultadoFinal.length);
            resultadoFinal[1] = Double.toString(resultadoFuncionObjetivo);
            for (int i = 0; i < resultadoVariablesAMostrar.length; i++) {
                resultadoFinal[i + 2] = resultadoVariablesAMostrar[i];
            }

            solver.printObjective();
            solver.printLp();
            solver.deleteLp();

        } else {
            JOptionPane.showMessageDialog(null, "La solucion no es factible");

        }
        return resultadoFinal;

    }

    public double definirM(double[] pCajas, double[] vCajas) throws LpSolveException {

        for (int i = 0; i < pCajas.length; i++) {
            M += pCajas[i];
        }

        for (int i = 0; i < vCajas.length; i++) {
            M += vCajas[i];
        }

        return M;
    }

    public void declararBinarias() throws LpSolveException {
        for (int i = 1; i <= Ncol; i++) {
            try {
                solver.setBinary(i, true);
            } catch (LpSolveException e) {
                e.printStackTrace();
            }
        }

    }

    public String[] clasificarCajas(double variables[], double resultadoFO) throws LpSolveException {

        //String morralesCaja[] = new String[numeroCajas];
        String morralesCaja[] = new String[variables.length];

        for (int i = 0; i < morralesCaja.length; i++) {
            morralesCaja[i] = "";

        }

        int caja = 1;
        for (int i = 0; i < numeroCajas; i++) {
            if (variables[i] == 1) {
//                 System.out.println("ENTRO i");
                for (int j = i + numeroCajas; j < variables.length; j = j + numeroCajas) {
                    if (variables[j] == 1) {
                        morralesCaja[i] += "Caja " + caja + ";";
                        //System.out.println("Morral " + solver.getColName(i) + " Caja " + (j - numeroCajas));
//                     System.out.println("ENTRO j" );                     
                    }
                    caja++;
                }
                caja = 1;
            }
        }

        return morralesCaja;
    }

    public void nombrarVariables() throws LpSolveException {

        int numcol = 0;
        numcol = 1;
        for (int i = 1; i <= numeroCajas; i++) {
            solver.setColName(i, "y" + i);
        }
        for (int i = 1; i <= numeroCajas; i++) {
            for (int j = 1; j <= numeroCajas; j++) {
                solver.setColName(numeroCajas + numcol, "x" + i + j);
                numcol++;
            }
        }

    }

    public void armarFuncionObjetivo() throws LpSolveException {
        for (int i = 1; i <= numeroCajas; i++) {
            row[i] = 1;
            //System.out.println(i + " row " + row[i]);
        }
        solver.setObjFn(row);
    }

    public void armarPrimeraRestriccion() throws LpSolveException {
        int valorinicial = 0;
        int limite = 0;

        valorinicial = numeroCajas + 1;
        limite = valorinicial + numeroCajas;
        for (int i = 0; i < numeroCajas; i++) {
            row = new double[Ncol + 1];
            for (int j = valorinicial; j <= limite - 1; j++) {
                row[j] = 1;
                //System.out.println("Restriccion primera " + j + " row " + row[j]);

            }
            valorinicial = limite;
            limite = valorinicial + numeroCajas;
            solver.addConstraint(row, LpSolve.EQ, 1);
        }
    }

    public void armarSegundaRestriccion() throws LpSolveException {

        int m = 0;
        int valorinicial = 0;

        for (int i = 0; i < numeroCajas; i++) {
            row = new double[Ncol + 1];
            valorinicial = numeroCajas + i + 1;
            //System.out.println("valor inicial " + valorinicial);
            m = 0;
            for (int j = valorinicial; j <= Ncol; j = j + numeroCajas) {

                row[j] = pesosCajas[m];
                //System.out.println(i + "Restriccion segundas " + j + " row " + row[j]);
                m++;

            }

            solver.addConstraint(row, LpSolve.LE, pesoMorral);

        }
    }

    public void armarTerceraRestriccion() throws LpSolveException {

        int valorinicial = 0;
        int m = 0;
        for (int i = 0; i < numeroCajas; i++) {
            row = new double[Ncol + 1];
            valorinicial = numeroCajas + i + 1;
            //System.out.println("valor inicial " + valorinicial);
            m = 0;
            for (int j = valorinicial; j <= Ncol; j = j + numeroCajas) {

                row[j] = volumenesCajas[m];
                //System.out.println(i + "Restriccion terceras " + j + " row " + row[j]);
                m++;

            }

            solver.addConstraint(row, LpSolve.LE, volumenMorral);

        }

    }

    public void armarCuartaRestriccion() throws LpSolveException {

        int valorinicial = 0;
        int m = 0;
        for (int i = 0; i < numeroCajas; i++) {
            row = new double[Ncol + 1];
            valorinicial = numeroCajas + i + 1;
            //System.out.println("valor inicial " + valorinicial);
            m = 0;
            row[i + 1] = -M;
            for (int j = valorinicial; j <= Ncol; j = j + numeroCajas) {

                row[j] = 1;
                //System.out.println(i + "Restriccion cuartas " + j + " row " + row[j]);
                m++;
            }
            solver.addConstraint(row, LpSolve.LE, 0);
        }
    }

}
