package SysY.AST;

import SysY.ErrorHandle.ErrorType;
import SysY.LexicAnalysis.Token;
import SysY.LexicAnalysis.TokenKey;
import SysY.SemanticAnalysis.Quadruple;
import SysY.SemanticAnalysis.QuadrupleOp;

public class StmtBreak extends Node {
    public StmtType stmtType;
    public Token tokenBreak;
    public Token semicn;

    public void setStmtType(StmtType stmtType) {
        this.stmtType = stmtType;
    }

    public void errorCheck() {
        // i
        this.errorCheckMissSemicn();
        // m
        this.errorCheckNotCircle();
    }

    // i
    public void errorCheckMissSemicn() {
        if (this.semicn.getTokenKey() != TokenKey.SEMICN) {
            Node.errorHandle.addError(this.semicn.getTokenPos(), ErrorType.i);
        }
    }

    // m
    public void errorCheckNotCircle() {
        if (this.stmtType != StmtType.WHILE) {
            Node.errorHandle.addError(this.tokenBreak.getTokenPos(), ErrorType.m);
        }
    }

    public void semanticAnalyse() {
        String labelLoopEnd = Node.intermediate.getLastLoopEnd();
        Quadruple quadruple = new Quadruple("", QuadrupleOp.GOTO, labelLoopEnd, "");
        Node.intermediate.addIntermediateCode(quadruple);
    }
}
