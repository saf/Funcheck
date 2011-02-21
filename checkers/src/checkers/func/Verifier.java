/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package checkers.func;

import checkers.source.Result;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Runs the Prolog verifier for the fact file and prints errors.
 *
 * @author saf
 */
public class Verifier {

    public static final String rulesFilename = "rules.pl";
    public static final String factsFilename = FactPrinter.factsFilename;

    public static final String prologPath = "/usr/bin/swipl";

    protected FuncChecker checker;
    protected Process prolog;

    protected void prepareProcess() {
        try {
            ProcessBuilder builder = new ProcessBuilder(prologPath, "-g", "[verify].", "-t", "halt");
            builder.redirectErrorStream(false);
            prolog = builder.start();
        } catch (IOException e) {
            e.printStackTrace();
        };
        System.err.println("Prepared process");
    }

    protected void analyseOutput() {
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(prolog.getInputStream()));
        try {
            while (reader.ready()) {
                String line = reader.readLine();
                String str[] = line.split("@");
                if (str.length >= 2) {
                    checker.report(Result.failure(str[0]), checker.getNodeMapping().get(str[1]));
                } else {
                    System.err.println("Prolog --> " + line);
                }
            }
            System.err.println("EOF on Prolog output.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Verifier(FuncChecker c) {
        checker = c;
        prolog = null;
    }

    public void run() {
        prepareProcess();
        analyseOutput();
        try {
            prolog.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
