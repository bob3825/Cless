package no.uio.ifi.cless.scanner;

/*
 * class Token
 */

/*
 * The different kinds of tokens read by Scanner.
 */
public enum Token {
    addToken, assignToken, commaToken, divideToken, elseToken,
    eofToken, equalToken, forToken, greaterEqualToken, greaterToken,
    ifToken, intToken, leftBracketToken, leftCurlToken, leftParToken,
    lessEqualToken, lessToken, multiplyToken, nameToken, notEqualToken,
    numberToken, rightBracketToken, rightCurlToken, rightParToken,
    returnToken, semicolonToken, subtractToken, whileToken;

    public static boolean isOperand(Token t) {
        return t == numberToken || t == nameToken || t == leftParToken;
    }

    public static boolean isOperator(Token t) {
        //Assuming everything else counts as an operator
        return !(t == numberToken || t == nameToken || t == leftParToken);
    }

    public static boolean isArithmetic(Token t) {
        return t == addToken || t == subtractToken || t == multiplyToken || t == divideToken;
    }

    public static boolean isLogic(Token t) {
        return t == equalToken || t == notEqualToken || t == greaterToken || t == lessToken || t == greaterEqualToken || t == lessEqualToken;
    }
}
