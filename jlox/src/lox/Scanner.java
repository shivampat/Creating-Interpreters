package lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static lox.TokenType.*;



public class Scanner {
    private final String source;
    private final List<Token> tokenList = new ArrayList<Token>();
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("fun",    FUN);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
    }


    private int line = 1; 
    private int start = 0;
    private int current = 0;

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // create Token object and assign char or chars (string) to TokenType 
            // tokenList.add(new Token());
            start = current;
            scanToken();
        }

        // End off with the EOF token
        tokenList.add(new Token(TokenType.EOF, line, "", null));
        return tokenList;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(L_PAREN); break;
            case ')': addToken(R_PAREN); break;
            case '{': addToken(L_BRACE); break;
            case '}': addToken(R_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(PERIOD); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            case '!': addToken(match('=') ? EXCL_EQUAL : EXCL); break;
            case '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
            case '>': addToken(match('=') ? GREATER_EQUAL : GREATER); break;
            case '<': addToken(match('=') ? LESS_EQUAL : LESS); break;
            
            // check if a slash command is actually a comment
            case '/':
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance();
                }
                else if (match('*')) {
                    while (peek() != '*' && peekNext() != '/' && !isAtEnd()) {
                        if (peek() == '\n') line++;
                        advance();
                    }
                    
                    if (peek() == '*' && peekNext() == '/') {
                        advance();
                        advance();
                    }
                    else {
                        Lox.error(line, "Unclosed comment!");
                    }
                }
                else {
                    addToken(SLASH);
                }
                break;
            
            // ignore whitespaces
            case ' ':
            case '\r':
            case '\t':
                break;
            
            // end of line
            case '\n':
                line++;
                break;
            
            // string literals
            case '"':
                string();
                break;
            default:
                // check for number
                if (isDigit(c)) {
                   number();         
                }
                else if (isAlpha(c)) {
                    identifier();
                }
                else {
                    Lox.error(line, "Unexpected character!");
                }
                break;
        }
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String lexeme = source.substring(start, current);
        tokenList.add(new Token(type, line, lexeme, literal));
    }

    private boolean match(char next) {
        if (isAtEnd() || source.charAt(current) != next) return false;
        
        // consume next char if they do match
        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string!");
            return;
        }

        // closing "
        advance();

        String val = source.substring(start + 1, current - 1);
        addToken(STRING, val);
    }

    private void number() {
        // 1122
        // 10000.0101
        // 1000. 
        // .1000
        while (isDigit(peek())) advance();

        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek())) advance();
        }

        double val = Double.parseDouble(source.substring(start, current));
        addToken(NUMBER, val);
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        
        String idenText = source.substring(start, current);
        TokenType type = keywords.get(idenText);
        if (type == null) type = IDENTIFIER;
        addToken(type);

    }

    private boolean isDigit(char c) {
        return '0' <= c && c <= '9'; 
    }

    private boolean isAlpha(char c) {
        return ('a' <= c && c <= 'z') ||
               ('A' <= c && c <= 'Z') ||
               c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
}
