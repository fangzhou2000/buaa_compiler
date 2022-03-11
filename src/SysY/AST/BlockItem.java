package SysY.AST;

public class BlockItem extends Node {
    public boolean isDecl;
    public Decl decl;
    public Stmt stmt;

    public BlockItem() {
        this.isDecl = true;
        this.decl = null;
        this.stmt = null;
    }

    public void setFuncType(FuncType funcType) {
        if (!this.isDecl) {
            this.stmt.setFuncType(funcType);
        }
    }

    public void setStmtType(StmtType stmtType) {
        if (!this.isDecl) {
            this.stmt.setStmtType(stmtType);
        }
    }

    public void errorCheck() {
        if (this.isDecl) {
            this.decl.errorCheck();
        } else {
            this.stmt.errorCheck();
        }
    }

    public void semanticAnalyse() {
        if (isDecl) {
            this.decl.semanticAnalyse();
        } else {
            this.stmt.semanticAnalyse();
        }
    }
}
