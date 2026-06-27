package com.shivam.lox;

import java.util.List;

import static com.shivam.lox.TokenType.*;

class Parser {
    private final List<Token> tokens;
    private int current = 0;


    private static class ParseError extends RuntimeException {}

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Expr expression() {
        return equality();
    }

    private Expr equality() {
        Expr left = comparison();

        while (match(EXCL_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            left = new Expr.Binary(left, operator, right);
        }

        return left;
    }

    private Expr comparison() {
        Expr left = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            left = new Expr.Binary(left, operator, right);
        }

        return left;
    }

    private Expr term() {
        Expr left = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            left = new Expr.Binary(left, operator, right);
        }

        return left;
    }

    private Expr factor() {
        Expr left = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            left = new Expr.Binary(left, operator, right);
        }

        return left;
    }

    private Expr unary() {
        if (match(EXCL, MINUS)) {
            Token operator = previous();
            return new Expr.Unary(operator, unary());
        }
        return primary();
    }

    private Expr primary() {
        if (match(FALSE))
            return new Expr.Literal(false);
        if (match(TRUE))
            return new Expr.Literal(true);
        if (match(NIL))
            return new Expr.Literal(null);
        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }
        if (match(L_PAREN)) {
            Expr expr = expression();
            consume(R_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect an expression.");
    }

    Expr parse() {
        try {
            return expression();
        } 
        catch (ParseError error) {
            return null;
        }
    }
    
    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;

            switch(peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case IF:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }

    private Token consume(TokenType type, String errorMessage) {
        if (check(type)) return advance();
        throw error(peek(), errorMessage);
    }

    private ParseError error(Token token, String errorMessage) {
        Lox.error(token, errorMessage);
        return new ParseError();
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd())
            return false;
        return peek().type == type;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token advance() {
        if (!isAtEnd())
            current++;
        return previous();
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }
}
