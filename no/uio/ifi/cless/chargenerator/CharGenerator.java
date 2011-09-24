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
            //Marks where we are in the file
            sourceFile.mark(10);
            String line = sourceFile.readLine();
            //If line is null we are at the end of the file
            if (line == null) {
                nextLine(false);
                return false;
            } else {
                //Resets the file back to where we began
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
        //Checks if we are finished with the current line
        if (sourceLine.length() == sourcePos) {
            nextLine(false);
            readNext();
            return;
        }
        if (isMoreToRead()) {
            //Gets the next character
            char newChar = sourceLine.charAt(sourcePos++);
            //Throws away the rest of the line with #
            if (newChar == '#') {
                nextLine(true);
                readNext();
                return;
            }
            nextC = newChar;
        }
    }

    public static void nextLine(boolean comment) {
        try {
            //Checks if it is the last line or a comment line
            //If not we write the line to the log
            if (sourceFile.getLineNumber() != 0 && !comment && sourceLine != null) {
                Log.noteSourceLine(sourceFile.getLineNumber(), sourceLine);
            }
            //Reads the next line and sets the position to zero
            sourceLine = sourceFile.readLine();
            sourcePos = 0;
        } catch (IOException e) {
            Error.error("Could not readNextLine");
        }
    }
}
