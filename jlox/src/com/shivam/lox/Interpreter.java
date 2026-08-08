package com.shivam.lox;

import static com.shivam.lox.TokenType.*;

import java.util.List;

import com.shivam.lox.Expr.Assign;
import com.shivam.lox.Expr.Binary;
import com.shivam.lox.Expr.Grouping;
import com.shivam.lox.Expr.Literal;
import com.shivam.lox.Expr.Logical;
import com.shivam.lox.Expr.Ternary;
import com.shivam.lox.Expr.Unary;
import com.shivam.lox.Expr.Variable;
import com.shivam.lox.Stmt.Block;
import com.shivam.lox.Stmt.Expression;
import com.shivam.lox.Stmt.If;
import com.shivam.lox.Stmt.Print;
import com.shivam.lox.Stmt.Var;
import com.shivam.lox.Stmt.While;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Object> {
    private Environment env = new Environment();

    @Override
    public Object visitBinaryExpr(Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left - (double) right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double) left * (double) right;
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (double) left / (double) right;
            case PLUS:
                // Addition of doubles
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }

                // Concat of strings
                if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                }

                if (left instanceof String || right instanceof String) {
                    return stringify(left) + stringify(right);
                }

                throw new RuntimeError(expr.operator, "Operands must be of the same type, either two numbers or two strings!");
            case GREATER:
                // Comparison of doubles
                if (left instanceof Double && right instanceof Double) {
                    return (double) left > (double) right;
                }

                // Comparison of strings
                if (left instanceof String && right instanceof String) {
                    return ((String) left).compareTo((String) right) > 0;
                }

                throw new RuntimeError(expr.operator, "Operands must be of the same type to compare, either two numbers or two strings!");
                
            case LESS:
                // Comparison of doubles
                if (left instanceof Double && right instanceof Double) {
                    return (double) left < (double) right;
                }
                
                // Comparison of strings
                if (left instanceof String && right instanceof String) {
                    return ((String) left).compareTo((String) right) < 0;
                }

                throw new RuntimeError(expr.operator, "Operands must be of the same type to compare, either two numbers or two strings!");
            case GREATER_EQUAL:
                // Comparison of doubles
                if (left instanceof Double && right instanceof Double) {
                    return (double) left >= (double) right;
                }
                
                // Comparison of strings
                if (left instanceof String && right instanceof String) {
                    return ((String) left).compareTo((String) right) >= 0;
                }

                throw new RuntimeError(expr.operator, "Operands must be of the same type to compare, either two numbers or two strings!");
            case LESS_EQUAL:
                // Comparison of doubles
                if (left instanceof Double && right instanceof Double) {
                    return (double) left <= (double) right;
                }
                
                // Comparison of strings
                if (left instanceof String && right instanceof String) {
                    return ((String) left).compareTo((String) right) <= 0;
                }

                throw new RuntimeError(expr.operator, "Operands must be of the same type to compare, either two numbers or two strings!");
            case EQUAL_EQUAL:
                return isEqual(left, right);
            case EXCL_EQUAL:
                return !isEqual(left, right);
        }

        return null;
    }

    @Override
    public Object visitGroupingExpr(Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Unary expr) {
        Object right = evaluate(expr.right);
        
        switch (expr.operator.type) {
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double)right;
            case EXCL:
                return !isTruthy(right);
        }
    
        return null;

    }

    @Override
    public Object visitTernaryExpr(Ternary expr) {
        Object condition = evaluate(expr.condition);

        if (isTruthy(condition)) {
            return evaluate(expr.trueExpr);
        }
        else {
            return evaluate(expr.elseExpr);
        }
    }

    void interpret(Expr expression) {
        try {
            Object val = evaluate(expression);
            System.out.println(stringify(val));
        } catch (RuntimeError re) {
            Lox.runtimeError(re);
        }
    }

    void interpret(List<Stmt> statements) {
        try {
            for (Stmt stmt : statements) {
                execute(stmt);
            }
        }
        catch (RuntimeError re) {
            Lox.runtimeError(re);
        }
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private String stringify(Object object) {
        if (object == null) return "nil";

        if (object instanceof Double) {
            String txt = object.toString();
            if (txt.endsWith(".0")) {
                txt = txt.substring(0, txt.length() - 2);
            }
            return txt;
        }

        return object.toString();
    }

    private boolean isTruthy(Object obj) {
        if (obj == null) return false;
        if (obj instanceof Boolean) return (boolean) obj;
        return true;
    }
     
    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
        return a.equals(b);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number!");
    }

    private void checkNumberOperands(Token operator, Object l_operand, Object r_operand) {
        if ((l_operand instanceof Double) && (r_operand instanceof Double)) return;
        throw new RuntimeError(operator, "Operands must be a number!");
    }

    @Override
    public Void visitExpressionStmt(Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Print stmt) {
        Object val = evaluate(stmt.expression);
        System.out.println(stringify(val));
        return null;
    }

    @Override
    public Void visitVarStmt(Var stmt) {
        Object value = null;

        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }
        
        env.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Object visitVariableExpr(Variable expr) {
        return env.get(expr.name);
    }

    @Override
    public Object visitAssignExpr(Assign expr) {
        Object val = evaluate(expr.val);
        env.assign(expr.name, val);
        return val;
    }

    @Override
    public Void visitBlockStmt(Block stmt) {
        executeBlock(stmt.statements, new Environment(env));
        return null;
    }

    private void executeBlock(List<Stmt> statements, Environment environment) {
        // we swap the object environment to make it easier to write new visitor methods and reduce how much code we would have to change
        Environment previous = this.env;

        try {
            this.env = environment;

            for (Stmt stmt : statements) {
                execute(stmt); 
            }
        }
        finally {
            // restores previous env even in the event of an exception
            this.env = previous;
        }
    }

    @Override
    public Void visitIfStmt(If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        }
        else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Object visitLogicalExpr(Logical expr) {
        Object left = evaluate(expr.left);
        // Since Lox is dynamically typed, we return the actual object instead of purely a boolean value.

        if (expr.operator.type == OR) {
            if (isTruthy(left)) return left;
            // if not truthy, we evaluate the right side
        }
        // and
        else {
            if (!isTruthy(left)) return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Void visitWhileStmt(While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
        }

        return null;
    }
}
