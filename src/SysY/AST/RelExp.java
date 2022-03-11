package SysY.AST;

import SysY.SemanticAnalysis.Quadruple;
import SysY.SemanticAnalysis.QuadrupleOp;

import java.util.ArrayList;

public class RelExp extends Node {
    public AddExp addExp;
    public ArrayList<RelOp> relOpList;
    public ArrayList<AddExp> addExpList;

    public RelExp() {
        this.relOpList = new ArrayList<>();
        this.addExpList = new ArrayList<>();
    }

    public void errorCheck() {
        this.addExp.errorCheck();
        for (AddExp a : this.addExpList) {
            a.errorCheck();
        }
    }

    public String semanticAnalyse() {
        String ret = this.addExp.semanticAnalyse();
        for (int i = 0; i < this.relOpList.size(); i++) {
            if (this.relOpList.get(i) == RelOp.GRE) {
                String arg1 = ret;
                String arg2 = this.addExpList.get(i).semanticAnalyse();
                String temp = Node.intermediate.generateTemp(0);
                Quadruple quadruple1 = new Quadruple(temp, QuadrupleOp.GRE, arg1, arg2);
                Node.intermediate.addIntermediateCode(quadruple1);
                ret = temp;
            } else if (this.relOpList.get(i) == RelOp.LSS) {
                String arg1 = ret;
                String arg2 = this.addExpList.get(i).semanticAnalyse();
                String temp = Node.intermediate.generateTemp(0);
                Quadruple quadruple1 = new Quadruple(temp, QuadrupleOp.LSS, arg1, arg2);
                Node.intermediate.addIntermediateCode(quadruple1);
                ret = temp;
            } else if (this.relOpList.get(i) == RelOp.GEQ) {
                String arg1 = ret;
                String arg2 = this.addExpList.get(i).semanticAnalyse();
                String temp = Node.intermediate.generateTemp(0);
                Quadruple quadruple1 = new Quadruple(temp, QuadrupleOp.GEQ, arg1, arg2);
                Node.intermediate.addIntermediateCode(quadruple1);
                ret = temp;
            } else if (this.relOpList.get(i) == RelOp.LEQ) {
                String arg1 = ret;
                String arg2 = this.addExpList.get(i).semanticAnalyse();
                String temp = Node.intermediate.generateTemp(0);
                Quadruple quadruple1 = new Quadruple(temp, QuadrupleOp.LEQ, arg1, arg2);
                Node.intermediate.addIntermediateCode(quadruple1);
                ret = temp;
            }
        }
        return ret;
    }
}
