package SysY.AST;

import SysY.SemanticAnalysis.Quadruple;
import SysY.SemanticAnalysis.QuadrupleOp;

import java.util.ArrayList;

public class AddExp extends Node{
    public MulExp mulExp;
    public ArrayList<AddOp> addOpList;
    public ArrayList<MulExp> mulExpList;

    public AddExp() {
        this.mulExp = null;
        this.addOpList = new ArrayList<>();
        this.mulExpList = new ArrayList<>();
    }

    public int getDim() {
        return this.mulExp.getDim();
    }

    public void errorCheck() {
        this.mulExp.errorCheck();
        for (MulExp m : this.mulExpList) {
            m.errorCheck();
        }
    }

    public String semanticAnalyse() {
        String ret = this.mulExp.semanticAnalyse();
        for (int i = 0; i < this.addOpList.size(); i++) {
            String arg1 = ret;
            String arg2 = this.mulExpList.get(i).semanticAnalyse();
            if (this.addOpList.get(i) == AddOp.PLUS) {
                if (Node.symbolTable.isImm(arg1) && Node.symbolTable.isImm(arg2)) {
                    ret = Integer.toString(Integer.parseInt(arg1) + Integer.parseInt(arg2));
                } else if (arg1.equals("0") || arg1.equals("-0")) {
                    ret = arg2;
                } else if (arg2.equals("0") || arg2.equals("-0")) {
                    ret = arg1;
                } else {
                    String temp = Node.intermediate.generateTemp(
                            Node.symbolTable.getSymbolValue(arg1) + Node.symbolTable.getSymbolValue(arg2));
                    Quadruple quadruple = new Quadruple(temp, QuadrupleOp.ADD, arg1, arg2);
                    Node.intermediate.addIntermediateCode(quadruple);
                    ret = temp;
                }
            } else if (this.addOpList.get(i) == AddOp.MINU) {
                if (Node.symbolTable.isImm(arg1) && Node.symbolTable.isImm(arg2)) {
                    ret = Integer.toString(Integer.parseInt(arg1) - Integer.parseInt(arg2));
                } else if (arg2.equals("0") || arg2.equals("-0")) {
                    ret = arg1;
                } else {
                    String temp = Node.intermediate.generateTemp(
                            Node.symbolTable.getSymbolValue(arg1) - Node.symbolTable.getSymbolValue(arg2));
                    Quadruple quadruple = new Quadruple(temp, QuadrupleOp.SUB, arg1, arg2);
                    Node.intermediate.addIntermediateCode(quadruple);
                    ret = temp;
                }
            }
        }
        return ret;
    }

    public int getConst() {
        int ret = this.mulExp.getConst();
        for (int i = 0; i < this.addOpList.size(); i++) {
            int arg1 = ret;
            int arg2 = this.mulExpList.get(i).getConst();
            if (this.addOpList.get(i) == AddOp.PLUS) {
                ret = arg1 + arg2;
            } else if (this.addOpList.get(i) == AddOp.MINU) {
                ret = arg1 - arg2;
            }
        }
        return ret;
    }
}
