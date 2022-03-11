package SysY.AST;

import SysY.LexicAnalysis.Token;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PNumber extends Node {
    public Token intConst;

    public void errorCheck() {}

    public String semanticAnalyse() {
        return this.intConst.getTokenString();
    }

    public int getConst() {
        Pattern patternPNumber = Pattern.compile("[0-9]+");
        Matcher matcherPNumber = patternPNumber.matcher(this.intConst.getTokenString());
        if (matcherPNumber.matches()) {
            return Integer.parseInt(this.intConst.getTokenString());
        } else {
            System.out.println("PNumber getConst wrong");
            return 0;
        }
    }
}
