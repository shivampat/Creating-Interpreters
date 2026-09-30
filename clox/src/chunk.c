#include <stdlib.h>

#include "chunk.h"
#include "memory.h"

void initChunk(Chunk* chunk) {
    chunk->capacity = 0;
    chunk->count = 0;
    chunk->code = NULL;
    // chunk->lines = NULL;
}

void writeChunk(Chunk* chunk, uint8_t byte, int line) {
    if (chunk->capacity < chunk->count + 1) {
        int oldCapacity = chunk->capacity;
        int newCapacity = GROW_CAPACITY(oldCapacity);
        chunk->code = GROW_ARRAY(uint8_t, chunk->code, oldCapacity, newCapacity);
        chunk->capacity = newCapacity;
    }

    chunk->code[chunk->count] = byte;
    addLine(chunk, chunk->count, line);

    chunk->count++;
}

void freeChunk(Chunk* chunk) {
    FREE_ARRAY(uint8_t, chunk->code, chunk->capacity);
    freeLineArray(&chunk->lines);
    freeValueArray(&chunk->constants);
    initChunk(chunk);
}

int addConstant(Chunk* chunk, Value value) {
    writeValueArray(&chunk->constants, value);
    return chunk->constants.count - 1;
}

void initLineArray(LineArray* array) {
    array->capacity = 0;
    array->count = 0;
    array->groups = NULL;
}

int addLine(Chunk* chunk, uint8_t offset, int line) {
    // Ensure we have enough capacity to add new LineGroup
    if (chunk->lines.capacity < chunk->lines.count + 1) {
        int oldCapacity = chunk->lines.count;
        int newCapacity = GROW_CAPACITY(oldCapacity);
        chunk->lines.groups = GROW_ARRAY(LineGroup, chunk->lines.groups, oldCapacity, newCapacity);
    }

    // Don't add a new LineGroup if we're still on the same line
    if (chunk->lines.groups[chunk->lines.count].lineNum == line) {
        return chunk->lines.count;
    }

    LineGroup lgroup;    
    lgroup.firstOffset = offset;
    lgroup.lineNum = line;

    chunk->lines.groups[chunk->lines.count] = lgroup;
    chunk->lines.count++;
    return chunk->lines.count - 1;
}

void freeLineArray(LineArray* array) {
    FREE_ARRAY(LineGroup, array->groups, array->capacity);
    initLineArray(array);
}

int getLine(Chunk* chunk, uint8_t offset) {
    for (int i = 0; i < chunk->lines.count - 1; i++) { 
        LineGroup currGroup = chunk->lines.groups[i];
        LineGroup nextGroup = chunk->lines.groups[i+1];
        int startOffset = currGroup.firstOffset;
        int endOffset = nextGroup.firstOffset;

        if (startOffset <= offset && offset < endOffset) {
            return currGroup.lineNum;
        }
    }

    // TODO: add EOF support to make this check unnecessary
    // Flush out the last LineGroup
    if (chunk->lines.groups[chunk->lines.count - 1].firstOffset <= offset)
        return chunk->lines.groups[chunk->lines.count - 1].lineNum;

    return -1;
}