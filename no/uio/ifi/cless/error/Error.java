package no.uio.ifi.cless.error;

/*
 * module Error
 */

import no.uio.ifi.cless.log.Log;

/*
 * Print error messages.
 */
public class Error {
    public static void error(String where, String message) {
        //TODO

        System.exit(1);
    }

    public static void error(String message) {
        error("", message);
    }

    public static void error(int lineNum, String message) {
        error((lineNum > 0 ? "in line " + lineNum : ""), message);
    }

    public static void giveUsage() {
        System.err.println("Usage: cless [-c] [-log{B|P|S|T}] " +
                "[-test{scanner|parser}] file");
        System.exit(2);
    }

    public static void init() {
        //TODO
    }

    public static void finish() {
        //TODO
    }
}
