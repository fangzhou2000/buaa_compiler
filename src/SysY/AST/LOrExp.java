package SysY.AST;

import SysY.SemanticAnalysis.Quadruple;
import SysY.SemanticAnalysis.QuadrupleOp;

import java.util.ArrayList;

public class LOrExp extends Node {
    public LAndExp lAndExp;
    public ArrayList<LOrOp> orOpList;
    public ArrayList<LAndExp> lAndExpList;

    public LOrExp() {
        this.orOpList = new ArrayList<>();
        this.lAndExpList = new ArrayList<>();
    }

    public void errorCheck() {
        this.lAndExp.errorCheck();
        for (LAndExp l : this.lAndExpList) {
            l.errorCheck();
        }
    }

    public void semanticAnalyse(String labelBegin, String labelEnd) {
        if (this.orOpList.size() == 0) {
            this.lAndExp.semanticAnalyse(labelEnd);
        } else {
            String label1 = Node.intermediate.generateLabel();
            this.lAndExp.semanticAnalyse(label1);
            Quadruple quadruple1 = new Quadruple("", QuadrupleOp.GOTO, labelBegin, "");
            Node.intermediate.addIntermediateCode(quadruple1);
            Quadruple quadruple2 = new Quadruple("", QuadrupleOp.LABEL, label1, "");
            Node.intermediate.addIntermediateCode(quadruple2);
            for (int i = 0; i < this.orOpList.size(); i++) {
                LAndExp lAndExp = this.lAndExpList.get(i);
                if (i == this.orOpList.size() - 1) {
                    lAndExp.semanticAnalyse(labelEnd);
                } else {
                    String label2 = Node.intermediate.generateLabel();
                    lAndExp.semanticAnalyse(label2);
                    Quadruple quadruple3 = new Quadruple("", QuadrupleOp.GOTO, labelBegin, "");
                    Node.intermediate.addIntermediateCode(quadruple3);
                    Quadruple quadruple4 = new Quadruple("", QuadrupleOp.LABEL, label2, "");
                    Node.intermediate.addIntermediateCode(quadruple4);
                }
            }
        }
    }

    public void semanticAnalyseOpt(String labelBegin, String labelEnd) {
        if (this.orOpList.size() == 0) {
            this.lAndExp.semanticAnalyseOpt(labelBegin, labelEnd);
        } else {
            String label1 = Node.intermediate.generateLabel();
            this.lAndExp.semanticAnalyseOpt(labelBegin, label1);
            Quadruple quadruple1 = new Quadruple("", QuadrupleOp.LABEL, label1, "");
            Node.intermediate.addIntermediateCode(quadruple1);
            for (int i = 0; i < this.orOpList.size(); i++) {
                LAndExp lAndExp = this.lAndExpList.get(i);
                if (i == this.orOpList.size() - 1) {
                    lAndExp.semanticAnalyseOpt(labelBegin, labelEnd);
                } else {
                    String label2 = Node.intermediate.generateLabel();
                    lAndExp.semanticAnalyseOpt(labelBegin, label2);
                    Quadruple quadruple4 = new Quadruple("", QuadrupleOp.LABEL, label2, "");
                    Node.intermediate.addIntermediateCode(quadruple4);
                }
            }
        }
    }
}
