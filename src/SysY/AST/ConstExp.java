package SysY.AST;

public class ConstExp extends Node {
    public AddExp addExp;

    public void errorCheck() {
        this.addExp.errorCheck();
    }

    public int semanticAnalyse() {
        return this.addExp.getConst();
    }
}
