#include <stdio.h>

#include "common.h"
#include "memory.h"
#include "vm.h"
#include "debug.h"
#include "value.h"

VM vm;

static void resetStack() {
    vm.stackTop = vm.stack;
}

static void initStack() {
    vm.stackSize = STACK_START_SIZE;
    // vm.stackCount = 0;
    vm.stack = GROW_ARRAY(Value, vm.stack, 0, STACK_START_SIZE);
}

void initVM() {
    initStack();
    resetStack();
}

void freeVM() {
    FREE_ARRAY(Value, vm.stack, vm.stackSize);
    vm.stackSize = 0;
    // vm.stackCount = 0;
    vm.stack = NULL;
    vm.stackTop = NULL;
}

static inline uint32_t readLongByte() {
    #define READ_BYTE() (*vm.ip++)

    uint8_t p1 = READ_BYTE();
    uint8_t p2 = READ_BYTE();
    uint8_t p3 = READ_BYTE();

    #undef READ_BYTE
    printf("READ_LONG_BYTE %d", (p1 << 16) | (p2 << 8) | p3);

    return (p1 << 16) | (p2 << 8) | p3;
}

void push(Value value) {
    // Resize the stack first, if necessary
    if (vm.stackSize < vm.stackTop - vm.stack + 1) {
        #ifdef DEBUG_TRACE_EXECUTION
            printf("Resizing stack.\n");
        #endif 

        int oldCapacity = vm.stackSize;
        int newCapacity = GROW_CAPACITY(oldCapacity);
        int stackCount = vm.stackTop - vm.stack;
        vm.stack = GROW_ARRAY(Value, vm.stack, oldCapacity, newCapacity);
        // vm.stackTop = &vm.stack[vm.stackCount];
        vm.stackTop = &vm.stack[stackCount];
        vm.stackSize = newCapacity;
    }

    *vm.stackTop = value;
    vm.stackTop++;
    // vm.stackCount++;
}

Value pop() {
    vm.stackTop--;
    // vm.stackCount--;
    return *vm.stackTop;
}

static InterpreterResult run() {
    #define READ_BYTE() (*vm.ip++)
    #define READ_CONSTANT() (vm.chunk->constants.values[READ_BYTE()])
    #define READ_CONSTANT_LONG() (vm.chunk->constants.values[readLongByte()])
    #define BINARY_OP(op) \
        do { \
            double b = pop(); \
            double a = pop(); \
            push(a op b); \
        } while(false)

    for (;;) {
        #ifdef DEBUG_TRACE_EXECUTION
            printf("            ");
            for (Value* slot = vm.stack; slot < vm.stackTop; slot++) {
                printf("[ ");
                printValue(*slot);
                printf(" ]");
            }
            printf("\n");

            disassembleInstruction(vm.chunk, 
                                (int)(vm.ip - vm.chunk->code));
        #endif

        uint8_t instruction;
        switch (instruction = READ_BYTE()) {
            case OP_RETURN: {
                printValue(pop());
                printf("\n");
                return INTERPRET_OK;
            }
            case OP_CONSTANT: {
                Value constant = READ_CONSTANT();
                push(constant);
                // printValue(constant);
                // printf("\n");
                break;
            }
            case OP_CONSTANT_LONG: {
                Value constant = READ_CONSTANT_LONG();
                push(constant);
                // printValue(constant);
                // printf("\n");
                break;
            }
            case OP_NEGATE: {
                // push(-pop());
                *(vm.stackTop - 1) = -*(vm.stackTop - 1);
                break;
            }
            case OP_ADD: BINARY_OP(+); break;
            case OP_SUBTRACT: BINARY_OP(-); break;
            case OP_MULTIPLY: BINARY_OP(*); break;
            case OP_DIVIDE: BINARY_OP(/); break;

        }
    }

    #undef BINARY_OP
    #undef READ_CONSTANT_LONG
    #undef READ_CONSTANT
    #undef READ_BYTE
}

InterpreterResult interpret(Chunk* chunk) {
    vm.chunk = chunk;
    vm.ip = vm.chunk->code;
    return run();
}