package no.uio.ifi.cless.scanner;

/*
 * module Scanner
 */

import no.uio.ifi.cless.chargenerator.CharGenerator;
import no.uio.ifi.cless.error.Error;
import no.uio.ifi.cless.log.Log;

import java.util.concurrent.ConcurrentHashMap;

import static no.uio.ifi.cless.scanner.Token.*;

/*
 * Module for forming characters into tokens.
 */
public class Scanner {
    public static Token curToken, nextToken, nextNextToken;
    public static String curName, nextName, nextNextName;
    public static int curNum, nextNum, nextNextNum;
    public static int curLine, nextLine, nextNextLine;

    public static void init() {
        //TODO
    }

    public static void finish() {
        //TODO
    }

    public static void readNext() {
        curToken = nextToken;
        nextToken = nextNextToken;
        curName = nextName;
        nextName = nextNextName;
        curNum = nextNum;
        nextNum = nextNextNum;
        curLine = nextLine;
        nextLine = nextNextLine;

        nextNextToken = null;
        while (nextNextToken == null) {
            nextNextLine = CharGenerator.curLineNum();
            String tokenName = "";
            if (!CharGenerator.isMoreToRead()) {
                nextNextToken = isBraceToken(CharGenerator.curC);
                CharGenerator.readNext();
            } else {
                illegal("Illegal symbol: '" + CharGenerator.curC + "'!");
                nextNextToken = eofToken;
            }
        }
        Log.noteToken();
    }

    private static boolean isLetterAZ(char c) {
        return (c >= 'A' && c <= 'z');
    }

    private static Token getTokenType(String token) {
        return forToken;
    }

    private static  Token isBraceToken(char c) {
        switch (c) {
            case '[': return leftBracketToken;
            case ']': return rightBracketToken;
            case '(': return leftParToken;
            case ')': return rightParToken;
            case '{': return leftCurlToken;
            case '}': return rightCurlToken;
            default: return null;
        }

    }

    // Various error reporting methods
    // (They are placed in this package because most of them include 
    // information found here.)

    public static void illegal(String message) {
        Error.error(curLine, message);
    }

    public static void expected(String exp) {
        illegal(exp + " expected, but found a " + curToken + "!");
    }

    public static void check(Token t) {
        if (curToken != t)
            expected("A " + t);
    }

    public static void check(Token t1, Token t2) {
        if (curToken != t1 && curToken != t2)
            expected("A " + t1 + " or a " + t2);
    }

    public static void skip(Token t) {
        check(t);
        readNext();
    }

    public static void skip(Token t1, Token t2) {
        check(t1, t2);
        readNext();
    }
}
