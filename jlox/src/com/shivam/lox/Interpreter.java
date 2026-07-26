package com.shivam.lox;

import static com.shivam.lox.TokenType.*;

import com.shivam.lox.Expr.Binary;
import com.shivam.lox.Expr.Grouping;
import com.shivam.lox.Expr.Literal;
import com.shivam.lox.Expr.Ternary;
import com.shivam.lox.Expr.Unary;

public class Interpreter implements Expr.Visitor<Object> {

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
        Object true_clause = evaluate(expr.trueExpr);
        Object else_clause = evaluate(expr.elseExpr);
        Token quesToken = expr.questTok;

        if (!isTruthy(condition))
            throw new RuntimeError(quesToken, "Condition in ternary expression must be boolean!");
        
        if ((Boolean) condition) {
            return true_clause;
        }

        return else_clause;
    }

    void interpret(Expr expression) {
        try {
            Object val = evaluate(expression);
            System.out.println(stringify(val));
        } catch (RuntimeError re) {
            Lox.runtimeError(re);
        }
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
}
