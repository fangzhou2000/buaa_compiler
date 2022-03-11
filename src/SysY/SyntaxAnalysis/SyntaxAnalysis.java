package SysY.SyntaxAnalysis;

import SysY.AST.*;
import SysY.ErrorHandle.*;
import SysY.SemanticAnalysis.Intermediate;
import SysY.SymbolTable.*;
import SysY.LexicAnalysis.Token;
import SysY.LexicAnalysis.TokenKey;

import java.util.ArrayList;

public class SyntaxAnalysis {
    private SymbolTable symbolTable;
    private ErrorHandle errorHandle;
    private Intermediate intermediate;
    private ArrayList<Token> tokenList;
    private ArrayList<String> syntaxList;
    private int cur;
    private Root ast;

    public SyntaxAnalysis(SymbolTable symbolTable, ErrorHandle errorHandle, Intermediate intermediate, ArrayList<Token> tokenList) {
        this.symbolTable = symbolTable;
        this.errorHandle = errorHandle;
        this.intermediate = intermediate;
        this.tokenList = tokenList;
        this.syntaxList = new ArrayList<>();
        this.cur = -1;
        this.ast = new Root(symbolTable, errorHandle, intermediate);
    }

    public Root getAst() {
        return ast;
    }

    public ArrayList<String> getSyntaxList() {
        return syntaxList;
    }

    private Token nextToken(int i){
        if (this.cur < tokenList.size() - i) {
            return this.tokenList.get(cur + i);
        } else {
            return null;
        }
    }

    private boolean isNext(int i, TokenKey tokenKey) {
        if (nextToken(i) == null) {
            return false;
        } else {
            return nextToken(i).getTokenKey() == tokenKey;
        }
    }

    private Token getNextToken(String s) {
        if (cur + 1 < tokenList.size()) {
            if (s.equals(tokenList.get(cur + 1).getTokenString())
                    || (s.equals("ident") && isNext(1, TokenKey.IDENFR))
                    || (s.equals("intConst") && isNext(1, TokenKey.INTCON))
                    || s.equals("formatString") && isNext(1, TokenKey.STRCON)) {
                this.syntaxList.add(tokenList.get(cur + 1).getTokenKey() + " " + tokenList.get(cur + 1).getTokenString());
                cur += 1;
                return tokenList.get(cur);
            } else {
                return null;
            }
        }
        return null;
    }

    public void analyse(){
        this.ast.compUnit = analyseCompUnit();
    }

    private CompUnit analyseCompUnit() {
        CompUnit compUnit = new CompUnit();
        while (isNext(1, TokenKey.CONSTTK)
                || (isNext(1, TokenKey.INTTK)
                    && isNext(2, TokenKey.IDENFR)
                    && !isNext(3, TokenKey.LPARENT))) {
            compUnit.declList.add(analyseDecl());
        }
        while (isNext(1, TokenKey.VOIDTK)
                || (isNext(1, TokenKey.INTTK)
                    && isNext(2, TokenKey.IDENFR)
                    && isNext(3, TokenKey.LPARENT))) {
            compUnit.funcDefList.add(analyseFuncDef());
        }
        if (isNext(1, TokenKey.INTTK)) {
            compUnit.mainFuncDef = analyseMainFuncDef();
        }
        this.syntaxList.add("<" + SyntaxType.CompUnit + ">");
        return compUnit;
    }

    private Decl analyseDecl() {
        Decl decl = new Decl();
        if (isNext(1, TokenKey.CONSTTK)) {
            decl.isConst = true;
            decl.constDecl = analyseConstDecl();
        } else if (isNext(1, TokenKey.INTTK)) {
            decl.isConst = false;
            decl.varDecl = analyseVarDecl();
        } else {
            System.out.println("Decl wrong!");
        }
        //this.syntaxList.add("<" + SysY.SyntaxAnalysis.SyntaxType.Decl + ">");
        return decl;
    }

    private ConstDecl analyseConstDecl() {
        ConstDecl constDecl = new ConstDecl();
        constDecl.tokenConst = getNextToken("const");
        constDecl.btype = analyseBType();
        constDecl.constDef = analyseConstDef();
        while (isNext(1, TokenKey.COMMA)) {
            constDecl.commaList.add(getNextToken(","));
            constDecl.constDefList.add(analyseConstDef());
        }
        // i
        if (isNext(1, TokenKey.SEMICN)) {
            constDecl.semicn = getNextToken(";");
        } else {
            constDecl.semicn = this.tokenList.get(cur);
        }
        this.syntaxList.add("<" + SyntaxType.ConstDecl + ">");
        return constDecl;
    }

