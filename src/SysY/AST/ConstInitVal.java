package SysY.AST;

import SysY.LexicAnalysis.Token;

import java.util.ArrayList;

public class ConstInitVal extends Node {
    public int dim;
    public ConstExp constExp;
    public Token lBrace;
    public ConstInitVal constInitVal;
    public ArrayList<Token> commaList;
    public ArrayList<ConstInitVal> constInitValList;
    public Token rBrace;

    public ConstInitVal() {
        this.commaList = new ArrayList<>();
        this.constInitValList = new ArrayList<>();
    }

    public ConstExp getConstExp() {
        return this.constExp;
    }

    public ConstExp getConstExp(int i) {
        if (i == 0) {
            return this.constInitVal.getConstExp();
        } else {
            return this.constInitValList.get(i - 1).getConstExp();
        }
    }

    public ConstExp getConstExp(int i, int j) {
        if (i == 0) {
            return this.constInitVal.getConstExp(j);
        } else {
            return this.constInitValList.get(i - 1).getConstExp(j);
        }
    }

    public void errorCheck() {
        if (this.constExp != null) {
            this.constExp.errorCheck();
        }
        if (this.constInitVal != null) {
            this.constInitVal.errorCheck();
        }
        for (ConstInitVal c : this.constInitValList) {
            c.errorCheck();
        }
    }
}
