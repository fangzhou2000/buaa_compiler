package SysY.AST;

import SysY.ErrorHandle.ErrorHandle;
import SysY.ErrorHandle.ErrorType;
import SysY.LexicAnalysis.TokenPos;
import SysY.SemanticAnalysis.Intermediate;
import SysY.SymbolTable.Symbol;
import SysY.SymbolTable.SymbolTable;

import java.util.ArrayList;

public class Node {
    public static SymbolTable symbolTable;
    public static ErrorHandle errorHandle;
    public static Intermediate intermediate;
    public static boolean whileOpt = true;

    public void errorCheck() {

    }

    //b
    public void addToSymbolTableAndErrorCheckSameName(Symbol symbol, TokenPos tokenPos) {
        if (Node.symbolTable.isContainSameName(symbol.getName())) {
            errorHandle.addError(tokenPos, ErrorType.b);
        } else {
            Node.symbolTable.addSymbol(symbol);
        }
    }

    //c
    public void errorCheckUndefinedName(String name, TokenPos tokenPos) {
        if (Node.symbolTable.isUndefinedName(name)) {
            errorHandle.addError(tokenPos, ErrorType.c);
        }
    }

    //d
    public void errorCheckMismatchFuncParamsNum(String name, int num, TokenPos tokenPos) {
        if (!Node.symbolTable.isUndefinedName(name)) {
            if (Node.symbolTable.IsMismatchFuncParamsNum(name, num)) {
                errorHandle.addError(tokenPos, ErrorType.d);
            }
        }
    }

    //e
    public void errorCheckMismatchFuncParamsDim(String name, ArrayList<Integer> dimList, TokenPos tokenPos) {
        if (!Node.symbolTable.isUndefinedName(name)) {
            if (Node.symbolTable.IsMismatchFuncParamsDim(name, dimList)) {
                errorHandle.addError(tokenPos, ErrorType.e);
            }
        }
    }

    //h
    public void errorCheckChangeConst(String name, TokenPos tokenPos) {
        if (Node.symbolTable.isChangeConst(name)) {
            errorHandle.addError(tokenPos, ErrorType.h);
        }
    }
}
