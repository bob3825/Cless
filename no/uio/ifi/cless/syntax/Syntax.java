package no.uio.ifi.cless.syntax;

/*
 * module Syntax
 */

import com.sun.java.swing.plaf.nimbus.State;
import no.uio.ifi.cless.cless.CLess;
import no.uio.ifi.cless.code.Code;
import no.uio.ifi.cless.error.Error;
import no.uio.ifi.cless.log.Log;
import no.uio.ifi.cless.scanner.Scanner;
import no.uio.ifi.cless.scanner.Token;

import java.sql.SQLOutput;

import static no.uio.ifi.cless.scanner.Token.*;

/*
 * Creates a syntax tree by parsing; checks it;
 * generates executable code; also prints the parse tree (if requested).
 */
public class Syntax {
    static DeclList library;
    static Program program;

    public static void init() {
        Scanner.readNext();
        Scanner.readNext();
        Scanner.readNext();
    }

    public static void finish() {
        //Havent found a use yet.
    }

    static void error(SyntaxUnit use, String message) {
        Error.error(use.lineNum, message);
    }

    public static void checkProgram() {
        program.check(library);
    }

    public static void genCode() {
        program.genCode(null);
    }

    public static void parseProgram() {
        program = new Program();
        program.parse();
    }

    public static void printProgram() {
        program.printTree();
    }
}


/*
 * Master class for all syntactic units.
 * (This class is not mentioned in the syntax diagrams.)
 */
abstract class SyntaxUnit {
    int lineNum;

    SyntaxUnit() {
        lineNum = Scanner.curLine;
    }

    abstract void check(DeclList curDecls);

    abstract void genCode(FuncDecl curFunc);

    abstract void parse();

    abstract void printTree();
}


/*
 * A <program>
 */
class Program extends SyntaxUnit {
    DeclList progDecls;

    Program() {
        progDecls = new GlobalDeclList();
    }

    @Override
    void check(DeclList curDecls) {
        progDecls.check(curDecls);

        if (!CLess.noLink) {
            // Check that 'main' has been declared properly:
            //-- Must be changed in part 2:
        }
    }

    @Override
    void genCode(FuncDecl curFunc) {
        progDecls.genCode(null);
    }

    @Override
    void parse() {
        Log.enterParser("<program>");
        progDecls.parse();
        if (Scanner.curToken != eofToken)
            Scanner.expected("Declaration");

        Log.leaveParser("</program>");
    }

    @Override
    void printTree() {
        progDecls.printTree();
    }
}


/*
 * A declaration list.
 * (This class is not mentioned in the syntax diagrams.)
 */

abstract class DeclList extends SyntaxUnit {
    Declaration firstDecl = null;
    DeclList outerScope;

    DeclList() {
        //No use found yet
    }

    @Override
    void check(DeclList curDecls) {
        outerScope = curDecls;

        Declaration dx = firstDecl;
        while (dx != null) {
            dx.check(this);
            dx = dx.nextDecl;
        }
    }

    @Override
    void printTree() {
        Declaration curDecl = firstDecl;
        while(curDecl != null) {
            curDecl.printTree();
            curDecl = curDecl.nextDecl;
        }
    }

    void addDecl(Declaration d) {
        if(firstDecl == null) {
            firstDecl = d;
        }
        else {
            Declaration curDecl = firstDecl;
            while (curDecl.nextDecl != null) {
                curDecl = curDecl.nextDecl;
            }
            curDecl.nextDecl = d;
        }
    }

    int dataSize() {
        Declaration dx = firstDecl;
        int res = 0;

        while (dx != null) {
            res += dx.dataSize();
            dx = dx.nextDecl;
        }
        return res;
    }

    Declaration findDecl(String name, SyntaxUnit usedIn) {
        //-- Must be changed in part 2:
        return null;
    }
}


/*
 * A list of global declarations. 
 * (This class is not mentioned in the syntax diagrams.)
 */
class GlobalDeclList extends DeclList {
    @Override
    void genCode(FuncDecl curFunc) {
        //-- Must be changed in part 2:
    }

