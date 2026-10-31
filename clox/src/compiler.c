#include <stdio.h>

#include "common.h"
#include "compiler.h"
#include "scanner.h"

typedef struct {
    Token previous;
    Token current;
    bool hadError;
    bool panicMode;
} Parser;

Parser parser;
Chunk* compilingChunk;

static Chunk* currentChunk() {
    return compilingChunk;
}

static void errorAtCurrent(const char* msg) {
    errorAt(&parser.current, msg);
}

static void error(const char* msg) {
    // because advance() consumes current and returns prevs
    errorAt(&parser.previous, msg);
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

void compile(const char* source) {
    initScanner(source);
    int line = -1;
    for (;;) {
        Token token = scanToken();

        if (token.line != line) {
            printf("%4d ", token.line);
            line = token.line;
        }
        else {
            printf("   | ");
        }
        printf("%2d '%.*s'\n", token.type, token.length, token.start);

        if (token.type == TOKEN_EOF) break;
    }
}