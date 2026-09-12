package com.shivam.lox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InterpreterTest {

    @BeforeEach
    void resetLoxErrorState() {
        Lox.hadError = false;
        Lox.hadRuntimeError = false;
    }

    private String run(String source) {
        List<Token> tokens = new Scanner(source).scanTokens();
        List<Stmt> statements = new Parser(tokens).parse();

        Interpreter interpreter = new Interpreter();
        new Resolver(interpreter).resolve(statements);

        ByteArrayOutputStream capturedOut = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(capturedOut));
        try {
            interpreter.interpret(statements);
        } finally {
            System.setOut(originalOut);
        }
        return capturedOut.toString();
    }

    @Test
    void breakInsideCalledFunctionIncorrectlyTerminatesCallersEnclosingLoop() {
        // LoxFunction.call() -> Interpreter.executeBlock() only resets
        // continueFound in its finally block, never breakFound. Both are
        // single unscoped instance fields on Interpreter, so a break
        // statement inside a called function's body leaks the shared
        // `breakFound` flag back out to whatever loop happens to be
        // running in the CALLER, terminating it early even though the
        // break has nothing to do with that loop.
        String output = run(
            "fun inner() { break; }" +
            "var i = 0;" +
            "while (i < 3) {" +
            "  i = i + 1;" +
            "  inner();" +
            "}" +
            "print i;"
        );

        assertEquals("3" + System.lineSeparator(), output,
            "Expected the while loop to run all 3 iterations; break inside inner() should not affect the caller's loop");
    }

    @Test
    void numberConcatenatedWithStringCoercesNumberToString() {
        // visitBinaryExpr's PLUS case only fast-paths pure Double+Double
        // and pure String+String; if EITHER side is a String it falls
        // back to stringify(left) + stringify(right), implicitly
        // coercing the number to text.
        String output = run("print 1 + \"abc\";");

        assertEquals("1abc" + System.lineSeparator(), output);
    }

    @Test
    void divisionByZeroProducesInfinityInsteadOfThrowing() {
        // checkNumberOperands only verifies both operands are Doubles; it
        // does not special-case a zero divisor. Java's double division
        // silently produces +/-Infinity rather than throwing.
        String output = run("print 10 / 0;");

        assertEquals("Infinity" + System.lineSeparator(), output);
        assertFalse(Lox.hadRuntimeError);
    }

    @Test
    void stringsCompareLexicographically() {
        // GREATER/LESS/etc. fall back to String.compareTo when both
        // operands are strings, rather than only supporting numbers.
        String output = run("print \"apple\" < \"banana\";");

        assertEquals("true" + System.lineSeparator(), output);
    }

    @Test
    void zeroIsTruthy() {
        // isTruthy() only treats null and Boolean.FALSE as falsy --
        // every other object, including the number 0, is truthy.
        String output = run("if (0) print \"truthy\"; else print \"falsy\";");

        assertEquals("truthy" + System.lineSeparator(), output);
    }

    @Test
    void blockScopedVariableShadowsOuterVariableAndOuterIsRestoredAfterBlock() {
        String output = run(
            "var a = \"global\";" +
            "{ var a = \"local\"; print a; }" +
            "print a;"
        );

        assertEquals("local" + System.lineSeparator() + "global" + System.lineSeparator(), output);
    }

    @Test
    void closureSharesMutableStateAcrossMultipleCalls() {
        String output = run(
            "fun makeCounter() {" +
            "  var count = 0;" +
            "  fun increment() {" +
            "    count = count + 1;" +
            "    return count;" +
            "  }" +
            "  return increment;" +
            "}" +
            "var counter = makeCounter();" +
            "print counter();" +
            "print counter();" +
            "print counter();"
        );

        assertEquals(
            "1" + System.lineSeparator() + "2" + System.lineSeparator() + "3" + System.lineSeparator(),
            output
        );
    }

    @Test
    void callingFunctionWithWrongArgumentCountThrowsRuntimeError() {
        String output = run("fun f(a, b) { return a + b; } f(1);");

        assertTrue(Lox.hadRuntimeError, "Expected an arity-mismatch call to report a runtime error");
        assertEquals("", output);
    }

    @Test
    void callingANonCallableValueThrowsRuntimeError() {
        String output = run("var x = 1; x();");

        assertTrue(Lox.hadRuntimeError, "Expected calling a non-function value to report a runtime error");
        assertEquals("", output);
    }

    @Test
    void accessingUndefinedGlobalVariableThrowsRuntimeError() {
        String output = run("print undefinedVar;");

        assertTrue(Lox.hadRuntimeError, "Expected reading an undefined global to report a runtime error");
        assertEquals("", output);
    }
}
