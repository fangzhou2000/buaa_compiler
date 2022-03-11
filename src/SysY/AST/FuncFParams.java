package SysY.AST;

import SysY.LexicAnalysis.Token;

import java.util.ArrayList;

public class FuncFParams extends Node {
    public int paramsNum;
    public ArrayList<Integer> paramsDimList;
    public FuncFParam funcFParam;
    public ArrayList<Token> commaList;
    public ArrayList<FuncFParam> funcFParamList;

    public FuncFParams() {
        this.paramsNum = 1;
        this.paramsDimList = new ArrayList<>();
        this.commaList = new ArrayList<>();
        this.funcFParamList = new ArrayList<>();
    }

    public void errorCheck() {
        this.funcFParam.errorCheck();
        for (FuncFParam f : funcFParamList) {
            f.errorCheck();
        }
    }

    public void semanticAnalyse() {
        if (this.paramsNum > 0) {
            this.funcFParam.semanticAnalyse();
            for (int i = 0; i < this.paramsNum - 1; i++) {
                this.funcFParamList.get(i).semanticAnalyse();
            }
        }
    }
}
