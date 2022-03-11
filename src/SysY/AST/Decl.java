package SysY.AST;

public class Decl extends Node {
    public boolean isConst;
    public ConstDecl constDecl;
    public VarDecl varDecl;

    public void errorCheck() {
        if (isConst) {
            this.constDecl.errorCheck();
        } else {
            this.varDecl.errorCheck();
        }
    }

    public void semanticAnalyse() {
        if (isConst) {
            this.constDecl.semanticAnalyse();
        } else {
            this.varDecl.semanticAnalyse();
        }
    }
}
