package SysY.AST;

import SysY.ErrorHandle.ErrorType;
import SysY.LexicAnalysis.Token;
import SysY.LexicAnalysis.TokenKey;

import java.util.ArrayList;

public class VarDecl extends Node {

    public BType btype;
    public VarDef varDef;
    public ArrayList<Token> commaList;
    public ArrayList<VarDef> varDefList;
    public Token semicn;

    public VarDecl() {
        this.commaList = new ArrayList<>();
        this.varDefList = new ArrayList<>();
    }

    public void errorCheck() {
        this.btype.errorCheck();
        this.varDef.errorCheck();
        for (VarDef varDef : this.varDefList) {
            varDef.errorCheck();
        }
        //i
        this.errorCheckMissSemicn();
    }

    public void errorCheckMissSemicn() {
        if (this.semicn.getTokenKey() != TokenKey.SEMICN) {
            Node.errorHandle.addError(this.semicn.getTokenPos(), ErrorType.i);
        }
    }

    public void semanticAnalyse() {
        this.varDef.semanticAnalyse();
        for (VarDef varDef : this.varDefList) {
            varDef.semanticAnalyse();
        }
    }
}
