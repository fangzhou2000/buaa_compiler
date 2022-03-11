package SysY.AST;

import SysY.ErrorHandle.ErrorHandle;
import SysY.SemanticAnalysis.Intermediate;
import SysY.SemanticAnalysis.Quadruple;
import SysY.SymbolTable.SymbolTable;

public class Root extends Node{
    public CompUnit compUnit;

    public Root(SymbolTable symbolTable, ErrorHandle errorHandle, Intermediate intermediate) {
        Node.symbolTable = symbolTable;
        Node.errorHandle = errorHandle;
        Node.intermediate = intermediate;
        this.compUnit = null;
    }

    public void errorCheck() {
        this.compUnit.errorCheck();
    }

    public void semanticAnalyse() {
        this.compUnit.semanticAnalyse();
    }
}

