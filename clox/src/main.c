#include "common.h"
#include "chunk.h"

int main(int argc, const char* argv[]) {
    Chunk chunk;
    initChunk(&chunk);
    initValueArray(&chunk.constants);
    initLineArray(&chunk.lines);

    // int constant = addConstant(&chunk, 1.2);
    // writeChunk(&chunk, OP_CONSTANT, 123);
    // writeChunk(&chunk, constant, 123);
    for (int i = 0; i <= 256; i++) {
        writeConstant(&chunk, 1.0 + 0.1 * i, 123);
    }

    writeChunk(&chunk, OP_RETURN, 123);

    disassembleChunk(&chunk, "test chunk");
    freeChunk(&chunk);
    return 0;
}