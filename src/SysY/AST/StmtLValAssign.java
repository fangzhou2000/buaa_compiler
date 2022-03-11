package SysY.AST;

import SysY.ErrorHandle.ErrorType;
import SysY.SemanticAnalysis.Quadruple;
import SysY.SemanticAnalysis.QuadrupleOp;
import SysY.LexicAnalysis.Token;
import SysY.LexicAnalysis.TokenKey;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StmtLValAssign extends Node {
    public LVal lVal;
    public Token assign;
    public Exp exp;
    public Token semicn;

    public void errorCheck() {
        //h
        this.errorCheckChangeConst(this.lVal.ident.getTokenString(), this.lVal.ident.getTokenPos());
        this.lVal.errorCheck();
        this.exp.errorCheck();
        //i
        this.errorCheckMissSemicn();
    }

    public void errorCheckMissSemicn() {
        if (this.semicn.getTokenKey() != TokenKey.SEMICN) {
            Node.errorHandle.addError(this.semicn.getTokenPos(), ErrorType.i);
        }
    }

    public void semanticAnalyse() {
        if (lVal.dim == 0) {
            String arg1 = this.exp.semanticAnalyse();
            String dst = this.lVal.semanticAnalyse();
            Quadruple quadruple = new Quadruple(dst, QuadrupleOp.LASS, arg1, "");
            Node.intermediate.addIntermediateCode(quadruple);
        } else {
            String arg1 = this.exp.semanticAnalyse();
            String string = this.lVal.semanticAnalyse();
            Pattern pattern = Pattern.compile("(.+?)\\[(.+)\\]");
            Matcher matcher = pattern.matcher(string);
            if (matcher.matches()) {
                String dst = matcher.group(1);
                String index = matcher.group(2);
                Quadruple quadruple = new Quadruple(dst, QuadrupleOp.VARAS, arg1, "");
                quadruple.setIndex(index);
                Node.intermediate.addIntermediateCode(quadruple);
            } else {
                System.out.println("StmtLValAssign wrong");
            }
        }
    }
}
