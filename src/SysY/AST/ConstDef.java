package SysY.AST;

import SysY.ErrorHandle.ErrorType;
import SysY.SemanticAnalysis.Quadruple;
import SysY.SemanticAnalysis.QuadrupleOp;
import SysY.SymbolTable.Symbol;
import SysY.SymbolTable.SymbolType;
import SysY.LexicAnalysis.Token;
import SysY.LexicAnalysis.TokenKey;

import java.util.ArrayList;

public class ConstDef extends Node {
    public int errorFlag;
    public int dim;
    public Token ident;
    public ArrayList<Token> lBrackList;
    public ArrayList<ConstExp> constExpList;
    public ArrayList<Token> rBrackList;
    public Token assign;
    public ConstInitVal constInitVal;

    public ConstDef() {
        this.dim = 0;
        this.lBrackList = new ArrayList<>();
        this.constExpList = new ArrayList<>();
        this.rBrackList = new ArrayList<>();
    }

    public void errorCheck() {
        // b
        Symbol symbol = new Symbol(this.ident.getTokenString(), SymbolType.CONST);
        symbol.setDim(this.dim);
        this.addToSymbolTableAndErrorCheckSameName(symbol, this.ident.getTokenPos());
        this.errorFlag = 0;
        for (int i = 0; i < this.constExpList.size(); i++) {
            this.constExpList.get(i).errorCheck();
            // k
            this.errorCheckMissRbrack(this.rBrackList.get(i));
        }
        this.constInitVal.errorCheck();
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
            int arg = this.constInitVal.getConstExp().semanticAnalyse();
            Quadruple quadruple = new Quadruple(this.ident.getTokenString(), QuadrupleOp.CASS, Integer.toString(arg), "");
            Node.intermediate.addIntermediateCode(quadruple);
            Symbol symbol = new Symbol(this.ident.getTokenString(), SymbolType.CONST);
            symbol.setDim(this.dim);
            symbol.setValue(arg);
            Node.symbolTable.addSymbol(symbol);
        } else if (this.dim == 1) {
            int sizeI = this.constExpList.get(0).semanticAnalyse();
            Symbol symbol = new Symbol(this.ident.getTokenString(), SymbolType.CONST);
            symbol.setDim(this.dim);
            symbol.setSizeI(sizeI);
            Quadruple quadruple0 = new Quadruple(this.ident.getTokenString(), QuadrupleOp.ARR, "", "");
            quadruple0.setLen(sizeI);
            Node.intermediate.addIntermediateCode(quadruple0);
            for (int i = 0; i < sizeI; i++) {
                int arg = this.constInitVal.getConstExp(i).semanticAnalyse();
                Quadruple quadruple = new Quadruple(this.ident.getTokenString(), QuadrupleOp.CARAS, Integer.toString(arg), "");
                quadruple.setIndex(Integer.toString(i));
                Node.intermediate.addIntermediateCode(quadruple);
                symbol.getValueList().add(arg);
            }
            Node.symbolTable.addSymbol(symbol);
        } else if (this.dim == 2) {
            int sizeI = this.constExpList.get(0).semanticAnalyse();
            int sizeJ = this.constExpList.get(1).semanticAnalyse();
            Symbol symbol = new Symbol(this.ident.getTokenString(), SymbolType.CONST);
            symbol.setDim(this.dim);
            symbol.setSizeI(sizeI);
            symbol.setSizeJ(sizeJ);
            Quadruple quadruple0 = new Quadruple(this.ident.getTokenString(), QuadrupleOp.ARR, "", "");
            quadruple0.setLen(sizeI * sizeJ);
            quadruple0.setSizeJ(sizeJ);
            Node.intermediate.addIntermediateCode(quadruple0);
            for (int i = 0; i < sizeI; i++) {
                for (int j = 0; j < sizeJ; j++) {
                    int arg = this.constInitVal.getConstExp(i, j).semanticAnalyse();
                    Quadruple quadruple = new Quadruple(this.ident.getTokenString(), QuadrupleOp.CARAS, Integer.toString(arg), "");
                    quadruple.setIndex(Integer.toString(i * sizeJ + j));
                    Node.intermediate.addIntermediateCode(quadruple);
                    symbol.getValueList().add(arg);
                }
            }
            Node.symbolTable.addSymbol(symbol);
        }
    }
}
