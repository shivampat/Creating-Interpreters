package com.shivam.lox;

import java.util.ArrayList;
import java.util.List;

import static com.shivam.lox.TokenType.*;

class Parser {
    private final List<Token> tokens;
    private int current = 0;


    private static class ParseError extends RuntimeException {}

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Expr comma() {
        Expr left = ternary();

        while (match(COMMA)) {
            Token operator = previous();
            Expr right = ternary();
            left = new Expr.Binary(left, operator, right);
        }

        return left;
    }

    private Expr ternary() {
        // a ? b : c
        Expr a = expression();

        if (match(QUESTION_MARK)) {
            Token ternTok = previous();
            Expr b = ternary();
            consume(COLON, "Expected : after ? to complete ternary operator.");
            Expr c = ternary();
            a = new Expr.Ternary(a, ternTok, b, c);
        }

        return a;
    }

    private Expr expression() {
        return assignment();
    }

    private Expr equality() {
        if (match(EXCL_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            comparison();
            throw error(operator, "Expected comparison before " + operator.lexeme + " operator.");
        }

        Expr left = comparison();

        while (match(EXCL_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            left = new Expr.Binary(left, operator, right);
        }

        return left;
    }

    private Expr comparison() {
        if (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            term();
            throw error(operator, "Expected term before " + operator.lexeme + " operator.");
        }

        Expr left = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            left = new Expr.Binary(left, operator, right);
        }

        return left;
    }

    private Expr term() {
        if (match(PLUS)) {
            Token operator = previous();
            factor();
            throw error(operator, "Expected factor before " + operator.lexeme + " operator.");
        }

        Expr left = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();

            left = new Expr.Binary(left, operator, right);
        }

        return left;
    }

    private Expr factor() {
        if (match(SLASH, STAR)) {
            Token operator = previous();
            unary();
            throw error(operator, "Expected unary before " + operator.lexeme + " operator.");
        }

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
        if (match(IDENTIFIER))
            return new Expr.Variable(previous());
        if (match(L_PAREN)) {
            Expr expr = expression();
            consume(R_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect an expression.");
        // return null; // return null expression if token not found.
    }

    // Expr parse() {
    //     try {
    //         return comma();
    //     } 
    //     catch (ParseError error) {
    //         return null;
    //     }
    // }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

    private Stmt declaration() {
        try {
            if (match(VAR)) return varDeclaration();
            return statement();
        }
        catch (ParseError pe) {
            synchronize();
            return null;
        }

    }

    private Stmt statement() {
        if (match(PRINT)) return printStatement();
        if (match(IF)) return ifStatement();
        if (match(L_BRACE)) return new Stmt.Block(block());

        return expressionStatement();
    }

    private Stmt ifStatement() {
        consume(L_PAREN, "Expected ( after if statement!");
        Expr condition = expression();
        consume(R_PAREN, "Expected ) after if statement condition!");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;

        if (match(ELSE)) elseBranch = statement();

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expect variable name after 'var' keyword!");

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = comma();
        }
        
        consume(SEMICOLON, "Expect ; after variable declaration statement.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt printStatement() {
        Expr expression = expression();
        consume(SEMICOLON, "Expect ; at the end of expression!");
        return new Stmt.Print(expression);
    }

    private Stmt expressionStatement() {
        Expr expression = expression();
        consume(SEMICOLON, "Expect ; at the end of expression!");
        return new Stmt.Expression(expression);
    }
    
    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();    

        while (!isAtEnd() && !check(R_BRACE)) {
            statements.add(declaration());
        }

        consume(R_BRACE, "Expected a '}' after '{'.");
        return statements;
    }

    private Expr assignment() {
        Expr lval = equality();

        if (match(EQUAL)) {
            Token equals = previous();
            Expr rval = equality();

            if (lval instanceof Expr.Variable) {
                Token name = ((Expr.Variable) lval).name;
                return new Expr.Assign(name, rval);
            }

            error(equals, "Invalid assignment target.");
        }

        return lval;
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
