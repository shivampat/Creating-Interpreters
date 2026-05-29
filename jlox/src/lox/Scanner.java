package lox;

import java.util.ArrayList;
import java.util.List;

import static lox.TokenType.*;

public class Scanner {
    private final String source;
    private final List<Token> tokenList = new ArrayList<Token>();

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



            default:
                Lox.error(line, "Unexpected character!");
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




}