    @Override
    void parse() {
        while (Scanner.curToken == intToken) {
            if (Scanner.nextToken == nameToken) {
                if (Scanner.nextNextToken == leftParToken) {
                    FuncDecl fd = new FuncDecl(Scanner.nextName);
                    fd.parse();
                    addDecl(fd);
                }
                else if (Scanner.nextNextToken == leftBracketToken) {
                    GlobalArrayDecl gad = new GlobalArrayDecl(Scanner.nextName);
                    gad.parse();
                    addDecl(gad);
                }
                else {
                    GlobalSimpleVarDecl var = new GlobalSimpleVarDecl(Scanner.nextName);
                    var.parse();
                    addDecl(var);
                }
            }
            else
            {
                Scanner.expected("Declaration");
            }
        }
    }
}


/*
 * A list of local declarations. 
 * (This class is not mentioned in the syntax diagrams.)
 */
class LocalDeclList extends DeclList {
    @Override
    void genCode(FuncDecl curFunc) {
        //-- Must be changed in part 2:
    }

    @Override
    void parse() {
        while(Scanner.curToken == intToken) {
            LocalSimpleVarDecl var = new LocalSimpleVarDecl(Scanner.nextName);
            var.parse();
            addDecl(var);
        }
    }
}


/*
 * A list of parameter declarations. 
 * (This class is not mentioned in the syntax diagrams.)
 */
class ParamDeclList extends DeclList {
    @Override
    void genCode(FuncDecl curFunc) {
        //-- Must be changed in part 2:
    }

    @Override
    void parse() {
        while(Scanner.curToken != rightParToken) {
            ParamDecl p = new ParamDecl(Scanner.nextName);
            p.parse();
            addDecl(p);
            if(Scanner.curToken == commaToken) {
                Scanner.skip(commaToken);
            }
            else break;
        }
    }

    @Override
    void printTree() {
        Declaration curParam = firstDecl;
        while(curParam != null) {
            curParam.printTree();
            if(curParam.nextDecl != null) {
                Log.wTree(", ");
            }
            curParam = curParam.nextDecl;
        }
    }
}


/*
 * Any kind of declaration.
 * (This class is not mentioned in the syntax diagrams.)
 */
abstract class Declaration extends SyntaxUnit {
    String name, assemblerName;
    boolean visible = false;
    Declaration nextDecl = null;

    Declaration(String n) {
        name = n;
    }

    abstract int dataSize();

    /**
     * checkWhetherArray: Utility method to check whether this Declaration is
     * really an array. The compiler must check that a name is used properly;
     * for instance, using an array name a in "a()" or in "x=a;" is illegal.
     * This is handled in the following way:
     * <ul>
     * <li> When a name a is found in a setting which implies that should be an
     * array (i.e., in a construct like "a["), the parser will first
     * search for a's declaration d.
     * <li> The parser will call d.checkWhetherArray(this).
     * <li> Every sub-class of Declaration will implement a checkWhetherArray.
     * If the declaration is indeed an array, checkWhetherArray will do
     * nothing, but if it is not, the method will give an error message
     * and thus stop the compilation.
     * </ul>
     * Examples
     * <dl>
     * <dt>GlobalArrayDecl.checkWhetherArray(this)</dt>
     * <dd>will do nothing, as everything is all right.</dd>
     * <dt>FuncDecl.checkWhetherArray(this)</dt>
     * <dd>will give an error message.</dd>
     * </dl>
     */
    abstract void checkWhetherArray(SyntaxUnit use);

    /**
     * checkWhetherFunction: Utility method to check whether this Declaration
     * is really a function.
     *
     * @param nParamsUsed Number of parameters used in the actual call.
     *                    (The method will give an error message if the
     *                    function was used with too many or too few parameters.)
     * @param use         From where is the check performed?
     * @see checkWhetherArray
     */
    abstract void checkWhetherFunction(int nParamsUsed, SyntaxUnit use);

    /**
     * checkWhetherSimpleVar: Utility method to check whether this
     * Declaration is really a simple variable.
     *
     * @see checkWhetherArray
     */
    abstract void checkWhetherSimpleVar(SyntaxUnit use);
}


/*
 * A <var decl>
 */
abstract class VarDecl extends Declaration {
    VarDecl(String n) {
        super(n);
    }

    @Override
    void checkWhetherFunction(int nParamsUsed, SyntaxUnit use) {
        Syntax.error(use, name + " is a variable and no function!");
    }

    @Override
    int dataSize() {
        return 4;
    }

    @Override
    void printTree() {
        Log.wTree("int " + name);
        Log.wTreeLn(";");
    }

