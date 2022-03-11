package SysY.LexicAnalysis;

import java.util.ArrayList;
import java.util.HashMap;

public class LexicAnalysis {
    //private ErrorHandle errorHandle;
    private ArrayList<String> stringList;
    private HashMap<String, TokenKey> keyWordHashMap;
    private ArrayList<Token> tokenList;
    private int curRow;
    private int curCol;
    private String curString;
    private char curChar;

    public LexicAnalysis(ArrayList<String> stringList) {
        this.stringList = stringList;
        this.tokenList = new ArrayList<>();
        this.curRow = 0;
        this.curCol = 0;
        if (stringList.size() > 0) {
            this.curString = stringList.get(0);
        } else {
            this.curString = "";
        }
        this.keyWordHashMap = new HashMap<>();
        this.keyWordHashMap.put("main", TokenKey.MAINTK);
        this.keyWordHashMap.put("const", TokenKey.CONSTTK);
        this.keyWordHashMap.put("int", TokenKey.INTTK);
        this.keyWordHashMap.put("break", TokenKey.BREAKTK);
        this.keyWordHashMap.put("continue", TokenKey.CONTINUETK);
        this.keyWordHashMap.put("if", TokenKey.IFTK);
        this.keyWordHashMap.put("else", TokenKey.ELSETK);
        this.keyWordHashMap.put("while", TokenKey.WHILETK);
        this.keyWordHashMap.put("getint", TokenKey.GETINTTK);
        this.keyWordHashMap.put("printf", TokenKey.PRINTFTK);
        this.keyWordHashMap.put("return", TokenKey.RETURNTK);
        this.keyWordHashMap.put("void", TokenKey.VOIDTK);
    }

    public void analyse() {
        while (curRow < stringList.size()) {
            curString = getCurString();
            if (curCol < curString.length()) {
                curChar = getCurChar();
                if (curChar == ' ' || curChar == '\r' || curChar == '\n' || curChar == '\t') {
                    //空白符
                    analyseBlank();
                } else if (Character.isLetter(curChar) || curChar == '_') {
                    analyseKeyWordOrIdent();
                } else if (Character.isDigit(curChar)) {
                    analyseIntConst();
                } else if (curChar == '\"') {
                    analyseFormatString();
                } else {
                    analyseOperatorOrUnknown();
                }
            } else {
                curCol = 0;
                curRow += 1;
            }
        }
    }

    public ArrayList<Token> getTokenList() {
        return tokenList;
    }

    private String getCurString(){
        return stringList.get(curRow);
    }

    private char getCurChar(){
        return curString.charAt(curCol);
    }

    private char nextChar() {
        if (curCol + 1 < curString.length()) {
            return curString.charAt(curCol + 1);
        } else {
            return 0;
        }
    }

    private char nextChar(int i) {
        if (curCol + i < curString.length()) {
            return curString.charAt(curCol + 1);
        } else {
            return 0;
        }
    }

    private char getNextChar(){
        curCol++;
        if (curCol < curString.length()) {
            curChar = curString.charAt(curCol);
            return curChar;
        } else {
            return 0;
        }
    }

    private Boolean needNextRow(){
        if (curCol >= curString.length()) {
            return true;
        } else {
            return false;
        }
    }

    private void nextRow() {
        curCol = 0;
        curRow += 1;
        if (curRow < stringList.size()) {
            curString = stringList.get(curRow);
            if (curChar < curString.length()) {
                curChar = curString.charAt(curCol);
            } else {
                curChar = 0;
            }
        } else {
            curString = "";
            curChar = 0;
        }
    }

    private void analyseBlank(){
        curChar = getNextChar();
    }

    private void analyseKeyWordOrIdent(){
        TokenPos tokenPos = new TokenPos(curRow, curCol);
        String tokenString = "" + curChar;
        while (Character.isLetterOrDigit(nextChar()) || nextChar() == '_') {
            curChar = getNextChar();
            tokenString = tokenString + curChar;
        }
        if (keyWordHashMap.containsKey(tokenString)) {
            Token token = new Token(keyWordHashMap.get(tokenString), tokenString, tokenPos);
            tokenList.add(token);
        } else {
            Token token = new Token(TokenKey.IDENFR, tokenString, tokenPos);
            tokenList.add(token);
        }
        curChar = getNextChar();
    }

    private void analyseIntConst(){
        TokenPos tokenPos = new TokenPos(curRow, curCol);
        String tokenString = "" + curChar;
        while (Character.isDigit(nextChar())) {
            curChar = getNextChar();
            tokenString = tokenString + curChar;
        }
        Token token = new Token(TokenKey.INTCON, tokenString, tokenPos);
        tokenList.add(token);
        curChar = getNextChar();
    }

    private void analyseFormatString(){
        TokenPos tokenPos = new TokenPos(curRow, curCol);
        String tokenString = "" + curChar;
        while (nextChar() != '\"' && nextChar() != 0) {
            curChar = getNextChar();
            tokenString = tokenString + curChar;
        }
        if (nextChar() == '\"') {
            curChar = getNextChar();
            tokenString = tokenString + curChar;
        }
        Token token = new Token(TokenKey.STRCON, tokenString, tokenPos);
        tokenList.add(token);
        curChar = getNextChar();
    }

