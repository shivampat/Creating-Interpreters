package com.shivam.lox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClassTest {

    @BeforeEach
    void resetLoxErrorState() {
        Lox.hadError = false;
        Lox.hadRuntimeError = false;
    }

    @Test 
    
    
}