    //Part 1 + 2:
}


/*
 * A global array declaration
 */
class GlobalArrayDecl extends VarDecl {
    int numElems;

    GlobalArrayDecl(String n) {
        super(n);
        assemblerName = (CLess.underscoredGlobals() ? "_" : "") + n;
    }

    @Override
    void check(DeclList curDecls) {
        visible = true;
        if (numElems < 0)
            Syntax.error(this, "Arrays cannot have negative size!");
    }

    @Override
    void checkWhetherArray(SyntaxUnit use) {
        /* OK */
    }

    @Override
    void checkWhetherSimpleVar(SyntaxUnit use) {
        Syntax.error(use, name + " is an array and no simple variable!");
    }

    @Override
    int dataSize() {
        return 4 * numElems;
    }

    @Override
    void genCode(FuncDecl curFunc) {
        //-- Must be changed in part 2:
    }

    @Override
    void parse() {
        Log.enterParser("<var decl>");
        Scanner.readNext();
        Scanner.skip(nameToken);
        Scanner.skip(leftBracketToken);
        numElems = Scanner.curNum;
        Scanner.skip(numberToken);
        Scanner.skip(rightBracketToken);
        Scanner.skip(semicolonToken);
        Log.leaveParser("</var decl>");
    }

    @Override
    void printTree() {
        Log.wTreeLn("int " + name + "[" + numElems +"];");
    }
}


/*
 * A global simple variable declaration
 */
class GlobalSimpleVarDecl extends VarDecl {
    GlobalSimpleVarDecl(String n) {
        super(n);
        assemblerName = (CLess.underscoredGlobals() ? "_" : "") + n;
    }

    @Override
    void check(DeclList curDecls) {
        //-- Must be changed in part 2:
    }

    @Override
    void checkWhetherArray(SyntaxUnit use) {
        //-- Must be changed in part 2:
    }

    @Override
    void checkWhetherSimpleVar(SyntaxUnit use) {
        /* OK */
    }

    @Override
    void genCode(FuncDecl curFunc) {
        //-- Must be changed in part 2:
    }

    @Override
    void parse() {
        Log.enterParser("<var decl>");
        Scanner.readNext();
        Scanner.skip(nameToken);
        Scanner.skip(semicolonToken);
        Log.leaveParser("</var decl>");
    }
}


/*
 * A local array declaration
 */
class LocalArrayDecl extends VarDecl {
    int numElems;

    LocalArrayDecl(String n) {
        super(n);
    }

    @Override
    void check(DeclList curDecls) {
        //-- Must be changed in part 2:
    }

    @Override
    void checkWhetherArray(SyntaxUnit use) {
        //-- Must be changed in part 2:
    }

    @Override
    void checkWhetherSimpleVar(SyntaxUnit use) {
        //-- Must be changed in part 2:
    }

    @Override
    int dataSize() {
        //-- Must be changed in part 2:
        return 0;
    }

    @Override
    void genCode(FuncDecl curFunc) {
        //-- Must be changed in part 2:
    }

    @Override
    void parse() {
        Log.enterParser("<var decl>");
        Scanner.readNext();
        Scanner.skip(nameToken);
        Scanner.skip(leftBracketToken);
        numElems = Scanner.curNum;
        Scanner.skip(numberToken);
        Scanner.skip(rightBracketToken);
        Scanner.skip(semicolonToken);
        Log.leaveParser("</var decl>");
    }

    @Override
    void printTree() {
        Log.wTreeLn("int " + name + "[" + numElems +"];");
    }

}


/*
 * A local simple variable declaration
 */
class LocalSimpleVarDecl extends VarDecl {
    LocalSimpleVarDecl(String n) {
        super(n);
    }

    @Override
    void check(DeclList curDecls) {
        //-- Must be changed in part 2:
    }

    @Override
    void checkWhetherArray(SyntaxUnit use) {
        //-- Must be changed in part 2:
    }

    @Override
    void checkWhetherSimpleVar(SyntaxUnit use) {
        //-- Must be changed in part 2:
    }

    @Override
    void genCode(FuncDecl curFunc) {
        //-- Must be changed in part 2:
    }

    @Override
    void parse() {
        Log.enterParser("<var decl>");
        Scanner.readNext();
        Scanner.skip(nameToken);
        Scanner.skip(semicolonToken);
        Log.leaveParser("</var decl>");
    }
}


