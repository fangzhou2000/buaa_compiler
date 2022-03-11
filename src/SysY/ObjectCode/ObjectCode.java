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

public class ObjectCode {
    private ArrayList<Quadruple> intermediateCode;
    private SymbolTable symbolTable;
    private RegisterPool registerPool;
    private ArrayList<String> mipsCode;

    public ObjectCode(ArrayList<Quadruple> intermediateCode, SymbolTable symbolTable) {
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
        Pattern pattern = Pattern.compile("(.+?)\\[(.+)\\]");
        Matcher matcher = pattern.matcher(arg);
        // 区分a和a[]
        if (matcher.matches()) {
            // a[]
            // 注意：a[]不可能为数组
            String ident = matcher.group(1);
            String index = matcher.group(2);
            String arrAddr = loadArrAddr(ident, index);
            valueReg = registerPool.allocTRegister();
            mipsCode.add("lw " + valueReg + ", " + arrAddr);
        } else {
            // a
            if (symbolTable.containSymbol(arg)) {
                // 注意：a不可能为数组
                String argAddr = loadAddr(arg);
                valueReg = registerPool.allocTRegister();
                mipsCode.add("lw " + valueReg + ", " + argAddr);
            } else if (arg.equals("@RET")) {
                valueReg = registerPool.allocTRegister();
                mipsCode.add("move " + valueReg + ", $v0");
            } else if (isImm(arg)) {
                valueReg = registerPool.allocTRegister();
                mipsCode.add("li " + valueReg + ", " + arg);
            } else {
                System.out.println("something wrong with load");
            }
        }
        return valueReg;
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
                // -1 未存入栈
                if (symbol.getOffset() == -1) {
                    storeAddr(arg);
                }
                String argAddr = loadAddr(arg);
                mipsCode.add("sw " + valueReg + ", " + argAddr);
            } else {
                System.out.println("something wrong with store");
            }
        }
    }

    // 获取数组地址
    public String loadArrAddr(String arg, String index) {
        // 注意节省寄存器
        Symbol symbol = symbolTable.getSymbol(arg);
        // index
        String index4Reg = load(index);
        mipsCode.add("sll " + index4Reg + ", " + index4Reg + ", " + 2);
        // a[] 为值
        if (symbolTable.containLocalSymbol(arg)) { // a 为局部数组
            if (symbol.isArrAddr()) { // a 为函数形参 形参以局部变量形式保存
                String addrReg = load(arg);
                // 取绝对地址
                // mipsCode.add("subu " + addrReg + ", $fp, " + addrReg);
                mipsCode.add("subu " + addrReg + ", " + addrReg + ", " + index4Reg);
                return "(" + addrReg + ")";
            } else { // a 为普通局部数组
                String addrReg = registerPool.allocTRegister();
                mipsCode.add("subu " + addrReg + ", $fp, " + symbol.getArrOffset());
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
    public void loadRegister() {
        String name = "$RA";
        String addr = loadAddr(name);
        mipsCode.add("lw $ra, " + addr);
        symbolTable.removeSymbol(name);
        int blockOffset = symbolTable.getTableList().get(symbolTable.getLevel()).getStackOffset();
        symbolTable.getTableList().get(symbolTable.getLevel()).setStackOffset(blockOffset - 4);
    }

    public void storeRegister() {
        String name = "$RA";
        Symbol symbol = new Symbol(name, SymbolType.VAR);
        symbolTable.addSymbol(symbol);
        storeAddr(name);
        String addr = loadAddr(name);
        mipsCode.add("sw $ra, " + addr);
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
        if (!symbolTable.containSymbol(dst)) {
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
        if (!symbolTable.containSymbol(dst)) {
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
        if (!symbolTable.containSymbol(dst)) {
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
        if (!symbolTable.containSymbol(dst)) {
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
        if (!symbolTable.containSymbol(dst)) {
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
        if (!symbolTable.containSymbol(dst)) {
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
        String argReg = load(arg);
        store(dst, argReg);
    }

    public void genVASS(Quadruple quadruple) {
        String dst = quadruple.getDst();
        String arg = quadruple.getArg1();
        Symbol symbol = new Symbol(dst, SymbolType.VAR);
        symbol.setValue(symbolTable.getSymbolValue(arg));
        symbolTable.addSymbol(symbol);
        String argReg = "$0";
        if (!arg.equals("")) {
            argReg = load(arg);
        }
        store(dst, argReg);
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
        String arg1 = quadruple.getArg1();
        String dst = quadruple.getDst();
        String valueReg = load(arg1);
        if (!symbolTable.containSymbol(dst)) {
            Symbol symbol = new Symbol(dst, SymbolType.VAR);
            symbolTable.addSymbol(symbol);
            store(dst, valueReg);
        } else {
            store(dst, valueReg);
        }
    }

    public void genADD(Quadruple quadruple) {
        String dst = quadruple.getDst();
        String arg1 = quadruple.getArg1();
        String arg2 = quadruple.getArg2();
        String t1 = load(arg1);
        String t2 = load(arg2);
        if (!symbolTable.containSymbol(dst)) {
            Symbol symbol = new Symbol(dst, SymbolType.VAR);
            symbolTable.addSymbol(symbol);
            String t = registerPool.allocTRegister();
            mipsCode.add("addu " + t + ", " + t1 + ", " + t2);
            store(dst, t);
        } else {
            String t = registerPool.allocTRegister();
            mipsCode.add("addu " + t + ", " + t1 + ", " + t2);
            store(dst, t);
        }
    }

    public void genSUB(Quadruple quadruple) {
        String dst = quadruple.getDst();
        String arg1 = quadruple.getArg1();
        String arg2 = quadruple.getArg2();
        String t1 = load(arg1);
        String t2 = load(arg2);
        if (!symbolTable.containSymbol(dst)) {
            Symbol symbol = new Symbol(dst, SymbolType.VAR);
            symbolTable.addSymbol(symbol);
            String t = registerPool.allocTRegister();
            mipsCode.add("subu " + t + ", " + t1 + ", " + t2);
            store(dst, t);
        } else {
            String t = registerPool.allocTRegister();
            mipsCode.add("subu " + t + ", " + t1 + ", " + t2);
            store(dst, t);
        }
    }

    public void genMUL(Quadruple quadruple) {
        String dst = quadruple.getDst();
        String arg1 = quadruple.getArg1();
        String arg2 = quadruple.getArg2();
        String t1 = load(arg1);
        String t2 = load(arg2);
        if (!symbolTable.containSymbol(dst)) {
            Symbol symbol = new Symbol(dst, SymbolType.VAR);
            symbolTable.addSymbol(symbol);
            String t = registerPool.allocTRegister();
            mipsCode.add("mul " + t + ", " + t1 + ", " + t2);
            store(dst, t);
        } else {
            String t = registerPool.allocTRegister();
            mipsCode.add("mul " + t + ", " + t1 + ", " + t2);
            store(dst, t);
        }
    }

    public void genDIV(Quadruple quadruple) {
        String dst = quadruple.getDst();
        String arg1 = quadruple.getArg1();
        String arg2 = quadruple.getArg2();
        String t1 = load(arg1);
        String t2 = load(arg2);
        if (!symbolTable.containSymbol(dst)) {
            Symbol symbol = new Symbol(dst, SymbolType.VAR);
            symbolTable.addSymbol(symbol);
            String t = registerPool.allocTRegister();
            mipsCode.add("div " + t1 + ", " + t2);
            mipsCode.add("mflo " + t);
            store(dst, t);
        } else {
            String t = registerPool.allocTRegister();
            mipsCode.add("div " + t1 + ", " + t2);
            mipsCode.add("mflo " + t);
            store(dst, t);
        }
    }

    public void genMOD(Quadruple quadruple) {
        String dst = quadruple.getDst();
        String arg1 = quadruple.getArg1();
        String arg2 = quadruple.getArg2();
        String t1 = load(arg1);
        String t2 = load(arg2);
        if (!symbolTable.containSymbol(dst)) {
            Symbol symbol = new Symbol(dst, SymbolType.VAR);
            symbolTable.addSymbol(symbol);
            String t = registerPool.allocTRegister();
            mipsCode.add("div " + t1 + ", " + t2);
            mipsCode.add("mfhi " + t);
            store(dst, t);
        } else {
            String t = registerPool.allocTRegister();
            mipsCode.add("div " + t1 + ", " + t2);
            mipsCode.add("mfhi " + t);
            store(dst, t);
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
        }
    }

    public void genCALL_BEGIN(Quadruple quadruple) {
        // 因为参数要压到新栈里，所以在调用开始就要创建新栈
        storeRegister();
        registerPool.clear();
        mipsCode.add("subu $sp, $sp, " + symbolTable.getTableList().get(symbolTable.getLevel()).getStackOffset());
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
                String iReg = load(i);
                String index4Reg = registerPool.allocTRegister();
                mipsCode.add("li " + index4Reg + ", " + sizeJ);
                mipsCode.add("mul " + index4Reg + ", " + index4Reg + ", " + iReg);
                mipsCode.add("sll " + index4Reg + ", " + index4Reg + ", " + 2);
                if (symbolTable.containLocalSymbol(ident)) { // 局部数组 a[]
                    if (symbol1.isArrAddr()) {
                        String arrOffsetReg = load(ident);
                        valueReg = registerPool.allocTRegister();
                        mipsCode.add("subu " + valueReg + ", " + arrOffsetReg + ", " + index4Reg);
                    } else {
                        // 存绝对地址
                        valueReg = registerPool.allocTRegister();
                        mipsCode.add("addu " + valueReg + ", " + index4Reg + ", " + symbol1.getArrOffset());
                        mipsCode.add("subu " + valueReg + ", $fp, " + valueReg);
                    }
                } else { // 全局数组 a[] // testfile10无
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
            } else { // 数组 a
                Symbol symbol1 = symbolTable.getSymbol(arg);
                if (symbolTable.containLocalSymbol(arg)) { // 局部数组 a
                    if (symbol1.isArrAddr()) {
                        valueReg = load(arg);
                    } else {
                        // 存绝对地址
                        valueReg = registerPool.allocTRegister();
                        mipsCode.add("subu " + valueReg + ", $fp, " + symbol1.getArrOffset());
                    }
                } else { // 全局数组 a // 10无
                    // 首尾反转
                    String len4Reg = registerPool.allocTRegister();
                    if (symbol1.getDim() == 1) {
                        mipsCode.add("li " + len4Reg + ", " + (symbol1.getSizeI() - 1) * 4);
                    } else {
                        mipsCode.add("li " + len4Reg + ", " + (symbol1.getSizeI() * symbol1.getSizeJ() - 1) * 4);
                    }
                    // 数组首地址
                    // 存绝对地址
                    valueReg = registerPool.allocTRegister();
                    mipsCode.add("la " + valueReg + ", " + arg + "(" + len4Reg + ")");
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
        mipsCode.add("addu $sp, $sp, " + symbolTable.getTableList().get(symbolTable.getLevel()).getStackOffset());
        mipsCode.add("move $fp, $sp");
        loadRegister();
    }
}
