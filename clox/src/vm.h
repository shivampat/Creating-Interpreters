#ifndef clox_vm_h
#define clox_vm_h

#include "chunk.h"
#include "value.h"

#define STACK_START_SIZE 8

typedef struct {
    Chunk* chunk;
    uint8_t* ip;
    // Value stack[STACK_MAX];
    int stackSize;
    // int stackCount;
    Value* stack;
    Value* stackTop;
} VM;

typedef enum {
    INTERPRET_OK,
    INTERPRET_COMPILE_ERROR,
    INTERPRET_RUNTIME_ERROR
} InterpreterResult;

void initVM();
void freeVM();
InterpreterResult interpret(Chunk* chunk);
void push(Value value);
Value pop();

#endif