package com.shivam.lox;

import java.util.List;

import com.shivam.lox.Stmt.Function;

class LoxFunction implements LoxCallable {
    private final Function definition;
    private final Environment closure;
    LoxFunction(Function definition, Environment closure) {
        this.definition = definition;
        this.closure = closure;
    }

    @Override
    public int arity() {
        return definition.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        Environment environment = new Environment(closure);

        for (int i = 0; i < arity(); i++) {
            environment.define(definition.params.get(i).lexeme, args.get(i));
        }

        try {
            interpreter.executeBlock(definition.body, environment);
        }
        catch (ReturnE retVal) {
            return retVal.value;
        }
        return null;
    }

    @Override
    public String toString() {
        return "<fn " + definition.name.lexeme + ">";
    } 
}
