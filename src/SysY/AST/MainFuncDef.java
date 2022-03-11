package SysY.AST;

import SysY.ErrorHandle.ErrorType;
import SysY.LexicAnalysis.Token;
import SysY.LexicAnalysis.TokenKey;
import SysY.SemanticAnalysis.Quadruple;
import SysY.SemanticAnalysis.QuadrupleOp;

public class MainFuncDef extends Node {
    public Token tokenInt;
    public Token main;
    public Token lParent;
    public Token rParent;
    public Block block;

    public void setFuncType(FuncType funcType) {
        this.block.setFuncType(funcType);
    }

    public void errorCheck() {
        this.errorCheckMissRparent();
        this.block.errorCheck();
        this.errorCheckMissReturn();
    }

    public void errorCheckMissRparent() {
        if (this.rParent.getTokenKey() != TokenKey.RPARENT) {
            Node.errorHandle.addError(this.rParent.getTokenPos(), ErrorType.j);
        }
    }

    public void errorCheckMissReturn() {
        if (this.block.isMissReturn()) {
            Node.errorHandle.addError(this.block.rBrace.getTokenPos(), ErrorType.g);
        }
    }

    public void semanticAnalyse() {
        Quadruple quadruple1 = new Quadruple("", QuadrupleOp.MAIN, "", "");
        Node.intermediate.addIntermediateCode(quadruple1);
        this.block.semanticAnalyse();
        Quadruple quadruple2 = new Quadruple("", QuadrupleOp.MAIN_END, "", "");
        Node.intermediate.addIntermediateCode(quadruple2);
    }
}
