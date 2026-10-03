CC := gcc
CFLAGS := -Wall -Wno-implicit-function-declaration -g

SRC_DIR := src
OBJ_DIR := build
BIN_DIR := bin

.PHONY: clean all

all: $(BIN_DIR)/main

run: $(BIN_DIR)/main
	@./$(BIN_DIR)/main

$(BIN_DIR)/main: $(OBJ_DIR)/chunk.o $(OBJ_DIR)/memory.o $(OBJ_DIR)/main.o $(OBJ_DIR)/debug.o $(OBJ_DIR)/value.o $(OBJ_DIR)/vm.o | $(BIN_DIR)
	$(CC) $(CFLAGS) $^ -o $@

$(OBJ_DIR)/chunk.o: $(SRC_DIR)/chunk.c $(SRC_DIR)/memory.h $(SRC_DIR)/chunk.h $(SRC_DIR)/value.h $(SRC_DIR)/common.h | $(OBJ_DIR)
	$(CC) $(CFLAGS) -c $< -o $@

$(OBJ_DIR)/memory.o: $(SRC_DIR)/memory.c $(SRC_DIR)/memory.h $(SRC_DIR)/common.h | $(OBJ_DIR)
	$(CC) $(CFLAGS) -c $< -o $@

$(OBJ_DIR)/main.o: $(SRC_DIR)/main.c $(SRC_DIR)/chunk.h $(SRC_DIR)/common.h | $(OBJ_DIR)
	$(CC) $(CFLAGS) -c $< -o $@

$(OBJ_DIR)/debug.o: $(SRC_DIR)/debug.c $(SRC_DIR)/debug.h $(SRC_DIR)/chunk.h $(SRC_DIR)/common.h | $(OBJ_DIR)
	$(CC) $(CFLAGS) -c $< -o $@

$(OBJ_DIR)/value.o: $(SRC_DIR)/value.c $(SRC_DIR)/value.h $(SRC_DIR)/memory.h $(SRC_DIR)/common.h | $(OBJ_DIR)
	$(CC) $(CFLAGS) -c $< -o $@

$(OBJ_DIR)/vm.o: $(SRC_DIR)/vm.c $(SRC_DIR)/vm.h $(SRC_DIR)/value.h $(SRC_DIR)/memory.h $(SRC_DIR)/memory.h $(SRC_DIR)/common.h $(SRC_DIR)/debug.h | $(OBJ_DIR)
	$(CC) $(CFLAGS) -c $< -o $@

$(OBJ_DIR):
	mkdir -p $@

$(BIN_DIR):
	mkdir -p $@

clean:
	@echo "Removing everything except source files"
	rm -f $(BIN_DIR)/main 
	rm -f $(OBJ_DIR)/*.o

# CC      := gcc
# CFLAGS  := -Wall -Wextra -g
# DEPFLAGS := -MMD -MP

# SRC_DIR := src
# OBJ_DIR := build
# BIN_DIR := bin

# SRCS := $(wildcard $(SRC_DIR)/*.c)
# OBJS := $(patsubst $(SRC_DIR)/%.c,$(OBJ_DIR)/%.o,$(SRCS))
# DEPS := $(OBJS:.o=.d)
# TARGET := $(BIN_DIR)/main

# .PHONY: all run clean

# all: $(TARGET)

# run: $(TARGET)
# 	@./$(TARGET)

# $(TARGET): $(OBJS) | $(BIN_DIR)
# 	$(CC) $(CFLAGS) $^ -o $@

# $(OBJ_DIR)/%.o: $(SRC_DIR)/%.c | $(OBJ_DIR)
# 	$(CC) $(CFLAGS) $(DEPFLAGS) -c $< -o $@

# $(OBJ_DIR) $(BIN_DIR):
# 	mkdir -p $@

# clean:
# 	rm -rf $(OBJ_DIR) $(BIN_DIR)

# -include $(DEPS)