/*
 * A <param decl>
 */
class ParamDecl extends VarDecl {
    int paramNum = 0;

    ParamDecl(String n) {
        super(n);
    }

    @Override
    void check(DeclList curDecls) {
        //-- Must be changed in part 2:
    }

    @Override
    void checkWhetherArray(SyntaxUnit use) {
        //-- Must be changed in part 2:
    }

    @Override
    void checkWhetherSimpleVar(SyntaxUnit use) {
        //-- Must be changed in part 2:
    }

    @Override
    void genCode(FuncDecl curFunc) {
        //-- Must be changed in part 2:
    }

    @Override
    void parse() {
        Log.enterParser("<param decl>");
        Scanner.skip(intToken);
        Scanner.skip(nameToken);
        Log.leaveParser("</param decl>");
    }
    @Override
    void printTree() {
        Log.wTree("int " + name);
    }
}


/*
 * A <func decl>
 */
class FuncDecl extends Declaration {
    //-- Must be changed in part 2
    ParamDeclList parameters;
    LocalDeclList localVariables;
    StatmList funcBody;

    FuncDecl(String n) {
        // Used for user functions:

        super(n);
        assemblerName = (CLess.underscoredGlobals() ? "_" : "") + n;
        parameters = new ParamDeclList();
        localVariables = new LocalDeclList();
        funcBody = new StatmList();
    }

    @Override
    void check(DeclList curDecls) {
        //-- Must be changed in part 2:
    }

    @Override
    void checkWhetherArray(SyntaxUnit use) {
        //-- Must be changed in part 2:
    }

    @Override
    void checkWhetherFunction(int nParamsUsed, SyntaxUnit use) {
        //-- Must be changed in part 2:
    }

    @Override
    void checkWhetherSimpleVar(SyntaxUnit use) {
        //-- Must be changed in part 2:
    }

    @Override
    int dataSize() {
        return 0;
    }

    @Override
    void genCode(FuncDecl curFunc) {
        Code.genInstr("", ".globl", assemblerName, "");
        Code.genInstr(assemblerName, "pushl", "%ebp", "Start function " + name);
        Code.genInstr("", "movl", "%esp,%ebp", "");
        //-- Must be changed in part 2:
    }

    @Override
    void parse() {
        Scanner.readNext();
        Scanner.skip(nameToken);
        Scanner.skip(leftParToken);
        parameters.parse();
        Scanner.skip(rightParToken);
        Scanner.skip(leftCurlToken);
        localVariables.parse();
        funcBody.parse();
        Scanner.skip(rightCurlToken);
    }

    @Override
    void printTree() {
        Log.wTree("int " + name + "(");
        parameters.printTree();
        Log.wTreeLn(")");
        Log.wTreeLn("{");
        Log.indentTree();
        localVariables.printTree();
        funcBody.printTree();
        Log.outdentTree();
        Log.wTreeLn("}");

    }
}




/*
 * A <statm list>.
 */
class StatmList extends SyntaxUnit {
    Statement firstStatm;

    @Override
    void check(DeclList curDecls) {
        //-- Must be changed in part 2:
    }

    @Override
    void genCode(FuncDecl curFunc) {
        //-- Must be changed in part 2:
    }

    @Override
    void parse() {
        Log.enterParser("<statm list>");

        Statement lastStatm = null;
        while (Scanner.curToken != rightCurlToken) {
            Log.enterParser("<statement>");
            Statement newStatm = Statement.makeNewStatement();
            addStatm(newStatm);
            lastStatm = newStatm;
            Log.leaveParser("</statement>");
        }

        Log.leaveParser("</statm list>");
    }

    @Override
    void printTree() {
        Log.wTreeLn("");
        Statement curStatm = firstStatm;
        while(curStatm != null) {
            curStatm.printTree();
            curStatm = curStatm.nextStatm;
        }
    }

    void addStatm(Statement statm) {
        Statement curStatm = firstStatm;
        if(curStatm == null){
            firstStatm = statm;
            return;
        }
        while(curStatm.nextStatm != null) curStatm = curStatm.nextStatm;
        curStatm.nextStatm = statm;
    }
}


/*
 * A <statement>.
 */
abstract class Statement extends SyntaxUnit {
    Statement nextStatm = null;

