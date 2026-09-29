#ifndef clox_chunk_h
#define clox_chunk_h

#include "common.h"
#include "value.h"

typedef enum {
    OP_RETURN,
    OP_CONSTANT
} OpCode;

typedef struct {
    int lineNum;
    int firstOffset;
} LineGroup;

typedef struct {
    int count;
    int capacity;
    LineGroup* groups;
} LineArray;

typedef struct {
    int count;
    int capacity;
    uint8_t* code;
    LineArray lines;
    ValueArray constants;
} Chunk;

void initChunk(Chunk* chunk);
void writeChunk(Chunk* chunk, uint8_t byte, int line);
void freeChunk(Chunk* chunk);
int addConstant(Chunk* chunk, Value value);
void initLineArray(LineArray* lines);
int addLine(Chunk* chunk, uint8_t offset, int line);
void freeLineArray(LineArray* lines);
int getLine(Chunk* chunk, uint8_t offset);

#endif