    private void analyseOperatorOrUnknown(){
        TokenPos tokenPos = new TokenPos(curRow, curCol);
        String tokenString = "" + curChar;
        if (curChar == '!') {
            if (nextChar() == '=') {
                curChar = getNextChar();
                tokenString = tokenString + curChar;
                Token token = new Token(TokenKey.NEQ, tokenString, tokenPos);
                tokenList.add(token);
                curChar = getNextChar();
            } else {
                Token token = new Token(TokenKey.NOT, tokenString, tokenPos);
                tokenList.add(token);
                curChar = getNextChar();
            }
        } else if (curChar == '&') {
            if (nextChar() == '&') {
                curChar = getNextChar();
                tokenString = tokenString + curChar;
                Token token = new Token(TokenKey.AND, tokenString, tokenPos);
                tokenList.add(token);
                curChar = getNextChar();
            } /* else error*/
        } else if (curChar == '|') {
            if (nextChar() == '|') {
                curChar = getNextChar();
                tokenString = tokenString + curChar;
                Token token = new Token(TokenKey.OR, tokenString, tokenPos);
                tokenList.add(token);
                curChar = getNextChar();
            } /* else error*/
        } else if (curChar == '+') {
            Token token = new Token(TokenKey.PLUS, tokenString, tokenPos);
            tokenList.add(token);
            curChar = getNextChar();
        } else if (curChar == '-') {
            Token token = new Token(TokenKey.MINU, tokenString, tokenPos);
            tokenList.add(token);
            curChar = getNextChar();
        } else if (curChar == '*') {
            Token token = new Token(TokenKey.MULT, tokenString, tokenPos);
            tokenList.add(token);
            curChar = getNextChar();
        } else if (curChar == '/') {
            //处理注释
            if (nextChar() == '/') {
                curChar = nextChar();
                nextRow();
            } else if (nextChar() == '*') {
                curChar = getNextChar();
                //shit
                curChar = getNextChar();
                while (true) {
                    if (curChar == '*') {
                        curChar = getNextChar();
                        if (needNextRow()) {
                            nextRow();
                            continue;
                        }
                        if (curChar == '/') {
                            break;
                        }
                    } else {
                        curChar = getNextChar();
                        if (needNextRow()) {
                            nextRow();
                        }
                    }
                }
                //end of shit
                curChar = getNextChar();
            } else {
                Token token = new Token(TokenKey.DIV, tokenString, tokenPos);
                tokenList.add(token);
                curChar = getNextChar();
            }
        } else if (curChar == '%') {
            Token token = new Token(TokenKey.MOD, tokenString, tokenPos);
            tokenList.add(token);
            curChar = getNextChar();
        } else if (curChar == '<') {
            if (nextChar() == '=') {
                curChar = getNextChar();
                tokenString = tokenString + curChar;
                Token token = new Token(TokenKey.LEQ, tokenString, tokenPos);
                tokenList.add(token);
                curChar = getNextChar();
            } else {
                Token token = new Token(TokenKey.LSS, tokenString, tokenPos);
                tokenList.add(token);
                curChar = getNextChar();
            }
        } else if (curChar == '>') {
            if (nextChar() == '=') {
                curChar = getNextChar();
                tokenString = tokenString + curChar;
                Token token = new Token(TokenKey.GEQ, tokenString, tokenPos);
                tokenList.add(token);
                curChar = getNextChar();
            } else {
                Token token = new Token(TokenKey.GRE, tokenString, tokenPos);
                tokenList.add(token);
                curChar = getNextChar();
            }
        } else if (curChar == '=') {
            if (nextChar() == '=') {
                curChar = getNextChar();
                tokenString = tokenString + curChar;
                Token token = new Token(TokenKey.EQL, tokenString, tokenPos);
                tokenList.add(token);
                curChar = getNextChar();
            } else {
                Token token = new Token(TokenKey.ASSIGN, tokenString, tokenPos);
                tokenList.add(token);
                curChar = getNextChar();
            }
        } else if (curChar == ';') {
            Token token = new Token(TokenKey.SEMICN, tokenString, tokenPos);
            tokenList.add(token);
            curChar = getNextChar();
        } else if (curChar == ',') {
            Token token = new Token(TokenKey.COMMA, tokenString, tokenPos);
            tokenList.add(token);
            curChar = getNextChar();
        } else if (curChar == '(') {
            Token token = new Token(TokenKey.LPARENT, tokenString, tokenPos);
            tokenList.add(token);
            curChar = getNextChar();
        } else if (curChar == ')') {
            Token token = new Token(TokenKey.RPARENT, tokenString, tokenPos);
            tokenList.add(token);
            curChar = getNextChar();
        } else if (curChar == '[') {
            Token token = new Token(TokenKey.LBRACK, tokenString, tokenPos);
            tokenList.add(token);
            curChar = getNextChar();
        } else if (curChar == ']') {
            Token token = new Token(TokenKey.RBRACK, tokenString, tokenPos);
            tokenList.add(token);
            curChar = getNextChar();
        } else if (curChar == '{') {
            Token token = new Token(TokenKey.LBRACE, tokenString, tokenPos);
            tokenList.add(token);
            curChar = getNextChar();
        } else if (curChar == '}') {
            Token token = new Token(TokenKey.RBRACE, tokenString, tokenPos);
            tokenList.add(token);
            curChar = getNextChar();
        } /* else error*/
    }
}
