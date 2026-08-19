package com.shivam.lox;

import java.util.List;

import com.shivam.lox.Stmt.Function;

class LoxFunction implements LoxCallable {
    final Function definition;
    LoxFunction(Function definition) {
        this.definition = definition;
    }

    @Override
    public int arity() {
        return definition.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        Environment environment = new Environment(interpreter.globals);

        for (int i = 0; i < arity(); i++) {
            environment.define(definition.params.get(i).lexeme, args.get(i));
        }

        interpreter.executeBlock(definition.body, environment);
        return null;
    }

    @Override
    public String toString() {
        return "<fn " + definition.name.lexeme + ">";
    } 
}
