package com.shivam.lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.shivam.lox.Expr.Assign;
import com.shivam.lox.Expr.Binary;
import com.shivam.lox.Expr.Call;
import com.shivam.lox.Expr.Grouping;
import com.shivam.lox.Expr.Lambda;
import com.shivam.lox.Expr.Literal;
import com.shivam.lox.Expr.Logical;
import com.shivam.lox.Expr.Ternary;
import com.shivam.lox.Expr.Unary;
import com.shivam.lox.Expr.Variable;
import com.shivam.lox.Stmt.Block;
import com.shivam.lox.Stmt.Break;
import com.shivam.lox.Stmt.Continue;
import com.shivam.lox.Stmt.Expression;
import com.shivam.lox.Stmt.Function;
import com.shivam.lox.Stmt.If;
import com.shivam.lox.Stmt.Print;
import com.shivam.lox.Stmt.Return;
import com.shivam.lox.Stmt.Var;
import com.shivam.lox.Stmt.While;

class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    private final Interpreter interpreter; 
    private final Stack<Map<String, Boolean>> scopes = new Stack<>(); // Key: identifier name, Value: is identifier resolved yet?

    Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    @Override
    public Void visitBlockStmt(Block block) {
        beginScope();
        resolve(block.statements);
        endScope();
        return null;
    }

    private void endScope() {
        scopes.pop();
    }

    private void beginScope() {
        scopes.push(new HashMap<String,Boolean>());
    }

    void resolve(List<Stmt> statements) {
        for (Stmt statement : statements) {
            resolve(statement);
        }
    }

    private void resolve(Stmt stmt) {
        stmt.accept(this);
    }

    private void resolve(Expr expr) {
        expr.accept(this);
    }
    
    @Override
    public Void visitVarStmt(Var stmt) {
        declare(stmt.name);
        if (stmt.initializer != null) {
            resolve(stmt.initializer);
        }
        define(stmt.name);
        return null;
    }

    private void define(Token name) {
        if (scopes.isEmpty()) return;
        scopes.peek().put(name.lexeme, true);
    }

    private void declare(Token name) {
        if (scopes.isEmpty()) return;

        Map<String, Boolean> scope = scopes.peek();

        if (scope.containsKey(name.lexeme)) {
            Lox.error(name.lineNum,
                "Already a variable with this name in this scope."
            );
        }
        scope.put(name.lexeme, false);
    }

    @Override
    public Void visitVariableExpr(Variable expr) {
        if (!scopes.isEmpty() && scopes.peek().get(expr.name.lexeme) == Boolean.FALSE) {
            Lox.error(expr.name,
                "Can't read local variable in its own initializer."
            );
        }

        resolveLocal(expr, expr.name);
        return null;
    }

    private void resolveLocal(Expr expr, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                int scopesAway = scopes.size() - 1 - i;
                interpreter.resolve(expr, scopesAway);
                return;
            }
        }
    }

    @Override
    public Void visitAssignExpr(Assign expr) {
        resolve(expr.val);
        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Function stmt) {
        declare(stmt.name);
        define(stmt.name);

        resolveFunction(stmt);
        return null;
    }

    private void resolveFunction(Function function) {
        beginScope();

        for (Token param : function.params) {
            declare(param);
            define(param);
        }

        resolve(function.body);

        endScope();
    }
    
    @Override
    public Void visitExpressionStmt(Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitIfStmt(If stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if (stmt.elseBranch != null) resolve(stmt.elseBranch);
        return null;    
    }

    @Override
    public Void visitPrintStmt(Print stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitReturnStmt(Return stmt) {
        if (stmt.value != null) resolve(stmt.value);
        return null;
    }

    @Override
    public Void visitWhileStmt(While stmt) {
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }

    @Override
    public Void visitBinaryExpr(Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitCallExpr(Call expr) {
        resolve(expr.calle);

        for (Expr arg : expr.args) {
            resolve(arg);
        }

        return null;
    }

    @Override
    public Void visitGroupingExpr(Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Literal expr) {
        return null;
    }

    @Override 
    public Void visitLogicalExpr(Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitUnaryExpr(Unary expr) {
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitBreakStmt(Break stmt) {
        return null;
    }

    @Override
    public Void visitContinueStmt(Continue stmt) {
        return null;
    }

    @Override
    public Void visitTernaryExpr(Ternary expr) {
        resolve(expr.condition);
        resolve(expr.trueExpr);
        resolve(expr.elseExpr);
        return null;
    }

    @Override
    public Void visitLambdaExpr(Lambda expr) {
        beginScope();
        for (Token arg : expr.args) {
            declare(arg);
            define(arg);
        }
        resolve(expr.body);
        endScope();
        return null;
    }

}
