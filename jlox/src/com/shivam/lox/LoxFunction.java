package com.shivam.lox;

import java.util.List;

import com.shivam.lox.Expr.Lambda;
import com.shivam.lox.Stmt.Function;

class LoxFunction implements LoxCallable {
    private final List<Token> params;
    private final List<Stmt> body;
    private final Token name;
    private final Environment closure;

    LoxFunction(Function definition, Environment closure) {
        this.params = definition.params;
        this.body = definition.body;
        this.name = definition.name;
        this.closure = closure;
    }

    LoxFunction(Lambda definition, Environment closure) {
        this.params = definition.args;
        this.body = definition.body;
        this.name = null;
        this.closure = closure;
    }

    @Override
    public int arity() {
        return params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        Environment environment = new Environment(closure);

        for (int i = 0; i < arity(); i++) {
            environment.define(params.get(i).lexeme, args.get(i));
        }

        try {
            interpreter.executeBlock(body, environment);
        }
        catch (ReturnE retVal) {
            return retVal.value;
        }
        return null;
    }

    @Override
    public String toString() {
        if (this.name != null)
            return "<fn " + name.lexeme + ">";
        return "<lambda fn>";
    } 
}