    private BType analyseBType() {
        BType btype = new BType();
        btype.tokenInt = getNextToken("int");
        //this.syntaxList.add("<" + SysY.SyntaxAnalysis.SyntaxType.BType + ">");
        return btype;
    }

    private ConstDef analyseConstDef() {
        ConstDef constDef = new ConstDef();
        constDef.dim = 0;
        constDef.ident = analyseIdent();
        while (isNext(1, TokenKey.LBRACK)) {
            constDef.dim++;
            constDef.lBrackList.add(getNextToken("["));
            constDef.constExpList.add(analyseConstExp());
            if (isNext(1, TokenKey.RBRACK)) {
                constDef.rBrackList.add(getNextToken("]"));
            } else {
                constDef.rBrackList.add(this.tokenList.get(cur));
            }
        }
        constDef.assign = getNextToken("=");
        constDef.constInitVal = analyseConstInitVal();
        this.syntaxList.add("<" + SyntaxType.ConstDef + ">");
        return constDef;
    }

    private ConstInitVal analyseConstInitVal() {
        ConstInitVal constInitVal = new ConstInitVal();
        constInitVal.dim = 0;
        if (isNext(1, TokenKey.LBRACE)) {
            constInitVal.dim++;
            constInitVal.lBrace = getNextToken("{");
            if (!isNext(1, TokenKey.RBRACE)) {
                constInitVal.constInitVal = analyseConstInitVal();
                constInitVal.dim += constInitVal.constInitVal.dim;
                while (isNext(1, TokenKey.COMMA)) {
                    constInitVal.commaList.add(getNextToken(","));
                    constInitVal.constInitValList.add(analyseConstInitVal());
                }
            }
            constInitVal.rBrace = getNextToken("}");
        } else {
            constInitVal.constExp = analyseConstExp();
        }
        this.syntaxList.add("<" + SyntaxType.ConstInitVal + ">");
        return constInitVal;
    }

    private Token analyseIdent() {
        if (isNext(1, TokenKey.IDENFR)) {
            return getNextToken("ident");
        } else {
            System.out.println("ident wrong!");
            return null;
        }
    }

    private VarDecl analyseVarDecl() {
        VarDecl varDecl = new VarDecl();
        varDecl.btype = analyseBType();
        varDecl.varDef = analyseVarDef();
        while (isNext(1, TokenKey.COMMA)) {
            varDecl.commaList.add(getNextToken(","));
            varDecl.varDefList.add(analyseVarDef());
        }
        //i
        if (isNext(1, TokenKey.SEMICN)) {
            varDecl.semicn = getNextToken(";");
        } else {
            varDecl.semicn = this.tokenList.get(cur);
        }
        this.syntaxList.add("<" + SyntaxType.VarDecl + ">");
        return varDecl;
    }
    private VarDef analyseVarDef() {
        VarDef varDef = new VarDef();
        varDef.dim = 0;
        varDef.ident = analyseIdent();
        while (isNext(1, TokenKey.LBRACK)) {
            varDef.dim++;
            varDef.lBrackList.add(getNextToken("["));
            varDef.constExpList.add(analyseConstExp());
            if (isNext(1, TokenKey.RBRACK)) {
                varDef.rBrackList.add(getNextToken("]"));
            } else {
                varDef.rBrackList.add(this.tokenList.get(cur));
            }
        }
        if (isNext(1, TokenKey.ASSIGN)) {
            varDef.assign = getNextToken("=");
            varDef.initVal = analyseInitVal();
        }
        this.syntaxList.add("<" + SyntaxType.VarDef + ">");
        return varDef;
    }

    private InitVal analyseInitVal() {
        InitVal initVal = new InitVal();
        initVal.dim = 0;
        if (isNext(1, TokenKey.LBRACE)) {
            initVal.dim++;
            initVal.lBrace = getNextToken("{");
            if (!isNext(1, TokenKey.RBRACE)) {
                initVal.initVal = analyseInitVal();
                initVal.dim += initVal.initVal.dim;
                while (isNext(1, TokenKey.COMMA)) {
                    initVal.commaList.add(getNextToken(","));
                    initVal.initValList.add(analyseInitVal());
                }
            }
            initVal.rBrace = getNextToken("}");
        } else {
            initVal.exp = analyseExp();
        }
        this.syntaxList.add("<" + SyntaxType.InitVal + ">");
        return initVal;
    }

