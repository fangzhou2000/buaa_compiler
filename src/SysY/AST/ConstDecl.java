package SysY.AST;

import SysY.ErrorHandle.ErrorType;
import SysY.LexicAnalysis.Token;
import SysY.LexicAnalysis.TokenKey;

import java.util.ArrayList;

public class ConstDecl extends Node {
    public Token tokenConst;
    public BType btype;
    public ConstDef constDef;
    public ArrayList<Token> commaList;
    public ArrayList<ConstDef> constDefList;
    public Token semicn;

    public ConstDecl() {
        this.tokenConst = null;
        this.btype = null;
        this.constDef = null;
        this.commaList = new ArrayList<>();
        this.constDefList = new ArrayList<>();
        this.semicn = null;
    }

    public void errorCheck() {
        this.btype.errorCheck();
        this.constDef.errorCheck();
        for (ConstDef constDef : this.constDefList) {
            constDef.errorCheck();
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
        this.constDef.semanticAnalyse();
        for (ConstDef constDef : this.constDefList) {
            constDef.semanticAnalyse();
        }
    }
}
