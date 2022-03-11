package SysY.AST;

import SysY.ErrorHandle.ErrorType;
import SysY.LexicAnalysis.Token;
import SysY.LexicAnalysis.TokenKey;
import SysY.LexicAnalysis.TokenPos;

public class PrimaryExp extends Node {
    public PrimaryExpType primaryExpType;
    public Token lParent;
    public Exp exp;
    public Token rParent;
    public LVal lVal;
    public PNumber pNumber;

    public int getDim() {
        if (this.primaryExpType == PrimaryExpType.EXP) {
            return this.exp.getDim();
        } else if (this.primaryExpType == PrimaryExpType.LVAL) {
            return this.lVal.getDim();
        } else if (this.primaryExpType == PrimaryExpType.NUMBER) {
            return 0;
        } else {
            return 0;
        }
    }

    public TokenPos getPos() {
        if (this.primaryExpType == PrimaryExpType.EXP) {
            return this.exp.addExp.mulExp.unaryExp.getPos();
        } else if (this.primaryExpType == PrimaryExpType.LVAL) {
            return this.lVal.ident.getTokenPos();
        } else {
            return this.pNumber.intConst.getTokenPos();
        }
    }

    public void errorCheck() {
        if (this.primaryExpType == PrimaryExpType.EXP) {
            this.exp.errorCheck();
            this.errorCheckMissRparent();
        } else if (this.primaryExpType == PrimaryExpType.LVAL) {
            this.lVal.errorCheck();
        } else if (this.primaryExpType == PrimaryExpType.NUMBER) {
            this.pNumber.errorCheck();
        }
    }

    public void errorCheckMissRparent() {
        if (this.rParent.getTokenKey() != TokenKey.RPARENT) {
            Node.errorHandle.addError(this.rParent.getTokenPos(), ErrorType.j);
        }
    }

    public String semanticAnalyse() {
        if (this.primaryExpType == PrimaryExpType.EXP) {
            return this.exp.semanticAnalyse();
        } else if (this.primaryExpType == PrimaryExpType.LVAL) {
            return this.lVal.semanticAnalyse();
        } else if (this.primaryExpType == PrimaryExpType.NUMBER){
            return this.pNumber.semanticAnalyse();
        }
        return "primaryExp wrong";
    }

    public int getConst() {
        if (this.primaryExpType == PrimaryExpType.EXP) {
            return this.exp.getConst();
        } else if (this.primaryExpType == PrimaryExpType.LVAL) {
            return this.lVal.getConst();
        } else if (this.primaryExpType == PrimaryExpType.NUMBER) {
            return this.pNumber.getConst();
        }
        return 0;
    }
}
