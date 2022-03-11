package SysY.SemanticAnalysis;

import java.util.ArrayList;

public class Quadruple {
    private String dst = "";
    private QuadrupleOp op;
    private String arg1 = "";
    private String arg2 = "";
    // 数组信息
    private int len = 0;
    private String index = "";
    private int sizeJ = 0;
    private int pushDim = 0;
    // 全局寄存器优化
    private ArrayList<String> sRegList;
    private int sRegFlag = 0;

    public Quadruple(String dst, QuadrupleOp op, String arg1, String arg2) {
        this.dst = dst;
        this.op = op;
        this.arg1 = arg1;
        this.arg2 = arg2;
    }

    public int getsRegFlag() {
        return sRegFlag;
    }

    public void setsRegFlag(int sRegFlag) {
        this.sRegFlag = sRegFlag;
    }

    public ArrayList<String> getsRegList() {
        return sRegList;
    }

    public void setsRegList(ArrayList<String> sRegList) {
        this.sRegList = sRegList;
    }

    public int getSizeJ() {
        return sizeJ;
    }

    public void setSizeJ(int sizeJ) {
        this.sizeJ = sizeJ;
    }

    public int getPushDim() {
        return pushDim;
    }

    public void setPushDim(int pushDim) {
        this.pushDim = pushDim;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public String getDst() {
        return dst;
    }

    public void setDst(String dst) {
        this.dst = dst;
    }

    public QuadrupleOp getOp() {
        return op;
    }

    public void setOp(QuadrupleOp op) {
        this.op = op;
    }

    public String getArg1() {
        return arg1;
    }

    public void setArg1(String arg1) {
        this.arg1 = arg1;
    }

    public String getArg2() {
        return arg2;
    }

    public void setArg2(String arg2) {
        this.arg2 = arg2;
    }

    public String toString() {
        if (this.op == QuadrupleOp.CASS) {
            return "const int " + this.dst + " = " + this.arg1;
        } else if (this.op == QuadrupleOp.VASS) {
            if (!this.arg1.equals("")) {
                return "var int " + this.dst + " = " + this.arg1;
            } else {
                return "var int " + this.dst;
            }
        } else if (this.op == QuadrupleOp.LASS) {
            return this.dst + " = " + this.arg1;
        } else if (this.op == QuadrupleOp.ADD) {
            return this.dst + " = " + this.arg1 + " + " + this.arg2;
        } else if (this.op == QuadrupleOp.SUB) {
            return this.dst + " = " + this.arg1 + " - " + this.arg2;
        } else if (this.op == QuadrupleOp.MUL) {
            return this.dst + " = " + this.arg1 + " * " + this.arg2;
        } else if (this.op == QuadrupleOp.DIV) {
            return this.dst + " = " + this.arg1 + " / " + this.arg2;
        } else if (this.op == QuadrupleOp.MOD) {
            return this.dst + " = " + this.arg1 + " % " + this.arg2;
        } else if (this.op == QuadrupleOp.VARAS) {
            return "var " + this.dst + "[" + this.index + "]" + " = " + this.arg1;
        } else if (this.op == QuadrupleOp.CARAS) {
            return "const " + this.dst + "[" + this.index + "]" + " = " + this.arg1;
        } else if (this.op == QuadrupleOp.ARR) {
            return "arr int " + this.dst + "[" + this.len + "]";
        } else if (this.op == QuadrupleOp.PRINT_STR) {
            return "print string " + this.arg1;
        } else if (this.op == QuadrupleOp.PRINT_INT) {
            return "print int " + this.arg1;
        } else if (this.op == QuadrupleOp.GETINT) {
            return "read " + this.dst;
        } else if (this.op == QuadrupleOp.FUNC_BEGIN) {
            return "func begin";
        } else if (this.op == QuadrupleOp.FUNC_END) {
            return "func end";
        } else if (this.op == QuadrupleOp.INT_FUNC) {
            return "int " + this.arg1 + "()";
        } else if (this.op == QuadrupleOp.VOID_FUNC) {
            return "void " + this.arg1 + "()";
        } else if (this.op == QuadrupleOp.PARA) {
            return "para int " + this.arg1;
        } else if (this.op == QuadrupleOp.RET) {
            return "ret " + this.arg1;
        } else if (this.op == QuadrupleOp.PUSH) {
            return "push " + this.arg1;
        } else if (this.op == QuadrupleOp.CALL_BEGIN) {
            return "call begin " + this.arg1;
        } else if (this.op == QuadrupleOp.CALL) {
            return "call " + this.arg1;
        } else if (this.op == QuadrupleOp.LABEL) {
            return "label " + this.arg1 + ":";
        } else if (this.op == QuadrupleOp.MAIN) {
            return "main begin";
        } else if (this.op == QuadrupleOp.MAIN_END) {
            return "main end";
        } else if (this.op == QuadrupleOp.WHILE_BEGIN) {
            return "while begin";
        } else if (this.op == QuadrupleOp.WHILE_END) {
            return "while end";
        } else if (this.op == QuadrupleOp.BLOCK_BEGIN) {
            return "block begin";
        } else if (this.op == QuadrupleOp.BLOCK_END) {
            return "block end";
        } else if (this.op == QuadrupleOp.GOTO) {
            return "goto " + this.arg1;
        } else if (this.op == QuadrupleOp.BEQ) {
            return "branch " + this.dst + " on " + this.arg1 + " == " + this.arg2;
        } else if (this.op == QuadrupleOp.BNE){
            return "branch " + this.dst + " on " + this.arg1 + " != " + this.arg2;
        } else if (this.op == QuadrupleOp.EQ) {
            return "set " + this.dst + " on " + this.arg1 + " == " + this.arg2;
        } else if (this.op == QuadrupleOp.NEQ) {
            return "set " + this.dst + " on " + this.arg1 + " != " + this.arg2;
        } else if (this.op == QuadrupleOp.GRE) {
            return "set " + this.dst + " on " + this.arg1 + " > " + this.arg2;
        } else if (this.op == QuadrupleOp.LSS) {
            return "set " + this.dst + " on " + this.arg1 + " < " + this.arg2;
        } else if (this.op == QuadrupleOp.GEQ) {
            return "set " + this.dst + " on " + this.arg1 + " >= " + this.arg2;
        } else if (this.op == QuadrupleOp.LEQ) {
            return "set " + this.dst + " on " + this.arg1 + " <= " + this.arg2;
        }
        return "";
    }
}
