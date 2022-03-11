package SysY.AST;

public class Exp extends Node {
    public AddExp addExp;

    public int getDim() {
        return this.addExp.getDim();
    }

    public void errorCheck() {
        this.addExp.errorCheck();
    }

    public String semanticAnalyse() {
        return this.addExp.semanticAnalyse();
    }

    public int getConst() {
        return this.addExp.getConst();
    }
}
