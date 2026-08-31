package com.shivam.lox;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, Object> values = new HashMap<>();
    final Environment enclosing;

    Environment() {
        enclosing = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    void define(String name, Object obj) {
        values.put(name, obj);
    }

    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            if (values.get(name.lexeme) != null) {
                return values.get(name.lexeme);
            }

            throw new RuntimeError(name, "Cannot access variable '" + name.lexeme + "' before it has been initialized!");
        }
        
        if (enclosing != null) return enclosing.get(name);

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    public void assign(Token name, Object val) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, val);
            return;
        }

        if (enclosing != null) {
            enclosing.assign(name, val);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    Environment ancestor(int distance) {
        Environment current = this;
        for (int i = 0; i < distance; i++) {
            current = current.enclosing;
        }

        return current;
    }

    public Object getAt(Integer distance, String lexeme) {
        return ancestor(distance).values.get(lexeme);
    }

    public void assignAt(Integer distance, Token name, Object val) {
        ancestor(distance).values.put(name.lexeme, val);
    }
}