    private FuncDef analyseFuncDef() {
        FuncDef funcDef = new FuncDef();
        funcDef.funcType = analyseFuncType();
        funcDef.ident = analyseIdent();
        funcDef.lParent = getNextToken("(");
        if (!isNext(1, TokenKey.RPARENT) && !isNext(1, TokenKey.LBRACE)) {
            FuncFParams funcFParams = analyseFuncFParams();
            funcDef.funcFParams = funcFParams;
            funcDef.paramsNum = funcFParams.paramsNum;
            funcDef.paramsDimList = funcFParams.paramsDimList;
        }
        // j
        if (isNext(1, TokenKey.RPARENT)) {
            funcDef.rParent = getNextToken(")");
        } else {
            funcDef.rParent = this.tokenList.get(cur);
        }
        funcDef.block = analyseBlock();
        funcDef.setFuncType(funcDef.funcType);
        this.syntaxList.add("<" + SyntaxType.FuncDef + ">");
        return funcDef;
    }

    private MainFuncDef analyseMainFuncDef() {
        MainFuncDef mainFuncDef = new MainFuncDef();
        mainFuncDef.tokenInt = getNextToken("int");
        mainFuncDef.main = getNextToken("main");
        mainFuncDef.lParent = getNextToken("(");
        if (isNext(1, TokenKey.RPARENT)) {
            mainFuncDef.rParent = getNextToken(")");
        } else {
            mainFuncDef.rParent = this.tokenList.get(cur);
        }
        mainFuncDef.block = analyseBlock();
        mainFuncDef.setFuncType(FuncType.INT);
        this.syntaxList.add("<" + SyntaxType.MainFuncDef + ">");
        return mainFuncDef;
    }

    private FuncType analyseFuncType() {
        if (isNext(1, TokenKey.VOIDTK)) {
            getNextToken("void");
            this.syntaxList.add("<" + SyntaxType.FuncType + ">");
            return FuncType.VOID;
        } else if (isNext(1, TokenKey.INTTK)){
            getNextToken("int");
            this.syntaxList.add("<" + SyntaxType.FuncType + ">");
            return FuncType.INT;
        } else {
            return FuncType.ERROR;
        }
    }

    private FuncFParams analyseFuncFParams() {
        FuncFParams funcFParams = new FuncFParams();
        FuncFParam funcFParam0 = analyseFuncFParam();
        funcFParams.funcFParam = funcFParam0;
        funcFParams.paramsNum = 1;
        funcFParams.paramsDimList.add(funcFParam0.dim);
        while (isNext(1, TokenKey.COMMA)) {
            funcFParams.commaList.add(getNextToken(","));
            FuncFParam funcFParam = analyseFuncFParam();
            funcFParams.funcFParamList.add(funcFParam);
            funcFParams.paramsNum++;
            funcFParams.paramsDimList.add(funcFParam.dim);
        }
        this.syntaxList.add("<" + SyntaxType.FuncFParams + ">");
        return funcFParams;
    }

    private FuncFParam analyseFuncFParam() {
        FuncFParam funcFParam = new FuncFParam();
        funcFParam.dim = 0;
        funcFParam.btype = analyseBType();
        funcFParam.ident = analyseIdent();
        if (isNext(1, TokenKey.LBRACK)) {
            funcFParam.dim++;
            funcFParam.lBrack = getNextToken("[");
            if (isNext(1, TokenKey.RBRACK)) {
                funcFParam.rBrack = getNextToken("]");
            } else {
                funcFParam.rBrack = this.tokenList.get(cur);
            }
            while (isNext(1, TokenKey.LBRACK)) {
                funcFParam.dim++;
                funcFParam.lBrackList.add(getNextToken("["));
                funcFParam.constExpList.add(analyseConstExp());
                if (isNext(1, TokenKey.RBRACK)) {
                    funcFParam.rBrackList.add(getNextToken("]"));
                } else {
                    funcFParam.rBrackList.add(this.tokenList.get(cur));
                }
            }
        }
        this.syntaxList.add("<" + SyntaxType.FuncFParam + ">");
        return funcFParam;
    }

