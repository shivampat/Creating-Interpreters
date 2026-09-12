package com.shivam.lox;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, Object> values;
    private Object[] local_values;
    final Environment enclosing;

    // Enforce dynamic global hash map
    Environment() {
        enclosing = null;
        values = new HashMap<>();
    }

    // Statically defined array fetching 
    Environment(Environment enclosing, int envSize) {
        this.enclosing = enclosing;
        local_values = new Object[envSize];
        values = null;
    }

    void define(String name, Object obj) {
        if (local_values != null) {
            throw new IllegalStateException("Cannot use map methods when in a local-scoped environment!");
        }
        values.put(name, obj);
    }

    void defineAt(int index, Object obj) {
        if (local_values == null) {
            throw new IllegalStateException("Cannot use defineAt on globally-scoped identifiers!");
        }
        this.local_values[index] = obj;
    }

    Object get(Token name) {
        if (local_values != null) {
            throw new IllegalStateException("Cannot use map methods when in a local-scoped environment!");
        }
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
        if (local_values != null) {
            throw new IllegalStateException("Cannot use map methods when in a local-scoped environment!");
        }
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

    public Object getAt(int index, Integer distance) {
        return ancestor(distance).local_values[index];
    }

    public void assignAt(int index, Integer distance, Object val) {
        ancestor(distance).local_values[index] = val;
    }
}
