package SysY.AST;

import SysY.SemanticAnalysis.Quadruple;
import SysY.SemanticAnalysis.QuadrupleOp;

import java.util.ArrayList;

public class EqExp extends Node {
    public RelExp relExp;
    public ArrayList<EqOp> eqOpList;
    public ArrayList<RelExp> relExpList;

    public EqExp() {
        this.eqOpList = new ArrayList<>();
        this.relExpList = new ArrayList<>();
    }

    public void errorCheck() {
        this.relExp.errorCheck();
        for (RelExp r : this.relExpList) {
            r.errorCheck();
        }
    }

    public void semanticAnalyse(String labelEnd) {
        String res = this.relExp.semanticAnalyse();
        for (int i = 0; i < this.eqOpList.size(); i++) {
            if (this.eqOpList.get(i) == EqOp.EQL) {
                String arg1 = res;
                String arg2 = this.relExpList.get(i).semanticAnalyse();
                String temp = Node.intermediate.generateTemp(0);
                Quadruple quadruple1 = new Quadruple(temp, QuadrupleOp.EQ, arg1, arg2);
                Node.intermediate.addIntermediateCode(quadruple1);
                res = temp;
            } else if (this.eqOpList.get(i) == EqOp.NEQ) {
                String arg1 = res;
                String arg2 = this.relExpList.get(i).semanticAnalyse();
                String temp = Node.intermediate.generateTemp(0);
                Quadruple quadruple1 = new Quadruple(temp, QuadrupleOp.NEQ, arg1, arg2);
                Node.intermediate.addIntermediateCode(quadruple1);
                res = temp;
            }
        }
        Quadruple quadruple2 = new Quadruple(labelEnd, QuadrupleOp.BEQ, res, "0");
        Node.intermediate.addIntermediateCode(quadruple2);
    }

    public void semanticAnalyseOpt(String labelBegin, String labelEnd, boolean isLast) {
        String res = this.relExp.semanticAnalyse();
        for (int i = 0; i < this.eqOpList.size(); i++) {
            if (this.eqOpList.get(i) == EqOp.EQL) {
                String arg1 = res;
                String arg2 = this.relExpList.get(i).semanticAnalyse();
                String temp = Node.intermediate.generateTemp(0);
                Quadruple quadruple1 = new Quadruple(temp, QuadrupleOp.EQ, arg1, arg2);
                Node.intermediate.addIntermediateCode(quadruple1);
                res = temp;
            } else if (this.eqOpList.get(i) == EqOp.NEQ) {
                String arg1 = res;
                String arg2 = this.relExpList.get(i).semanticAnalyse();
                String temp = Node.intermediate.generateTemp(0);
                Quadruple quadruple1 = new Quadruple(temp, QuadrupleOp.NEQ, arg1, arg2);
                Node.intermediate.addIntermediateCode(quadruple1);
                res = temp;
            }
        }
        if (isLast) {
            Quadruple quadruple2 = new Quadruple(labelBegin, QuadrupleOp.BNE, res, "0");
            Node.intermediate.addIntermediateCode(quadruple2);
        } else {
            Quadruple quadruple2 = new Quadruple(labelEnd, QuadrupleOp.BEQ, res, "0");
            Node.intermediate.addIntermediateCode(quadruple2);
        }
    }
}
