package SysY.AST;

import java.util.ArrayList;

public class LAndExp extends Node {
    public EqExp eqExp;
    public ArrayList<LAndOp> andOpList;
    public ArrayList<EqExp> eqExpList;

    public LAndExp() {
        this.andOpList = new ArrayList<>();
        this.eqExpList = new ArrayList<>();
    }

    public void errorCheck() {
        this.eqExp.errorCheck();
        for (EqExp e : this.eqExpList) {
            e.errorCheck();
        }
    }

    public void semanticAnalyse(String labelEnd) {
        this.eqExp.semanticAnalyse(labelEnd);
        for (EqExp eqExp : eqExpList) {
            eqExp.semanticAnalyse(labelEnd);
        }
    }

    public void semanticAnalyseOpt(String labelBegin, String labelEnd) {
        if (andOpList.size() == 0) {
            this.eqExp.semanticAnalyseOpt(labelBegin, labelEnd, true);
        } else {
            this.eqExp.semanticAnalyseOpt(labelBegin, labelEnd, false);
            for (int i = 0; i < this.andOpList.size(); i++) {
                EqExp eqExp = this.eqExpList.get(i);
                if (i == this.andOpList.size() - 1) {
                    eqExp.semanticAnalyseOpt(labelBegin, labelEnd, true);
                } else {
                    eqExp.semanticAnalyseOpt(labelBegin, labelEnd, false);
                }
            }
        }
    }
}
