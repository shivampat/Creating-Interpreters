package com.shivam.lox;


class ReturnE extends RuntimeException {
    final Object value;
    
    ReturnE(Object value) {
        super(null, null, false, false);
        this.value = value;
    }
}