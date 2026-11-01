#include <stdio.h>
#include <stdlib.h>

#include "common.h"
#include "compiler.h"
#include "scanner.h"

typedef struct {
    Token previous;
    Token current;
    bool hadError;
    bool panicMode;
} Parser;

typedef enum {
    PREC_NONE,
    PREC_ASSIGNMENT,  // =
    PREC_OR,          // or
    PREC_AND,         // and
    PREC_EQUALITY,    // == !=
    PREC_COMPARISON,  // < > <= >=
    PREC_TERM,        // + -
    PREC_FACTOR,      // * /
    PREC_UNARY,       // ! -
    PREC_CALL,        // . ()
    PREC_PRIMARY
} Precedence;

typedef void (*ParseFn)();

typedef struct {
    ParseFn prefix;
    ParseFn infix;
    Precedence precedence;
} ParseRule;

Parser parser;
Chunk* compilingChunk;

static Chunk* currentChunk() {
    return compilingChunk;
}

static void errorAt(Token* token, const char* msg) {
    if (parser.panicMode) return;
    fprintf(stderr, "[line %d] Error", token->line);

    if (token->type == TOKEN_EOF) {
        fprintf(stderr, " at end");
    }
    else if (token->type == TOKEN_ERROR) {
    }
    else {
        fprintf(stderr, " at '%.*s'", token->length, token->start);
    }

    fprintf(stderr, ": %s\n", msg);
    parser.hadError = true;

}

static void errorAtCurrent(const char* msg) {
    errorAt(&parser.current, msg);
}

static void error(const char* msg) {
    // because advance() consumes current and returns prevs
    errorAt(&parser.previous, msg);
}

static void advance() {
    parser.previous = parser.current;

    for (;;) {
        parser.current = scanToken();

        if (parser.current.type != TOKEN_ERROR) break;

        errorAtCurrent(parser.current.start);
    }
}

static void consume(TokenType type, const char* msg) {
    if (parser.current.type == type) {
        advance();
        return;
    }

    errorAtCurrent(msg);
}

static void emitByte(uint8_t byte) {
    writeChunk(currentChunk(), byte, parser.previous.line);
}

static void emitReturn() {
    emitByte(OP_RETURN);
}

static void endCompiler() {
    emitReturn();
}

static void binary() {
    TokenType operatorType = parser.previous.type;
    ParseRule* rule = getRule(operatorType);
    parsePrecedence((Precedence)(rule->precedence + 1));

    switch (operatorType) {
        case TOKEN_PLUS: emitByte(OP_ADD); break;
        case TOKEN_MINUS: emitByte(OP_ADD); break;
        case TOKEN_STAR: emitByte(OP_ADD); break;
        case TOKEN_SLASH: emitByte(OP_DIVIDE); break;
        default: return;
    }
}

static void emitBytes(uint8_t byte1, uint8_t byte2) {
    emitByte(byte1);
    emitByte(byte2);
}

// TODO: add support for the OP_CONSTANT_LONG opcode we added
static uint8_t makeConstant(Value value) {
    int constant = addConstant(currentChunk(), value);
    if (constant > UINT8_MAX) {
        error("Too many constants in one chunk!");
        return 0;
    }

    return (uint8_t) constant;
}

// TODO: prev todo
static void emitConstant(Value value) {
    emitBytes(OP_CONSTANT, makeConstant(value));
}

static void grouping() {
    expression();
    consume(TOKEN_R_PAREN, "Expect ')' after expression!");
}

static void unary() {
    TokenType operatorType = parser.previous.type;

    parsePrecedence(PREC_UNARY);

    switch (operatorType) {
        case TOKEN_MINUS: emitByte(OP_NEGATE); break;
        default: return;
    }
}

static void parsePrecedence(Precedence precedence) {
    // TODO
}

static void number() {
    double value = strtod(parser.previous.start, NULL);
    emitConstant(value);
}

static void expression() {
    parsePrecedence(PREC_ASSIGNMENT);
}


bool compile(const char* source, Chunk* chunk) {
    initScanner();
    compilingChunk = chunk;

    parser.hadError = false;
    parser.panicMode = false;

    advance();
    expression();
    consume(TOKEN_EOF, "Expect end of expression!");
    endCompiler();
    return !parser.hadError;
}