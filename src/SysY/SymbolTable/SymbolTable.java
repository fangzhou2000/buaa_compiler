package SysY.SymbolTable;

import SysY.ObjectCode.RegisterPool;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SymbolTable {
    private int level;
    private ArrayList<Integer> stackPointerLevelList;
    private ArrayList<BlockTable> tableList;
    private int stringCount;
    private HashMap<String, Integer> stringConstMap;
    private int paraCount;
    private RegisterPool registerPool;

    public SymbolTable() {
        this.level = 0;
        this.stackPointerLevelList = new ArrayList<>();
        this.stackPointerLevelList.add(0);
        this.tableList = new ArrayList<>();
        this.stringCount = 0;
        this.stringConstMap = new HashMap<>();
        this.paraCount = 0;
        this.registerPool = new RegisterPool();
        this.tableList.add(new BlockTable(this.registerPool, 0));
    }

    // 代码生成
    public String generateParaName() {
        return "@para" + this.paraCount++;
    }

    public boolean containLocalSymbol(String name) {
        for (int i = 1; i < tableList.size(); i++) {
            if (tableList.get(i).contain(name)) {
                return true;
            }
        }
        return false;
    }

    public void newStack() {
        tableList.get(level).setStackOffset(0);
        stackPointerLevelList.add(level);
    }

    public void lastStack() {
        int index = stackPointerLevelList.size() - 1;
        stackPointerLevelList.remove(index);
    }

    public int getSymbolOffset(String name) {
        int symbolLevel = 0;
        for (int i = 0; i < tableList.size(); i++) {
            if (tableList.get(i).contain(name)) {
                symbolLevel = i;
            }
        }
        if (symbolLevel >= stackPointerLevelList.get(stackPointerLevelList.size() - 1)) {
            // 在现在栈上
            return -tableList.get(symbolLevel).getMap().get(name).getOffset();
        } else {
            // 在过去的栈中，不一定是上一层，例如 fun(f1(f2(a)));
            int symbolStackPointLevelIndex = 0;
            for (int i = 1; i < stackPointerLevelList.size(); i++) {
                if (symbolLevel < stackPointerLevelList.get(i)) {
                    symbolStackPointLevelIndex = i - 1;
                    break;
                }
            }
            int allStackOffset = 0;
            for (int i = stackPointerLevelList.size() - 1; i > symbolStackPointLevelIndex; i--) {
                allStackOffset += tableList.get(stackPointerLevelList.get(i) - 1).getStackOffset();
            }
            return allStackOffset - tableList.get(symbolLevel).getMap().get(name).getOffset();
        }
    }

    public void setSymbolOffset(String name) {
        if (tableList.get(level).contain(name)) {
            tableList.get(level).setOffset(name);
        } else {
            System.out.println("Something wrong with setSymbolOffset");
        }
    }

    public void setArrOffset(String name, int len) {
        if (tableList.get(level).contain(name)) {
            tableList.get(level).setArrOffset(name, len);
        } else {
            System.out.println("Something wrong with setArrOffset");
        }
    }

    public RegisterPool getRegisterPool() {
        return registerPool;
    }

    public HashMap<String, Integer> getStringConstMap() {
        return stringConstMap;
    }

    public void addStringConst(String string) {
        stringConstMap.put(string, stringCount++);
    }

    // 语义分析
    public boolean containSymbol(String name) {
        for (BlockTable blockTable : tableList) {
            if (blockTable.contain(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean containFuncSymbol(String name) {
        return tableList.get(0).containFunc(name);
    }

    public Symbol getSymbol(String name) {
        Symbol symbol = null;
        for (BlockTable blockTable : tableList) {
            if (blockTable.contain(name)) {
                symbol = blockTable.getMap().get(name);
            }
        }
        return symbol;
    }

    public Symbol getFuncSymbol(String name) {
        Symbol symbol = null;
        if (tableList.get(0).containFunc(name)) {
            symbol = tableList.get(0).getFuncMap().get(name);
        }
        return symbol;
    }

    public boolean isImm(String arg) {
        Pattern patternImm = Pattern.compile("-?[0-9]+");
        Matcher matcherImm = patternImm.matcher(arg);
        return matcherImm.matches();
    }

    public int getSymbolValue(String name) {
        Pattern patternArr = Pattern.compile("(.+?)\\[(.+)\\]");
        Matcher matcherArr = patternArr.matcher(name);
        if (isImm(name)) {
            return Integer.parseInt(name);
        } else if (matcherArr.matches()) {
            String ident = matcherArr.group(1);
            String index = matcherArr.group(2);
            if (containSymbol(ident)) {
                if (getSymbolValue(index) >= 0 &&
                        getSymbolValue(index) < getSymbol(ident).getValueList().size()) {
                    return getSymbol(ident).getValueList().get(getSymbolValue(index));
                } else {
                    return 0;
                }
            } else {
                System.out.println("getSymbolValue wrong1: " + name + " not exists!");
                return 0;
            }
        } else if (containSymbol(name)){
            return getSymbol(name).getValue();
        } else {
            System.out.println("getSymbolValue wrong: " + name + " not exists!");
            return 0;
        }
    }

    // 错误处理
    public boolean isContainSameName(String name) {
        return tableList.get(level).containFunc(name) || tableList.get(level).contain(name);
    }

    public boolean isUndefinedName(String name) {
        for (BlockTable blockTable : tableList) {
            if (blockTable.contain(name) || blockTable.containFunc(name)) {
                return false;
            }
        }
        return true;
    }

    public boolean IsMismatchFuncParamsNum(String name, int num) {
        int fParamsNum = tableList.get(0).getFuncMap().get(name).getParamsNum();
        return fParamsNum != num;
    }

    public boolean IsMismatchFuncParamsDim(String name, ArrayList<Integer> dimList) {
        ArrayList<Integer> fParamsDimList = tableList.get(0).getFuncMap().get(name).getParamsDimList();
        if (fParamsDimList.size() != dimList.size()) {
            return true;
        } else {
            for (int i = 0; i < fParamsDimList.size(); i++) {
                if (!fParamsDimList.get(i).equals(dimList.get(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isChangeConst(String name) {
        boolean ret = false;
        for (BlockTable blockTable : tableList) {
            if (blockTable.contain(name)) {
                if (blockTable.getMap().get(name).getSymbolType() == SymbolType.CONST) {
                    ret = true;
                } else {
                    ret = false;
                }
            }
        }
        return ret;
    }

    // 基本操作
    public int getLevel() {
        return level;
    }

    public ArrayList<BlockTable> getTableList() {
        return tableList;
    }

    public void addBlockTable() {
        level++;
        tableList.add(new BlockTable(registerPool, tableList.get(level - 1).getStackOffset()));
    }

    public void removeBlockTable() {
        tableList.remove(level);
        level--;
    }

    public void addSymbol(Symbol symbol) {
        tableList.get(level).addSymbol(symbol);
    }

    public void removeSymbol(String name) {
        tableList.get(level).removeSymbol(name);
    }
}