    private boolean isNextLValAssignGetint() {
        int i = 1;
        if (isNext(i, TokenKey.IDENFR)) {
            i++;
            if (isNext(i, TokenKey.LBRACK)) {
                i++;
                while (!isNext(i, TokenKey.ASSIGN) && !isNext(i, TokenKey.SEMICN) && cur + i < tokenList.size()) {
                    if (isNext(i, TokenKey.IDENFR) || isNext(i, TokenKey.INTCON)) {
                        if (isNext(i+1, TokenKey.IDENFR)
                            || isNext(i+1, TokenKey.INTCON)
                            || isNext(i+1, TokenKey.INTTK)
                            || isNext(i+1, TokenKey.CONSTTK)
                            || isNext(i+1, TokenKey.INTCON)
                            || isNext(i+1, TokenKey.LBRACE)
                            || isNext(i+1, TokenKey.IFTK)
                            || isNext(i+1, TokenKey.WHILETK)
                            || isNext(i+1, TokenKey.BREAKTK)
                            || isNext(i+1, TokenKey.CONTINUETK)
                            || isNext(i+1, TokenKey.RETURNTK)
                            || isNext(i+1, TokenKey.PRINTFTK)) {
                            return false;
                        }
                    }
                    i++;
                }
                if (isNext(i, TokenKey.SEMICN)) {
                    return false;
                }
            }
            if (isNext(i, TokenKey.ASSIGN)) {
                i++;
                if (isNext(i, TokenKey.GETINTTK)) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

    private boolean isNextLValAssignExp() {
        int i = 1;
        if (isNext(i, TokenKey.IDENFR)) {
            i++;
            if (isNext(i, TokenKey.LBRACK)) {
                i++;
                while (!isNext(i, TokenKey.ASSIGN) && !isNext(i, TokenKey.SEMICN) && cur + i < tokenList.size()) {
                    if (isNext(i, TokenKey.IDENFR) || isNext(i, TokenKey.INTCON)) {
                        if (isNext(i+1, TokenKey.IDENFR)
                                || isNext(i+1, TokenKey.INTCON)
                                || isNext(i+1, TokenKey.INTTK)
                                || isNext(i+1, TokenKey.CONSTTK)
                                || isNext(i+1, TokenKey.INTCON)
                                || isNext(i+1, TokenKey.LBRACE)
                                || isNext(i+1, TokenKey.IFTK)
                                || isNext(i+1, TokenKey.WHILETK)
                                || isNext(i+1, TokenKey.BREAKTK)
                                || isNext(i+1, TokenKey.CONTINUETK)
                                || isNext(i+1, TokenKey.RETURNTK)
                                || isNext(i+1, TokenKey.PRINTFTK)) {
                            return false;
                        }
                    }
                    i++;
                }
                if (isNext(i, TokenKey.SEMICN)) {
                    return false;
                }
            }
            if (isNext(i, TokenKey.ASSIGN)) {
                return true;
            }
            return false;
        } else {
            return false;
        }
    }

    private boolean isNextExp() {
        if (isNext(1, TokenKey.IDENFR)
                || isNext(1, TokenKey.LPARENT)
                || isNext(1, TokenKey.PLUS)
                || isNext(1, TokenKey.MINU)
                || isNext(1, TokenKey.NOT)
                || isNext(1, TokenKey.INTCON)) {
            return true;
        } else {
            return false;
        }
    }

    private Block analyseBlock() {
        Block block = new Block();
        block.lBrace = getNextToken("{");
        while (isNext(1, TokenKey.CONSTTK) || isNext(1, TokenKey.INTTK) //Decl
                //Stmt
                || isNext(1, TokenKey.LBRACE) //Block
                || isNext(1, TokenKey.IFTK) //if
                || isNext(1, TokenKey.WHILETK) //while
                || isNext(1, TokenKey.BREAKTK) //break
                || isNext(1, TokenKey.CONTINUETK) //continue
                || isNext(1, TokenKey.RETURNTK) //return
                || isNext(1, TokenKey.PRINTFTK) //printf
                || (isNext(1, TokenKey.SEMICN) //;
                || isNextExp())) { //Exp
            block.blockItemList.add(analyseBlockItem());
        }
        block.rBrace = getNextToken("}");
        this.syntaxList.add("<" + SyntaxType.Block + ">");
        return block;
    }

    private BlockItem analyseBlockItem() {
        BlockItem blockItem = new BlockItem();
        if (isNext(1, TokenKey.CONSTTK) || isNext(1, TokenKey.INTTK)) {
            blockItem.isDecl = true;
            blockItem.decl = analyseDecl();
        } else {
            blockItem.isDecl = false;
            blockItem.stmt = analyseStmt();
        }
        //this.syntaxList.add("<" + SysY.SyntaxAnalysis.SyntaxType.BlockItem + ">");
        return blockItem;
    }

    private Stmt analyseStmt() {
        Stmt stmt = new Stmt();
        if (isNext(1, TokenKey.LBRACE)) {
            stmt.stmtType = StmtType.BLOCK;
            stmt.block = analyseBlock();
        } else if (isNext(1, TokenKey.IFTK)) {
            stmt.stmtType = StmtType.IF;
            StmtIf stmtIf = new StmtIf();
            stmtIf.tokenIf = getNextToken("if");
            stmtIf.lParent = getNextToken("(");
            stmtIf.cond = analyseCond();
            // j
            if (isNext(1, TokenKey.RPARENT)) {
                stmtIf.rParent = getNextToken(")");
            } else {
                stmtIf.rParent = this.tokenList.get(cur);
            }
            stmtIf.stmt = analyseStmt();
            if (isNext(1, TokenKey.ELSETK)) {
                stmtIf.tokenElse = getNextToken("else");
                stmtIf.elseStmt = analyseStmt();
            }
            stmt.stmtIf = stmtIf;
        } else if (isNext(1, TokenKey.WHILETK)) {
            stmt.stmtType = StmtType.WHILE;
            StmtWhile stmtWhile = new StmtWhile();
            stmtWhile.tokenWhile = getNextToken("while");
            stmtWhile.lParent = getNextToken("(");
            stmtWhile.cond = analyseCond();
            // j
            if (isNext(1, TokenKey.RPARENT)) {
                stmtWhile.rParent = getNextToken(")");
            } else {
                stmtWhile.rParent = this.tokenList.get(cur);
            }
            stmtWhile.stmt = analyseStmt();
            stmt.stmtWhile = stmtWhile;
            stmt.setStmtType(StmtType.WHILE);
        } else if (isNext(1, TokenKey.BREAKTK)) {
            stmt.stmtType = StmtType.BREAK;
            StmtBreak stmtBreak = new StmtBreak();
            stmtBreak.tokenBreak = getNextToken("break");
            // i
            if (isNext(1, TokenKey.SEMICN)) {
                stmtBreak.semicn = getNextToken(";");
            } else {
                stmtBreak.semicn = this.tokenList.get(cur);
            }
            stmt.stmtBreak = stmtBreak;
        } else if (isNext(1, TokenKey.CONTINUETK)) {
            stmt.stmtType = StmtType.CONTINUE;
            StmtContinue stmtContinue = new StmtContinue();
            stmtContinue.tokenContinue = getNextToken("continue");
            // i
            if (isNext(1, TokenKey.SEMICN)) {
                stmtContinue.semicn = getNextToken(";");
            } else {
                stmtContinue.semicn = this.tokenList.get(cur);
            }
            stmt.stmtContinue = stmtContinue;
        } else if (isNext(1, TokenKey.RETURNTK)) {
            stmt.stmtType = StmtType.RETURN;
            StmtReturn stmtReturn = new StmtReturn();
            stmtReturn.tokenReturn = getNextToken("return");
            if (isNextExp()) {
                stmtReturn.exp = analyseExp();
            }
            // i
            if (isNext(1, TokenKey.SEMICN)) {
                stmtReturn.semicn = getNextToken(";");
            } else {
                stmtReturn.semicn = this.tokenList.get(cur);
            }
            stmt.stmtReturn = stmtReturn;
        } else if (isNext(1, TokenKey.PRINTFTK)) {
            stmt.stmtType = StmtType.PRINTF;
            StmtPrintf stmtPrintf = new StmtPrintf();
            stmtPrintf.printf = getNextToken("printf");
            stmtPrintf.lParent = getNextToken("(");
            stmtPrintf.formatString = getNextToken("formatString");
            while (isNext(1, TokenKey.COMMA)) {
                stmtPrintf.commaList.add(getNextToken(","));
                stmtPrintf.expList.add(analyseExp());
            }
            // j
            if (isNext(1, TokenKey.RPARENT)) {
                stmtPrintf.rParent = getNextToken(")");
            } else {
                stmtPrintf.rParent = this.tokenList.get(cur);
            }
            // i
            if (isNext(1, TokenKey.SEMICN)) {
                stmtPrintf.semicn = getNextToken(";");
            } else {
                stmtPrintf.semicn = this.tokenList.get(cur);
            }
            stmt.stmtPrintf = stmtPrintf;
        } else if (isNextLValAssignGetint()) {
            stmt.stmtType = StmtType.LVALGETINT;
            StmtLValGetint stmtLValGetint = new StmtLValGetint();
            stmtLValGetint.lVal = analyseLVal();
            stmtLValGetint.assign = getNextToken("=");
            stmtLValGetint.getint = getNextToken("getint");
            stmtLValGetint.lParent = getNextToken("(");
            // j
            if (isNext(1, TokenKey.RPARENT)) {
                stmtLValGetint.rParent = getNextToken(")");
            } else {
                stmtLValGetint.rParent = this.tokenList.get(cur);
            }
            // i
            if (isNext(1, TokenKey.SEMICN)) {
                stmtLValGetint.semicn = getNextToken(";");
            } else {
                stmtLValGetint.semicn = this.tokenList.get(cur);
            }
            stmt.stmtLValGetint = stmtLValGetint;
        } else if (isNextLValAssignExp()) {
            stmt.stmtType = StmtType.LVALASSIGN;
            StmtLValAssign stmtLValAssign = new StmtLValAssign();
            stmtLValAssign.lVal = analyseLVal();
            stmtLValAssign.assign = getNextToken("=");
            stmtLValAssign.exp = analyseExp();
            // i
            if (isNext(1, TokenKey.SEMICN)) {
                stmtLValAssign.semicn = getNextToken(";");
            } else {
                stmtLValAssign.semicn = this.tokenList.get(cur);
            }
            stmt.stmtLValAssign = stmtLValAssign;
        } else if (isNextExp()) {
            stmt.stmtType = StmtType.EXP;
            stmt.exp = analyseExp();
            // i
            if (isNext(1, TokenKey.SEMICN)) {
                stmt.semicn = getNextToken(";");
            } else {
                stmt.semicn = this.tokenList.get(cur);
            }
        } else {
            stmt.stmtType = StmtType.SEMICN;
            // i
            if (isNext(1, TokenKey.SEMICN)) {
                stmt.semicn = getNextToken(";");
            } else {
                stmt.semicn = this.tokenList.get(cur);
            }
        }
        this.syntaxList.add("<" + SyntaxType.Stmt + ">");
        return stmt;
    }
    private Exp analyseExp() {
        Exp exp = new Exp();
        exp.addExp = analyseAddExp();
        this.syntaxList.add("<" + SyntaxType.Exp + ">");
        return exp;
    }

    private Cond analyseCond() {
        Cond cond = new Cond();
        cond.lOrExp = analyseLOrExp();
        this.syntaxList.add("<" + SyntaxType.Cond + ">");
        return cond;
    }

    private LVal analyseLVal() {
        LVal lVal = new LVal();
        lVal.dim = 0;
        lVal.ident = analyseIdent();
        while (isNext(1, TokenKey.LBRACK)) {
            lVal.dim++;
            lVal.lBrackList.add(getNextToken("["));
            lVal.expList.add(analyseExp());
            if (isNext(1, TokenKey.RBRACK)) {
                lVal.rBrackList.add(getNextToken("]"));
            } else {
                lVal.rBrackList.add(this.tokenList.get(cur));
            }
        }
        this.syntaxList.add("<" + SyntaxType.LVal + ">");
        return lVal;
    }

    private PrimaryExp analysePrimaryExp() {
        PrimaryExp primaryExp = new PrimaryExp();
        if (isNext(1, TokenKey.LPARENT)) {
            primaryExp.primaryExpType = PrimaryExpType.EXP;
            primaryExp.lParent = getNextToken("(");
            primaryExp.exp = analyseExp();
            if (isNext(1, TokenKey.RPARENT)) {
                primaryExp.rParent = getNextToken(")");
            } else {
                primaryExp.rParent = this.tokenList.get(cur);
            }
        } else if (isNext(1, TokenKey.IDENFR)) {
            primaryExp.primaryExpType = PrimaryExpType.LVAL;
            primaryExp.lVal = analyseLVal();
        } else if (isNext(1, TokenKey.INTCON)) {
            primaryExp.primaryExpType = PrimaryExpType.NUMBER;
            primaryExp.pNumber = analyseNumber();
        } else {
            primaryExp.primaryExpType = PrimaryExpType.ERROR;
            System.out.println("PrimaryExp wrong!");
        }
        this.syntaxList.add("<" + SyntaxType.PrimaryExp + ">");
        return primaryExp;
    }

    private PNumber analyseNumber() {
        PNumber pNumber = new PNumber();
        pNumber.intConst = getNextToken("intConst");
        this.syntaxList.add("<" + SyntaxType.Number + ">");
        return pNumber;
    }

    private UnaryExp analyseUnaryExp() {
        UnaryExp unaryExp = new UnaryExp();
        if (isNext(1, TokenKey.LPARENT)
                || (isNext(1, TokenKey.IDENFR) && !isNext(2, TokenKey.LPARENT))
                || isNext(1, TokenKey.INTCON)) {
            unaryExp.unaryExpType = UnaryExpType.PRIMARYEXP;
            unaryExp.primaryExp = analysePrimaryExp();
        } else if (isNext(1, TokenKey.IDENFR) && isNext(2, TokenKey.LPARENT)) {
            unaryExp.unaryExpType = UnaryExpType.IDENT;
            unaryExp.ident = analyseIdent();
            unaryExp.lParent = getNextToken("(");
            if (isNextExp()) {
                unaryExp.funcRParams = analyseFuncRParams();
            }
            // j
            if (isNext(1, TokenKey.RPARENT)) {
                unaryExp.rParent = getNextToken(")");
            } else {
                unaryExp.rParent = this.tokenList.get(cur);
            }
        } else if (isNext(1, TokenKey.PLUS) || isNext(1, TokenKey.MINU) || isNext(1, TokenKey.NOT)) {
            unaryExp.unaryExpType = UnaryExpType.UNARYEXP;
            unaryExp.unaryOp = analyseUnaryOp();
            unaryExp.unaryExp = analyseUnaryExp();
        } else {
            unaryExp.unaryExpType = UnaryExpType.ERROR;
            System.out.println("unaryExp wrong");
        }
        this.syntaxList.add("<" + SyntaxType.UnaryExp + ">");
        return unaryExp;
    }

    private UnaryOp analyseUnaryOp() {
        if (isNext(1, TokenKey.PLUS)) {
            getNextToken("+");
            this.syntaxList.add("<" + SyntaxType.UnaryOp + ">");
            return UnaryOp.PLUS;
        } else if (isNext(1, TokenKey.MINU)) {
            getNextToken("-");
            this.syntaxList.add("<" + SyntaxType.UnaryOp + ">");
            return UnaryOp.MINU;
        } else if (isNext(1, TokenKey.NOT)) {
            getNextToken("!");
            this.syntaxList.add("<" + SyntaxType.UnaryOp + ">");
            return UnaryOp.NOT;
        } else {
            System.out.println("unaryExp wrong");
            return UnaryOp.ERROR;
        }
    }

    private FuncRParams analyseFuncRParams() {
        FuncRParams funcRParams = new FuncRParams();
        funcRParams.exp = analyseExp();
        while (isNext(1, TokenKey.COMMA)) {
            funcRParams.commaList.add(getNextToken(","));
            Exp exp = analyseExp();
            funcRParams.expList.add(exp);
        }
        this.syntaxList.add("<" + SyntaxType.FuncRParams + ">");
        return funcRParams;
    }

    private MulExp analyseMulExp() {
        MulExp mulExp = new MulExp();
        mulExp.unaryExp = analyseUnaryExp();
        this.syntaxList.add("<" + SyntaxType.MulExp + ">");
        while (isNext(1, TokenKey.MULT) || isNext(1, TokenKey.DIV) || isNext(1, TokenKey.MOD)) {
            if (isNext(1, TokenKey.MULT)) {
                getNextToken("*");
                mulExp.mulOpList.add(MulOp.MULT);
            } else if (isNext(1, TokenKey.DIV)) {
                getNextToken("/");
                mulExp.mulOpList.add(MulOp.DIV);
            } else if (isNext(1, TokenKey.MOD)) {
                getNextToken("%");
                mulExp.mulOpList.add(MulOp.MOD);
            } else {
                System.out.println("MulExp wrong");
                mulExp.mulOpList.add(MulOp.ERROR);
            }
            mulExp.unaryExpList.add(analyseUnaryExp());
            this.syntaxList.add("<" + SyntaxType.MulExp + ">");
        }
        return mulExp;
    }

    private AddExp analyseAddExp() {
        AddExp addExp = new AddExp();
        addExp.mulExp = analyseMulExp();
        this.syntaxList.add("<" + SyntaxType.AddExp + ">");
        while (isNext(1, TokenKey.PLUS) || isNext(1, TokenKey.MINU)) {
            if (isNext(1, TokenKey.PLUS)) {
                getNextToken("+");
                addExp.addOpList.add(AddOp.PLUS);
            } else if (isNext(1, TokenKey.MINU)) {
                getNextToken("-");
                addExp.addOpList.add(AddOp.MINU);
            } else {
                System.out.println("AddExp wrong");
                addExp.addOpList.add(AddOp.ERROR);
            }
            addExp.mulExpList.add(analyseMulExp());
            this.syntaxList.add("<" + SyntaxType.AddExp + ">");
        }
        return addExp;
    }

    private RelExp analyseRelExp() {
        RelExp relExp = new RelExp();
        relExp.addExp = analyseAddExp();
        this.syntaxList.add("<" + SyntaxType.RelExp + ">");
        while (isNext(1, TokenKey.LSS) || isNext(1, TokenKey.GRE)
                || isNext(1, TokenKey.LEQ) || isNext(1, TokenKey.GEQ)) {
            if (isNext(1, TokenKey.LSS)) {
                getNextToken("<");
                relExp.relOpList.add(RelOp.LSS);
            } else if (isNext(1, TokenKey.GRE)) {
                getNextToken(">");
                relExp.relOpList.add(RelOp.GRE);
            } else if (isNext(1, TokenKey.LEQ)) {
                getNextToken("<=");
                relExp.relOpList.add(RelOp.LEQ);
            } else if (isNext(1, TokenKey.GEQ)) {
                getNextToken(">=");
                relExp.relOpList.add(RelOp.GEQ);
            } else {
                System.out.println("RelExp wrong");
                relExp.relOpList.add(RelOp.ERROR);
            }
            relExp.addExpList.add(analyseAddExp());
            this.syntaxList.add("<" + SyntaxType.RelExp + ">");
        }
        return relExp;
    }

    private EqExp analyseEqExp() {
        EqExp eqExp = new EqExp();
        eqExp.relExp = analyseRelExp();
        this.syntaxList.add("<" + SyntaxType.EqExp + ">");
        while (isNext(1, TokenKey.EQL) || isNext(1, TokenKey.NEQ)) {
            if (isNext(1, TokenKey.EQL)) {
                getNextToken("==");
                eqExp.eqOpList.add(EqOp.EQL);
            } else if (isNext(1, TokenKey.NEQ)) {
                getNextToken("!=");
                eqExp.eqOpList.add(EqOp.NEQ);
            } else {
                System.out.println("EqExp wrong");
                eqExp.eqOpList.add(EqOp.ERROR);
            }
            eqExp.relExpList.add(analyseRelExp());
            this.syntaxList.add("<" + SyntaxType.EqExp + ">");
        }
        return eqExp;
    }

    private LAndExp analyseLAndExp() {
        LAndExp lAndExp = new LAndExp();
        lAndExp.eqExp = analyseEqExp();
        this.syntaxList.add("<" + SyntaxType.LAndExp + ">");
        while (isNext(1, TokenKey.AND)) {
            getNextToken("&&");
            lAndExp.andOpList.add(LAndOp.AND);
            lAndExp.eqExpList.add(analyseEqExp());
            this.syntaxList.add("<" + SyntaxType.LAndExp + ">");
        }
        return lAndExp;
    }

    private LOrExp analyseLOrExp() {
        LOrExp lOrExp = new LOrExp();
        lOrExp.lAndExp = analyseLAndExp();
        this.syntaxList.add("<" + SyntaxType.LOrExp + ">");
        while (isNext(1, TokenKey.OR)) {
            getNextToken("||");
            lOrExp.orOpList.add(LOrOp.OR);
            lOrExp.lAndExpList.add(analyseLAndExp());
            this.syntaxList.add("<" + SyntaxType.LOrExp + ">");
        }
        return lOrExp;
    }

    private ConstExp analyseConstExp() {
        ConstExp constExp = new ConstExp();
        constExp.addExp = analyseAddExp();
        this.syntaxList.add("<" + SyntaxType.ConstExp + ">");
        return constExp;
    }
}
