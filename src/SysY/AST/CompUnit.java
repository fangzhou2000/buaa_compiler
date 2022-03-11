package SysY.AST;

import SysY.SemanticAnalysis.Quadruple;

import java.util.ArrayList;

public class CompUnit extends Node {
    public ArrayList<Decl> declList;
    public ArrayList<FuncDef> funcDefList;
    public MainFuncDef mainFuncDef;

    public CompUnit() {
        this.declList = new ArrayList<>();
        this.funcDefList = new ArrayList<>();
        this.mainFuncDef = null;
    }

    public void errorCheck() {
        for (Decl d : this.declList) {
            d.errorCheck();
        }
        for (FuncDef f : this.funcDefList) {
            f.errorCheck();
        }
        this.mainFuncDef.errorCheck();
    }

    public void semanticAnalyse() {
        for (Decl decl : this.declList) {
            decl.semanticAnalyse();
        }
        for (FuncDef funcDef : this.funcDefList) {
            funcDef.semanticAnalyse();
        }
        this.mainFuncDef.semanticAnalyse();
    }
}
