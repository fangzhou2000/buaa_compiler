package SysY.ErrorHandle;

import SysY.AST.Node;
import SysY.LexicAnalysis.TokenPos;

import java.util.ArrayList;

public class ErrorHandle {
    private ArrayList<ComplierError> errorList;
    private int errorCount;

    public ErrorHandle() {
        this.errorList = new ArrayList<>();
        this.errorCount = 0;
    }

    public ArrayList<ComplierError> getErrorList() {
        return errorList;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void addError(TokenPos tokenPos, ErrorType errorType) {
        ComplierError error = new SyntaxError();
        error.setTokenPos(tokenPos);
        error.setErrorType(errorType);
        this.errorList.add(error);
        this.errorCount++;
    }
}
