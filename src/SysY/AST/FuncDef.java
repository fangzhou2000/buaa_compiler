package SysY.AST;

import SysY.ErrorHandle.ErrorType;
import SysY.SemanticAnalysis.Quadruple;
import SysY.SemanticAnalysis.QuadrupleOp;
import SysY.SymbolTable.Symbol;
import SysY.SymbolTable.SymbolType;
import SysY.LexicAnalysis.Token;
import SysY.LexicAnalysis.TokenKey;

import java.util.ArrayList;

public class FuncDef extends Node {
    public int paramsNum;
    public ArrayList<Integer> paramsDimList;
    public FuncType funcType;
    public Token ident;
    public Token lParent;
    public FuncFParams funcFParams;
    public Token rParent;
    public Block block;

    public FuncDef() {
        this.paramsNum = 0;
        this.paramsDimList = new ArrayList<>();
    }

    public void setFuncType(FuncType funcType) {
        this.block.setFuncType(funcType);
    }

    public void errorCheck() {
        Symbol symbol;
        if (this.funcType == FuncType.INT) {
            symbol = new Symbol(this.ident.getTokenString(), SymbolType.INT_FUNC, this.paramsNum, this.paramsDimList);
        } else {
            symbol = new Symbol(this.ident.getTokenString(), SymbolType.VOID_FUNC, this.paramsNum, this.paramsDimList);
        }
        //b
        this.addToSymbolTableAndErrorCheckSameName(symbol, this.ident.getTokenPos());
        Node.symbolTable.addBlockTable();
        if (this.funcFParams != null) {
            this.funcFParams.errorCheck();
        }
        //j
        this.errorCheckMissRparent();
        this.block.errorCheck(true);
        //g
        this.errorCheckMissReturn();
        Node.symbolTable.removeBlockTable();
    }

    public void errorCheckMissRparent() {
        if (this.rParent.getTokenKey() != TokenKey.RPARENT) {
            Node.errorHandle.addError(this.rParent.getTokenPos(), ErrorType.j);
        }
    }

    public void errorCheckMissReturn() {
        if (this.funcType == FuncType.INT && this.block.isMissReturn()) {
            Node.errorHandle.addError(this.block.rBrace.getTokenPos(), ErrorType.g);
        }
    }

    public void semanticAnalyse() {
        Quadruple quadrupleBegin = new Quadruple("", QuadrupleOp.FUNC_BEGIN, this.ident.getTokenString(), "");
        Node.intermediate.addIntermediateCode(quadrupleBegin);
        Symbol symbol;
        if (this.funcType == FuncType.INT) {
            symbol = new Symbol(this.ident.getTokenString(), SymbolType.INT_FUNC, this.paramsNum, this.paramsDimList);
        } else {
            symbol = new Symbol(this.ident.getTokenString(), SymbolType.VOID_FUNC, this.paramsNum, this.paramsDimList);
        }
        Node.symbolTable.addSymbol(symbol);
        Quadruple quadrupleFunc;
        if (this.funcType == FuncType.INT) {
            quadrupleFunc = new Quadruple("", QuadrupleOp.INT_FUNC, this.ident.getTokenString(), "");
        } else {
            quadrupleFunc = new Quadruple("", QuadrupleOp.VOID_FUNC, this.ident.getTokenString(), "");
        }
        Node.intermediate.addIntermediateCode(quadrupleFunc);
        Node.symbolTable.addBlockTable();
        if (this.funcFParams != null) {
            this.funcFParams.semanticAnalyse();
        }
        this.block.semanticAnalyse(true);
        Node.symbolTable.removeBlockTable();
        Quadruple quadrupleEnd = new Quadruple("", QuadrupleOp.FUNC_END, this.ident.getTokenString(), "");
        Node.intermediate.addIntermediateCode(quadrupleEnd);
    }
}
