package com.shivam.lox;

import java.util.List;
abstract class Expr {
	public class Binary implements Expr {
		Binary(Expr left, Token operator, Expr right) {
			this.left = left;
			this.operator = operator;
			this.right = right;
		}

		final Expr left;
		final Token operator;
		final Expr right;
	}

	public class Grouping implements Expr {
		Grouping(Expr expression) {
			this.expression = expression;
		}

		final Expr expression;
	}

	public class Literal implements Expr {
		Literal(Object value) {
			this.value = value;
		}

		final Object value;
	}

	public class Unary implements Expr {
		Unary(Token operator, Expr right) {
			this.operator = operator;
			this.right = right;
		}

		final Token operator;
		final Expr right;
	}

}
