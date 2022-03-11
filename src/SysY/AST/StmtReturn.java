package SysY.AST;

import SysY.ErrorHandle.ErrorType;
import SysY.LexicAnalysis.Token;
import SysY.LexicAnalysis.TokenKey;
import SysY.SemanticAnalysis.Quadruple;
import SysY.SemanticAnalysis.QuadrupleOp;

public class StmtReturn extends Node {
    private FuncType funcType;
    public Token tokenReturn;
    public Exp exp;
    public Token semicn;

    public void setFuncType(FuncType funcType) {
        this.funcType = funcType;
    }

    public void errorCheck() {
        if (this.exp != null) {
            this.exp.errorCheck();
        }
        // f
        this.errorCheckMismatchReturnType();
        // g
        this.errorCheckMissSemicn();

    }

    public void errorCheckMismatchReturnType() {
        if ((this.funcType == FuncType.VOID && this.exp != null)
                || (this.funcType == FuncType.INT && this.exp == null)) {
            Node.errorHandle.addError(this.tokenReturn.getTokenPos(), ErrorType.f);
        }
    }

    public void errorCheckMissSemicn() {
        if (this.semicn.getTokenKey() != TokenKey.SEMICN) {
            Node.errorHandle.addError(this.semicn.getTokenPos(), ErrorType.i);
        }
    }

    public void semanticAnalyse() {
        Quadruple quadruple;
        if (this.funcType == FuncType.INT) {
            quadruple = new Quadruple("", QuadrupleOp.RET, this.exp.semanticAnalyse(), "");
        } else {
            quadruple = new Quadruple("", QuadrupleOp.RET, "", "");
        }
        Node.intermediate.addIntermediateCode(quadruple);
    }
}
