package com.shivam.lox;

import java.util.List;

import com.shivam.lox.Expr.Lambda;
import com.shivam.lox.Stmt.Function;

enum FunctionType {
    NONE,
    FUNCTION,
    LAMBDA,
    METHOD,
    INITIALIZER,
    GETTER
}

class LoxFunction implements LoxCallable {
    private final List<Token> params;
    private final List<Stmt> body;
    private final Token name;
    private final Environment closure;
    private final int envSize;
    private final Function funcDef;
    private final Lambda lambdaDef;
    final FunctionType type;

    // LoxFunction(Function definition, Environment closure) {
    //     this.params = definition.params;
    //     this.body = definition.body;
    //     this.name = definition.name;
    //     this.closure = closure;
    //     this.envSize = definition.envSize;
    //     this.funcDef = definition;
    //     this.lambdaDef = null;
    //     this.isInitializer = false;
    // }

    LoxFunction(Function definition, Environment closure, FunctionType type) {
        this.params = definition.params;
        this.body = definition.body;
        this.name = definition.name;
        this.closure = closure;
        this.envSize = definition.envSize;
        this.funcDef = definition;
        this.lambdaDef = null;
        this.type = type;
    }

    LoxFunction(Lambda definition, Environment closure) {
        this.params = definition.args;
        this.body = definition.body;
        this.name = null;
        this.closure = closure;
        this.envSize = definition.envSize;
        this.funcDef = null;
        this.lambdaDef = definition;
        this.type = FunctionType.LAMBDA;
    }

    @Override
    public int arity() {
        return params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        Environment environment = new Environment(closure, envSize);

        for (int i = 0; i < arity(); i++) {
            environment.defineAt(i, args.get(i));
        }

        try {
            interpreter.executeBlock(body, environment);
        }
        catch (ReturnE retVal) {
            if (type == FunctionType.INITIALIZER) return closure.getAt(0, 0);
            return retVal.value;
        }
        return null;
    }

    LoxFunction bind(LoxInstance instance) {
        Environment environment = new Environment(closure, 1);
        environment.defineAt(0, instance);
        return new LoxFunction(funcDef, environment, type);
    }

    @Override
    public String toString() {
        if (this.name != null)
            return "<fn " + name.lexeme + ">";
        return "<lambda fn>";
    } 
}
