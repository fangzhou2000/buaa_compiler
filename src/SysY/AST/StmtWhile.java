package SysY.AST;

import SysY.ErrorHandle.ErrorType;
import SysY.LexicAnalysis.Token;
import SysY.LexicAnalysis.TokenKey;
import SysY.SemanticAnalysis.Quadruple;
import SysY.SemanticAnalysis.QuadrupleOp;

public class StmtWhile extends Node {
    public Token tokenWhile;
    public Token lParent;
    public Cond cond;
    public Token rParent;
    public Stmt stmt;

    public void setFuncType(FuncType funcType) {
        this.stmt.setFuncType(funcType);
    }

    public void setStmtType(StmtType stmtType) {
        this.stmt.setStmtType(stmtType);
    }

    public void errorCheck() {
        this.cond.errorCheck();
        // j
        this.errorCheckMissRparent();
        this.stmt.errorCheck();
    }

    // j
    public void errorCheckMissRparent() {
        if (this.rParent.getTokenKey() != TokenKey.RPARENT) {
            Node.errorHandle.addError(this.rParent.getTokenPos(), ErrorType.j);
        }
    }

    public void semanticAnalyse() {
        if (!Node.whileOpt) {
            /*
            loop:
            if !<cond> goto loopEnd:
            loopBegin:
            <stmt>
            goto loop
            loopEnd:
            ...
             */
            String labelLoop = Node.intermediate.generateLoop();
            String labelLoopBegin = Node.intermediate.generateLoopBegin();
            String labelLoopEnd = Node.intermediate.generateLoopEnd();
            // loop:
            Quadruple quadruple1 = new Quadruple("", QuadrupleOp.LABEL, labelLoop, "");
            Node.intermediate.addIntermediateCode(quadruple1);
            // if
            this.cond.semanticAnalyse(labelLoopBegin, labelLoopEnd);
            // loopBegin:
            Quadruple quadruple2 = new Quadruple("", QuadrupleOp.LABEL, labelLoopBegin, "");
            Node.intermediate.addIntermediateCode(quadruple2);
            // stmt
            String lastLoop = Node.intermediate.getLastLoop();
            String lastLoopEnd = Node.intermediate.getLastLoopEnd();
            Node.intermediate.setLabelLastLoop(labelLoop);
            Node.intermediate.setLabelLastLoopEnd(labelLoopEnd);
            this.stmt.semanticAnalyse();
            Node.intermediate.setLabelLastLoop(lastLoop);
            Node.intermediate.setLabelLastLoopEnd(lastLoopEnd);
            Quadruple quadruple3 = new Quadruple("", QuadrupleOp.GOTO, labelLoop, "");
            Node.intermediate.addIntermediateCode(quadruple3);
            // loopEnd:
            Quadruple quadruple4 = new Quadruple("", QuadrupleOp.LABEL, labelLoopEnd, "");
            Node.intermediate.addIntermediateCode(quadruple4);
        } else {
            /*
            goto loop
            loopBegin:
            <stmt>
            loop:
            if cond goto loopBegin
            loopEnd:
            ...
             */
            Quadruple quadruple0 = new Quadruple("", QuadrupleOp.WHILE_BEGIN, "", "");
            Node.intermediate.addIntermediateCode(quadruple0);

            String labelLoop = Node.intermediate.generateLoop();
            String labelLoopBegin = Node.intermediate.generateLoopBegin();
            String labelLoopEnd = Node.intermediate.generateLoopEnd();
            // goto loop
            Quadruple quadruple1 = new Quadruple("", QuadrupleOp.GOTO, labelLoop, "");
            Node.intermediate.addIntermediateCode(quadruple1);
            // loopBegin:
            Quadruple quadruple2 = new Quadruple("", QuadrupleOp.LABEL, labelLoopBegin, "");
            Node.intermediate.addIntermediateCode(quadruple2);
            // stmt
            String lastLoop = Node.intermediate.getLastLoop();
            String lastLoopEnd = Node.intermediate.getLastLoopEnd();
            Node.intermediate.setLabelLastLoop(labelLoop);
            Node.intermediate.setLabelLastLoopEnd(labelLoopEnd);
            this.stmt.semanticAnalyse();
            Node.intermediate.setLabelLastLoop(lastLoop);
            Node.intermediate.setLabelLastLoopEnd(lastLoopEnd);
            // loop
            Quadruple quadruple3 = new Quadruple("", QuadrupleOp.LABEL, labelLoop, "");
            Node.intermediate.addIntermediateCode(quadruple3);
            // if
            this.cond.semanticAnalyseOpt(labelLoopBegin, labelLoopEnd);
            // loopEnd:
            Quadruple quadruple4 = new Quadruple("", QuadrupleOp.LABEL, labelLoopEnd, "");
            Node.intermediate.addIntermediateCode(quadruple4);

            Quadruple quadruple5 = new Quadruple("", QuadrupleOp.WHILE_END, "", "");
            Node.intermediate.addIntermediateCode(quadruple5);
        }
    }
}
