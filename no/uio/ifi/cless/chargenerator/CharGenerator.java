package no.uio.ifi.cless.chargenerator;

/*
 * module CharGenerator
 */

import java.io.*;

import no.uio.ifi.cless.cless.CLess;
import no.uio.ifi.cless.error.Error;
import no.uio.ifi.cless.log.Log;

/*
 * Module for reading single characters.
 */
public class CharGenerator {
    public static char curC, nextC;

    private static LineNumberReader sourceFile = null;
    private static String sourceLine;
    private static int sourcePos;

    public static void init() {
        try {
            sourceFile = new LineNumberReader(new FileReader(CLess.sourceName));
        } catch (FileNotFoundException e) {
            Error.error("Cannot read " + CLess.sourceName + "!");
        }
        sourceLine = "";
        sourcePos = 0;
        curC = nextC = ' ';
        readNext();
        readNext();
    }

    public static void finish() {
        if (sourceFile != null) {
            try {
                sourceFile.close();
            } catch (IOException e) {
                Error.error("Could not close source file!");
            }
        }
    }

    public static boolean isMoreToRead() {
        try {
            //Markerer hvor vi startet i filen
            sourceFile.mark(10);
            if (sourceFile.read() == -1) {
                //Returnerer true hvis det er slutt på filen
                return true;
            } else {
                //Setter tilbake til starten
                sourceFile.reset();
                return false;
            }
        } catch (IOException e) {
            Error.error("Could not read source file");
            return false;
        }
    }

    public static int curLineNum() {
        return (sourceFile == null ? 0 : sourceFile.getLineNumber());
    }

    public static void readNext() {
        curC = nextC;
        if (!isMoreToRead()) {
            try {
                char newChar = (char)sourceFile.read();
                curC = nextC;
                nextC = newChar;
                sourcePos++;
            }
            catch (IOException e) {
                Error.error("Could not read character from sourcefile");
            }
        }
    }
}
