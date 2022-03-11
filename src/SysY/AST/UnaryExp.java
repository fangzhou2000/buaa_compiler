package SysY.AST;

import SysY.ErrorHandle.ErrorType;
import SysY.SemanticAnalysis.Quadruple;
import SysY.SemanticAnalysis.QuadrupleOp;
import SysY.LexicAnalysis.Token;
import SysY.LexicAnalysis.TokenKey;
import SysY.LexicAnalysis.TokenPos;
import SysY.SymbolTable.Symbol;
import SysY.SymbolTable.SymbolType;

import java.util.ArrayList;

public class UnaryExp extends Node {
    public UnaryExpType unaryExpType;
    //
    public PrimaryExp primaryExp;
    //
    public Token ident;
    public Token lParent;
    public FuncRParams funcRParams;
    public Token rParent;
    //
    public UnaryOp unaryOp;
    public UnaryExp unaryExp;

    public UnaryExp() {
    }

    public int getDim() {
        if (this.unaryExpType == UnaryExpType.PRIMARYEXP) {
            return this.primaryExp.getDim();
        } else if (this.unaryExpType == UnaryExpType.UNARYEXP) {
            return this.unaryExp.getDim();
        } else {
            if (Node.symbolTable.containFuncSymbol(this.ident.getTokenString())) {
                Symbol symbol = Node.symbolTable.getFuncSymbol(this.ident.getTokenString());
                if (symbol.getSymbolType() == SymbolType.INT_FUNC) {
                    return 0;
                } else {
                    return -1;
                }
            }
            return -1;
        }
    }

    public TokenPos getPos() {
        if (this.unaryExpType == UnaryExpType.PRIMARYEXP) {
            return this.primaryExp.getPos();
        } else if (this.unaryExpType == UnaryExpType.IDENT) {
            return this.ident.getTokenPos();
        } else {
            return this.unaryExp.getPos();
        }
    }

    public int getParamsNum() {
        if (this.funcRParams != null) {
            return this.funcRParams.getParamsNum();
        } else {
            return 0;
        }
    }

    public ArrayList<Integer> getParamsDimList() {
        if (this.funcRParams != null) {
            return this.funcRParams.getParamsDimList();
        } else {
            return new ArrayList<>();
        }
    }

    public void errorCheck() {
        if (this.unaryExpType == UnaryExpType.PRIMARYEXP) {
            this.primaryExp.errorCheck();
        } else if (this.unaryExpType == UnaryExpType.IDENT) {
            if (this.lParent != null) {
                //c
                this.errorCheckUndefinedName(this.ident.getTokenString(), this.ident.getTokenPos());
                //d
                this.errorCheckMismatchFuncParamsNum(this.ident.getTokenString(), getParamsNum(), this.ident.getTokenPos());
                //e
                this.errorCheckMismatchFuncParamsDim(this.ident.getTokenString(), getParamsDimList(), this.ident.getTokenPos());
                if (this.funcRParams != null) {
                    this.funcRParams.errorCheck();
                }
            } else {
                //c
                this.errorCheckUndefinedName(this.ident.getTokenString(), this.ident.getTokenPos());
            }
            //j
            this.errorCheckMissRparent();
        } else if (this.unaryExpType == UnaryExpType.UNARYEXP) {
            this.unaryExp.errorCheck();
        }
    }

    public void errorCheckMissRparent() {
        if (this.rParent.getTokenKey() != TokenKey.RPARENT) {
            Node.errorHandle.addError(this.rParent.getTokenPos(), ErrorType.j);
        }
    }

    public String semanticAnalyse() {
        if (this.unaryExpType == UnaryExpType.PRIMARYEXP) {
            return this.primaryExp.semanticAnalyse();
        } else if (this.unaryExpType == UnaryExpType.IDENT) {
            if (this.lParent != null) {
                ArrayList<String> regList = new ArrayList<>();
                String ret = "";
                Quadruple quadruple1 = new Quadruple("", QuadrupleOp.CALL_BEGIN, this.ident.getTokenString(), "");
                quadruple1.setsRegList(regList);
                Node.intermediate.addIntermediateCode(quadruple1);
                if (funcRParams != null) {
                    this.funcRParams.semanticAnalyse();
                }
                Quadruple quadruple2 = new Quadruple("", QuadrupleOp.CALL, this.ident.getTokenString(), "");
                quadruple2.setsRegList(regList);
                Node.intermediate.addIntermediateCode(quadruple2);
                if (symbolTable.getFuncSymbol(this.ident.getTokenString()).getSymbolType() == SymbolType.INT_FUNC) {
                    String temp = Node.intermediate.generateTemp(0);
                    Quadruple quadruple3 = new Quadruple(temp, QuadrupleOp.LASS, "@RET", "");
                    Node.intermediate.addIntermediateCode(quadruple3);
                    ret = temp;
                }
                return ret;
            } else {
                return this.ident.getTokenString();
            }
        } else if (this.unaryExpType == UnaryExpType.UNARYEXP) {
            String ret = this.unaryExp.semanticAnalyse();
            if (this.unaryOp == UnaryOp.PLUS) {
                return ret;
            } else if (this.unaryOp == UnaryOp.MINU) {
                if (Node.symbolTable.isImm(ret)) {
                    return Integer.toString(-Integer.parseInt(ret));
                } else {
                    String temp = Node.intermediate.generateTemp(-Node.symbolTable.getSymbolValue(ret));
                    Quadruple quadruple = new Quadruple(temp, QuadrupleOp.SUB, "0", ret);
                    Node.intermediate.addIntermediateCode(quadruple);
                    ret = temp;
                    return ret;
                }
            } else if (this.unaryOp == UnaryOp.NOT) {
                if (Node.symbolTable.isImm(ret)) {
                    int i = Integer.parseInt(ret);
                    if (i == 0) {
                        return "1";
                    } else {
                        return "0";
                    }
                } else {
                    String temp = Node.intermediate.generateTemp(0);
                    Quadruple quadruple = new Quadruple(temp, QuadrupleOp.EQ, ret, "0");
                    Node.intermediate.addIntermediateCode(quadruple);
                    ret = temp;
                    return ret;
                }
            }
        }
        return "UnaryExp wrong";
    }

    public int getConst() {
        if (this.unaryExpType == UnaryExpType.PRIMARYEXP) {
            return this.primaryExp.getConst();
        } else if (this.unaryExpType == UnaryExpType.IDENT) {
            if (this.lParent != null) {
                return 0;
            } else {
                return Node.symbolTable.getSymbolValue(ident.getTokenString());
            }
        } else if (this.unaryExpType == UnaryExpType.UNARYEXP){
            int ret = this.unaryExp.getConst();
            if (this.unaryOp == UnaryOp.PLUS) {
                return ret;
            } else if (this.unaryOp == UnaryOp.MINU) {
                return -ret;
            } else if (this.unaryOp == UnaryOp.NOT) {
                return (ret == 0) ? 1:0;
            }
        }
        return 0;
    }
}
