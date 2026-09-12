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
    private FunctionType currentFunction = FunctionType.NONE;
    private boolean inLoop = false;
    // private final Stack<Map<String, Boolean>> scopes = new Stack<>(); // Key: identifier name, Value: is identifier resolved yet?
    private final Stack<Map<String, ScopeEntry>> scopes = new Stack<>(); // Key: identifier name, Value: is identifier resolved yet?
        
    Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    private class ScopeEntry {
        public final Token token;
        public Boolean isResolved;
        public UsedState isUsed;
        public DeclarationType decType;
        public final int envIndex;

        ScopeEntry(Token token, Boolean isResolved, UsedState isUsed, DeclarationType decType, int envIndex) {
            this.token = token;
            this.isResolved = isResolved;
            this.isUsed = isUsed;
            this.decType = decType;
            this.envIndex = envIndex;
        }
    }

    private enum FunctionType {
        NONE,
        FUNCTION
    }

    private enum UsedState {
        USED,
        UNUSED,
        EXEMPT
    }

    private enum DeclarationType {
        VAR,
        FUNCTION
    }

    @Override
    public Void visitBlockStmt(Block block) {
        beginScope();
        resolve(block.statements);
        checkUnused();
        block.envSize = scopes.peek().size();
        endScope();
        return null;
    }

    private void endScope() {
        scopes.pop();
    }

    private void beginScope() {
        // scopes.push(new HashMap<String,Boolean>());
        scopes.push(new HashMap<String,ScopeEntry>());
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
        declare(stmt.name, DeclarationType.VAR);
        if (stmt.initializer != null) {
            resolve(stmt.initializer);
        }
        define(stmt.name);
        if (scopes.isEmpty()) return null;
        stmt.index = scopes.peek().get(stmt.name.lexeme).envIndex;
        return null;
    }

    private void define(Token name) {
        if (scopes.isEmpty()) return;
        // scopes.peek().put(name.lexeme, true);
        ScopeEntry data = scopes.peek().get(name.lexeme);
        data.isResolved = true;
    }

    private void declare(Token name, DeclarationType type) {
        if (scopes.isEmpty()) return;

        // Map<String, Boolean> scope = scopes.peek();
        Map<String, ScopeEntry> scope = scopes.peek();

        if (scope.containsKey(name.lexeme)) {
            Lox.error(name.lineNum,
                "Already a variable with this name in this scope."
            );
        }
        // scope.put(name.lexeme, false);
        scope.put(name.lexeme, new ScopeEntry(name, false, UsedState.UNUSED, type, scope.size()));
    }

    @Override
    public Void visitVariableExpr(Variable expr) {
        Boolean used = true;
        if (
        !scopes.isEmpty() && 
        scopes.peek().get(expr.name.lexeme) != null && 
        scopes.peek().get(expr.name.lexeme).isResolved == Boolean.FALSE) {
            Lox.error(expr.name,
                "Can't read local variable in its own initializer."
            );
            used = false;
        }

        resolveLocal(expr, expr.name, used);
        return null;
    }

    private void resolveLocal(Expr expr, Token name, Boolean used) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                int scopesAway = scopes.size() - 1 - i;

                ScopeEntry data = scopes.get(i).get(name.lexeme);
                if (used) {
                    data.isUsed = UsedState.USED;
                }
                
                interpreter.resolve(expr, scopesAway, data.envIndex);

                return;
            }
        }
    }

    @Override
    public Void visitAssignExpr(Assign expr) {
        resolve(expr.val);
        resolveLocal(expr, expr.name, false);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Function stmt) {
        declare(stmt.name, DeclarationType.FUNCTION);
        define(stmt.name);
        resolveFunction(stmt, FunctionType.FUNCTION);
        if (scopes.empty()) return null;
        stmt.index = scopes.peek().get(stmt.name.lexeme).envIndex;
        return null;
    }

    private void resolveFunction(Function function, FunctionType type) {
        FunctionType enclosing = currentFunction;
        currentFunction = type;
        beginScope();

        for (Token param : function.params) {
            declare(param, DeclarationType.VAR);
            define(param);
            scopes.peek().get(param.lexeme).isUsed = UsedState.EXEMPT;
        }

        resolve(function.body);
        checkUnused();
        function.envSize = scopes.peek().size();
        endScope();
        currentFunction = enclosing;
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
        if (currentFunction == FunctionType.NONE) {
            Lox.error(stmt.returnTok, 
                "Cannot use return statement outside of function declaration!"
            );
        }

        if (stmt.value != null) resolve(stmt.value);
        return null;
    }

    @Override
    public Void visitWhileStmt(While stmt) {
        boolean enclosingLoop = inLoop;
        inLoop = true;
        resolve(stmt.condition);
        resolve(stmt.body);
        inLoop = enclosingLoop;
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
        if (!inLoop) {
            Lox.error(stmt.breakTok,
                "Cannot call break statement outside of loop!"
            );
        }
        return null;
    }

    @Override
    public Void visitContinueStmt(Continue stmt) {
        if (!inLoop) {
            Lox.error(stmt.contTok,
                "Cannot call continue statement outside of loop!"
            );
        }
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
        FunctionType enclosingFunction = currentFunction;
        currentFunction = FunctionType.FUNCTION;
        beginScope();
        for (Token arg : expr.args) {
            declare(arg, DeclarationType.VAR);
            define(arg);
            scopes.peek().get(arg.lexeme).isUsed = UsedState.EXEMPT;
        }
        resolve(expr.body);
        checkUnused();
        expr.envSize = scopes.peek().size();
        endScope();
        currentFunction = enclosingFunction;
        return null;
    }

    private void checkUnused() {
        Map<String, ScopeEntry> scope = scopes.peek();

        for (String tokName : scope.keySet()) {
            Token tok = scope.get(tokName).token;
            DeclarationType decType = scope.get(tokName).decType;
            String typeStr = (decType == DeclarationType.VAR) ? "variable" : "function";

            if (scope.get(tokName).isUsed == UsedState.UNUSED) {
                Lox.error(tok, 
                    "Unused " + typeStr + " '" + tok.lexeme + "'!");
            }
        }
    }

}