    static Statement makeNewStatement() {
        if (Scanner.curToken == nameToken &&
                Scanner.nextToken == leftParToken) {
            CallStatm call = new CallStatm(Scanner.curName);
            call.parse();
            return call;
        } else if (Scanner.curToken == nameToken) {
            AssignStatm assStatm = new AssignStatm(Scanner.curName);
            assStatm.parse();
            return assStatm;
        } else if (Scanner.curToken == forToken) {
            ForStatm forey = new ForStatm();
            forey.parse();
            return forey;
        } else if (Scanner.curToken == ifToken) {
            IfStatm ifey = new IfStatm();
            ifey.parse();
            return ifey;
        } else if (Scanner.curToken == returnToken) {
           ReturnStatm returney = new ReturnStatm();
            returney.parse();
            return returney;
        } else if (Scanner.curToken == whileToken) {
            WhileStatm whiley = new WhileStatm();
            whiley.parse();
            return whiley;
        } else if (Scanner.curToken == semicolonToken) {
            EmptyStatm empty = new EmptyStatm();
            empty.parse();
            return empty;
        } else {
            Scanner.expected("Statement");
        }
        return null;  // Just to keep the Java compiler happy. :-)
    }
}

class CallStatm extends Statement {
    FunctionCall call;

    CallStatm(String name) {
        call = new FunctionCall(name);
    }
    @Override
    void check(DeclList curDecls) {
        //Part 2, i guess
    }

    @Override
    void genCode(FuncDecl curFunc) {
        //Part 2, i guess
    }

    @Override
    void parse() {
        call.parse();
        Scanner.skip(semicolonToken);
    }

    @Override
    void printTree() {
        call.printTree();
        Log.wTreeLn(";");
    }
}


/*
* An <empty statm>.
*/
class EmptyStatm extends Statement {
    //Part 1 + 2:

    @Override
    void check(DeclList curDecls) {
        //-- Must be changed in part 2:
    }

    @Override
    void genCode(FuncDecl curFunc) {
        //-- Must be changed in part 2:
    }

    @Override
    void parse() {
        Scanner.skip(semicolonToken);
    }

    @Override
    void printTree() {
        Log.wTreeLn(";");
    }
}


/*
* A <for-statm>.
*/
//Part 1 + 2:

class ForStatm extends Statement {
    ForAssignStatm start;
    Expression test;
    ForAssignStatm step;
    StatmList body;

    @Override
    void check(DeclList curDecls) {
        //Part 2, i guess
    }

    @Override
    void genCode(FuncDecl curFunc) {
        //Part 2, i guess
    }

    @Override
    void parse() {
        Scanner.skip(forToken);
        Scanner.skip(leftParToken);
        start = new ForAssignStatm(Scanner.curName);
        start.parse();
        System.out.println("1");
        Scanner.skip(semicolonToken);
        test = new Expression();
        test.parse();
        System.out.println("2");
        Scanner.skip(semicolonToken);
        step = new ForAssignStatm(Scanner.curName);
        step.parse();
        Scanner.skip(rightParToken);
        Scanner.skip(leftCurlToken);
        body = new StatmList();
        body.parse();
        Scanner.skip(rightCurlToken);
    }

    @Override
    void printTree() {
        Log.wTree("for (");
        start.printTree();
        Log.wTree(";");
        test.printTree();
        Log.wTree(";");
        step.printTree();
        Log.wTreeLn(") {");
        body.printTree();
        Log.wTreeLn("}");
    }
}
/*
 * An <if-statm>.
 */
class IfStatm extends Statement {
    Expression test;
    StatmList ifpart;
    StatmList elsepart = null;
    //Part 1 + 2:

    IfStatm() {
        test = new Expression();
        ifpart = new StatmList();
    }

    @Override
    void check(DeclList curDecls) {
        //-- Must be changed in part 2:
    }

    @Override
    void genCode(FuncDecl curFunc) {
        //-- Must be changed in part 2:
    }

    @Override
    void parse() {
        Scanner.skip(ifToken);
        Scanner.skip(leftParToken);
        test.parse();
        Scanner.skip(rightParToken);
        Scanner.skip(leftCurlToken);
        ifpart.parse();
        Scanner.skip(rightCurlToken);
        if(Scanner.curToken == elseToken) {
            elsepart = new StatmList();
            Scanner.skip(elseToken);
            Scanner.skip(leftCurlToken);
            elsepart.parse();
            Scanner.skip(rightCurlToken);
        }
    }

