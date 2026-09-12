package com.shivam.lox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScannerTest {

    @BeforeEach
    void resetLoxErrorState() {
        Lox.hadError = false;
    }

    @Test
    void scansEmptySourceAsJustEof() {
        List<Token> tokens = new Scanner("").scanTokens();

        assertEquals(1, tokens.size());
        assertEquals(TokenType.EOF, tokens.get(0).type);
    }

    @Test
    void scansVarDeclaration() {
        List<Token> tokens = new Scanner("var x = 1;").scanTokens();

        assertEquals(6, tokens.size());
        assertEquals(TokenType.VAR, tokens.get(0).type);
        assertEquals(TokenType.IDENTIFIER, tokens.get(1).type);
        assertEquals(TokenType.EQUAL, tokens.get(2).type);
        assertEquals(TokenType.NUMBER, tokens.get(3).type);
        assertEquals(TokenType.SEMICOLON, tokens.get(4).type);
        assertEquals(TokenType.EOF, tokens.get(5).type);
    }

    @Test
    void scansIntegerWithNoDecimalPoint() {
        List<Token> tokens = new Scanner("123").scanTokens();

        assertEquals(2, tokens.size());
        assertEquals(TokenType.NUMBER, tokens.get(0).type);
        assertEquals(123.0, tokens.get(0).literal);
        assertEquals(TokenType.EOF, tokens.get(1).type);
    }

    @Test
    void scansDecimalNumberWithIntegerAndFractionalParts() {
        List<Token> tokens = new Scanner("123.456").scanTokens();

        assertEquals(2, tokens.size());
        assertEquals(TokenType.NUMBER, tokens.get(0).type);
        assertEquals(123.456, tokens.get(0).literal);
        assertEquals(TokenType.EOF, tokens.get(1).type);
    }

    @Test
    void scansNumberWithTrailingDecimalPointAndNoFractionalPart() {
        List<Token> tokens = new Scanner("123.").scanTokens();

        assertEquals(2, tokens.size());
        assertEquals(TokenType.NUMBER, tokens.get(0).type);
        assertEquals(123.0, tokens.get(0).literal);
        assertEquals(TokenType.EOF, tokens.get(1).type);
    }

    @Test
    void scansNumberWithLeadingDecimalPointAndNoIntegerPart() {
        List<Token> tokens = new Scanner(".1000").scanTokens();

        assertEquals(2, tokens.size());
        assertEquals(TokenType.NUMBER, tokens.get(0).type);
        assertEquals(0.1, tokens.get(0).literal);
        assertEquals(TokenType.EOF, tokens.get(1).type);
    }

    @Test
    void reportsErrorForUnterminatedStringAtEof() {
        List<Token> tokens = new Scanner("\"unterminated").scanTokens();

        assertEquals(1, tokens.size());
        assertEquals(TokenType.EOF, tokens.get(0).type);
        assertTrue(Lox.hadError);
    }

    @Test
    void identifierCannotStartWithADigit() {
        List<Token> tokens = new Scanner("123abc").scanTokens();

        // "123abc" is not a valid number nor a valid identifier -- it must be
        // scanned as a single invalid lexeme, not fragmented into a NUMBER
        // token ("123") followed by a separate IDENTIFIER token ("abc").
        assertEquals(2, tokens.size());
        assertEquals("123abc", tokens.get(0).lexeme);
        assertEquals(TokenType.EOF, tokens.get(1).type);
        assertTrue(Lox.hadError);
    }

    @Test
    void identifierCannotStartWithASpecialChar() {
        List<Token> tokens = new Scanner("@foo").scanTokens();

        // "@foo" is not a valid identifier -- the leading special char and
        // the trailing identifier-like chars must be scanned as a single
        // invalid lexeme, not fragmented into a skipped "@" plus a
        // separate, otherwise-valid IDENTIFIER token ("foo").
        assertEquals(2, tokens.size());
        assertEquals("@foo", tokens.get(0).lexeme);
        assertEquals(TokenType.EOF, tokens.get(1).type);
        assertTrue(Lox.hadError);
    }

    @Test
    void ignoresLineComments() {
        List<Token> tokens = new Scanner("// this is a comment\nvar x = 1;").scanTokens();

        assertEquals(6, tokens.size());
        assertEquals(TokenType.VAR, tokens.get(0).type);
        assertEquals(TokenType.IDENTIFIER, tokens.get(1).type);
        assertEquals(TokenType.EQUAL, tokens.get(2).type);
        assertEquals(TokenType.NUMBER, tokens.get(3).type);
        assertEquals(TokenType.SEMICOLON, tokens.get(4).type);
        assertEquals(TokenType.EOF, tokens.get(5).type);
    }

    @Test
    void singleSlashIsScannedAsDivisionNotAComment() {
        List<Token> tokens = new Scanner("/").scanTokens();

        assertEquals(2, tokens.size());
        assertEquals(TokenType.SLASH, tokens.get(0).type);
        assertEquals(TokenType.EOF, tokens.get(1).type);
    }

    @Test
    void ignoresMultiLineComments() {
        List<Token> tokens = new Scanner(
            "/* this is a\n" +
            "multi-line comment\n" +
            "spanning several lines */\n" +
            "var x = 1;"
        ).scanTokens();

        assertEquals(6, tokens.size());
        assertEquals(TokenType.VAR, tokens.get(0).type);
        assertEquals(TokenType.IDENTIFIER, tokens.get(1).type);
        assertEquals(TokenType.EQUAL, tokens.get(2).type);
        assertEquals(TokenType.NUMBER, tokens.get(3).type);
        assertEquals(TokenType.SEMICOLON, tokens.get(4).type);
        assertEquals(TokenType.EOF, tokens.get(5).type);
    }

    @Test
    void reportsErrorForUnclosedMultiLineCommentAtEof() {
        List<Token> tokens = new Scanner("/* this comment never closes").scanTokens();

        assertEquals(1, tokens.size());
        assertEquals(TokenType.EOF, tokens.get(0).type);
        assertTrue(Lox.hadError);
    }

    @Test
    void multiLineCommentContainingALoneAsteriskIsStillClosedCorrectly() {
        // The comment body contains a "*" that is not immediately followed
        // by "/" -- it must not be mistaken for the closing "*/".
        List<Token> tokens = new Scanner("/* a * b */\nvar x = 1;").scanTokens();

        assertEquals(6, tokens.size());
        assertEquals(TokenType.VAR, tokens.get(0).type);
        assertEquals(TokenType.IDENTIFIER, tokens.get(1).type);
        assertEquals(TokenType.EQUAL, tokens.get(2).type);
        assertEquals(TokenType.NUMBER, tokens.get(3).type);
        assertEquals(TokenType.SEMICOLON, tokens.get(4).type);
        assertEquals(TokenType.EOF, tokens.get(5).type);
        assertFalse(Lox.hadError);
    }
}
