package SysY.SymbolTable;

import java.util.ArrayList;

public class Symbol {
    // 基本信息
    private String name;
    private SymbolType symbolType;
    // 语法分析 错误处理 语义分析
    private int paramsNum = 0;// FUNCDEF_IDENT, UNARYEXP_IDENT
    private ArrayList<Integer> paramsDimList= new ArrayList<>();
    private int dim = 0;
    private int sizeI = 0;
    private int sizeJ = 0;
    private int value = 0; // 中间代码过程值
    private ArrayList<Integer> valueList = new ArrayList<>(); // 中间代码过程值
    // 代码生成
    private int offset = -1;
    private boolean isArrAddr = false;
    private int arrOffset = 0; // 相对于$fp

    public Symbol(String name, SymbolType symbolType) {
        this.name = name;
        this.symbolType = symbolType;
    }

    // func
    public Symbol(String name, SymbolType symbolType, int ParamsNum, ArrayList<Integer> paramsDimList) {
        this.name = name;
        this.symbolType = symbolType;
        this.paramsNum = ParamsNum;
        this.paramsDimList = paramsDimList;
    }

    public void setArrAddr(boolean arrAddr) {
        isArrAddr = arrAddr;
    }

    public boolean isArrAddr() {
        return isArrAddr;
    }

    public int getArrOffset() {
        return arrOffset;
    }

    public void setArrOffset(int arrOffset) {
        this.arrOffset = arrOffset;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SymbolType getSymbolType() {
        return symbolType;
    }

    public int getDim() {
        return dim;
    }

    public void setDim(int dim) {
        this.dim = dim;
    }

    public int getParamsNum() {
        return paramsNum;
    }

    public ArrayList<Integer> getParamsDimList() {
        return paramsDimList;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public ArrayList<Integer> getValueList() {
        return valueList;
    }

    public int getSizeI() {
        return sizeI;
    }

    public void setSizeI(int sizeI) {
        this.sizeI = sizeI;
    }

    public int getSizeJ() {
        return sizeJ;
    }

    public void setSizeJ(int sizeJ) {
        this.sizeJ = sizeJ;
    }
}
