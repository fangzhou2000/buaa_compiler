package SysY.ErrorHandle;
import SysY.LexicAnalysis.TokenPos;

public class ComplierError {
    private TokenPos tokenPos;
    private ErrorType errorType;

    @Override
    public String toString() {
        return tokenPos.getRow() + 1 + " " + errorType;
    }

    public void setTokenPos(TokenPos tokenPos) {
        this.tokenPos = tokenPos;
    }

    public void setErrorType(ErrorType errorType) {
        this.errorType = errorType;
    }
}
