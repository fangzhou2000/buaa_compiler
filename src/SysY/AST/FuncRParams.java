package SysY.AST;

import SysY.LexicAnalysis.Token;
import SysY.SemanticAnalysis.Quadruple;
import SysY.SemanticAnalysis.QuadrupleOp;

import java.util.ArrayList;

public class FuncRParams extends Node {
    public Exp exp;
    public ArrayList<Token> commaList;
    public ArrayList<Exp> expList;

    public FuncRParams() {
        this.commaList = new ArrayList<>();
        this.expList = new ArrayList<>();
    }

    public int getParamsNum() {
        return this.expList.size() + 1;
    }

    public ArrayList<Integer> getParamsDimList() {
        ArrayList<Integer> paramsDimList = new ArrayList<>();
        paramsDimList.add(this.exp.getDim());
        for (Exp exp : this.expList) {
            paramsDimList.add(exp.getDim());
        }
        return paramsDimList;
    }

    public void errorCheck() {
        this.exp.errorCheck();
        for (Exp e : expList) {
            e.errorCheck();
        }
    }

    public void semanticAnalyse() {
        if (getParamsNum() > 0) {
            Quadruple quadruple1 = new Quadruple("", QuadrupleOp.PUSH, this.exp.semanticAnalyse(), "");
            quadruple1.setPushDim(this.exp.getDim());
            Node.intermediate.addIntermediateCode(quadruple1);
            for (int i = 0; i < getParamsNum() - 1; i++) {
                Quadruple quadruple2 = new Quadruple("", QuadrupleOp.PUSH, this.expList.get(i).semanticAnalyse(), "");
                quadruple2.setPushDim(this.expList.get(i).getDim());
                Node.intermediate.addIntermediateCode(quadruple2);
            }
        }
    }
}
