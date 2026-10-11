#ifndef clox_scanner_h
#define clox_scanner_h

typedef enum {
    // Single-character token types
    TOKEN_L_PAREN,
    TOKEN_R_PAREN,
    TOKEN_L_BRACKET,
    TOKEN_R_BRACKET,
    TOKEN_L_BRACE,
    TOKEN_R_BRACE,
    TOKEN_COMMA,
    TOKEN_PERIOD,
    TOKEN_PLUS,
    TOKEN_MINUS,
    TOKEN_SEMICOLON,
    TOKEN_SLASH,
    TOKEN_STAR,
    TOKEN_QUESTION_MARK,
    TOKEN_COLON,

    // Comparatives & Equality
    TOKEN_EXCL,
    TOKEN_EXCL_EQUAL,
    TOKEN_EQUAL,
    TOKEN_EQUAL_EQUAL,
    TOKEN_GREATER,
    TOKEN_GREATER_EQUAL,
    TOKEN_LESS,
    TOKEN_LESS_EQUAL,

    // Literals
    TOKEN_IDENTIFIER,
    TOKEN_STRING,
    TOKEN_NUMBER,

    // Keywords
    TOKEN_AND,
    TOKEN_CLASS,
    TOKEN_ELSE,
    TOKEN_FALSE,
    TOKEN_FUN,
    TOKEN_FOR,
    TOKEN_IF,
    TOKEN_NIL,
    TOKEN_OR,
    TOKEN_PRINT,
    TOKEN_RETURN,
    TOKEN_SUPER,
    TOKEN_THIS,
    TOKEN_TRUE,
    TOKEN_VAR,
    TOKEN_WHILE,
    TOKEN_BREAK,
    TOKEN_CONTINUE,

    TOKEN_EOF,
    TOKEN_ERROR
} TokenType;

typedef struct {
    TokenType type;
    const char* start;
    int length;
    int line;
} Token;

void initScanner(const char* source);
Token scanToken();

#endif