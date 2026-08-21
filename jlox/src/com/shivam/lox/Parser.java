package com.shivam.lox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.shivam.lox.Expr.Literal;
import com.shivam.lox.Stmt.Function;

import static com.shivam.lox.TokenType.*;

class Parser {
    private final List<Token> tokens;
    private int current = 0;
    private boolean inLoop = false;


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
        return call();
    }

    private Expr call() {
        Expr expr = primary();

        while (true) {
            if (match(L_PAREN)) {
                expr = finishExpr(expr);
            }
            else {
                break;
            }
        }

        return expr;
    }


    private Expr finishExpr(Expr expr) {
        List<Expr> args = new ArrayList<>();
        
        if (!check(R_PAREN)) {
            do {
                if (args.size() >= 255) {
                    error(peek(), "Cannot have more than 255 arguments in a function call!");
                }
                args.add(expression());
            } 
            while(match(COMMA));
        }
        
        Token paren = consume(R_PAREN, "Expected ) to finish argument list for function call!");
        return new Expr.Call(expr, paren, args);
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
            if (match(FUN)) return function("function");
            return statement();
        }
        catch (ParseError pe) {
            synchronize();
            return null;
        }

    }

    private Function function(String kind) {
        Token name = consume(IDENTIFIER, "Expect " + kind + " name!");
        consume(L_PAREN, "Expect ( after " + kind + " name!");
        List<Token> params = new ArrayList<>();
        if (!check(R_PAREN)) {
            if (params.size() >= 255) {
                error(peek(), "Cannot have more than 255 parameters inside " + kind + " definition!");
            }
            do {
                params.add(
                    consume(IDENTIFIER, "Expect parameter name!")
                );
            }
            while (match(COMMA));
        }
        consume(R_PAREN, "Expect ) after parameters!");
        consume(L_BRACE, "Expect { before " + kind + " body!");
        List<Stmt> body = block();
        return new Function(name, params, body);
    }

    private Stmt returnStatement() {
        // parsed in match() statement to find return statement
        Token returnTok = previous();
        Expr value = null;
        
        if (!check(SEMICOLON)) {
            value = expression();
        }
        consume(SEMICOLON, "Expected ; after return statement!");
        return new Stmt.Return(returnTok, value);

    }

    private Stmt statement() {
        if (match(PRINT)) return printStatement();

        boolean prev = inLoop;
        inLoop = true;

        try {
            if (match(WHILE)) return whileStatement();
            if (match(FOR)) return forStatement();
        }
        finally {
            inLoop = prev;
        }


        if (match(IF)) return ifStatement();
        if (match(BREAK)) return breakStatement();
        if (match(RETURN)) return returnStatement();
        if (match(CONTINUE)) return continueStatement();
        if (match(L_BRACE)) return new Stmt.Block(block());

        return expressionStatement();
    }

    private Stmt breakStatement() {
        Token breakTok = previous();
        if (inLoop){
            consume(SEMICOLON, "Expect ; after break statement.");
            return new Stmt.Break(breakTok);
        }
        
        throw error(breakTok, "Cannot have a break statement that is not in a loop!");
    }

    private Stmt continueStatement() {
        Token continueTok = previous();
        if (inLoop) {
            consume(SEMICOLON, "Expect ; after continue statement.");
            return new Stmt.Continue(continueTok);
        }

        throw error(continueTok, "Cannot have a continue statement that is not in a loop!");
    }

    private Stmt forStatement() {
        consume(L_PAREN, "Expected ( after if statement!");
        Stmt initializer;
        if (match(SEMICOLON)) {
            initializer = null;
        }
        // these all consume the semicolon so we don't need to worry about that
        else if (match(VAR)) {
            initializer = varDeclaration();
        }
        else {
            initializer = expressionStatement();
        }

        Expr condition = new Literal(true);
        if (!check(SEMICOLON)) {
            condition = expression();
        }
        consume(SEMICOLON,"Expected ; after loop condition!");

        Expr increment = null;
        if (!check(R_PAREN)) {
            increment = expression();
        }
        consume(R_PAREN, "Expected ) after for loop clause!");

        Stmt body = statement();

        if (increment != null) {
            body = new Stmt.Block(
                Arrays.asList(
                new Stmt.Expression(increment),
                body
            ));
        }

        body = new Stmt.While(condition, body);

        if (initializer != null) {
            body = new Stmt.Block(
                Arrays.asList(
                    initializer,
                    body
                )
            );
        }

        return body;
        
    }

    private Stmt whileStatement() {
        consume(L_PAREN, "Expected ( after if statement!");
        Expr condition = expression();
        consume(R_PAREN, "Expected ) after if statement condition!");

        Stmt body = statement();

        return new Stmt.While(condition, body);

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
        Expr lval = or();

        if (match(EQUAL)) {
            Token equals = previous();
            Expr rval = or();

            if (lval instanceof Expr.Variable) {
                Token name = ((Expr.Variable) lval).name;
                return new Expr.Assign(name, rval);
            }

            error(equals, "Invalid assignment target.");
        }

        return lval;
    }

    private Expr or() {
        Expr left = and();
        
        while (match(OR)) {
            Token operator = previous();
            Expr right = and();
            left = new Expr.Logical(left, operator, right);
        }

        return left;
    }

    private Expr and() {
        Expr left = equality();

        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            left = new Expr.Logical(left, operator, right);
        }

        return left;
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
