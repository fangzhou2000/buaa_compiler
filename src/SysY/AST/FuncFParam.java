package SysY.AST;

import SysY.ErrorHandle.ErrorType;
import SysY.SemanticAnalysis.Quadruple;
import SysY.SemanticAnalysis.QuadrupleOp;
import SysY.SymbolTable.Symbol;
import SysY.SymbolTable.SymbolType;
import SysY.LexicAnalysis.Token;
import SysY.LexicAnalysis.TokenKey;

import java.util.ArrayList;

public class FuncFParam extends Node {
    public int errorFlag;
    public int dim;
    public BType btype;
    public Token ident;
    public Token lBrack;
    public Token rBrack;
    public ArrayList<Token> lBrackList;
    public ArrayList<ConstExp> constExpList;
    public ArrayList<Token> rBrackList;

    public FuncFParam() {
        this.dim = 0;
        this.lBrackList = new ArrayList<>();
        this.constExpList = new ArrayList<>();
        this.rBrackList = new ArrayList<>();
    }

    public void errorCheck() {
        // b
        Symbol symbol = new Symbol(this.ident.getTokenString(), SymbolType.VAR);
        symbol.setDim(this.dim);
        this.addToSymbolTableAndErrorCheckSameName(symbol, this.ident.getTokenPos());
        this.btype.errorCheck();
        // k
        this.errorFlag = 0;
        if (this.lBrack != null) {
            this.errorCheckMissRbrack(this.rBrack);
        }
        for (int i = 0; i < this.constExpList.size(); i++) {
            this.constExpList.get(i).errorCheck();
            // k
            this.errorCheckMissRbrack(this.rBrackList.get(i));
        }
    }

    public void errorCheckMissRbrack(Token rBrack) {
        if (this.errorFlag == 0) {
            if (rBrack.getTokenKey() != TokenKey.RBRACK) {
                this.errorFlag = 1;
                Node.errorHandle.addError(rBrack.getTokenPos(), ErrorType.k);
            }
        }
    }

    public void semanticAnalyse() {
        if (this.dim == 0) {
            Symbol symbol = new Symbol(this.ident.getTokenString(), SymbolType.PARA);
            Node.symbolTable.addSymbol(symbol);
            Quadruple quadruple = new Quadruple("", QuadrupleOp.PARA, this.ident.getTokenString(), "");
            Node.intermediate.addIntermediateCode(quadruple);
        } else if (this.dim == 1) {
            Symbol symbol = new Symbol(this.ident.getTokenString(), SymbolType.PARA);
            symbol.setDim(this.dim);
            Node.symbolTable.addSymbol(symbol);
            Quadruple quadruple = new Quadruple("", QuadrupleOp.PARA, this.ident.getTokenString() + "[]", "");
            Node.intermediate.addIntermediateCode(quadruple);
        } else if (this.dim == 2) {
            int sizeJ = this.constExpList.get(0).semanticAnalyse();
            Symbol symbol = new Symbol(this.ident.getTokenString(), SymbolType.PARA);
            symbol.setDim(this.dim);
            symbol.setSizeJ(sizeJ);
            Quadruple quadruple = new Quadruple("", QuadrupleOp.PARA, this.ident.getTokenString() + "[]" + "[" + sizeJ + "]", "");
            quadruple.setSizeJ(sizeJ);
            Node.intermediate.addIntermediateCode(quadruple);
            Node.symbolTable.addSymbol(symbol);
        }
    }
}