    @Override
    void printTree() {
        Log.wTree("if(");
        test.printTree();
        Log.wTree(") {");
        ifpart.printTree();
        Log.wTreeLn();
        Log.wTreeLn("}");
        if(elsepart != null) {
            Log.wTree("else {");
            elsepart.printTree();
            Log.wTreeLn("}");
        }
    }
}


/*
 * A <return-statm>.
 */
//Part 1 + 2:
class ReturnStatm extends Statement {
    Expression returnExpression;

    ReturnStatm() {
        returnExpression = new Expression();
    }
    @Override
    void check(DeclList curDecls) {
        //Part 2, i guess
    }

    @Override
    void genCode(FuncDecl curFunc) {
        //Part 2, i guess
    }

    @Override
    void parse() {
        Scanner.skip(returnToken);
        returnExpression.parse();
        Scanner.skip(semicolonToken);
    }

    @Override
    void printTree() {
        Log.wTree("return ");
        returnExpression.printTree();
        Log.wTreeLn(";");
    }
}


/*
 * A <while-statm>.
 */
class WhileStatm extends Statement {
    Expression test = new Expression();
    StatmList body = new StatmList();

    @Override
    void check(DeclList curDecls) {
        test.check(curDecls);
        body.check(curDecls);
    }

    @Override
    void genCode(FuncDecl curFunc) {
        String testLabel = Code.getLocalLabel(),
                endLabel = Code.getLocalLabel();

        Code.genInstr(testLabel, "", "", "Start while-statement");
        test.genCode(curFunc);
        Code.genInstr("", "cmpl", "$0,%eax", "");
        Code.genInstr("", "je", endLabel, "");
        body.genCode(curFunc);
        Code.genInstr("", "jmp", testLabel, "");
        Code.genInstr(endLabel, "", "", "End while-statement");
    }

    @Override
    void parse() {
        Log.enterParser("<while-statm>");

        Scanner.readNext();
        Scanner.skip(leftParToken);
        test.parse();
        Scanner.skip(rightParToken);
        Scanner.skip(leftCurlToken);
        body.parse();
        Scanner.skip(rightCurlToken);

        Log.leaveParser("</while-statm>");
    }

    @Override
    void printTree() {
        Log.wTree("while (");
        test.printTree();
        Log.wTreeLn(") {");
        Log.indentTree();
        body.printTree();
        Log.outdentTree();
        Log.wTreeLn("}");
    }
}


class AssignStatm extends Statement {
    Variable var;
    Expression value;

    AssignStatm(String name) {
        var = new Variable(name);
        value = new Expression();
    }
    @Override
    void check(DeclList curDecls) {
        //Part 2, i guess
    }

    @Override
    void genCode(FuncDecl curFunc) {
        //Part 2, i guess
    }

    @Override
    void parse() {
        var.parse();
        Scanner.skip(assignToken);
        value.parse();
        Scanner.skip(semicolonToken);
    }

    @Override
    void printTree() {
        var.printTree();
        Log.wTree("=");
        value.printTree();
        Log.wTreeLn(";");
    }
}

class ForAssignStatm extends AssignStatm {
    @Override
    void parse() {
        var.parse();
        Scanner.skip(assignToken);
        value.parse();
    }

    @Override
    void printTree() {
        var.printTree();
        Log.wTree("=");
        value.printTree();
    }

    ForAssignStatm(String name) {
        super(name);
    }
}


/*
 * An <expression list>.
 */

class ExprList extends SyntaxUnit {
    Expression firstExpr = null;

    @Override
    void check(DeclList curDecls) {
        //-- Must be changed in part 2:
    }

    @Override
    void genCode(FuncDecl curFunc) {
        //-- Must be changed in part 2:
    }

    @Override
    void parse() {
        Expression lastExpr = null;

        Log.enterParser("<expr list>");

        while (Scanner.curToken != rightParToken) {
            Expression curExpr = new Expression();
            curExpr.parse();
            addExpression(curExpr);
            if(Scanner.curToken != rightParToken) Scanner.skip(commaToken);
        }

        Log.leaveParser("</expr list>");
    }

