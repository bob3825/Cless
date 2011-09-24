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
            //Markerer hvor vi er i filen
            sourceFile.mark(10);
            String line = sourceFile.readLine();
            //Hvis vi linjen er null er vi på slutten av filen
            if (line == null) {
                nextLine(false);
                return false;
            } else {
                //Setter filen tilbake til der vi markerte hvis den er mer a lese
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
        //Sjekker om vi er ferdig med gjeldende linje
        if (sourceLine.length() == sourcePos) {
            nextLine(false);
            readNext();
            return;
        }
        if (isMoreToRead()) {
            //Henter neste char
            char newChar = sourceLine.charAt(sourcePos++);
            //Kaster resten av linja som inneholder #
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
            //Sjekker om det er kommentarlinje og om det er siste linje
            //Hvis det er slik så skriver vi ikke linjen til fil
            if (sourceFile.getLineNumber() != 0 && !comment && sourceLine != null) {
                Log.noteSourceLine(sourceFile.getLineNumber(), sourceLine);
            }
            //Leser neste linje og setter posisjon til starten av den
            sourceLine = sourceFile.readLine();
            sourcePos = 0;
        } catch (IOException e) {
            Error.error("Could not readNextLine");
        }
    }
}
