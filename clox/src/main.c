#include "common.h"
#include "chunk.h"
#include "debug.h"
#include "vm.h"



int main(int argc, const char* argv[]) {
    initVM();

    Chunk chunk;
    initChunk(&chunk);
    initValueArray(&chunk.constants);
    initLineArray(&chunk.lines);

    // -((1.2 + 3.4) / 5.6)
    // int constant = addConstant(&chunk, 1.2);
    // writeChunk(&chunk, OP_CONSTANT, 123);
    // writeChunk(&chunk, constant, 123);

    // constant = addConstant(&chunk, 3.4);
    // writeChunk(&chunk, OP_CONSTANT, 123);
    // writeChunk(&chunk, constant, 123);

    // writeChunk(&chunk, OP_ADD, 123);

    // constant = addConstant(&chunk, 5.6);
    // writeChunk(&chunk, OP_CONSTANT, 123);
    // writeChunk(&chunk, constant, 123);

    // writeChunk(&chunk, OP_DIVIDE, 123);
    // writeChunk(&chunk, OP_NEGATE, 123);


    // 1 * 2 + 3
    // postfix: 1 2 * 3 +
    // int constant = addConstant(&chunk, 1);
    // writeChunk(&chunk, OP_CONSTANT, 123);
    // writeChunk(&chunk, constant, 123);

    // constant = addConstant(&chunk, 2);
    // writeChunk(&chunk, OP_CONSTANT, 123);
    // writeChunk(&chunk, constant, 123);

    // writeChunk(&chunk, OP_MULTIPLY, 123);

    // constant = addConstant(&chunk, 3);
    // writeChunk(&chunk, OP_CONSTANT, 123);
    // writeChunk(&chunk, constant, 123);

    // writeChunk(&chunk, OP_ADD, 123);

    // 1 + 2 * 3
    // postfix: 1 2 3 * +
    int constant = addConstant(&chunk, 1);
    writeChunk(&chunk, OP_CONSTANT, 123);
    writeChunk(&chunk, constant, 123);

    constant = addConstant(&chunk, 2);
    writeChunk(&chunk, OP_CONSTANT, 123);
    writeChunk(&chunk, constant, 123);

    constant = addConstant(&chunk, 3);
    writeChunk(&chunk, OP_CONSTANT, 123);
    writeChunk(&chunk, constant, 123);
    
    writeChunk(&chunk, OP_MULTIPLY, 123);

    writeChunk(&chunk, OP_ADD, 123);

    // Return
    writeChunk(&chunk, OP_RETURN, 123);

    interpret(&chunk);
    freeVM();
    freeChunk(&chunk);
    return 0;
}