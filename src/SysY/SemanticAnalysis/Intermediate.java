package SysY.SemanticAnalysis;

import SysY.SymbolTable.Symbol;
import SysY.SymbolTable.SymbolTable;
import SysY.SymbolTable.SymbolType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Intermediate {
    private int temp;
    private int label;
    private int labelIfBegin;
    private int labelIfEnd;
    private int labelLoop;
    private int labelLoopBegin;
    private int labelLoopEnd;
    private String labelLastLoop;
    private String labelLastLoopEnd;
    private SymbolTable symbolTable;
    private ArrayList<Quadruple> intermediateCode;

    public Intermediate(SymbolTable symbolTable) {
        this.temp = 0;
        this.label = 0;
        this.labelIfBegin = 0;
        this.labelIfEnd = 0;
        this.labelLoop = 0;
        this.labelLoopBegin = 0;
        this.labelLoopEnd = 0;
        this.labelLastLoop = "";
        this.labelLastLoopEnd = "";
        this.symbolTable = symbolTable;
        this.intermediateCode = new ArrayList<>();
    }

    public ArrayList<Quadruple> getIntermediateCode() {
        return intermediateCode;
    }

    public String generateTemp(int value) {
        String name = "$T" + this.temp++;
        Symbol symbol = new Symbol(name, SymbolType.VAR);
        symbol.setValue(value);
        symbolTable.addSymbol(symbol);
        return name;
    }

    public void setLabelLastLoop(String labelLastLoop) {
        this.labelLastLoop = labelLastLoop;
    }

    public void setLabelLastLoopEnd(String labelLastLoopEnd) {
        this.labelLastLoopEnd = labelLastLoopEnd;
    }

    public String getLastLoop() {
        return this.labelLastLoop;
    }

    public String getLastLoopEnd() {
        return this.labelLastLoopEnd;
    }

    public String generateIfBegin() {
        return "if_begin" + this.labelIfBegin++;
    }

    public String generateIfEnd() {
        return "if_end" + this.labelIfEnd++;
    }

    public String generateLoop() {
        return "loop" + this.labelLoop++;
    }

    public String generateLoopBegin() {
        return "loop_begin" + this.labelLoopBegin++;
    }

    public String generateLoopEnd() {
        return "loop_end" + this.labelLoopEnd++;
    }

    public String generateLabel() {
        return "label" + this.label++;
    }

    public void addIntermediateCode(Quadruple quadruple) {
        this.intermediateCode.add(quadruple);
    }

    public void IROpt() {
        for (int i = 1; i < this.intermediateCode.size(); i++) {
            Quadruple preCode = this.intermediateCode.get(i - 1);
            Quadruple curCode = this.intermediateCode.get(i);
            // 跳转指令的标签是下一条语句
            if (preCode.getOp() == QuadrupleOp.GOTO && curCode.getOp() == QuadrupleOp.LABEL
                    && preCode.getArg1().equals(curCode.getArg1())) {
                this.intermediateCode.remove(--i); // 删除跳转
            }
            // 消除表达式赋值产生的冗余变量
            if ((preCode.getOp() == QuadrupleOp.ADD
                    || preCode.getOp() == QuadrupleOp.SUB
                    || preCode.getOp() == QuadrupleOp.MUL
                    || preCode.getOp() == QuadrupleOp.DIV
                    || preCode.getOp() == QuadrupleOp.MOD)
                    && (curCode.getOp() == QuadrupleOp.LASS)
                    && (preCode.getDst().equals(curCode.getArg1()))) {
                preCode.setDst(curCode.getDst());
                this.intermediateCode.remove(i--);
            }
            if ((preCode.getOp() == QuadrupleOp.ADD
                    || preCode.getOp() == QuadrupleOp.SUB
                    || preCode.getOp() == QuadrupleOp.MUL
                    || preCode.getOp() == QuadrupleOp.DIV
                    || preCode.getOp() == QuadrupleOp.MOD)
                    && (curCode.getOp() == QuadrupleOp.VARAS)
                    && (preCode.getDst().equals(curCode.getArg1()))) {
                preCode.setDst(curCode.getDst() + "[" + curCode.getIndex() + "]");
                this.intermediateCode.remove(i--);
            }
        }
        // 全局寄存器分配
        HashMap<Symbol, Integer> countMap = new HashMap<>();
        HashMap<Symbol, Integer> defMap = new HashMap<>(); // 记录定义变量的四元式
        int whileCount = 0;
        for (int i = 0; i < this.intermediateCode.size(); i++) {
            Quadruple quadruple = this.intermediateCode.get(i);
            if (quadruple.getOp() == QuadrupleOp.MAIN) {
                countMap = new HashMap<>();
                defMap = new HashMap<>();
                whileCount = 0;
            } else if (quadruple.getOp() == QuadrupleOp.MAIN_END) {
                List<Map.Entry<Symbol, Integer>> list = new ArrayList<Map.Entry<Symbol, Integer>>(countMap.entrySet());
                list.sort(new Comparator<Map.Entry<Symbol, Integer>>() {
                    @Override
                    public int compare(Map.Entry<Symbol, Integer> o1, Map.Entry<Symbol, Integer> o2) {
                        return o2.getValue().compareTo(o1.getValue());
                    }
                });
                for (int j = 0; j < Math.min(list.size(), 8); j++) {
                    if (list.get(j).getValue() == 0) {
                        break;
                    } else {
                        this.intermediateCode.get(defMap.get(list.get(j).getKey())).setsRegFlag(1);
                    }
                }
                countMap = new HashMap<>();
                defMap = new HashMap<>();
                whileCount = 0;
            } else if (quadruple.getOp() == QuadrupleOp.FUNC_BEGIN) {
                symbolTable.addBlockTable();
                countMap = new HashMap<>();
                defMap = new HashMap<>();
                whileCount = 0;
            } else if (quadruple.getOp() == QuadrupleOp.FUNC_END) {
                List<Map.Entry<Symbol, Integer>> list = new ArrayList<Map.Entry<Symbol, Integer>>(countMap.entrySet());
                list.sort(new Comparator<Map.Entry<Symbol, Integer>>() {
                    @Override
                    public int compare(Map.Entry<Symbol, Integer> o1, Map.Entry<Symbol, Integer> o2) {
                        return o2.getValue().compareTo(o1.getValue());
                    }
                });
                for (int j = 0; j < Math.min(list.size(), 8); j++) {
                    if (list.get(j).getValue() == 0) {
                        break;
                    } else {
                        this.intermediateCode.get(defMap.get(list.get(j).getKey())).setsRegFlag(1);
                    }
                }
                countMap = new HashMap<>();
                defMap = new HashMap<>();
                whileCount = 0;
                symbolTable.removeBlockTable();
            } else if (quadruple.getOp() == QuadrupleOp.WHILE_BEGIN) {
                whileCount++;
            } else if (quadruple.getOp() == QuadrupleOp.WHILE_END) {
                whileCount--;
            } else if (quadruple.getOp() == QuadrupleOp.BLOCK_BEGIN) {
                symbolTable.addBlockTable();
            } else if (quadruple.getOp() == QuadrupleOp.BLOCK_END) {
                symbolTable.removeBlockTable();
            } else if (quadruple.getOp() == QuadrupleOp.VASS || quadruple.getOp() == QuadrupleOp.CASS) {
                if (symbolTable.getLevel() != 0) {
                    String dst = quadruple.getDst();
                    Symbol symbol = new Symbol(dst, SymbolType.VAR);
                    symbolTable.addSymbol(symbol);
                    defMap.put(symbol, i);
                    countMap.put(symbol, 0);
                }
            } else if (quadruple.getOp() == QuadrupleOp.PARA) {
                String arg = quadruple.getArg1();
                Symbol symbol = new Symbol(arg, SymbolType.VAR);
                symbolTable.addSymbol(symbol);
                defMap.put(symbol, i);
                countMap.put(symbol, 0);
            }

            String dst = quadruple.getDst();
            String arg1 = quadruple.getArg1();
            String arg2 = quadruple.getArg2();
            if (symbolTable.containSymbol(dst)) {
                Symbol symbol0 = symbolTable.getSymbol(dst);
                if (countMap.containsKey(symbol0)) {
                    int originCount = countMap.get(symbol0);
                    countMap.put(symbol0, originCount + 10 * whileCount + 1);
                }
            }
            if (symbolTable.containSymbol(arg1)) {
                Symbol symbol1 = symbolTable.getSymbol(arg1);
                if (countMap.containsKey(symbol1)) {
                    int originCount = countMap.get(symbol1);
                    countMap.put(symbol1, originCount + 10 * whileCount + 1);
                }
            }
            if (symbolTable.containSymbol(arg2)) {
                Symbol symbol2 = symbolTable.getSymbol(arg2);
                if (countMap.containsKey(symbol2)) {
                    int originCount = countMap.get(symbol2);
                    countMap.put(symbol2, originCount + 10 * whileCount + 1);
                }
            }
        }
    }
}
