package SysY.AST;

import SysY.SemanticAnalysis.Quadruple;
import SysY.SemanticAnalysis.QuadrupleOp;

import java.util.ArrayList;

public class MulExp extends Node {
    public UnaryExp unaryExp;
    public ArrayList<MulOp> mulOpList;
    public ArrayList<UnaryExp> unaryExpList;

    public MulExp() {
        this.mulOpList = new ArrayList<>();
        this.unaryExpList = new ArrayList<>();
    }

    public int getDim() {
        return this.unaryExp.getDim();
    }

    public void errorCheck() {
        this.unaryExp.errorCheck();
        for (UnaryExp u : this.unaryExpList) {
            u.errorCheck();
        }
    }

    public String semanticAnalyse() {
        String ret = this.unaryExp.semanticAnalyse();
        for (int i = 0; i < this.mulOpList.size(); i++) {
            String arg1 = ret;
            String arg2 = this.unaryExpList.get(i).semanticAnalyse();
            if (this.mulOpList.get(i) == MulOp.MULT) {
                if (Node.symbolTable.isImm(arg1) && Node.symbolTable.isImm(arg2)) {
                    ret = Integer.toString(Integer.parseInt(arg1) * Integer.parseInt(arg2));
                } else if (arg1.equals("0") || arg1.equals("-0")) {
                    ret = "0";
                } else if (arg2.equals("0") || arg2.equals("-0")) {
                    ret = "0";
                } else if (arg1.equals("1")) {
                    ret = arg2;
                } else if (arg2.equals("1")) {
                    ret = arg1;
                } else {
                    String temp = Node.intermediate.generateTemp(
                            Node.symbolTable.getSymbolValue(arg1) * Node.symbolTable.getSymbolValue(arg2));
                    Quadruple quadruple = new Quadruple(temp, QuadrupleOp.MUL, arg1, arg2);
                    Node.intermediate.addIntermediateCode(quadruple);
                    ret = temp;
                }
            } else if (this.mulOpList.get(i) == MulOp.DIV) {
                if (Node.symbolTable.isImm(arg1) && Node.symbolTable.isImm(arg2)) {
                    if (Integer.parseInt(arg2) != 0) {
                        ret = Integer.toString(Integer.parseInt(arg1) / Integer.parseInt(arg2));
                    } else {
                        ret = "0";
                    }
                } else if (arg1.equals("0") || arg1.equals("-0")){
                    ret = "0";
                } else if (arg2.equals("1")) {
                    ret = arg1;
                } else {
                    String temp;
                    if (Node.symbolTable.getSymbolValue(arg2) != 0) {
                        temp = Node.intermediate.generateTemp(
                                Node.symbolTable.getSymbolValue(arg1) / Node.symbolTable.getSymbolValue(arg2));
                    } else {
                        temp = Node.intermediate.generateTemp(0);
                    }
                    Quadruple quadruple = new Quadruple(temp, QuadrupleOp.DIV, arg1, arg2);
                    Node.intermediate.addIntermediateCode(quadruple);
                    ret = temp;
                }
            } else if (this.mulOpList.get(i) == MulOp.MOD) {
                if (Node.symbolTable.isImm(arg1) && Node.symbolTable.isImm(arg2)) {
                    if (Integer.parseInt(arg2) != 0) {
                        ret = Integer.toString(Integer.parseInt(arg1) % Integer.parseInt(arg2));
                    } else {
                        ret = "0";
                    }
                } else if (arg1.equals("0") || arg1.equals("-0")){
                    ret = "0";
                } else if (arg2.equals("1") || arg2.equals("-1")) {
                    ret = "0";
                } else {
                    String temp;
                    if (Node.symbolTable.getSymbolValue(arg2) != 0) {
                        temp = Node.intermediate.generateTemp(
                                Node.symbolTable.getSymbolValue(arg1) % Node.symbolTable.getSymbolValue(arg2));
                    } else {
                        temp = Node.intermediate.generateTemp(0);
                    }
                    Quadruple quadruple = new Quadruple(temp, QuadrupleOp.MOD, arg1, arg2);
                    Node.intermediate.addIntermediateCode(quadruple);
                    ret = temp;
                }
            }
        }
        return ret;
    }

    public int getConst() {
        int ret = this.unaryExp.getConst();
        for (int i = 0; i < this.mulOpList.size(); i++) {
            int arg1 = ret;
            int arg2 = this.unaryExpList.get(i).getConst();
            if (this.mulOpList.get(i) == MulOp.MULT) {
                ret = arg1 * arg2;
            } else if (this.mulOpList.get(i) == MulOp.DIV) {
                ret = (arg2 != 0) ? arg1 / arg2 : 0;
            } else if (this.mulOpList.get(i) == MulOp.MOD) {
                ret = (arg2 != 0) ? arg1 % arg2 : 0;
            }
        }
        return ret;
    }
}