    @Override
    void printTree() {
        Expression curExpr = firstExpr;
        while (curExpr != null) {
            curExpr.printTree();
            if(curExpr.nextExpr != null) Log.wTree(", ");
            curExpr = curExpr.nextExpr;
        }
    }

    int nExprs() {
        int n = 0;
        Expression curExpr = firstExpr;
        while(curExpr != null) {
            n++;
            curExpr = curExpr.nextExpr;
        }
        return n;
    }

    void addExpression(Expression e) {
        if(firstExpr == null) {
            firstExpr = e;
            return;
        }
        Expression curExpr = firstExpr;
        while (curExpr.nextExpr != null) curExpr = curExpr.nextExpr;
        curExpr.nextExpr = e;

    }
}


/*
 * An <expression>
 */
class Expression extends Operand {
    Operand firstOp = null;
    Expression nextExpr = null;

    @Override
    void check(DeclList curDecls) {
        //-- Must be changed in part 2:
    }

    @Override
    void genCode(FuncDecl curFunc) {
        //-- Must be changed in part 2:
    }

    @Override
    void parse() {
        Log.enterParser("<expression>");

        firstOp = Operand.makeNewOperand();
        firstOp.parse();



        Log.leaveParser("</expression>");
    }

    @Override
    void printTree() {
        //Log.wTree("(");
        firstOp.printTree();
        //Log.wTree(")");
    }
}

class InternalExpression extends Expression {
    @Override
    void parse() {
        Scanner.skip(leftParToken);
        Log.enterParser("<expression>");
        firstOp = Operand.makeNewOperand();
        firstOp.parse();
        Log.leaveParser("</expression>");
        Scanner.skip(rightParToken);
    }

    @Override
    void printTree() {
        Log.wTree("(");
        firstOp.printTree();
        Log.wTree(")");
    }
}


/*
 * An <operator>
 */
abstract class Operator extends SyntaxUnit {
    Operand secondOp;
    String operation;
    //Part 1 + 2:

    public static Operator makeNewOperator() {
        if(isArithmetic(Scanner.curToken)) return new ArithmeticOperator();
        else return new LogicOperator();
    }

    public static boolean operandNext() {
        return Token.isOperand(Scanner.curToken);
    }
}

class ArithmeticOperator extends Operator {

    @Override
    void check(DeclList curDecls) {
        //Part 2, i guess
    }

    @Override
    void genCode(FuncDecl curFunc) {
        //Part 2, i guess
    }

    @Override
    void parse() {
        Log.enterParser("<operator>");
        if(Scanner.curToken == addToken) {
            Scanner.skip(addToken);
            operation = "+";
        }
        else if(Scanner.curToken == subtractToken) {
            Scanner.skip(subtractToken);
            operation = "-";
        }
        else if(Scanner.curToken == multiplyToken) {
            Scanner.skip(multiplyToken);
            operation = "*";
        }
        else if(Scanner.curToken == divideToken) {
            Scanner.skip(divideToken);
            operation = "/";
        }
        Log.leaveParser("</operator>");

        if(operandNext()) {
            secondOp = Operand.makeNewOperand();
            secondOp.parse();
        }
    }

    @Override
    void printTree() {
        Log.wTree(operation + "");
        if(secondOp != null) {
            secondOp.printTree();
        }
    }

}

class LogicOperator extends Operator {
    @Override
    void check(DeclList curDecls) {
        ////Part 2, i guess
    }

    @Override
    void genCode(FuncDecl curFunc) {
        ////Part 2, i guess
    }

    @Override
    void parse() {
        if(Scanner.curToken == equalToken) {
            Scanner.skip(equalToken);
            operation = "==";
        }
        if(Scanner.curToken == notEqualToken) {
            Scanner.skip(notEqualToken);
            operation = "!=";
        }
        if(Scanner.curToken == greaterToken) {
            Scanner.skip(greaterToken);
            operation = ">";
        }
        if(Scanner.curToken == lessToken) {
            Scanner.skip(lessToken);
            operation = "<";
        }
        if(Scanner.curToken == greaterEqualToken) {
            Scanner.skip(greaterEqualToken);
            operation = ">=";
        }
        if(Scanner.curToken == lessEqualToken) {
            Scanner.skip(lessToken);
            operation = "<=" ;
        }

        if(operandNext()) {
            secondOp = Operand.makeNewOperand();
            secondOp.parse();
        }
    }

