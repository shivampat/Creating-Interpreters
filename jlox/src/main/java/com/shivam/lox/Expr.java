package com.shivam.lox;

import java.util.List;
abstract class Expr {
	interface Visitor<R> {
		R visitBinaryExpr(Binary expr);
		R visitCallExpr(Call expr);
		R visitGetExpr(Get expr);
		R visitLogicalExpr(Logical expr);
		R visitSetExpr(Set set);
		R visitGroupingExpr(Grouping expr);
		R visitLiteralExpr(Literal expr);
		R visitAssignExpr(Assign expr);
		R visitUnaryExpr(Unary expr);
		R visitTernaryExpr(Ternary expr);
		R visitVariableExpr(Variable expr);
		R visitLambdaExpr(Lambda expr);
	}
	public static class Binary extends Expr {
		Binary(Expr left, Token operator, Expr right) {
			this.left = left;
			this.operator = operator;
			this.right = right;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitBinaryExpr(this);
		}
		final Expr left;
		final Token operator;
		final Expr right;

	}

	public static class Call extends Expr {
		Call(Expr calle, Token paren, List<Expr> args) {
			this.calle = calle;
			this.paren = paren;
			this.args = args;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitCallExpr(this);
		}
		final Expr calle;
		final Token paren;
		final List<Expr> args;

	}

	public static class Get extends Expr {
		Get(Expr object, Token name) {
			this.object = object;
			this.name = name;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitGetExpr(this);
		}
		final Expr object;
		final Token name;

	}

	public static class Logical extends Expr {
		Logical(Expr left, Token operator, Expr right) {
			this.left = left;
			this.operator = operator;
			this.right = right;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitLogicalExpr(this);
		}
		final Expr left;
		final Token operator;
		final Expr right;

	}

	public static class Set extends Expr {
		Set(Expr object, Token name, Expr value) {
			this.object = object;
			this.name = name;
			this.value = value;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitSetExpr(this);
		}
		final Expr object;
		final Token name;
		final Expr value;

	}

	public static class Grouping extends Expr {
		Grouping(Expr expression) {
			this.expression = expression;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitGroupingExpr(this);
		}
		final Expr expression;

	}

	public static class Literal extends Expr {
		Literal(Object value) {
			this.value = value;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitLiteralExpr(this);
		}
		final Object value;

	}

	public static class Assign extends Expr {
		Assign(Token name, Expr val) {
			this.name = name;
			this.val = val;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitAssignExpr(this);
		}
		final Token name;
		final Expr val;

	}

	public static class Unary extends Expr {
		Unary(Token operator, Expr right) {
			this.operator = operator;
			this.right = right;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitUnaryExpr(this);
		}
		final Token operator;
		final Expr right;

	}

	public static class Ternary extends Expr {
		Ternary(Expr condition, Token questTok, Expr trueExpr, Expr elseExpr) {
			this.condition = condition;
			this.questTok = questTok;
			this.trueExpr = trueExpr;
			this.elseExpr = elseExpr;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitTernaryExpr(this);
		}
		final Expr condition;
		final Token questTok;
		final Expr trueExpr;
		final Expr elseExpr;

	}

	public static class Variable extends Expr {
		Variable(Token name) {
			this.name = name;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitVariableExpr(this);
		}
		final Token name;

	}

	public static class Lambda extends Expr {
		Lambda(Token funTok, List<Token> args, List<Stmt> body) {
			this.funTok = funTok;
			this.args = args;
			this.body = body;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitLambdaExpr(this);
		}
		final Token funTok;
		final List<Token> args;
		final List<Stmt> body;
        public int envSize;

	}


	abstract <R> R accept(Visitor<R> visitor);
}
