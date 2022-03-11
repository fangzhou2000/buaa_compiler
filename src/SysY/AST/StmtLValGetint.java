package SysY.AST;

import SysY.ErrorHandle.ErrorType;
import SysY.SemanticAnalysis.Quadruple;
import SysY.SemanticAnalysis.QuadrupleOp;
import SysY.SymbolTable.Symbol;
import SysY.SymbolTable.SymbolType;
import SysY.LexicAnalysis.Token;
import SysY.LexicAnalysis.TokenKey;

public class StmtLValGetint extends Node {
    public LVal lVal;
    public Token assign;
    public Token getint;
    public Token lParent;
    public Token rParent;
    public Token semicn;

    public void errorCheck() {
        // h
        this.errorCheckChangeConst(this.lVal.ident.getTokenString(), this.lVal.ident.getTokenPos());
        this.lVal.errorCheck();
        // i
        this.errorCheckMissSemicn();
        // j
        this.errorCheckMissRparent();
    }

    public void errorCheckMissSemicn() {
        if (this.semicn.getTokenKey() != TokenKey.SEMICN) {
            Node.errorHandle.addError(this.semicn.getTokenPos(), ErrorType.i);
        }
    }

    public void errorCheckMissRparent() {
        if (this.rParent.getTokenKey() != TokenKey.RPARENT) {
            Node.errorHandle.addError(this.rParent.getTokenPos(), ErrorType.j);
        }
    }

    public void semanticAnalyse() {
        Quadruple quadruple = new Quadruple(this.lVal.semanticAnalyse(), QuadrupleOp.GETINT, "", "");
        Node.intermediate.addIntermediateCode(quadruple);
    }
}
