package SysY.AST;

import SysY.ErrorHandle.ErrorType;
import SysY.LexicAnalysis.Token;
import SysY.LexicAnalysis.TokenKey;
import SysY.SemanticAnalysis.Quadruple;
import SysY.SemanticAnalysis.QuadrupleOp;

import java.util.ArrayList;

public class StmtPrintf extends Node {
    public Token printf;
    public Token lParent;
    public Token formatString;
    public ArrayList<Token> commaList;
    public ArrayList<Exp> expList;
    public Token rParent;
    public Token semicn;

    public StmtPrintf() {
        this.printf = null;
        this.lParent = null;
        this.formatString = null;
        this.commaList = new ArrayList<>();
        this.expList = new ArrayList<>();
        this.rParent = null;
        this.semicn = null;
    }

    public void errorCheck() {
        //l
        this.errorCheckMismatchPrintfNum();
        //a
        this.errorCheckIllegalFormatString(this.formatString);
        for (Exp e : this.expList) {
            e.errorCheck();
        }
        //j
        this.errorCheckMissRparent();
        //i
        this.errorCheckMissSemicn();
    }

    // a
    public void errorCheckIllegalFormatString(Token formatString) {
        if (IsIllegalFormatString(formatString)) {
            errorHandle.addError(formatString.getTokenPos(), ErrorType.a);
        }
    }

    // i
    public void errorCheckMissSemicn() {
        if (this.semicn.getTokenKey() != TokenKey.SEMICN) {
            Node.errorHandle.addError(this.semicn.getTokenPos(), ErrorType.i);
        }
    }

    // j
    public void errorCheckMissRparent() {
        if (this.rParent.getTokenKey() != TokenKey.RPARENT) {
            Node.errorHandle.addError(this.rParent.getTokenPos(), ErrorType.j);
        }
    }

    public void errorCheckMismatchPrintfNum() {
        if (this.getPrintfNum() != this.expList.size()) {
            Node.errorHandle.addError(this.printf.getTokenPos(), ErrorType.l);
        }

    }

    // a
    private boolean IsIllegalFormatString(Token formatString) {
        int i;
        String s = formatString.getTokenString();
        for (i = 1; i < s.length() - 1; i++) {
            if (s.charAt(i) == 32 || s.charAt(i) == 33 || s.charAt(i) == 37
                    || (s.charAt(i) >= 40 && s.charAt(i) <= 126)) {
                if (s.charAt(i) == '%') {
                    if (i + 1 < s.length()) {
                        if (s.charAt(i + 1) != 'd') {
                            return true;
                        }
                    } else {
                        return true;
                    }
                }
                if (s.charAt(i) == 92) {
                    if (i + 1 < s.length()) {
                        if (s.charAt(i + 1) != 'n') {
                            return true;
                        }
                    } else {
                        return true;
                    }
                }
            } else {
                return true;
            }
        }
        return false;
    }

    public int getPrintfNum() {
        int num = 0;
        for (int i = 0; i < this.formatString.getTokenString().length() - 1; i++) {
            if (this.formatString.getTokenString().charAt(i) == '%' && this.formatString.getTokenString().charAt(i + 1) == 'd'){
                num++;
            }
        }
        return num;
    }

    public void semanticAnalyse() {
        // 这个算法太蠢了，可惜我不会别的QAQ
        ArrayList<Quadruple> quadrupleList = new ArrayList<>();
        String string = this.formatString.getTokenString();
        String[] stringConstList = string.split("%d");
        stringConstList[0] = stringConstList[0].substring(1);
        stringConstList[stringConstList.length - 1] = stringConstList[stringConstList.length - 1].substring(0, stringConstList[stringConstList.length - 1].length() - 1);
        if (!stringConstList[0].equals("")) {
            Quadruple quadruple = new Quadruple("", QuadrupleOp.PRINT_STR, stringConstList[0], "");
            quadrupleList.add(quadruple);
            //Node.intermediate.addIntermediateCode(quadruple);
            Node.symbolTable.addStringConst(stringConstList[0]);
        }
        for (int i = 0; i < this.expList.size(); i++) {
            Quadruple quadruple1 = new Quadruple("", QuadrupleOp.PRINT_INT, this.expList.get(i).semanticAnalyse(), "");
            quadrupleList.add(quadruple1);
            //Node.intermediate.addIntermediateCode(quadruple1);
            if (!stringConstList[i + 1].equals("")) {
                Quadruple quadruple2 = new Quadruple("", QuadrupleOp.PRINT_STR, stringConstList[i + 1], "");
                quadrupleList.add(quadruple2);
                //Node.intermediate.addIntermediateCode(quadruple2);
                Node.symbolTable.addStringConst(stringConstList[i+1]);
            }
        }
        for (Quadruple quadruple : quadrupleList) {
            Node.intermediate.addIntermediateCode(quadruple);
        }
    }
}
