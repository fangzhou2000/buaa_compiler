package SysY.SymbolTable;

import SysY.ObjectCode.RegisterPool;

import java.util.ArrayList;
import java.util.HashMap;

public class BlockTable {
    private HashMap<String, Symbol> map;
    private HashMap<String, Symbol> funcMap;
    private RegisterPool registerPool;
    private int stackOffset;

    public BlockTable(RegisterPool registerPool, int stackOffset) {
        this.map = new HashMap<>();
        this.funcMap = new HashMap<>();
        this.registerPool = registerPool;
        this.stackOffset = stackOffset;
    }

    public void clearVar() {
        ArrayList<String> varList = new ArrayList<>();
        for (String name : map.keySet()) {
            if (map.get(name).getSymbolType() == SymbolType.VAR) {
                varList.add(name);
            }
        }
        stackOffset = stackOffset - 4 * varList.size();
        for (String name : varList) {
            map.remove(name);
        }
    }

    public int getStackOffset() {
        return stackOffset;
    }

    public void setStackOffset(int stackOffset) {
        this.stackOffset = stackOffset;
    }

    public void setOffset(String name) {
        if (this.map.containsKey(name)) {
            this.map.get(name).setOffset(this.stackOffset);
            this.stackOffset += 4;
        } else {
            System.out.println("setOffset wrong");
        }
    }

    public void setArrOffset(String name, int len) {
        if (this.map.containsKey(name)) {
            this.map.get(name).setArrOffset(stackOffset);
            this.stackOffset += len * 4;
        } else {
            System.out.println("setArrOffset wrong");
        }
    }

    public HashMap<String, Symbol> getMap() {
        return map;
    }

    public HashMap<String, Symbol> getFuncMap() {
        return funcMap;
    }

    public boolean contain(String name) {
        return map.containsKey(name);
    }

    public boolean containFunc(String name) {
        return funcMap.containsKey(name);
    }

    public void addSymbol(Symbol symbol) {
        if (symbol.getSymbolType() == SymbolType.INT_FUNC || symbol.getSymbolType() == SymbolType.VOID_FUNC) {
            this.funcMap.put(symbol.getName(), symbol);
        } else {
            this.map.put(symbol.getName(), symbol);
        }
    }

    public void removeSymbol(String name) {
        this.funcMap.remove(name);
        this.map.remove(name);
    }
}
