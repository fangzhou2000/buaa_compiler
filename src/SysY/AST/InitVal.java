package SysY.AST;

import SysY.LexicAnalysis.Token;

import java.util.ArrayList;

public class InitVal extends Node {
    public int dim;
    public Exp exp;
    public Token lBrace;
    public InitVal initVal;
    public ArrayList<Token> commaList;
    public ArrayList<InitVal> initValList;
    public Token rBrace;

    public InitVal() {
        this.commaList = new ArrayList<>();
        this.initValList = new ArrayList<>();
    }

    public Exp getExp() {
        return this.exp;
    }

    public Exp getExp(int i) {
        if (i == 0) {
            return this.initVal.getExp();
        } else {
            return this.initValList.get(i - 1).getExp();
        }
    }

    public Exp getExp(int i, int j) {
        if (i == 0) {
            return this.initVal.getExp(j);
        } else {
            return this.initValList.get(i - 1).getExp(j);
        }
    }

    public void errorCheck() {
        if (this.exp != null) {
            this.exp.errorCheck();
        }
        if (this.initVal != null) {
            this.initVal.errorCheck();
        }
        for (InitVal i : this.initValList) {
            i.errorCheck();
        }
    }
}
