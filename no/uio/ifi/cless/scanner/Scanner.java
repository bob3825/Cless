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
            if (CharGenerator.isMoreToRead()) {
                if(CharGenerator.curC == '!') System.out.println("!");
                if(isSingleToken(CharGenerator.curC)) CharGenerator.readNext();
                else if(isTwoToken(CharGenerator.curC)) CharGenerator.readNext();
                else if(isNumber(CharGenerator.curC)) {
                    String number = Character.toString(CharGenerator.curC);
                    CharGenerator.readNext();
                    while (isNumber(CharGenerator.curC)) {
                        number = number + Character.toString(CharGenerator.curC);
                        CharGenerator.readNext();
                    }

                    nextNextToken = numberToken;
                    nextNextNum = new Integer(number);
                }
                else if(CharGenerator.curC != '\n' && CharGenerator.curC != ' ') isWord(CharGenerator.curC);
                else CharGenerator.readNext();


            } else {
                nextNextToken = eofToken;

            }
        }
        Log.noteToken();
    }

    private static boolean isLetterAZ(char c) {
        return (c >= 'A' && c <= 'z');
    }
    private static boolean isNumber(char c) {
        return (c >= '0' && c <= '9');
    }

    private static Token getTokenType(String token) {
        return forToken;
    }

    private static void isWord(char c) {
        String word = Character.toString(CharGenerator.curC);
        CharGenerator.readNext();
        while (isLetterAZ(CharGenerator.curC) || isNumber(CharGenerator.curC)) {
            word = word + Character.toString(CharGenerator.curC);
            CharGenerator.readNext();
        }
        word = word.trim();
        if(word.compareTo("")==0) return;
        if(word.compareTo("int") == 0) nextNextToken = intToken;
        else if(word.compareTo("if") == 0) nextNextToken = ifToken;
        else if(word.compareTo("else") == 0) nextNextToken = elseToken;
        else if(word.compareTo("while") == 0) nextNextToken = whileToken;
        else if(word.compareTo("return") == 0) nextNextToken = returnToken;
        else {
            nextNextToken = nameToken;
            nextNextName = word;
        }
    }

    private static boolean isSingleToken(char c) {
        switch (c) {
            case '[': nextNextToken = leftBracketToken; return true;
            case ']': nextNextToken = rightBracketToken; return true;
            case '(': nextNextToken = leftParToken; return true;
            case ')': nextNextToken = rightParToken; return true;
            case '{': nextNextToken = leftCurlToken; return true;
            case '}': nextNextToken = rightCurlToken; return true;
            case '+': nextNextToken = addToken; return true;
            case '/': nextNextToken = divideToken; return true;
            case ',': nextNextToken = commaToken; return true;
            case '*': nextNextToken = multiplyToken; return true;
            case ';': nextNextToken = semicolonToken; return true;
            case '-': nextNextToken = subtractToken; return true;
            default: return false;
        }

    }

    private static boolean isTwoToken(char c) {
        if(c == '=') {
            if(CharGenerator.nextC == '=') nextNextToken = equalToken;
            else nextNextToken = assignToken;
            return true;
        }
        else if(c == '!' && CharGenerator.nextC == '=') {
            nextNextToken = notEqualToken;
            CharGenerator.readNext();
            return true;
        }
        else if(c == '<') {
            if(CharGenerator.nextC == '=') nextNextToken = lessEqualToken;
            else nextNextToken = lessToken;
            return true;
        }
        else if(c == '>') {
            if(CharGenerator.nextC == '=') nextNextToken = greaterEqualToken;
            else nextNextToken = greaterToken;
            return true;
        }
        return false;
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
