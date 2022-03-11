package SysY.LexicAnalysis;

public class Token {
    private String tokenString;
    private TokenKey tokenKey;
    private TokenPos tokenPos;

    public Token(TokenKey tokenKey, String tokenString, TokenPos tokenPos){
        this.tokenKey = tokenKey;
        this.tokenString = tokenString;
        this.tokenPos = tokenPos;
    }

    public String getTokenString() {
        return tokenString;
    }

    public TokenKey getTokenKey() {
        return tokenKey;
    }

    public TokenPos getTokenPos() {
        return tokenPos;
    }

    public void setTokenKey(TokenKey tokenKey) {
        this.tokenKey = tokenKey;
    }
}
