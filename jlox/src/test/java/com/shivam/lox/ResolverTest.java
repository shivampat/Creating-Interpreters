package com.shivam.lox;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ResolverTest {

    @BeforeEach
    void resetLoxErrorState() {
        Lox.hadError = false;
    }

    private void resolve(String source) {
        List<Token> tokens = new Scanner(source).scanTokens();
        List<Stmt> statements = new Parser(tokens).parse();
        Interpreter interpreter = new Interpreter();
        new Resolver(interpreter).resolve(statements);
    }

    @Test
    void redeclaringAVariableInTheSameBlockScopeReportsError() {
        resolve("{ var a = 1; var a = 2; }");

        assertTrue(Lox.hadError, "Expected re-declaring 'a' in the same block to be reported");
    }

    @Test
    void selfReferenceInLocalVariableInitializerReportsError() {
        // declare() puts "a" into the current scope (unresolved) BEFORE
        // the initializer is resolved, so referencing "a" from within its
        // own initializer must be caught.
        resolve("{ var a = a; }");

        assertTrue(Lox.hadError, "Expected 'var a = a;' inside a block to report a self-reference error");
    }

    @Test
    void selfReferenceInGlobalVariableInitializerDoesNotReportAResolverError() {
        // The `scopes` stack is only ever pushed for blocks/functions/
        // lambdas -- the top-level program is never wrapped in a scope.
        // So the same "a = a" self-reference check simply never runs for
        // globals; the mistake surfaces later as a runtime
        // "Undefined variable" error instead, not a resolver error.
        resolve("var a = a;");

        assertFalse(Lox.hadError, "Global self-reference is not caught by the resolver (only local scopes are checked)");
    }

    @Test
    void duplicateParameterNamesInFunctionReportsError() {
        // getParams() declares each parameter via declare(), which is the
        // same duplicate-name check used for local variables.
        resolve("fun f(a, a) { print a; }");

        assertTrue(Lox.hadError, "Expected duplicate parameter names to be reported");
    }

    @Test
    void unusedLocalVariableInBlockReportsError() {
        resolve("{ var unused = 1; }");

        assertTrue(Lox.hadError, "Expected an unused local variable to be reported");
    }

    @Test
    void unusedGlobalVariableDoesNotReportError() {
        // checkUnused() is only invoked from visitBlockStmt/resolveFunction/
        // visitLambdaExpr, right before their matching endScope() -- it is
        // never invoked for the top-level statement list, so unused
        // globals are never flagged.
        resolve("var unused = 1;");

        assertFalse(Lox.hadError, "Global variables are not checked for unused-ness");
    }

    @Test
    void unusedFunctionParameterDoesNotReportError() {
        // resolveFunction() marks every parameter UsedState.EXEMPT right
        // after declaring/defining it, specifically so checkUnused()
        // skips them.
        resolve("fun f(unusedParam) { }");

        assertFalse(Lox.hadError, "Function parameters are exempt from the unused-variable check");
    }

    @Test
    void returnOutsideFunctionReportsError() {
        resolve("return 1;");

        assertTrue(Lox.hadError, "Expected a top-level return statement to be reported");
    }

    @Test
    void breakOutsideLoopReportsError() {
        resolve("break;");

        assertTrue(Lox.hadError, "Expected a top-level break statement to be reported");
    }

    @Test
    void continueOutsideLoopReportsError() {
        resolve("continue;");

        assertTrue(Lox.hadError, "Expected a top-level continue statement to be reported");
    }

    @Test
    void breakInsideFunctionNestedInLoopIsNotFlaggedAsInvalid() {
        // resolveFunction() saves/restores currentFunction but never
        // touches `inLoop`. So when a function is declared inside a
        // while/for loop, inLoop is still true while resolving the
        // function's own body -- meaning a break statement that doesn't
        // actually reach any loop of its OWN (it's inside a function call
        // frame) is incorrectly treated as valid.
        resolve("while (true) { fun f() { break; } f(); }");

        assertTrue(Lox.hadError,
            "Expected a break statement inside a nested function to be rejected, since break must not cross a function boundary");
    }
}
