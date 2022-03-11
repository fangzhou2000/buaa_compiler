package SysY.ObjectCode;

import SysY.SemanticAnalysis.Quadruple;
import SysY.SemanticAnalysis.QuadrupleOp;
import SysY.SymbolTable.Symbol;
import SysY.SymbolTable.SymbolTable;
import SysY.SymbolTable.SymbolType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ObjectCodeOpt {
    private ArrayList<Quadruple> intermediateCode;
    private SymbolTable symbolTable;
    private RegisterPool registerPool;
    private ArrayList<String> mipsCode;

    public ObjectCodeOpt(ArrayList<Quadruple> intermediateCode, SymbolTable symbolTable) {
        this.intermediateCode = intermediateCode;
        this.symbolTable = symbolTable;
        this.mipsCode = new ArrayList<>();
        this.registerPool = symbolTable.getRegisterPool();
    }

    // 判断是否为立即数
    public boolean isImm(String arg) {
        Pattern patternImm = Pattern.compile("-?[0-9]+");
        Matcher matcherImm = patternImm.matcher(arg);
        return matcherImm.matches();
    }

    // 返回寄存器 加载普通变量的值
    public String load(String arg) {
        String valueReg = "";
        Pattern patternArr = Pattern.compile("(.+?)\\[(.+)\\]");
        Matcher matcherArr = patternArr.matcher(arg);
        // 区分a和a[]
        if (matcherArr.matches()) {
            // a[]
            // 注意：a[]不可能为数组
            String ident = matcherArr.group(1);
            String index = matcherArr.group(2);
            String arrAddr = loadArrAddr(ident, index);
            valueReg = registerPool.allocTRegister();
            mipsCode.add("lw " + valueReg + ", " + arrAddr);
        } else {
            // a
            // 注意：a不可能为数组
            Pattern patternTemp = Pattern.compile("^\\$T[0-9]+");
            Matcher matcherTemp = patternTemp.matcher(arg);
            if (symbolTable.containSymbol(arg)) {
                Symbol symbol = symbolTable.getSymbol(arg);
                if (registerPool.containSRegister(symbol)) {
                    valueReg = registerPool.getSRegister(symbol);
                } else {
                    String argAddr = loadAddr(arg);
                    valueReg = registerPool.allocTRegister();
                    mipsCode.add("lw " + valueReg + ", " + argAddr);
                }
            } else if (matcherTemp.matches()) {
                valueReg = registerPool.getTRegister(arg);
                registerPool.freeTRegister(valueReg);
            } else if (arg.equals("@RET")) {
                valueReg = "$v0";
            } else if (isImm(arg)) {
                valueReg = registerPool.allocTRegister();
                mipsCode.add("li " + valueReg + ", " + arg);
            } else {
                System.out.println("load wrong");
            }
        }
        return valueReg;
    }

    public void load(String arg, String dstReg) {
        Pattern patternArr = Pattern.compile("(.+?)\\[(.+)\\]");
        Matcher matcherArr = patternArr.matcher(arg);
        // 区分a和a[]
        if (matcherArr.matches()) {
            // a[]
            // 注意：a[]不可能为数组
            String ident = matcherArr.group(1);
            String index = matcherArr.group(2);
            String arrAddr = loadArrAddr(ident, index);
            mipsCode.add("lw " + dstReg + ", " + arrAddr);
        } else {
            // a
            // 注意：a不可能为数组
            Pattern patternTemp = Pattern.compile("^\\$T[0-9]+");
            Matcher matcherTemp = patternTemp.matcher(arg);
            if (symbolTable.containSymbol(arg)) {
                Symbol symbol = symbolTable.getSymbol(arg);
                if (registerPool.containSRegister(symbol)) {
                    String sReg = registerPool.getSRegister(symbol);
                    if (!sReg.equals(dstReg)) {
                        mipsCode.add("move " + dstReg + ", " + sReg);
                    } else {
                        String argAddr = loadAddr(arg);
                        mipsCode.add("lw " + dstReg + ", " + argAddr);
                    }
                } else {
                    String argAddr = loadAddr(arg);
                    mipsCode.add("lw " + dstReg + ", " + argAddr);
                }
            } else if (matcherTemp.matches()) {
                String valueReg = registerPool.getTRegister(arg);
                registerPool.freeTRegister(valueReg);
                mipsCode.add("move " + dstReg + ", " + valueReg);
            } else if (arg.equals("@RET")) {
                mipsCode.add("move " + dstReg + ", $v0");
            } else if (isImm(arg)) {
                mipsCode.add("li " + dstReg + ", " + arg);
            } else {
                System.out.println("load wrong " + dstReg + " " + arg);
            }
        }
    }

    // 将寄存器值存入栈，注意区分已分配栈和未分配栈
    public void store(String arg, String valueReg) {
        Pattern pattern = Pattern.compile("(.+?)\\[(.+)\\]");
        Matcher matcher = pattern.matcher(arg);
        if (matcher.matches()) {
            String ident = matcher.group(1);
            String index = matcher.group(2);
            String arrAddr = loadArrAddr(ident, index);
            mipsCode.add("sw " + valueReg + ", " + arrAddr);
        } else {
            if (symbolTable.containSymbol(arg)) {
                Symbol symbol = symbolTable.getSymbol(arg);
                if (registerPool.containSRegister(symbol)) {
                    String sReg = registerPool.getSRegister(symbol);
                    if (!sReg.equals(valueReg)) {
                        mipsCode.add("move " + sReg + ", " + valueReg);
                    } else {
                        String argAddr = loadAddr(arg);
                        mipsCode.add("sw " + valueReg + ", " + argAddr);
                    }
                } else {
                    // -1 未存入栈
                    if (symbol.getOffset() == -1) {
                        storeAddr(arg);
                    }
                    String argAddr = loadAddr(arg);
                    mipsCode.add("sw " + valueReg + ", " + argAddr);
                }
            } else {
                System.out.println("store wrong " + valueReg + " " + arg);
            }
        }
    }

    // 获取数组地址
    public String loadArrAddr(String arg, String index) {
        // 注意节省寄存器
        Symbol symbol = symbolTable.getSymbol(arg);
        // index
        if (isImm(index)) {
            int indexImm = Integer.parseInt(index);
            int index4Imm = indexImm * 4;
            int negIndex4Imm = -index4Imm;
            if (symbolTable.containLocalSymbol(arg)) { // a 为局部数组
                if (symbol.isArrAddr()) { // a 为函数形参 形参以局部变量形式保存
                    String addrReg = load(arg);
                    // 取绝对地址
                    mipsCode.add("addiu " + addrReg + ", " + addrReg + ", " + negIndex4Imm);
                    return "(" + addrReg + ")";
                } else { // a 为普通局部数组
                    String addrReg = registerPool.allocTRegister();
                    int negArrOffset = -symbol.getArrOffset();
                    mipsCode.add("addiu " + addrReg + ", $fp, " + negArrOffset);
                    mipsCode.add("addiu " + addrReg + ", " + addrReg + ", " + negIndex4Imm);
                    return "(" + addrReg + ")";
                }
            } else { // a 为全局数组 全局数组的 label 指向数组尾
                int labelOffsetImm;
                if (symbol.getDim() == 1) {
                    labelOffsetImm = (symbol.getSizeI() - 1) * 4;
                } else {
                    labelOffsetImm = (symbol.getSizeI() * symbol.getSizeJ() - 1) * 4;
                }
                int offsetImm = labelOffsetImm - index4Imm;
                return symbol.getName() + " + " + offsetImm;
            }
        } else {
            String index4Reg = load(index);
            mipsCode.add("sll " + index4Reg + ", " + index4Reg + ", " + 2);
            // a[] 为值
            if (symbolTable.containLocalSymbol(arg)) { // a 为局部数组
                if (symbol.isArrAddr()) { // a 为函数形参 形参以局部变量形式保存
                    String addrReg = load(arg);
                    // 取绝对地址
                    mipsCode.add("subu " + addrReg + ", " + addrReg + ", " + index4Reg);
                    return "(" + addrReg + ")";
                } else { // a 为普通局部数组
                    String addrReg = registerPool.allocTRegister();
                    int negArrOffset = -symbol.getArrOffset();
                    mipsCode.add("addiu " + addrReg + ", $fp, " + negArrOffset);
                    mipsCode.add("subu " + addrReg + ", " + addrReg + ", " + index4Reg);
                    return "(" + addrReg + ")";
                }
            } else { // a 为全局数组 全局数组的 label 指向数组尾
                String labelOffsetReg = registerPool.allocTRegister();
                if (symbol.getDim() == 1) {
                    mipsCode.add("li " + labelOffsetReg + ", " + (symbol.getSizeI() - 1) * 4);
                } else {
                    mipsCode.add("li " + labelOffsetReg + ", " + (symbol.getSizeI() * symbol.getSizeJ() - 1) * 4);
                }
                mipsCode.add("subu " + labelOffsetReg + ", " + labelOffsetReg + ", " + index4Reg);
                return symbol.getName() + "(" + labelOffsetReg + ")";
            }
        }
    }

    // 无需返回
    public void storeArrAddr(String arg, int len) {
        symbolTable.setArrOffset(arg, len);
    }

    // 获取内存地址（全局变量或栈）
    public String loadAddr(String arg) {
        if (symbolTable.containLocalSymbol(arg)) {
            return symbolTable.getSymbolOffset(arg) + "($sp)";
        } else {
            return symbolTable.getTableList().get(0).getMap().get(arg).getName();
        }
    }

    // 存储变量
    public void storeAddr(String arg) {
        symbolTable.setSymbolOffset(arg);
    }

    // 先只加载ra
    public void loadRegister(ArrayList<String> regList) {
        int blockOffset;
        for (int i = regList.size() - 1; i >= 0; i--) {
            String name = regList.get(i);
            load(name, "$s" + i);
            symbolTable.removeSymbol(name);
            blockOffset = symbolTable.getTableList().get(symbolTable.getLevel()).getStackOffset();
            symbolTable.getTableList().get(symbolTable.getLevel()).setStackOffset(blockOffset - 4);
        }
        String ra = "$RA";
        load(ra, "$ra");
        symbolTable.removeSymbol(ra);
        blockOffset = symbolTable.getTableList().get(symbolTable.getLevel()).getStackOffset();
        symbolTable.getTableList().get(symbolTable.getLevel()).setStackOffset(blockOffset - 4);
    }

    public void storeRegister(ArrayList<String> regList) {
        // printf 的临时变量会跨函数调用，所以要存
        for (int i = 4; i < registerPool.gettRegList().size(); i++) {
            if (registerPool.gettRegList().get(i) == 1) {
                Symbol symbolTemp = new Symbol(registerPool.gettRegMap().get(i), SymbolType.VAR);
                symbolTable.addSymbol(symbolTemp);
                store(registerPool.gettRegMap().get(i), "$t" + i);
            }
        }
        String ra = "$RA";
        Symbol symbol = new Symbol(ra, SymbolType.VAR);
        symbolTable.addSymbol(symbol);
        store(ra, "$ra");
        for (int i = 0; i < registerPool.getsRegList().size(); i++) {
            if (registerPool.getsRegList().get(i) == 1) {
                Symbol symbol1 = new Symbol("$S" + i, SymbolType.VAR);
                symbolTable.addSymbol(symbol1);
                store("$S" + i, "$s" + i);
                regList.add("$S" + i);
            }
        }
    }

    public ArrayList<String> getMipsCode() {
        return mipsCode;
    }

    public void genMipsCode() {
        genData();
        genText();
    }

    public void genData() {
        mipsCode.add(".data");
        HashMap<String, Symbol> varHashMap = symbolTable.getTableList().get(0).getMap();
        HashMap<String, Integer> stringConstHashMap = symbolTable.getStringConstMap();
        Symbol symbol;
        for (String name : varHashMap.keySet()) {
            symbol = varHashMap.get(name);
            symbol.setOffset(0); // 防止被误判未入栈
            if (symbol.getDim() == 0) {
                mipsCode.add(name + ": .word " + symbol.getValue());
            } else if (symbol.getDim() == 1) {
                String code = name + ": .word ";
                for (int i = symbol.getSizeI() - 1; i >= 0; i--) {
                    code += (symbol.getValueList().size() <= i) ? "0," : symbol.getValueList().get(i) + ",";
                }
                mipsCode.add(code);
            } else if (symbol.getDim() == 2) {
                String code = name + ": .word ";
                for (int i = symbol.getSizeI() * symbol.getSizeJ() - 1; i >= 0; i--) {
                    code += (symbol.getValueList().size() <= i) ? "0," : symbol.getValueList().get(i) + ",";
                }
                mipsCode.add(code);
            }
        }
        for (String stringConst : stringConstHashMap.keySet()) {
            int index = stringConstHashMap.get(stringConst);
            String strCon = stringConst;
            mipsCode.add("str" + index + ": .asciiz \"" + strCon + "\"");
        }
    }

    public void genText() {
        mipsCode.add(".text");
        mipsCode.add("move $fp, $sp");
        mipsCode.add("jal main");
        mipsCode.add("li $v0, 10");
        mipsCode.add("syscall");
        for (Quadruple quadruple : intermediateCode) {
            mipsCode.add("# " + quadruple.getOp() + ":" + quadruple.toString());
            if (quadruple.getOp() == QuadrupleOp.FUNC_BEGIN) {
                symbolTable.addBlockTable();
            } else if (quadruple.getOp() == QuadrupleOp.FUNC_END) {
                mipsCode.add("jr $ra");
                symbolTable.removeBlockTable();
                registerPool.clear();
            } else if (quadruple.getOp() == QuadrupleOp.INT_FUNC || quadruple.getOp() == QuadrupleOp.VOID_FUNC) {
                mipsCode.add(quadruple.getArg1() + ":");
            } else if (quadruple.getOp() == QuadrupleOp.PARA) {
                genPARA(quadruple);
            } else if (quadruple.getOp() == QuadrupleOp.MAIN) {
                mipsCode.add("main:");
            } else if (quadruple.getOp() == QuadrupleOp.BLOCK_BEGIN) {
                symbolTable.addBlockTable();
            } else if (quadruple.getOp() == QuadrupleOp.BLOCK_END) {
                symbolTable.removeBlockTable();
            } else if (quadruple.getOp() == QuadrupleOp.CASS) {
                // 非全局变量
                if (symbolTable.getLevel() != 0) {
                    genCASS(quadruple);
                }
            } else if (quadruple.getOp() == QuadrupleOp.VASS) {
                // 非全局变量
                if (symbolTable.getLevel() != 0) {
                    genVASS(quadruple);
                }
            } else if (quadruple.getOp() == QuadrupleOp.ARR) {
                // 非全局变量
                if (symbolTable.getLevel() != 0) {
                    genARR(quadruple);
                }
            } else if (quadruple.getOp() == QuadrupleOp.CARAS) {
                // 非全局变量
                if (symbolTable.getLevel() != 0) {
                    genCARSA(quadruple);
                }
            } else if (quadruple.getOp() == QuadrupleOp.VARAS) {
                // 非全局变量
                if (symbolTable.getLevel() != 0) {
                    genVARSA(quadruple);
                }
            } else if (quadruple.getOp() == QuadrupleOp.GETINT) {
                genGETINT(quadruple);
            } else if (quadruple.getOp() == QuadrupleOp.PRINT_STR) {
                genPRINT_STR(quadruple);
            } else if (quadruple.getOp() == QuadrupleOp.PRINT_INT) {
                genPRINT_INT(quadruple);
            } else if (quadruple.getOp() == QuadrupleOp.LASS) {
                genLASS(quadruple);
            } else if (quadruple.getOp() == QuadrupleOp.ADD) {
                genADD(quadruple);
            } else if (quadruple.getOp() == QuadrupleOp.SUB) {
                genSUB(quadruple);
            } else if (quadruple.getOp() == QuadrupleOp.MUL) {
                genMUL(quadruple);
            } else if (quadruple.getOp() == QuadrupleOp.DIV) {
                genDIV(quadruple);
            } else if (quadruple.getOp() == QuadrupleOp.MOD) {
                genMOD(quadruple);
            } else if (quadruple.getOp() == QuadrupleOp.CALL_BEGIN) {
                genCALL_BEGIN(quadruple);
            } else if (quadruple.getOp() == QuadrupleOp.PUSH) {
                genPUSH(quadruple);
            } else if (quadruple.getOp() == QuadrupleOp.CALL) {
                genCALL(quadruple);
            } else if (quadruple.getOp() == QuadrupleOp.RET) {
                genRET(quadruple);
            } else if (quadruple.getOp() == QuadrupleOp.LABEL) {
                mipsCode.add(quadruple.getArg1() + ":");
            } else if (quadruple.getOp() == QuadrupleOp.GOTO) {
                mipsCode.add("j " + quadruple.getArg1());
            } else if (quadruple.getOp() == QuadrupleOp.BEQ) {
                genBEQ(quadruple);
            } else if (quadruple.getOp() == QuadrupleOp.BNE) {
                genBNE(quadruple);
            } else if (quadruple.getOp() == QuadrupleOp.EQ) {
                genEQ(quadruple);
            } else if (quadruple.getOp() == QuadrupleOp.NEQ) {
                genNEQ(quadruple);
            } else if (quadruple.getOp() == QuadrupleOp.GRE) {
                genGRE(quadruple);
            } else if (quadruple.getOp() == QuadrupleOp.LSS) {
                genLSS(quadruple);
            } else if (quadruple.getOp() == QuadrupleOp.GEQ) {
                genGEQ(quadruple);
            } else if (quadruple.getOp() == QuadrupleOp.LEQ) {
                genLEQ(quadruple);
            }
        }
    }

    public void genLEQ(Quadruple quadruple) {
        String dst = quadruple.getDst();
        String arg1 = quadruple.getArg1();
        String arg2 = quadruple.getArg2();
        String t1 = load(arg1);
        String t2 = load(arg2);
        Pattern patternTemp = Pattern.compile("^\\$T[0-9]+");
        Matcher matcherTemp = patternTemp.matcher(dst);
        if (matcherTemp.matches() && registerPool.isTRegAvailable()) {
            String t = registerPool.allocTRegister(dst);
            mipsCode.add("sle " + t + ", " + t1 + ", " + t2);
        } else {
            Symbol symbol = new Symbol(dst, SymbolType.VAR);
            symbolTable.addSymbol(symbol);
            String t = registerPool.allocTRegister();
            mipsCode.add("sle " + t + ", " + t1 + ", " + t2);
            store(dst, t);
        }
    }

    public void genGEQ(Quadruple quadruple) {
        String dst = quadruple.getDst();
        String arg1 = quadruple.getArg1();
        String arg2 = quadruple.getArg2();
        String t1 = load(arg1);
        String t2 = load(arg2);
        Pattern patternTemp = Pattern.compile("^\\$T[0-9]+");
        Matcher matcherTemp = patternTemp.matcher(dst);
        if (matcherTemp.matches() && registerPool.isTRegAvailable()) {
            String t = registerPool.allocTRegister(dst);
            mipsCode.add("sge " + t + ", " + t1 + ", " + t2);
        } else {
            Symbol symbol = new Symbol(dst, SymbolType.VAR);
            symbolTable.addSymbol(symbol);
            String t = registerPool.allocTRegister();
            mipsCode.add("sge " + t + ", " + t1 + ", " + t2);
            store(dst, t);
        }
    }

    public void genLSS(Quadruple quadruple) {
        String dst = quadruple.getDst();
        String arg1 = quadruple.getArg1();
        String arg2 = quadruple.getArg2();
        String t1 = load(arg1);
        String t2 = load(arg2);
        Pattern patternTemp = Pattern.compile("^\\$T[0-9]+");
        Matcher matcherTemp = patternTemp.matcher(dst);
        if (matcherTemp.matches() && registerPool.isTRegAvailable()) {
            String t = registerPool.allocTRegister(dst);
            mipsCode.add("slt " + t + ", " + t1 + ", " + t2);
        } else {
            Symbol symbol = new Symbol(dst, SymbolType.VAR);
            symbolTable.addSymbol(symbol);
            String t = registerPool.allocTRegister();
            mipsCode.add("slt " + t + ", " + t1 + ", " + t2);
            store(dst, t);
        }
    }

    public void genGRE(Quadruple quadruple) {
        String dst = quadruple.getDst();
        String arg1 = quadruple.getArg1();
        String arg2 = quadruple.getArg2();
        String t1 = load(arg1);
        String t2 = load(arg2);
        Pattern patternTemp = Pattern.compile("^\\$T[0-9]+");
        Matcher matcherTemp = patternTemp.matcher(dst);
        if (matcherTemp.matches() && registerPool.isTRegAvailable()) {
            String t = registerPool.allocTRegister(dst);
            mipsCode.add("sgt " + t + ", " + t1 + ", " + t2);
        } else {
            Symbol symbol = new Symbol(dst, SymbolType.VAR);
            symbolTable.addSymbol(symbol);
            String t = registerPool.allocTRegister();
            mipsCode.add("sgt " + t + ", " + t1 + ", " + t2);
            store(dst, t);
        }
    }

    public void genNEQ(Quadruple quadruple) {
        String dst = quadruple.getDst();
        String arg1 = quadruple.getArg1();
        String arg2 = quadruple.getArg2();
        String t1 = load(arg1);
        String t2 = load(arg2);
        Pattern patternTemp = Pattern.compile("^\\$T[0-9]+");
        Matcher matcherTemp = patternTemp.matcher(dst);
        if (matcherTemp.matches() && registerPool.isTRegAvailable()) {
            String t = registerPool.allocTRegister(dst);
            mipsCode.add("sne " + t + ", " + t1 + ", " + t2);
        } else {
            Symbol symbol = new Symbol(dst, SymbolType.VAR);
            symbolTable.addSymbol(symbol);
            String t = registerPool.allocTRegister();
            mipsCode.add("sne " + t + ", " + t1 + ", " + t2);
            store(dst, t);
        }
    }

    public void genEQ(Quadruple quadruple) {
        String dst = quadruple.getDst();
        String arg1 = quadruple.getArg1();
        String arg2 = quadruple.getArg2();
        String t1 = load(arg1);
        String t2 = load(arg2);
        Pattern patternTemp = Pattern.compile("^\\$T[0-9]+");
        Matcher matcherTemp = patternTemp.matcher(dst);
        if (matcherTemp.matches() && registerPool.isTRegAvailable()) {
            String t = registerPool.allocTRegister(dst);
            mipsCode.add("seq " + t + ", " + t1 + ", " + t2);
        } else {
            Symbol symbol = new Symbol(dst, SymbolType.VAR);
            symbolTable.addSymbol(symbol);
            String t = registerPool.allocTRegister();
            mipsCode.add("seq " + t + ", " + t1 + ", " + t2);
            store(dst, t);
        }
    }

    public void genBEQ(Quadruple quadruple) {
        String label = quadruple.getDst();
        String arg1 = quadruple.getArg1();
        String arg2 = quadruple.getArg2();
        String t1 = load(arg1);
        String t2 = load(arg2);
        mipsCode.add("beq " + t1 + ", " + t2 + ", " + label);
    }

    public void genBNE(Quadruple quadruple) {
        String label = quadruple.getDst();
        String arg1 = quadruple.getArg1();
        String arg2 = quadruple.getArg2();
        String t1 = load(arg1);
        String t2 = load(arg2);
        mipsCode.add("bne " + t1 + ", " + t2 + ", " + label);
    }

    public void genCASS(Quadruple quadruple) {
        String dst = quadruple.getDst();
        String arg = quadruple.getArg1();
        Symbol symbol = new Symbol(dst, SymbolType.CONST);
        symbol.setValue(Integer.parseInt(arg));
        symbolTable.addSymbol(symbol);
        storeAddr(dst);
        if (quadruple.getsRegFlag() == 1 && registerPool.isSRegAvailable()) {
            registerPool.mapSRegister(symbol);
            load(arg, registerPool.getSRegister(symbol));
        } else {
            String argReg = load(arg);
            store(dst, argReg);
        }
    }

    public void genVASS(Quadruple quadruple) {
        String dst = quadruple.getDst();
        String arg = quadruple.getArg1();
        Symbol symbol = new Symbol(dst, SymbolType.VAR);
        symbolTable.addSymbol(symbol);
        storeAddr(dst);
        if (quadruple.getsRegFlag() == 1 && registerPool.isSRegAvailable()) {
            registerPool.mapSRegister(symbol);
            if (!arg.equals("")) {
                load(arg, registerPool.getSRegister(symbol));
            }
        } else {
            if (!arg.equals("")) {
                String argReg = load(arg);
                store(dst, argReg);
            }
        }
    }

    public void genARR(Quadruple quadruple) {
        String dst = quadruple.getDst();
        int len = quadruple.getLen();
        int sizeJ = quadruple.getSizeJ();
        Symbol symbol = new Symbol(dst, SymbolType.VAR);
        symbol.setSizeJ(sizeJ);
        for (int i = 0; i < len; i++) {
            symbol.getValueList().add(0);
        }
        symbolTable.addSymbol(symbol);
        storeArrAddr(dst, len);
    }

    public void genCARSA(Quadruple quadruple) {
        String dst = quadruple.getDst();
        String index = quadruple.getIndex();
        String arg = quadruple.getArg1();
        if (symbolTable.containSymbol(dst)) {
            Symbol arr = symbolTable.getSymbol(dst);
            arr.getValueList().set(Integer.parseInt(index), Integer.parseInt(arg));
            String valueReg = load(arg);
            String dstAddr = loadArrAddr(dst, index);
            mipsCode.add("sw " + valueReg + ", " + dstAddr);
        } else {
            System.out.println("genCARSA wrong");
        }
    }

    public void genVARSA(Quadruple quadruple) {
        String dst = quadruple.getDst();
        String index = quadruple.getIndex();
        String arg = quadruple.getArg1();
        if (symbolTable.containSymbol(dst)) {
            String valueReg = load(arg);
            String dstAddr = loadArrAddr(dst, index);
            mipsCode.add("sw " + valueReg + ", " + dstAddr);
        } else {
            System.out.println("genVARSA wrong");
        }
    }

    public void genGETINT(Quadruple quadruple) {
        String dst = quadruple.getDst();
        mipsCode.add("li $v0, 5");
        mipsCode.add("syscall");
        store(dst, "$v0");
    }

    public void genPRINT_STR(Quadruple quadruple) {
        mipsCode.add("la $a0, str" + symbolTable.getStringConstMap().get(quadruple.getArg1()));
        mipsCode.add("li $v0, 4");
        mipsCode.add("syscall");
    }

    public void genPRINT_INT(Quadruple quadruple) {
        String arg = quadruple.getArg1();
        String valueReg = load(arg);
        mipsCode.add("move $a0, " + valueReg);
        mipsCode.add("li $v0, 1");
        mipsCode.add("syscall");
    }

    public void genLASS(Quadruple quadruple) {
        String arg = quadruple.getArg1();
        String dst = quadruple.getDst();
        if (!symbolTable.containSymbol(dst)) {
            Pattern patternTemp = Pattern.compile("^\\$T[0-9]+");
            Matcher matcherTemp = patternTemp.matcher(dst);
            if (matcherTemp.matches() && registerPool.isTRegAvailable()) {
                String t = registerPool.allocTRegister(dst);
                load(arg, t);
            } else {
                Symbol symbol = new Symbol(dst, SymbolType.VAR);
                symbolTable.addSymbol(symbol);
                String valueReg = load(arg);
                store(dst, valueReg);
            }
        } else {
            String valueReg = load(arg);
            store(dst, valueReg);
        }
    }

    public void genADD(Quadruple quadruple) {
        String dst = quadruple.getDst();
        String arg1 = quadruple.getArg1();
        String arg2 = quadruple.getArg2();
        if (isImm(arg2)) {
            String t1 = load(arg1);
            int imm = Integer.parseInt(arg2);
            if (!symbolTable.containSymbol(dst)) {
                Pattern patternTemp = Pattern.compile("^\\$T[0-9]+");
                Matcher matcherTemp = patternTemp.matcher(dst);
                if (matcherTemp.matches() && registerPool.isTRegAvailable()) {
                    String t = registerPool.allocTRegister(dst);
                    mipsCode.add("addiu " + t + ", " + t1 + ", " + imm);
                } else {
                    Symbol symbol = new Symbol(dst, SymbolType.VAR);
                    symbolTable.addSymbol(symbol);
                    String t = registerPool.allocTRegister();
                    mipsCode.add("addiu " + t + ", " + t1 + ", " + imm);
                    store(dst, t);
                }
            } else {
                String t = registerPool.allocTRegister();
                mipsCode.add("addiu " + t + ", " + t1 + ", " + imm);
                store(dst, t);
            }
        } else if (isImm(arg1)) {
            int imm = Integer.parseInt(arg1);
            String t2 = load(arg2);
            if (!symbolTable.containSymbol(dst)) {
                Pattern patternTemp = Pattern.compile("^\\$T[0-9]+");
                Matcher matcherTemp = patternTemp.matcher(dst);
                if (matcherTemp.matches() && registerPool.isTRegAvailable()) {
                    String t = registerPool.allocTRegister(dst);
                    mipsCode.add("addiu " + t + ", " + t2 + ", " + imm);
                } else {
                    Symbol symbol = new Symbol(dst, SymbolType.VAR);
                    symbolTable.addSymbol(symbol);
                    String t = registerPool.allocTRegister();
                    mipsCode.add("addiu " + t + ", " + t2 + ", " + imm);
                    store(dst, t);
                }
            } else {
                String t = registerPool.allocTRegister();
                mipsCode.add("addiu " + t + ", " + t2 + ", " + imm);
                store(dst, t);
            }
        } else {
            String t1 = load(arg1);
            String t2 = load(arg2);
            if (!symbolTable.containSymbol(dst)) {
                Pattern patternTemp = Pattern.compile("^\\$T[0-9]+");
                Matcher matcherTemp = patternTemp.matcher(dst);
                if (matcherTemp.matches() && registerPool.isTRegAvailable()) {
                    String t = registerPool.allocTRegister(dst);
                    mipsCode.add("addu " + t + ", " + t1 + ", " + t2);
                } else {
                    Symbol symbol = new Symbol(dst, SymbolType.VAR);
                    symbolTable.addSymbol(symbol);
                    String t = registerPool.allocTRegister();
                    mipsCode.add("addu " + t + ", " + t1 + ", " + t2);
                    store(dst, t);
                }
            } else {
                String t = registerPool.allocTRegister();
                mipsCode.add("addu " + t + ", " + t1 + ", " + t2);
                store(dst, t);
            }
        }
    }

    public void genSUB(Quadruple quadruple) {
        String dst = quadruple.getDst();
        String arg1 = quadruple.getArg1();
        String arg2 = quadruple.getArg2();
        if (isImm(arg2)) {
            String t1 = load(arg1);
            int imm = Integer.parseInt(arg2);
            int negImm = -imm;
            if (!symbolTable.containSymbol(dst)) {
                Pattern patternTemp = Pattern.compile("^\\$T[0-9]+");
                Matcher matcherTemp = patternTemp.matcher(dst);
                if (matcherTemp.matches() && registerPool.isTRegAvailable()) {
                    String t = registerPool.allocTRegister(dst);
                    mipsCode.add("addiu " + t + ", " + t1 + ", " + negImm);
                } else {
                    Symbol symbol = new Symbol(dst, SymbolType.VAR);
                    symbolTable.addSymbol(symbol);
                    String t = registerPool.allocTRegister();
                    mipsCode.add("addiu " + t + ", " + t1 + ", " + negImm);
                    store(dst, t);
                }
            } else {
                String t = registerPool.allocTRegister();
                mipsCode.add("addiu " + t + ", " + t1 + ", " + negImm);
                store(dst, t);
            }
        } else {
            String t1 = load(arg1);
            String t2 = load(arg2);
            if (!symbolTable.containSymbol(dst)) {
                Pattern patternTemp = Pattern.compile("^\\$T[0-9]+");
                Matcher matcherTemp = patternTemp.matcher(dst);
                if (matcherTemp.matches() && registerPool.isTRegAvailable()) {
                    String t = registerPool.allocTRegister(dst);
                    mipsCode.add("subu " + t + ", " + t1 + ", " + t2);
                } else {
                    Symbol symbol = new Symbol(dst, SymbolType.VAR);
                    symbolTable.addSymbol(symbol);
                    String t = registerPool.allocTRegister();
                    mipsCode.add("subu " + t + ", " + t1 + ", " + t2);
                    store(dst, t);
                }
            } else {
                String t = registerPool.allocTRegister();
                mipsCode.add("subu " + t + ", " + t1 + ", " + t2);
                store(dst, t);
            }
        }
    }

    public void genMUL(Quadruple quadruple) {
        String dst = quadruple.getDst();
        String arg1 = quadruple.getArg1();
        String arg2 = quadruple.getArg2();
        if (!symbolTable.containSymbol(dst)) {
            Pattern patternTemp = Pattern.compile("^\\$T[0-9]+");
            Matcher matcherTemp = patternTemp.matcher(dst);
            if (matcherTemp.matches() && registerPool.isTRegAvailable()) {
                if (isImm(arg2)) {
                    String t1 = load(arg1);
                    String t = registerPool.allocTRegister(dst);
                    mulOpt(t, t1, arg2);
                } else if (isImm(arg1)) {
                    String t2 = load(arg2);
                    String t = registerPool.allocTRegister(dst);
                    mulOpt(t, t2, arg1);
                } else {
                    String t1 = load(arg1);
                    String t2 = load(arg2);
                    String t = registerPool.allocTRegister(dst);
                    mipsCode.add("mul " + t + ", " + t1 + ", " + t2);
                }
            } else {
                Symbol symbol = new Symbol(dst, SymbolType.VAR);
                symbolTable.addSymbol(symbol);
                if (isImm(arg2)) {
                    String t1 = load(arg1);
                    String t = registerPool.allocTRegister();
                    mulOpt(t, t1, arg2);
                    store(dst, t);
                } else if (isImm(arg1)) {
                    String t2 = load(arg2);
                    String t = registerPool.allocTRegister();
                    mulOpt(t, t2, arg1);
                    store(dst, t);
                } else {
                    String t1 = load(arg1);
                    String t2 = load(arg2);
                    String t = registerPool.allocTRegister();
                    mipsCode.add("mul " + t + ", " + t1 + ", " + t2);
                    store(dst, t);
                }
            }
        } else {
            if (isImm(arg2)) {
                String t1 = load(arg1);
                String t = registerPool.allocTRegister();
                mulOpt(t, t1, arg2);
                store(dst, t);
            } else if (isImm(arg1)) {
                String t2 = load(arg2);
                String t = registerPool.allocTRegister();
                mulOpt(t, t2, arg1);
                store(dst, t);
            } else {
                String t1 = load(arg1);
                String t2 = load(arg2);
                String t = registerPool.allocTRegister();
                mipsCode.add("mul " + t + ", " + t1 + ", " + t2);
                store(dst, t);
            }
        }
    }

    public void mulOpt(String t, String t1, String arg2) {
        int d = Integer.parseInt(arg2);
        int d_abs = Math.abs(d);
        if ((d_abs & (d_abs - 1)) == 0) {
            int sh_bit = (int) (Math.log(d_abs) / Math.log(2));
            mipsCode.add("sll " + t + ", " + t1 + ", " + sh_bit);
            if (d < 0) {
                mipsCode.add("subu " + t + ", $0, " + t);
            }
        } else {
            String t2 = load(arg2);
            mipsCode.add("mul " + t + ", " + t1 + ", " + t2);
        }
    }

    public void genDIV(Quadruple quadruple) {
        String dst = quadruple.getDst();
        String arg1 = quadruple.getArg1();
        String arg2 = quadruple.getArg2();
        String t1 = load(arg1);
        if (!symbolTable.containSymbol(dst)) {
            Pattern patternTemp = Pattern.compile("^\\$T[0-9]+");
            Matcher matcherTemp = patternTemp.matcher(dst);
            if (matcherTemp.matches() && registerPool.isTRegAvailable()) {
                if (isImm(arg2)) {
                    String t0 = registerPool.allocTRegister();
                    divOpt(t0, t1, arg2);
                    String t = registerPool.allocTRegister(dst);
                    mipsCode.add("move " + t + ", " + t0);
                } else {
                    String t2 = load(arg2);
                    mipsCode.add("div " + t1 + ", " + t2);
                    String t = registerPool.allocTRegister(dst);
                    mipsCode.add("mflo " + t);
                }
            } else {
                Symbol symbol = new Symbol(dst, SymbolType.VAR);
                symbolTable.addSymbol(symbol);
                if (isImm(arg2)) {
                    String t = registerPool.allocTRegister();
                    divOpt(t, t1, arg2);
                    store(dst, t);
                } else {
                    String t2 = load(arg2);
                    String t = registerPool.allocTRegister();
                    mipsCode.add("div " + t1 + ", " + t2);
                    mipsCode.add("mflo " + t);
                    store(dst, t);
                }
            }
        } else {
            if (isImm(arg2)) {
                String t = registerPool.allocTRegister();
                divOpt(t, t1, arg2);
                store(dst, t);
            } else {
                String t2 = load(arg2);
                String t = registerPool.allocTRegister();
                mipsCode.add("div " + t1 + ", " + t2);
                mipsCode.add("mflo " + t);
                store(dst, t);
            }
        }
    }

    public void divOpt(String t, String n, String arg2) {
        int d = Integer.parseInt(arg2);
        int N = 32;
        long m;
        int l, sh_post;
        int prec = N - 1;
        l = (int) Math.ceil(Math.log(Math.abs(d)) / Math.log(2));
        sh_post = l;
        double mlow = Math.floor(Math.pow(2, N + l) / Math.abs(d));
        double mhigh = Math.floor((Math.pow(2, N + l) + Math.pow(2, N + l - prec)) / Math.abs(d));
        while (Math.floor(mlow / 2.0) < Math.floor(mhigh / 2.0) && sh_post > 0) {
            mlow = Math.floor(mlow / 2.0);
            mhigh = Math.floor(mhigh / 2.0);
            sh_post = sh_post - 1;
        }
        m = (long) mhigh;
        if (Math.abs(d) == 1) {
            t = n;
        } else if (Math.abs(d) == Math.pow(2, l)) {
            mipsCode.add("sra " + t + ", " + n + ", " + (l - 1));
            mipsCode.add("srl " + t + ", " + t + ", " + (N - l));
            mipsCode.add("add " + t + ", " + n + ", " + t);
            mipsCode.add("sra " + t + ", " + t + ", " + l);
        } else if (m < Math.pow(2, N - 1)) {
            mipsCode.add("li " + t + ", " + m);
            mipsCode.add("mult " + t + ", " + n);
            mipsCode.add("mfhi " + t);
            mipsCode.add("sra " + t + ", " + t + ", " + sh_post);
            String temp = registerPool.allocTRegister();
            mipsCode.add("sra " + temp + ", " + n + ", 31");
            mipsCode.add("sub " + t + ", " + t + ", " + temp);
        } else {
            long m1 = m - (long) Math.pow(2, N);
            mipsCode.add("li " + t + ", " + m1);
            mipsCode.add("mult " + t + ", " + n);
            mipsCode.add("mfhi " + t);
            mipsCode.add("add " + t + ", " + n + ", " + t);
            mipsCode.add("sra " + t + ", " + t + ", " + sh_post);
            String temp = registerPool.allocTRegister();
            mipsCode.add("sra " + temp + ", " + n + ", 31");
            mipsCode.add("sub " + t + ", " + t + ", " + temp);
        }
        if (d < 0) {
            mipsCode.add("sub " + t + ", " + "$0, " + t);
        }
    }

    public void genMOD(Quadruple quadruple) {
        String dst = quadruple.getDst();
        String arg1 = quadruple.getArg1();
        String arg2 = quadruple.getArg2();
        String t1 = load(arg1);
        if (!symbolTable.containSymbol(dst)) {
            Pattern patternTemp = Pattern.compile("^\\$T[0-9]+");
            Matcher matcherTemp = patternTemp.matcher(dst);
            if (matcherTemp.matches() && registerPool.isTRegAvailable()) {
                if (isImm(arg2)) {
                    String t10 = registerPool.allocTRegister();
                    mipsCode.add("move " + t10 + ", " + t1);
                    String t0 = registerPool.allocTRegister();
                    divOpt(t0, t1, arg2);
                    mulOpt(t0, t0, arg2);
                    String t = registerPool.allocTRegister(dst);
                    mipsCode.add("subu " + t + ", " + t10 + ", " + t0);
                } else {
                    String t2 = load(arg2);
                    mipsCode.add("div " + t1 + ", " + t2);
                    String t = registerPool.allocTRegister(dst);
                    mipsCode.add("mfhi " + t);
                }
            } else {
                Symbol symbol = new Symbol(dst, SymbolType.VAR);
                symbolTable.addSymbol(symbol);
                if (isImm(arg2)) {
                    String t = registerPool.allocTRegister();
                    divOpt(t, t1, arg2);
                    mulOpt(t, t, arg2);
                    mipsCode.add("subu " + t + ", " + t1 + ", " + t);
                    store(dst, t);
                } else {
                    String t2 = load(arg2);
                    String t = registerPool.allocTRegister();
                    mipsCode.add("div " + t1 + ", " + t2);
                    mipsCode.add("mfhi " + t);
                    store(dst, t);
                }
            }
        } else {
            if (isImm(arg2)) {
                String t = registerPool.allocTRegister();
                divOpt(t, t1, arg2);
                mulOpt(t, t, arg2);
                mipsCode.add("subu " + t + ", " + t1 + ", " + t);
                store(dst, t);
            } else {
                String t2 = load(arg2);
                String t = registerPool.allocTRegister();
                mipsCode.add("div " + t1 + ", " + t2);
                mipsCode.add("mfhi " + t);
                store(dst, t);
            }
        }
    }

    public void genRET(Quadruple quadruple) {
        String arg1 = quadruple.getArg1();
        if (!arg1.equals("")) {
            String t = load(arg1);
            mipsCode.add("move $v0, " + t);
        }
        mipsCode.add("jr $ra");
    }

    public void genPARA(Quadruple quadruple) {
        String arg = quadruple.getArg1();
        Pattern pattern1 = Pattern.compile("(.+?)\\[\\]");
        Pattern pattern2 = Pattern.compile("(.+?)\\[\\]\\[(.+)\\]");
        Matcher matcher1 = pattern1.matcher(arg);
        Matcher matcher2 = pattern2.matcher(arg);
        // 数组
        if (matcher1.matches()) {
            String ident = matcher1.group(1);
            Symbol symbol = new Symbol(ident, SymbolType.VAR);
            symbol.setDim(1);
            symbol.setArrAddr(true);
            symbolTable.addSymbol(symbol);
            storeAddr(ident);
        } else if (matcher2.matches()) {
            String ident = matcher2.group(1);
            Symbol symbol = new Symbol(ident, SymbolType.VAR);
            symbol.setDim(2);
            symbol.setArrAddr(true);
            int sizeJ = quadruple.getSizeJ();
            symbol.setSizeJ(sizeJ);
            symbolTable.addSymbol(symbol);
            storeAddr(ident);
        } else {
            Symbol symbol = new Symbol(arg, SymbolType.VAR);
            symbolTable.addSymbol(symbol);
            storeAddr(arg);
            if (quadruple.getsRegFlag() == 1 && registerPool.isSRegAvailable()) {
                registerPool.mapSRegister(symbol);
                load(arg, registerPool.getSRegister(symbol));
            }
        }
    }

    public void genCALL_BEGIN(Quadruple quadruple) {
        // 因为参数要压到新栈里，所以在调用开始就要创建新栈
        storeRegister(quadruple.getsRegList());
        int negStackOffset = -symbolTable.getTableList().get(symbolTable.getLevel()).getStackOffset();
        mipsCode.add("addiu $sp, $sp, " + negStackOffset);
        symbolTable.addBlockTable();
        symbolTable.newStack();
    }

    public void genPUSH(Quadruple quadruple) {
        String dst = symbolTable.generateParaName();
        String arg = quadruple.getArg1();
        int pushDim = quadruple.getPushDim();
        Symbol symbol = new Symbol(dst, SymbolType.PARA);
        symbolTable.addSymbol(symbol);
        String valueReg = "";
        if (pushDim == 0) { // 普通变量 a a[]
            valueReg = load(arg);
        } else { // 数组 a a[]
            Pattern pattern = Pattern.compile("(.+?)\\[(.+)\\]");
            Matcher matcher = pattern.matcher(arg);
            if (matcher.matches()) { // 数组 a[]
                String ident = matcher.group(1);
                String i = matcher.group(2);
                Symbol symbol1 = symbolTable.getSymbol(ident);
                int sizeJ = symbol1.getSizeJ();
                if (isImm(i)) {
                    int indexImm = Integer.parseInt(i);
                    int index4Imm = indexImm * sizeJ * 4;
                    int negIndex4Imm = -index4Imm;
                    if (symbolTable.containLocalSymbol(ident)) { // 局部数组 a[]
                        if (symbol1.isArrAddr()) {
                            String arrOffsetReg = load(ident);
                            valueReg = registerPool.allocTRegister();
                            mipsCode.add("addiu " + valueReg + ", " + arrOffsetReg + ", " + negIndex4Imm);
                        } else {
                            // 存绝对地址
                            int offset = index4Imm + symbol1.getArrOffset();
                            int negOffset = -offset;
                            valueReg = registerPool.allocTRegister();
                            mipsCode.add("addiu " + valueReg + ", $fp, " + negOffset);
                        }
                    } else { // 全局数组 a[]
                        // 首位反转 + 下标偏移
                        int len4Imm;
                        if (symbol1.getDim() == 1) {
                            len4Imm = (symbol1.getSizeI() - 1) * 4;
                        } else {
                            len4Imm = (symbol1.getSizeI() * symbol1.getSizeJ() - 1) * 4;
                        }
                        int offset = len4Imm - index4Imm;
                        valueReg = registerPool.allocTRegister();
                        mipsCode.add("la " + valueReg + ", " + ident + " + " + offset);
                    }
                } else {
                    String iReg = load(i);
                    String index4Reg = registerPool.allocTRegister();
                    mulOpt(index4Reg, iReg, Integer.toString(sizeJ));
                    mipsCode.add("sll " + index4Reg + ", " + index4Reg + ", " + 2);
                    if (symbolTable.containLocalSymbol(ident)) { // 局部数组 a[]
                        if (symbol1.isArrAddr()) {
                            String arrOffsetReg = load(ident);
                            valueReg = registerPool.allocTRegister();
                            mipsCode.add("subu " + valueReg + ", " + arrOffsetReg + ", " + index4Reg);
                        } else {
                            // 存绝对地址
                            valueReg = registerPool.allocTRegister();
                            mipsCode.add("addiu " + valueReg + ", " + index4Reg + ", " + symbol1.getArrOffset());
                            mipsCode.add("subu " + valueReg + ", $fp, " + valueReg);
                        }
                    } else { // 全局数组 a[]
                        // 首位反转 + 下标偏移
                        String len4Reg = registerPool.allocTRegister();
                        if (symbol1.getDim() == 1) {
                            mipsCode.add("li " + len4Reg + ", " + (symbol1.getSizeI() - 1) * 4);
                        } else {
                            mipsCode.add("li " + len4Reg + ", " + (symbol1.getSizeI() * symbol1.getSizeJ() - 1) * 4);
                        }
                        String offsetReg = registerPool.allocTRegister();
                        mipsCode.add("subu " + offsetReg + ", " + len4Reg + ", " + index4Reg);
                        // 数组首地址偏移
                        // 存绝对地址
                        valueReg = registerPool.allocTRegister();
                        mipsCode.add("la " + valueReg + ", " + ident + "(" + offsetReg + ")");
                    }
                }
            } else { // 数组 a
                Symbol symbol1 = symbolTable.getSymbol(arg);
                if (symbolTable.containLocalSymbol(arg)) { // 局部数组 a
                    if (symbol1.isArrAddr()) {
                        valueReg = load(arg);
                    } else {
                        // 存绝对地址
                        valueReg = registerPool.allocTRegister();
                        int negArrOffset = -symbol1.getArrOffset();
                        mipsCode.add("addiu " + valueReg + ", $fp, " + negArrOffset);
                    }
                } else { // 全局数组 a
                    // 首尾反转
                    int len4Imm;
                    if (symbol1.getDim() == 1) {
                        len4Imm = (symbol1.getSizeI() - 1) * 4;
                    } else {
                        len4Imm = (symbol1.getSizeI() * symbol1.getSizeJ() - 1) * 4;
                    }
                    valueReg = registerPool.allocTRegister();
                    mipsCode.add("la " + valueReg + ", " + arg + " + " + len4Imm);
                }
            }
        }
        symbolTable.getTableList().get(symbolTable.getLevel()).clearVar();
        store(dst, valueReg);
    }

    public void genCALL(Quadruple quadruple) {
        // fp移动 一定是一层？
        mipsCode.add("move $fp, $sp");
        mipsCode.add("jal " + quadruple.getArg1());
        symbolTable.removeBlockTable();
        symbolTable.lastStack();
        mipsCode.add("addiu $sp, $sp, " + symbolTable.getTableList().get(symbolTable.getLevel()).getStackOffset());
        mipsCode.add("move $fp, $sp");
        loadRegister(quadruple.getsRegList());
    }
}
