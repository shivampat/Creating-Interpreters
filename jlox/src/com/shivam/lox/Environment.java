package com.shivam.lox;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, Object> values = new HashMap<>();

    void define(String name, Object obj) {
        values.put(name, obj);
    }

    Object get(Token name) {
        if (values.containsKey(name.lexeme))
            return values.get(name.lexeme);

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    public void assign(Token name, Object val) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, val);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }
}
