package SysY.AST;

public class Cond extends Node {
    public LOrExp lOrExp;

    public Cond() {
        this.lOrExp = null;
    }

    public void errorCheck() {
        this.lOrExp.errorCheck();
    }

    public void semanticAnalyse(String labelBegin, String labelEnd) {
        this.lOrExp.semanticAnalyse(labelBegin, labelEnd);
    }

    public void semanticAnalyseOpt(String labelBegin, String labelEnd) {
        this.lOrExp.semanticAnalyseOpt(labelBegin, labelEnd);
    }
}
