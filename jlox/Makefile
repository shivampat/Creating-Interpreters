SRC_DIR   := src
BIN_DIR   := bin
MAIN      := com.shivam.lox.Lox
AST_TOOL  := com.shivam.tool.GenerateAst
AST_DIR   := src/com/shivam/lox

SOURCES := $(wildcard $(SRC_DIR)/com/shivam/lox/*.java) $(wildcard $(SRC_DIR)/com/shivam/tool/*.java)

.PHONY: build run ast clean

build: $(SOURCES)
	javac -d $(BIN_DIR) $(SOURCES)

# make run            -> interactive prompt
# make run FILE=x.lox -> run a script
run: build
	java -cp $(BIN_DIR) $(MAIN) $(FILE)

# regenerate Expr.java / Stmt.java from GenerateAst.java
ast: build
	java -cp $(BIN_DIR) $(AST_TOOL) $(AST_DIR)

clean:
	rm -rf $(BIN_DIR)/*
