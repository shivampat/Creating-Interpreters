package com.shivam.lox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ParserTest {

    @BeforeEach
    void resetLoxErrorState() {
        Lox.hadError = false;
    }

    private List<Stmt> parse(String source) {
        List<Token> tokens = new Scanner(source).scanTokens();
        return new Parser(tokens).parse();
    }

    @Test
    void ternaryOperatorIsUsableInAnyExpressionPositionNotJustVarInitializers() {
        // comma()/ternary() are only ever invoked from varDeclaration()'s
        // initializer -- expression() (used by print/if/return/expression
        // statements) never routes through them, so "?:" silently fails
        // to parse anywhere outside of "var x = ... ? ... : ...;".
        List<Stmt> statements = parse("print true ? 1 : 2;");

        assertEquals(1, statements.size());
        assertNotNull(statements.get(0), "Expected the ternary print statement to parse successfully");
        assertTrue(statements.get(0) instanceof Stmt.Print);

        Expr expr = ((Stmt.Print) statements.get(0)).expression;
        assertTrue(expr instanceof Expr.Ternary, "Expected a Ternary expression; the '?:' was not recognized at all");
        assertFalse(Lox.hadError);
    }

    @Test
    void missingLeftOperandForEqualityReportsFriendlyErrorAndRecovers() {
        // equality()/comparison()/term()/factor() deliberately detect a
        // leading binary operator with no left-hand operand (e.g. "== 5")
        // and report a targeted error instead of the generic "Expect
        // expression." from primary(). This is intentional design -- the
        // parser should recover to a null statement via synchronize(),
        // not throw an uncaught exception.
        List<Stmt> statements = parse("== 5;");

        assertEquals(1, statements.size());
        assertNull(statements.get(0));
        assertTrue(Lox.hadError);
    }

    @Test
    void invalidAssignmentTargetIsAParseErrorNotASilentlyDroppedAssignment() {
        // Every other call site wraps error(...) in a throw, so the
        // ParseError propagates to declaration()'s catch and synchronizes.
        // assignment()'s "Invalid assignment target." call is missing that
        // throw, so "1 = 2;" reports the error but keeps parsing as if the
        // "= 2" had never been there, silently producing Expression(1)
        // instead of failing like every other malformed statement does.
        List<Stmt> statements = parse("1 = 2;");

        assertEquals(1, statements.size());
        assertNull(statements.get(0));
        assertTrue(Lox.hadError);
    }

    @Test
    void lambdaExpressionStatementParsesSuccessfullyAtTheParserLayer() {
        // A bare anonymous function statement -- "fun () {};" -- is an
        // unusual but valid expression statement (Expr.Lambda wrapped in
        // Stmt.Expression). This confirms the Parser handles it cleanly
        // with no reported error.
        List<Stmt> statements = parse("fun () {};");

        assertEquals(1, statements.size());
        assertNotNull(statements.get(0));
        assertTrue(statements.get(0) instanceof Stmt.Expression);

        Expr expr = ((Stmt.Expression) statements.get(0)).expression;
        assertTrue(expr instanceof Expr.Lambda);
        assertFalse(Lox.hadError);
    }

    @Test
    void forLoopBodyRunsBeforeIncrementNotAfter() {
        // forStatement() desugars into: Block[ init, While(cond,
        // Block[increment, body]) ]. Putting the increment BEFORE the body
        // in that inner block means "i" is bumped before it's ever used,
        // so `for (var i = 0; i < 10; i = i + 1) print i;` prints 1..10
        // instead of 0..9. The correct desugaring runs body then
        // increment: Block[body, increment].
        List<Stmt> statements = parse("for (var i = 0; i < 10; i = i + 1) print i;");

        assertEquals(1, statements.size());
        assertTrue(statements.get(0) instanceof Stmt.Block);
        Stmt.Block outer = (Stmt.Block) statements.get(0);
        assertEquals(2, outer.statements.size());
        assertTrue(outer.statements.get(1) instanceof Stmt.While);

        Stmt.While whileStmt = (Stmt.While) outer.statements.get(1);
        assertTrue(whileStmt.body instanceof Stmt.Block);
        Stmt.Block loopBody = (Stmt.Block) whileStmt.body;
        assertEquals(2, loopBody.statements.size());

        assertTrue(loopBody.statements.get(0) instanceof Stmt.Print,
            "Expected the loop body (print i) to run before the increment");
        assertTrue(loopBody.statements.get(1) instanceof Stmt.Expression,
            "Expected the increment (i = i + 1) to run after the loop body");
    }

    @Test
    void chainedAssignmentIsRightAssociative() {
        // assignment() parses its right-hand side with or() instead of a
        // recursive call to assignment() itself, so "a = b = c;" only
        // consumes "a = b" before choking on the leftover "= c" (expecting
        // a semicolon instead). Right-associative chaining requires the
        // rval to recurse back into assignment().
        List<Stmt> statements = parse("a = b = c;");

        assertEquals(1, statements.size());
        assertNotNull(statements.get(0), "Expected chained assignment 'a = b = c;' to parse as one statement");
        assertTrue(statements.get(0) instanceof Stmt.Expression);

        Expr expr = ((Stmt.Expression) statements.get(0)).expression;
        assertTrue(expr instanceof Expr.Assign);
        Expr.Assign outer = (Expr.Assign) expr;
        assertEquals("a", outer.name.lexeme);
        assertTrue(outer.val instanceof Expr.Assign,
            "Expected 'b = c' to itself be parsed as a nested assignment");
    }

    @Test
    void unclosedGroupingIsAParseErrorNotAnEscapingException() {
        // primary()'s L_PAREN branch requires a matching R_PAREN via
        // consume(). A missing ')' should surface as a normal parse error
        // (null statement via synchronize()), not an uncaught exception.
        List<Stmt> statements = parse("(1 + 2;");

        assertEquals(1, statements.size());
        assertNull(statements.get(0));
        assertTrue(Lox.hadError);
    }

    @Test
    void logicalAndOrProduceLogicalExpressionsNotBinaryExpressions() {
        // and()/or() are a separate precedence tier from equality()/etc.
        // and must build Expr.Logical nodes (which short-circuit at
        // runtime) rather than Expr.Binary nodes (which always evaluate
        // both sides).
        List<Stmt> statements = parse("true and false;");

        assertEquals(1, statements.size());
        assertNotNull(statements.get(0));
        Expr expr = ((Stmt.Expression) statements.get(0)).expression;
        assertTrue(expr instanceof Expr.Logical, "Expected 'and' to produce an Expr.Logical node");
        assertEquals(TokenType.AND, ((Expr.Logical) expr).operator.type);
    }

    @Test
    void danglingElseAttachesToTheNearestIfStatement() {
        // Classic dangling-else ambiguity: "if (a) if (b) X; else Y;"
        // must attach the "else" to the inner "if", not the outer one.
        // Recursive descent naturally does this since ifStatement()
        // greedily consumes a trailing ELSE before returning to its
        // caller -- this pins that behavior down as a regression test.
        List<Stmt> statements = parse("if (true) if (false) print 1; else print 2;");

        assertEquals(1, statements.size());
        assertTrue(statements.get(0) instanceof Stmt.If);
        Stmt.If outerIf = (Stmt.If) statements.get(0);
        assertNull(outerIf.elseBranch, "Expected the else to belong to the inner if, not the outer one");

        assertTrue(outerIf.thenBranch instanceof Stmt.If);
        Stmt.If innerIf = (Stmt.If) outerIf.thenBranch;
        assertNotNull(innerIf.elseBranch, "Expected the inner if to have the else branch");
    }
}
