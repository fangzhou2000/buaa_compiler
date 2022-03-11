package SysY.AST;

import SysY.LexicAnalysis.Token;
import SysY.SemanticAnalysis.Quadruple;
import SysY.SemanticAnalysis.QuadrupleOp;

import java.util.ArrayList;

public class Block extends Node {
    public Token lBrace;
    public ArrayList<BlockItem> blockItemList;
    public Token rBrace;

    public Block() {
        this.lBrace = null;
        this.blockItemList = new ArrayList<>();
        this.rBrace = null;
    }

    public void setFuncType(FuncType funcType) {
        for (BlockItem blockItem : this.blockItemList) {
            blockItem.setFuncType(funcType);
        }
    }

    public void setStmtType(StmtType stmtType) {
        for (BlockItem blockItem : this.blockItemList) {
            blockItem.setStmtType(stmtType);
        }
    }

    public void errorCheck() {
        Node.symbolTable.addBlockTable();
        for (BlockItem b : this.blockItemList) {
            b.errorCheck();
        }
        Node.symbolTable.removeBlockTable();
    }

    public void errorCheck(boolean noBlockTable) {
        for (BlockItem b : this.blockItemList) {
            b.errorCheck();
        }
    }

    public boolean isMissReturn() {
        //无需考虑数据流
        if (this.blockItemList.size() > 0) {
            if (!this.blockItemList.get(this.blockItemList.size() - 1).isDecl) {
                return this.blockItemList.get(this.blockItemList.size() - 1).stmt.stmtType != StmtType.RETURN;
            }
        }
        return true;
    }

    public void semanticAnalyse() {
        Quadruple quadruple1 = new Quadruple("", QuadrupleOp.BLOCK_BEGIN, "main", "");
        Node.intermediate.addIntermediateCode(quadruple1);
        Node.symbolTable.addBlockTable();
        for (BlockItem blockItem : this.blockItemList) {
            blockItem.semanticAnalyse();
        }
        Quadruple quadruple2 = new Quadruple("", QuadrupleOp.BLOCK_END, "", "");
        Node.intermediate.addIntermediateCode(quadruple2);
        Node.symbolTable.removeBlockTable();
    }

    public void semanticAnalyse(boolean noBlockTable) {
        for (BlockItem blockItem : this.blockItemList) {
            blockItem.semanticAnalyse();
        }
    }
}