    @Override
    void printTree() {
        Log.wTree(operation + "");
        if(secondOp != null) {
            secondOp.printTree();
        }
    }
}


//Part 1 + 2:


/*
 * An <operand>
 */
abstract class Operand extends SyntaxUnit {
    Operator nextOperator = null;

    public static Operand makeNewOperand() {
        if(Scanner.curToken == numberToken) {
            return new Number(Scanner.curNum);
        }
        else if(Scanner.curToken == nameToken && Scanner.nextToken == leftParToken) {
            return new FunctionCall(Scanner.curName);
        }
        else if(Scanner.curToken == nameToken) {
            return new Variable(Scanner.curName);
        }
        else if(Scanner.curToken == leftParToken) {
            return new InternalExpression();
        }
        return null;
    }

    public static boolean operatorNext() {
        return Token.isArithmetic(Scanner.curToken) || Token.isLogic(Scanner.curToken);
    }
}


/*
 * A <function call>.
 */
class FunctionCall extends Operand {
    //Part 1 + 2:
    ExprList functionParameters;
    String functionName;

    FunctionCall(String name) {
        functionName = name;
        functionParameters = new ExprList();
    }

    @Override
    void check(DeclList curDecls) {
        //-- Must be changed in part 2:

        if (nextOperator != null) nextOperator.check(curDecls);
    }

    @Override
    void genCode(FuncDecl curFunc) {
        //-- Must be changed in part 2:
    }

    @Override
    void parse() {
        Log.enterParser("<function call>");
        Scanner.skip(nameToken);
        Scanner.skip(leftParToken);
        functionParameters.parse();
        Scanner.skip(rightParToken);
        Log.leaveParser("</function call>");
        if(operatorNext()) {
            nextOperator = Operator.makeNewOperator();
            nextOperator.parse();
        }
    }

    @Override
    void printTree() {
        Log.wTree(functionName + "(");
        functionParameters.printTree();
        Log.wTree(")");
         if(nextOperator != null) {
            nextOperator.printTree();
        }
    }

    //Part 1 + 2:
}


/*
 * A <number>.
 */
class Number extends Operand {
    int numVal;

    Number(int val) {
        numVal = val;
    }

    @Override
    void check(DeclList curDecls) {
        //-- Must be changed in part 2:
        if (nextOperator != null) nextOperator.check(curDecls);
    }

    @Override
    void genCode(FuncDecl curFunc) {
        Code.genInstr("", "movl", "$" + numVal + ",%eax", "" + numVal);
    }

    @Override
    void parse() {
        Log.enterParser("<number>");
        Scanner.skip(numberToken);
        Log.leaveParser("</number");
        if(operatorNext()) {
            nextOperator = Operator.makeNewOperator();
            nextOperator.parse();
        }
    }

    @Override
    void printTree() {
        Log.wTree("" + numVal);
        if(nextOperator != null) {
            nextOperator.printTree();
        }
    }
}


/*
 * A <variable>.
 */

class Variable extends Operand {
    String varName;
    VarDecl declRef = null;
    Expression index = null;

    Variable(String name) {
        varName = name;
    }

    @Override
    void check(DeclList curDecls) {
        Declaration d = curDecls.findDecl(varName, this);
        if (index == null) {
            d.checkWhetherSimpleVar(this);
        } else {
            d.checkWhetherArray(this);
            index.check(curDecls);
        }
        declRef = (VarDecl) d;

        if (nextOperator != null) nextOperator.check(curDecls);
    }

    @Override
    void genCode(FuncDecl curFunc) {
        //-- Must be changed in part 2:
    }

    @Override
    void parse() {
        Log.enterParser("<variable>");
        Scanner.skip(nameToken);
        if(Scanner.curToken == leftBracketToken) {
            Scanner.skip(leftBracketToken);
            index = new Expression();
            index.parse();
            Scanner.skip(rightBracketToken);
        }
        Log.leaveParser("</variable>");
        if(operatorNext()) {
            nextOperator = Operator.makeNewOperator();
            nextOperator.parse();
        }
    }

    @Override
    void printTree() {
        Log.wTree(varName);
        if(index != null) {
            Log.wTree("[");
            index.printTree();
            Log.wTree("]");
        }
        if(nextOperator != null) {
            nextOperator.printTree();
        }
    }
}