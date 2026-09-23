package com.shivam.lox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClassTest {

    @BeforeEach
    void resetLoxErrorState() {
        Lox.hadError = false;
        Lox.hadRuntimeError = false;
    }

    private List<Stmt> parse(String source) {
        List<Token> tokens = new Scanner(source).scanTokens();
        return new Parser(tokens).parse();
    }

    private List<Token> scanSource(String source) {
        return new Scanner(source).scanTokens();
    }

    private List<Stmt> parse(List<Token> tokens) {
        return new Parser(tokens).parse();
    }

    private String run(String source) {
        List<Stmt> stmts = parse(source);

        Interpreter interpreter = new Interpreter();
        new Resolver(interpreter).resolve(stmts);

        ByteArrayOutputStream capturedOut = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(capturedOut));
        try {
            interpreter.interpret(stmts);
        } finally {
            System.setOut(originalOut);
        }
        return capturedOut.toString();
    }

    private String resolveAndCaptureErrors(String source) {
        List<Stmt> stmts = parse(source);

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));
        try {
            new Resolver(new Interpreter()).resolve(stmts);
        } finally {
            System.setErr(originalErr);
        }

        return errContent.toString();
    }

    @Test
    void emptyClassDeclarationProducesSingleStatement() {
        List<Stmt> stmts = parse("class Test {}");

        assertEquals(1, stmts.size());
        assertTrue(stmts.get(0) instanceof Stmt.Class);
    }

    @Test
    void classDeclarationCanContainInitializerMethod() {
        List<Stmt> stmts = parse("class Test { init() {} }");

        Stmt.Class classStmt = (Stmt.Class) stmts.get(0);
        assertEquals(1, classStmt.methods.size());
        assertEquals("init", classStmt.methods.get(0).name.lexeme);
    }

    @Test
    void returningValueFromInitializerProducesError() {
        String errorOutput = resolveAndCaptureErrors("class Test { init() { return 1; } }");

        assertTrue(Lox.hadError);
        assertTrue(errorOutput.contains("Cannot return a value from an initializer!"));
    }

    @Test
    void returningWithoutValueFromInitializerProducesNoError() {
        resolveAndCaptureErrors("class Test { init() { return; } }");

        assertFalse(Lox.hadError);
    }

    @Test
    void staticMethodDeclarationProducesNoError() {
        String errorOutput = resolveAndCaptureErrors("class Test { class greet() {} }");

        assertFalse(Lox.hadError);
        assertEquals("", errorOutput);
    }

    @Test
    void staticMethodNamedInitProducesError() {
        String errorOutput = resolveAndCaptureErrors("class Test { class init() {} }");

        assertTrue(Lox.hadError);
        assertTrue(errorOutput.contains("Static methods cannot be initializers!"));
    }

    @Test
    void printingClassValueOutputsClassName() {
        String output = run("class Test {} print Test;");

        assertEquals("<class Test>" + System.lineSeparator(), output);
    }

    @Test
    void callingStaticMethodOnInstanceThrowsUndefinedPropertyError() {
        List<Stmt> stmts = parse(
            "class Test { class greet() {} } var t = Test(); t.greet();"
        );

        Interpreter interpreter = new Interpreter();
        new Resolver(interpreter).resolve(stmts);

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));
        try {
            interpreter.interpret(stmts);
        } finally {
            System.setErr(originalErr);
        }

        assertTrue(Lox.hadRuntimeError);
        assertTrue(errContent.toString().contains("Undefined property 'greet'."));
    }

}
