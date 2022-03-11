package SysY.ObjectCode;

import SysY.SymbolTable.Symbol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterPool {
    private int tPoint;
    private ArrayList<Integer> tRegList;
    private ArrayList<Integer> sRegList;
    private ArrayList<Integer> aRegList;
    private Integer v1Reg;
    private Integer gpReg;
    private Integer k0Reg;
    private Integer k1Reg;
    private HashMap<Integer, String> tRegMap;
    private HashMap<Integer, Symbol> sRegMap;
    private HashMap<Integer, Symbol> aRegMap;
    private Symbol v1Symbol;
    private Symbol gpSymbol;
    private Symbol k0Symbol;
    private Symbol k1Symbol;

    public RegisterPool() {
        this.tRegList = new ArrayList<>();
        this.sRegList = new ArrayList<>();
        this.aRegList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            this.tRegList.add(0);
        }
        this.sRegList = new ArrayList<>();
        for (int i = 0; i < 8; i++ ) {
            this.sRegList.add(0);
        }
        this.aRegList = new ArrayList<>();
        for (int i = 0; i < 4; i++ ) {
            this.aRegList.add(0);
        }
        this.v1Reg = 0;
        this.gpReg = 0;
        this.k0Reg = 0;
        this.k1Reg = 0;
        this.tRegMap = new HashMap<>();
        this.sRegMap = new HashMap<>();
        this.aRegMap = new HashMap<>();
        this.v1Symbol = null;
        this.gpSymbol = null;
        this.k0Symbol = null;
        this.k1Symbol = null;
        this.tPoint = 0;
    }

    public ArrayList<Integer> gettRegList() {
        return tRegList;
    }

    public HashMap<Integer, String> gettRegMap() {
        return tRegMap;
    }

    public ArrayList<Integer> getsRegList() {
        return sRegList;
    }

    public HashMap<Integer, Symbol> getsRegMap() {
        return sRegMap;
    }

    public boolean isSRegAvailable() {
        for (int i = 0; i < this.sRegList.size(); i++) {
            if (this.sRegList.get(i) == 0) {
                return true;
            }
        }
        return false;
    }

    public void mapSRegister(Symbol symbol) {
        for (int i = 0; i < this.sRegList.size(); i++) {
            if (this.sRegList.get(i) == 0) {
                this.sRegMap.put(i, symbol);
                this.sRegList.set(i, 1);
                return ;
            }
        }
        System.out.println("map wrong");
    }

    public boolean containSRegister(Symbol symbol) {
        for (Symbol s : this.sRegMap.values()) {
            if (s.equals(symbol)) {
                return true;
            }
        }
//        for (Symbol s : this.aRegMap.values()) {
//            if (s.equals(symbol)) {
//                return true;
//            }
//        }
//        if (symbol.equals(this.v1Symbol) || symbol.equals(this.gpSymbol) ||
//                symbol.equals(this.k0Symbol) || symbol.equals(this.k1Symbol)) {
//            return true;
//        }
        return false;
    }

    public String getSRegister(Symbol symbol) {
        for (Integer i : this.sRegMap.keySet()) {
            if (this.sRegMap.get(i).equals(symbol)) {
                return "$s" + i;
            }
        }
//        for (Integer i : this.aRegMap.keySet()) {
//            if (this.aRegMap.get(i).equals(symbol)) {
//                return "$a" + i;
//            }
//        }
//        if (symbol.equals(v1Symbol)) {
//            return "$v1";
//        }
//        if (symbol.equals(gpSymbol)) {
//            return "$gp";
//        }
//        if (symbol.equals(k0Symbol)) {
//            return "$k0";
//        }
//        if (symbol.equals(k1Symbol)) {
//            return "$k1";
//        }
        return "getSRegister wrong";
    }

    public void freeSRegister(String t) {
        Pattern patternTReg = Pattern.compile("^\\$s([0-9])");
        Matcher matcherTReg = patternTReg.matcher(t);
        if (matcherTReg.matches()) {
            int i = Integer.parseInt(matcherTReg.group(1));
            this.sRegMap.remove(i);
            this.sRegList.set(i, 0);
        }
    }

    public boolean isTRegAvailable() {
        for (int i = 4; i < this.tRegList.size(); i++) {
            if (this.tRegList.get(i) == 0) {
                return true;
            }
        }
        return false;
    }

    // 约定 $t0-$t4 为中间结果寄存器，可任意分配
    public String allocTRegister() {
        tPoint++;
        if (tPoint >= 4) {
            tPoint = 0;
        }
        return "$t" + tPoint;
    }

    // 约定 $t4-$t9 为临时变量的寄存器
    public String allocTRegister(String varName) {
        for (int i = 4; i < this.tRegList.size(); i++) {
            if (this.tRegList.get(i) == 0) {
                this.tRegMap.put(i, varName);
                this.tRegList.set(i, 1);
                return "$t" + i;
            }
        }
        return "allocTRegister wrong";
    }

    public void freeTRegister(String t) {
        Pattern patternTReg = Pattern.compile("^\\$t([0-9])");
        Matcher matcherTReg = patternTReg.matcher(t);
        if (matcherTReg.matches()) {
            int i = Integer.parseInt(matcherTReg.group(1));
            this.tRegMap.remove(i);
            this.tRegList.set(i, 0);
        }
    }

    public String getTRegister(String varName) {
        for (Integer i : this.tRegMap.keySet()) {
            if (this.tRegMap.get(i).equals(varName)) {
                return "$t" + i;
            }
        }
        return "getTRegister wrong";
    }

    public void clear() {
        this.tRegList = new ArrayList<>();
        this.sRegList = new ArrayList<>();
        this.aRegList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            this.tRegList.add(0);
        }
        this.sRegList = new ArrayList<>();
        for (int i = 0; i < 8; i++ ) {
            this.sRegList.add(0);
        }
        this.aRegList = new ArrayList<>();
        for (int i = 0; i < 4; i++ ) {
            this.aRegList.add(0);
        }
        this.v1Reg = 0;
        this.gpReg = 0;
        this.k0Reg = 0;
        this.k1Reg = 0;
        this.tRegMap = new HashMap<>();
        this.sRegMap = new HashMap<>();
        this.aRegMap = new HashMap<>();
        this.v1Symbol = null;
        this.gpSymbol = null;
        this.k0Symbol = null;
        this.k1Symbol = null;
        this.tPoint = 0;
    }
}
