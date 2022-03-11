package SysY.AST;

import SysY.ErrorHandle.ErrorType;
import SysY.LexicAnalysis.Token;
import SysY.LexicAnalysis.TokenKey;
import SysY.LexicAnalysis.TokenPos;

public class Stmt extends Node {
    public StmtType stmtType;
    public StmtLValAssign stmtLValAssign;
    public Exp exp;
    public Token semicn;
    public Block block;
    public StmtIf stmtIf;
    public StmtWhile stmtWhile;
    public StmtBreak stmtBreak;
    public StmtContinue stmtContinue;
    public StmtReturn stmtReturn;
    public StmtLValGetint stmtLValGetint;
    public StmtPrintf stmtPrintf;

    public void setFuncType(FuncType funcType) {
        if (this.stmtType == StmtType.RETURN) {
            this.stmtReturn.setFuncType(funcType);
        } else if (this.stmtType == StmtType.BLOCK) {
            this.block.setFuncType(funcType);
        } else if (this.stmtType == StmtType.IF) {
            this.stmtIf.setFuncType(funcType);
        } else if (this.stmtType == StmtType.WHILE) {
            this.stmtWhile.setFuncType(funcType);
        }
    }

    public void setStmtType(StmtType stmtType) {
        if (this.stmtType == StmtType.BREAK) {
            this.stmtBreak.setStmtType(stmtType);
        } else if (this.stmtType == StmtType.CONTINUE) {
            this.stmtContinue.setStmtType(stmtType);
        } else if (this.stmtType == StmtType.BLOCK) {
            this.block.setStmtType(stmtType);
        } else if (this.stmtType == StmtType.IF) {
            this.stmtIf.setStmtType(stmtType);
        } else if (this.stmtType == StmtType.WHILE) {
            this.stmtWhile.setStmtType(stmtType);
        }
    }

    public void errorCheck() {
        if (this.stmtType == StmtType.ERROR) {
            System.out.println("stmtType wrong");
        } else if (this.stmtType == StmtType.LVALASSIGN) {
            this.stmtLValAssign.errorCheck();
        } else if (this.stmtType == StmtType.EXP) {
            this.exp.errorCheck();
            this.errorCheckMissSemicn();
        } else if (this.stmtType == StmtType.SEMICN) {
            this.errorCheckMissSemicn();
        } else if (this.stmtType == StmtType.BLOCK) {
            this.block.errorCheck();
        } else if (this.stmtType == StmtType.IF) {
            this.stmtIf.errorCheck();
        } else if (this.stmtType == StmtType.WHILE) {
            this.stmtWhile.errorCheck();
        } else if (this.stmtType == StmtType.BREAK) {
            this.stmtBreak.errorCheck();
        } else if (this.stmtType == StmtType.CONTINUE) {
            this.stmtContinue.errorCheck();
        } else if (this.stmtType == StmtType.RETURN) {
            this.stmtReturn.errorCheck();
        } else if (this.stmtType == StmtType.LVALGETINT) {
            this.stmtLValGetint.errorCheck();
        } else if (this.stmtType == StmtType.PRINTF) {
            this.stmtPrintf.errorCheck();
        } else {
            System.out.println("stmtType wrong");
        }
    }

    public void errorCheckMissSemicn() {
        if (this.semicn.getTokenKey() != TokenKey.SEMICN) {
            Node.errorHandle.addError(this.semicn.getTokenPos(), ErrorType.i);
        }
    }

    public void semanticAnalyse() {
        if (this.stmtType == StmtType.BLOCK) {
            this.block.semanticAnalyse();
        } else if (this.stmtType == StmtType.PRINTF) {
            this.stmtPrintf.semanticAnalyse();
        } else if (this.stmtType == StmtType.LVALGETINT) {
            this.stmtLValGetint.semanticAnalyse();
        } else if (this.stmtType == StmtType.LVALASSIGN) {
            this.stmtLValAssign.semanticAnalyse();
        } else if (this.stmtType == StmtType.RETURN) {
            this.stmtReturn.semanticAnalyse();
        } else if (this.stmtType == StmtType.EXP) {
            this.exp.semanticAnalyse();
        } else if (this.stmtType == StmtType.IF) {
            this.stmtIf.semanticAnalyse();
        } else if (this.stmtType == StmtType.WHILE) {
            this.stmtWhile.semanticAnalyse();
        } else if (this.stmtType == StmtType.BREAK) {
            this.stmtBreak.semanticAnalyse();
        } else if (this.stmtType == StmtType.CONTINUE) {
            this.stmtContinue.semanticAnalyse();
        }
    }
}
