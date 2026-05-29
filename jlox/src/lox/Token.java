package lox;

enum TokenType {
    // Single char token types
    L_PAREN, R_PAREN,
    L_BRACKET, R_BRACKET,
    L_BRACE, R_BRACE,
    COMMA, PERIOD, PLUS, MINUS, SEMICOLON, SLASH, STAR,

    // Comparatives
    EXCL, EXCL_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    // Literals
    IDENTIFIER, STRING, NUMBER,

    // Keywords
    AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR,
    PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,

    EOF
}

public class Token {
    final TokenType type; 
    final int lineNum; 
    final String lexeme; // actual string representation of code
    final Object literal; // runtime representation of the code

    Token(TokenType type, int lineNum, String lexeme, Object literal) {
        this.type = type;
        this.lineNum = lineNum;
        this.lexeme = lexeme;
        this.literal = literal;
    }

    public String toString() {
       return type + " " + lexeme + " " + literal; 
    }
}
