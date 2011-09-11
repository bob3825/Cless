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

            sourceFile.mark(10);
            String line = sourceFile.readLine();
            //System.out.println(line);
            if (line == null) {
                nextLine(false);
                return false;
            } else {
                sourceFile.reset();
                return true;
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
        if (sourceLine.length() == sourcePos) {
            nextLine(false);
            readNext();
            return;
        }
        if (isMoreToRead()) {

            char newChar = sourceLine.charAt(sourcePos++);
            if (newChar == '#') {
                nextLine(true);
                readNext();
                return;
            }
            nextC = newChar;
            //System.out.println(nextC);

        }
    }

    public static void nextLine(boolean comment) {
        try {
            if (sourceFile.getLineNumber() != 0 && !comment && sourceLine != null) {
                Log.noteSourceLine(sourceFile.getLineNumber(), sourceLine);
            }
            sourceLine = sourceFile.readLine();
            sourcePos = 0;
        } catch (IOException e) {
            Error.error("Could not readNextLine");
        }
    }
}
