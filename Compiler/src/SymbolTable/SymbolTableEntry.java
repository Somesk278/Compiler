/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SymbolTable;

import Parser.TokenType;
import SemanticActions.ParamInfo;

/**
 *
 * @author Solomon
 */
public class SymbolTableEntry {

    boolean var;
    boolean kword;
    boolean proc;
    boolean func;
    boolean funcres;
    boolean param;
    boolean array;
    boolean cons;
    boolean reserved;
    String name;
    int numParams;
    int address;
    TokenType type;
    ParamInfo pi;

    public SymbolTableEntry(String name) {
        this.name = name;
        this.var = false;
        this.kword = false;
        this.proc = false;
        this.func = false;
        this.funcres = false;
        this.param = false;
        this.array = false;
        this.cons = false;
        this.reserved = false;
        this.numParams = 0;
        int address = 0;
        this.type = type;
        this.pi = null;
    }
    
    public SymbolTableEntry() {
        
    }
    
    public String getName() {
        return this.name;
    }

    public boolean isVariable() {
        return this.var;
    }

    public void setIsVariable(boolean v) {
        this.var = v;
    }
    
    public void setNumParams(int num) {
        this.numParams = num;
    }

    public int getNumParams() {
        return this.numParams;
    }
    
    public void setAddress(int add) {
        this.address = add;
    }
    
    public boolean isKeyword() {
        return this.kword;
    }

    public void setIsKeyword(boolean k) {
        this.kword = k;
    }

    public boolean isProcedure() {
        return this.proc;
    }

    public void setIsProcedure(boolean p) {
        this.proc = p;
    }
    
    public void setType(TokenType t) {
        this.type = t;
    }

    public boolean isFunction() {
        return this.func;
    }

    public void setIsFunction(boolean f) {
        this.func = f;
    }

    public boolean isFunctionResult() {
        return this.funcres;
    }

    public void setIsFunctionResult(boolean f) {
        this.funcres = f;
    }

    public boolean isParameter() {
        return this.param;
    }

    public void setIsParameter(boolean p) {
        this.param = p;
    }

    public boolean isArray() {
        return this.array;
    }

    public void setIsArray(boolean a) {
        this.array = a;
    }

    public boolean isConstant() {
        return this.cons;
    }

    public void setIsConstant(boolean c) {
        this.cons = c;
    }

    public boolean isReserved() {
        return this.reserved;
    }

    public void setIsReserved(boolean r) {
        this.reserved = r;
    }
    
    public TokenType getType() {
        return this.type;
    }

    public ParamInfo getPI() {
        return this.pi;
    }
}
