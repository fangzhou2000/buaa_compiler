package SysY.AST;

import SysY.ErrorHandle.ErrorType;
import SysY.LexicAnalysis.Token;
import SysY.LexicAnalysis.TokenKey;
import SysY.SemanticAnalysis.Quadruple;
import SysY.SemanticAnalysis.QuadrupleOp;

public class StmtIf extends Node {
    public Token tokenIf;
    public Token lParent;
    public Cond cond;
    public Token rParent;
    public Stmt stmt;
    public Token tokenElse;
    public Stmt elseStmt;

    public void setFuncType(FuncType funcType) {
        this.stmt.setFuncType(funcType);
        if (this.elseStmt != null) {
            this.elseStmt.setFuncType(funcType);
        }
    }

    public void setStmtType(StmtType stmtType) {
        this.stmt.setStmtType(stmtType);
        if (this.elseStmt != null) {
            this.elseStmt.setStmtType(stmtType);
        }
    }

    public void errorCheck() {
        this.cond.errorCheck();
        //j
        this.errorCheckMissRparent();
        this.stmt.errorCheck();
        if (this.elseStmt != null) {
            this.elseStmt.errorCheck();
        }
    }

    public void errorCheckMissRparent() {
        if (this.rParent.getTokenKey() != TokenKey.RPARENT) {
            Node.errorHandle.addError(this.rParent.getTokenPos(), ErrorType.j);
        }
    }

    public void semanticAnalyse() {
        if (this.elseStmt == null) {
            String labelIfBegin = Node.intermediate.generateIfBegin();
            String labelIfEnd = Node.intermediate.generateIfEnd();
            this.cond.semanticAnalyse(labelIfBegin, labelIfEnd);
            Quadruple quadruple1 = new Quadruple("", QuadrupleOp.LABEL, labelIfBegin, "");
            Node.intermediate.addIntermediateCode(quadruple1);
            this.stmt.semanticAnalyse();
            Quadruple quadruple2 = new Quadruple("", QuadrupleOp.LABEL, labelIfEnd, "");
            Node.intermediate.addIntermediateCode(quadruple2);
        } else {
            String labelIfBegin1 = Node.intermediate.generateIfBegin();
            String labelIfBegin2 = Node.intermediate.generateIfBegin();
            String labelIfEnd = Node.intermediate.generateIfEnd();
            this.cond.semanticAnalyse(labelIfBegin1, labelIfBegin2);
            Quadruple quadruple1 = new Quadruple("", QuadrupleOp.LABEL, labelIfBegin1, "");
            Node.intermediate.addIntermediateCode(quadruple1);
            this.stmt.semanticAnalyse();
            Quadruple quadruple2 = new Quadruple("", QuadrupleOp.GOTO, labelIfEnd, "");
            Node.intermediate.addIntermediateCode(quadruple2);
            Quadruple quadruple3 = new Quadruple("", QuadrupleOp.LABEL, labelIfBegin2, "");
            Node.intermediate.addIntermediateCode(quadruple3);
            this.elseStmt.semanticAnalyse();
            Quadruple quadruple4 = new Quadruple("", QuadrupleOp.LABEL, labelIfEnd, "");
            Node.intermediate.addIntermediateCode(quadruple4);
        }
    }
}
