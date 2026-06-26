package com.shivam.lox;

import com.shivam.lox.Expr.Binary;
import com.shivam.lox.Expr.Grouping;
import com.shivam.lox.Expr.Literal;
import com.shivam.lox.Expr.Unary;

public class AstPrinter implements Expr.Visitor<String> {
    String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Literal expr) {
        if (expr == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(");
        builder.append(name).append(" ");
        for (Expr expr : exprs) {
            builder.append(expr.accept(this)).append(" ");
        }
        builder.append(")");

        return builder.toString();
    }

    public static void main(String[] args) {
        
    } 
}
