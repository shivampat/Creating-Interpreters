SRC_DIR   := src
BIN_DIR   := bin
MAIN      := com.shivam.lox.Lox
AST_TOOL  := com.shivam.tool.GenerateAst
AST_DIR   := src/com/shivam/lox

LOX_SOURCES  := $(wildcard $(SRC_DIR)/com/shivam/lox/*.java)
TOOL_SOURCES := $(wildcard $(SRC_DIR)/com/shivam/tool/*.java)

.PHONY: build build-tools run ast clean

build: $(LOX_SOURCES)
	javac -d $(BIN_DIR) $(LOX_SOURCES)

build-tools: $(TOOL_SOURCES)
	javac -d $(BIN_DIR) $(TOOL_SOURCES)

# make run            -> interactive prompt
# make run FILE=x.lox -> run a script
run: build
	java -cp $(BIN_DIR) $(MAIN) $(FILE)

# regenerate Expr.java / Stmt.java from GenerateAst.java
ast: build-tools
	java -cp $(BIN_DIR) $(AST_TOOL) $(AST_DIR)

clean:
	rm -rf $(BIN_DIR)/*
