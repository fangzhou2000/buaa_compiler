package SysY.AST;

import SysY.ErrorHandle.ErrorType;
import SysY.SemanticAnalysis.Quadruple;
import SysY.SemanticAnalysis.QuadrupleOp;
import SysY.LexicAnalysis.Token;
import SysY.LexicAnalysis.TokenKey;

import java.util.ArrayList;

public class LVal extends Node {
    public int errorFlag;
    public int dim;
    public Token ident;
    public ArrayList<Token> lBrackList;
    public ArrayList<Exp> expList;
    public ArrayList<Token> rBrackList;

    public LVal() {
        this.lBrackList = new ArrayList<>();
        this.expList = new ArrayList<>();
        this.rBrackList = new ArrayList<>();
    }

    public int getDim() {
        int IdentDim =  Node.symbolTable.getSymbol(this.ident.getTokenString()).getDim();
        if (this.dim <= IdentDim) {
            return IdentDim - this.dim;
        }
        return 0;
    }

    public void errorCheck() {
        // c
        this.errorCheckUndefinedName(this.ident.getTokenString(), this.ident.getTokenPos());
        this.errorFlag = 0;
        for (int i = 0; i < this.expList.size(); i++) {
            this.expList.get(i).errorCheck();
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

    public String semanticAnalyse() {
        if (this.dim == 0) {
            return this.ident.getTokenString();
        } else if (this.dim == 1) {
            String i = this.expList.get(0).semanticAnalyse();
            if (symbolTable.isImm(i)) {
                return this.ident.getTokenString() + "[" + i + "]";
            } else {
                String temp = Node.intermediate.generateTemp(0);
                Quadruple quadruple = new Quadruple(temp, QuadrupleOp.LASS, i, "");
                Node.intermediate.addIntermediateCode(quadruple);
                return this.ident.getTokenString() + "[" + temp + "]";
            }
        } else if (this.dim == 2) {
            String i = this.expList.get(0).semanticAnalyse();
            String j = this.expList.get(1).semanticAnalyse();
            String temp1 = Node.intermediate.generateTemp(
                    Node.symbolTable.getSymbolValue(i) * Node.symbolTable.getSymbol(this.ident.getTokenString()).getSizeJ());
            Quadruple quadruple1 = new Quadruple(temp1, QuadrupleOp.MUL,
                    i, Integer.toString(Node.symbolTable.getSymbol(this.ident.getTokenString()).getSizeJ()));
            Node.intermediate.addIntermediateCode(quadruple1);
            String temp2 = Node.intermediate.generateTemp(
                    Node.symbolTable.getSymbolValue(j) + Node.symbolTable.getSymbolValue(temp1));
            Quadruple quadruple2 = new Quadruple(temp2, QuadrupleOp.ADD,
                    j, temp1);
            Node.intermediate.addIntermediateCode(quadruple2);
            return this.ident.getTokenString() + "[" + temp2 + "]";
        }
        return "LVal wrong";
    }

    public int getConst() {
        if (this.dim == 0) {
            return Node.symbolTable.getSymbolValue(this.ident.getTokenString());
        } else if (this.dim == 1) {
            int i = this.expList.get(0).getConst();
            return Node.symbolTable.getSymbolValue(this.ident.getTokenString() + "[" + i + "]");
        } else if (this.dim == 2) {
            int i = this.expList.get(0).getConst();
            int j = this.expList.get(1).getConst();
            int index = i * Node.symbolTable.getSymbol(this.ident.getTokenString()).getSizeJ() + j;
            return Node.symbolTable.getSymbolValue(this.ident.getTokenString() + "[" + index + "]");
        }
        return 0;
    }
}
