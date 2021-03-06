package SemanticActions;

import Lexer.Token;
import java.util.*;
import SymbolTable.*;
import CompilerError.*;
import Parser.*;
import static Parser.TokenType.*;

public class SemanticActions {

    private Stack<Object> semanticStack;
    private boolean insert;
    private boolean isArray;
    private boolean global;
    private boolean isParam;
    private int globalMemory;
    private int localMemory;
    private SymbolTable globalTable;
    private SymbolTable localTable;
    private SymbolTable constantTable;
    private int tableSize = 100;
    private Quadruple quadruple;
    private int globalStore = 0;
    private int localStore = 0;
    private SymbolTableEntry currentFunction = null;
    private Stack<Integer> paramCount;
    private Stack<SymbolTableEntry> nextParam;

    public enum etype {
        ARITHMETIC, RELATIONAL
    };

    public SemanticActions() {
        semanticStack = new Stack<Object>();
        insert = true;
        isArray = false;
        isParam = false;
        global = true;
        globalMemory = 0;
        localMemory = 0;
        globalTable = new SymbolTable(tableSize);
        localTable = new SymbolTable(tableSize);
        constantTable = new SymbolTable(tableSize);
        globalTable.installBuiltins();
        paramCount = new Stack<Integer>();
    }

    public void execute(int action, Token token) throws SemanticError, SymbolTableError {
        List Etrue;
        List Efalse;
        List skipElse;
        int beginLoop;
        etype et;
        SymbolTableEntry $$TEMP;

        switch (action) {

            case 1:
                insert = true;
                break;
            case 2:
                insert = false;
                break;
            case 3:
                TokenType typ = (TokenType) semanticStack.pop();
                if (isArray) {
                    Token u = (Token) semanticStack.pop();
                    int ub = Integer.parseInt(u.getValue());
                    Token l = (Token) semanticStack.pop();
                    int lb = Integer.parseInt(l.getValue());
                    int msize = (ub - lb) + 1;
                    while (!semanticStack.isEmpty()) {
                        Token id = (Token) semanticStack.pop();
                        if (global) {
                            ArrayEntry entry = new ArrayEntry(id.getValue(), globalMemory, typ, ub, lb);
                            globalTable.insert(entry);
                            globalMemory += msize;
                        } else {
                            ArrayEntry entry = new ArrayEntry(id.getValue(), localMemory, typ, ub, lb);
                            localMemory += msize;
                        }
                    }
                } else {
                    while (!semanticStack.isEmpty()) {
                        Token id = (Token) semanticStack.pop();
                        if (global) {
                            VariableEntry entry = new VariableEntry(id.getValue(), globalMemory, typ);
                            globalTable.insert(entry);
                            globalMemory += 1;
                        } else {
                            VariableEntry entry = new VariableEntry(id.getValue(), localMemory, typ);
                            localMemory += 1;
                        }
                    }
                }
                isArray = false;
                break;
            case 4:
                semanticStack.push(token.getType());
                break;
            case 5:
                insert = false;
                SymbolTableEntry id5 = (SymbolTableEntry) semanticStack.pop();
                gen("PROCBEGIN", id5);
                localStore = quadruple.getNextQuad();
                gen("alloc", "_");
                break;
            case 6:
                isArray = true;
                break;
            case 7:
                ConstantEntry cons = new ConstantEntry(token.getValue(), token.getType());
                semanticStack.push(cons);
                break;
            case 9:
                Token id1 = (Token) semanticStack.pop();
                IODeviceEntry iod1 = new IODeviceEntry(id1.getValue());
                globalTable.insert(iod1);
                iod1.setIsReserved(true);

                Token id2 = (Token) semanticStack.pop();
                IODeviceEntry iod2 = new IODeviceEntry(id2.getValue());
                globalTable.insert(iod2);
                iod2.setIsReserved(true);

                Token id3 = (Token) semanticStack.pop();
                ProcedureEntry pe = new ProcedureEntry(id3.getValue(), 0, new LinkedList<>());

                insert = false;
                break;
            case 11:
                global = true;
                localTable.delete();
                currentFunction = null;
                backpatch(localStore, localMemory);
                gen("free", localMemory);
                gen("PROCEND");
                break;
            case 13:
                semanticStack.push(token);
                break;
            case 15:
                FunctionEntry fe = new FunctionEntry(token.getValue());
                if (global) {
                    globalTable.insert(fe);
                } else {
                    localTable.insert(fe);
                }
                semanticStack.push(fe);
                VariableEntry $$FUN_NAME = create(fe.getName(), TokenType.INTEGER);
                fe.setResult($$FUN_NAME);
                global = false;
                localMemory = 0;
                break;
            case 16:
                Token tk = (Token) semanticStack.pop();
                TokenType type = tk.getType();
                SymbolTableEntry $$FUN_NAME16 = (SymbolTableEntry) semanticStack.pop();
                $$FUN_NAME16.setType(type);
                currentFunction = $$FUN_NAME16;
                break;
            case 17:
                ProcedureEntry pe17 = new ProcedureEntry(token.getType().toString());
                if (global) {
                    globalTable.insert(pe17);
                } else {
                    localTable.insert(pe17);
                }
                semanticStack.push(pe17);
                global = false;
                localMemory = 0;
                break;
            case 19:
                paramCount.push(0);
                break;
            case 20:
                SymbolTableEntry id20 = (SymbolTableEntry) semanticStack.peek();
                int pc20 = paramCount.pop();
                id20.setNumParams(pc20);
                break;
            case 21:
                Token id21 = (Token) semanticStack.peek();
                TokenType type21 = id21.getType();
                List<ParamInfo> pInfo = new LinkedList<ParamInfo>();
                int pc21 = paramCount.pop();

                while (type21 == TokenType.IDENTIFIER) {
                    ParamInfo pi = new ParamInfo();
                    if (isArray) {
                        ArrayEntry entry = new ArrayEntry(id21.getValue(), type21);
                        entry.setIsParameter(true);
                        int ub21 = Integer.parseInt(((Token) semanticStack.pop()).getValue());
                        int lb21 = Integer.parseInt(((Token) semanticStack.pop()).getValue());
                        entry.setUpperBound(ub21);
                        entry.setLowerBound(lb21);
                        pi.setUB(ub21);
                        pi.setLB(lb21);
                        pi.setArray(true);
                    } else {
                        VariableEntry entry = new VariableEntry(id21.getValue(), type21);
                        pi.setArray(false);
                    }
                    SymbolTableEntry entry21 = new SymbolTableEntry(id21.getValue());
                    entry21.setAddress(localMemory);
                    localMemory++;
                    entry21.setType(type21);
                    pi.setType(type21);
                    pc21++;
                    pInfo.add(pi);
                    semanticStack.pop();
                }
                isArray = false;
                break;
            case 22:
                et = (etype) semanticStack.pop();
                if (et != etype.RELATIONAL) {
                    throw SemanticError.eTypeMismatch(et);
                }

                Efalse = (LinkedList) semanticStack.pop();
                Etrue = (LinkedList) semanticStack.peek();

                backpatch(Etrue, quadruple.getNextQuad());

                semanticStack.push(Efalse);
                break;
            case 24:
                beginLoop = quadruple.getNextQuad();
                semanticStack.push(beginLoop);
                break;
            case 25:
                et = (etype) semanticStack.pop();
                if (et != etype.RELATIONAL) {
                    throw SemanticError.eTypeMismatch(et);
                }

                Efalse = (LinkedList) semanticStack.pop();
                Etrue = (LinkedList) semanticStack.peek();

                backpatch(Etrue, quadruple.getNextQuad());

                semanticStack.push(Efalse);
                break;
            case 26:
                Efalse = (LinkedList) semanticStack.pop();
                Etrue = (LinkedList) semanticStack.pop();
                beginLoop = (int) semanticStack.pop();
                gen("goto", Integer.toString(beginLoop));
                backpatch(Efalse, quadruple.getNextQuad());
                break;
            case 27:
                skipElse = makeList(quadruple.getNextQuad());
                semanticStack.push(skipElse);
                gen("goto", "_");

                Efalse = (LinkedList) semanticStack.peek();
                backpatch(Efalse, quadruple.getNextQuad());
                break;
            case 28:
                skipElse = (LinkedList) semanticStack.pop();
                Efalse = (LinkedList) semanticStack.pop();
                Etrue = (LinkedList) semanticStack.pop();

                backpatch(skipElse, quadruple.getNextQuad());
                break;
            case 29:
                Efalse = (LinkedList) semanticStack.pop();
                Etrue = (LinkedList) semanticStack.pop();

                backpatch(Efalse, quadruple.getNextQuad());
                break;
            case 30:
                String name = token.getValue();
                SymbolTableEntry id;
                if (global) {
                    id = globalTable.lookup(name);
                } else {
                    id = localTable.lookup(name);
                }

                if (id == null) {
                    throw SemanticError.undeclaredVariable(name);
                }

                semanticStack.push(id);
                break;
            case 31:
                SymbolTableEntry idsec = (SymbolTableEntry) semanticStack.pop();
                SymbolTableEntry offset = (SymbolTableEntry) semanticStack.pop();
                SymbolTableEntry idfir = (SymbolTableEntry) semanticStack.pop();

                if (typecheck(idfir, idsec) == 3) {
                    throw SemanticError.mismatch(idfir.getName(), idsec.getName());
                }
                if (typecheck(idfir, idsec) == 2) {
                    $$TEMP = create("TEMP", TokenType.REAL);
                    gen("ltof", idsec, $$TEMP);
                    if (offset == null) {
                        gen("move", idsec, idfir);
                    } else {
                        gen("store", $$TEMP, offset, idfir);
                    }
                } else if (offset == null) {
                    gen("move", idsec, idfir);
                } else {
                    gen("store", idsec, offset, idfir);
                }
                break;
            case 32:
                et = (etype) semanticStack.pop();
                if (et != etype.ARITHMETIC) {
                    throw SemanticError.eTypeMismatch(et);
                }
                if (!(((SymbolTableEntry) semanticStack.peek()).isArray())) {
                    throw SemanticError.arrayError();
                }
                break;
            case 33:
                et = (etype) semanticStack.pop();
                if (et != etype.ARITHMETIC) {
                    throw SemanticError.eTypeMismatch(et);
                }
                SymbolTableEntry id33 = (SymbolTableEntry) semanticStack.pop();
                if (id33.getType() != TokenType.INTEGER) {
                    throw SemanticError.arrayBoundError();
                }
                $$TEMP = create("TEMP", TokenType.INTEGER);
                ArrayEntry ae = (ArrayEntry) semanticStack.get(0);
                gen("sub", id33, ae.getLowerBound(), $$TEMP);
                break;
            case 34:
                et = (etype) semanticStack.pop();
                if (((SymbolTableEntry) semanticStack.peek()).isFunction()) {
                    execute(52, token);
                } else {
                    semanticStack.push(null);
                }
                break;
            case 35:
                paramCount.push(0);
                ProcedureEntry entry35 = (ProcedureEntry) semanticStack.peek();
                nextParam.push(entry35);
                break;
            case 36:
                etype et36 = (etype) semanticStack.pop();
                ProcedureEntry entry36 = (ProcedureEntry) semanticStack.pop();

                if (entry36.getNumParam() != 0) {
                    throw SemanticError.wrongNumParams();
                }

                gen("call", entry36, 0);
                break;
            case 37:
                etype et37 = (etype) semanticStack.pop();
                if (et37 != etype.ARITHMETIC) {
                    throw SemanticError.eTypeMismatch(et37);
                }
                SymbolTableEntry id37 = (SymbolTableEntry) semanticStack.peek();

                if (!(id37.isVariable() || id37.isConstant() || id37.isFunctionResult() || id37.isArray())) {
                    throw SemanticError.typeMismatch(id37);
                }

                int pcnew = paramCount.pop();
                pcnew++;
                paramCount.push(pcnew);

                SymbolTableEntry procOrFun = (SymbolTableEntry) semanticStack.get(0);
                if (!procOrFun.getName().equals("read") || !procOrFun.getName().equals("write")) {
                    if (paramCount.peek() > procOrFun.getNumParams()) {
                        throw SemanticError.wrongNumParams();
                    }
                    if (id37.getType() != nextParam.peek().getType()) {
                        throw SemanticError.typeMismatch(id37);
                    }
                    if (nextParam.peek().isArray()) {
                        if ((((ArrayEntry) id37).getLowerBound() != ((ArrayEntry) nextParam.peek()).getLowerBound())
                                || (((ArrayEntry) id37).getUpperBound() != ((ArrayEntry) nextParam.peek()).getUpperBound())) {
                            throw SemanticError.arrayBoundError();
                        }
                    }

                    nextParam.pop();
                }
                break;
            case 38:
                et = (etype) semanticStack.pop();
                if (et != etype.ARITHMETIC) {
                    throw SemanticError.eTypeMismatch(et);
                }
                semanticStack.push(token);
                break;
            case 39:
                et = (etype) semanticStack.pop();
                if (et != etype.ARITHMETIC) {
                    throw SemanticError.eTypeMismatch(et);
                }

                SymbolTableEntry id39 = (SymbolTableEntry) semanticStack.pop();
                Token operator = (Token) semanticStack.pop();
                SymbolTableEntry id392 = (SymbolTableEntry) semanticStack.pop();

                if (typecheck(id39, id392) == 2) {
                    $$TEMP = create("TEMP1", TokenType.REAL);
                    gen("ltof", id392, $$TEMP);
                    gen(getString(token), id39, $$TEMP, "_");
                } else if (typecheck(id39, id392) == 3) {
                    $$TEMP = create("TEMP1", TokenType.REAL);
                    gen("ltof", id39, $$TEMP);
                    gen(getString(token), $$TEMP, id392, "_");
                } else {
                    gen(getString(token), id39, id392, "_");
                }
                gen("goto", "_");
                Etrue = makeList(quadruple.getNextQuad() - 2);
                Efalse = makeList(quadruple.getNextQuad() - 1);
                semanticStack.push(Etrue);
                semanticStack.push(Efalse);
                semanticStack.push(etype.RELATIONAL);
                break;
            case 40:
                semanticStack.push(token);
                break;
            case 42:
                et = (etype) semanticStack.pop();
                if (token.getOpType() == Token.OperatorType.OR) {
                    if (et != etype.RELATIONAL) {
                        throw SemanticError.eTypeMismatch(et);
                    }
                    Efalse = (LinkedList) semanticStack.peek();
                    backpatch(Efalse, quadruple.getNextQuad());
                } else if (et == etype.ARITHMETIC) {

                }
                semanticStack.push(token);
                break;
            case 43:
                et = (etype) semanticStack.pop();
                if (et == etype.RELATIONAL) {
                    if (token.getOpType() == Token.OperatorType.OR) {
                        List Efalse2 = (LinkedList) semanticStack.pop();
                        List Etrue2 = (LinkedList) semanticStack.pop();
                        Token id43 = (Token) semanticStack.pop();
                        List Efalse1 = (LinkedList) semanticStack.pop();
                        List Etrue1 = (LinkedList) semanticStack.pop();

                        Etrue = merge(Etrue1, Etrue2);
                        Efalse = Efalse2;

                        semanticStack.push(Etrue);
                        semanticStack.push(Efalse);
                        semanticStack.push(etype.RELATIONAL);
                    }
                } else {
                    SymbolTableEntry idsecc = (SymbolTableEntry) semanticStack.pop();
                    SymbolTableEntry idfirr = (SymbolTableEntry) semanticStack.pop();
                    if (typecheck(idfirr, idsecc) == 0) {
                        $$TEMP = create("TEMP", TokenType.INTEGER);
                        gen(getString(token), idfirr, idsecc, $$TEMP);
                        semanticStack.push($$TEMP);
                    }

                    if (typecheck(idfirr, idsecc) == 1) {
                        $$TEMP = create("TEMP", TokenType.REAL);
                        gen("f" + getString(token), idfirr, idsecc, $$TEMP);
                        semanticStack.push($$TEMP);
                    }

                    if (typecheck(idfirr, idsecc) == 2) {
                        SymbolTableEntry $$TEMP1 = create("TEMP1", TokenType.REAL);
                        gen("ltof", idsecc, $$TEMP1);
                        SymbolTableEntry $$TEMP2 = create("TEMP2", TokenType.REAL);
                        gen("f" + getString(token), idfirr, $$TEMP1, $$TEMP2);
                        semanticStack.push($$TEMP2);
                    }

                    if (typecheck(idfirr, idsecc) == 3) {
                        SymbolTableEntry $$TEMP1 = create("TEMP1", TokenType.REAL);
                        gen("ltof", idfirr, $$TEMP1);
                        SymbolTableEntry $$TEMP2 = create("TEMP2", TokenType.REAL);
                        gen("f" + getString(token), $$TEMP1, idsecc, $$TEMP2);
                        semanticStack.push($$TEMP2);
                    }
                    semanticStack.push(etype.ARITHMETIC);
                }
                break;
            case 44:
                et = (etype) semanticStack.pop();
                if (et == etype.RELATIONAL) {
                    if (token.getOpType() == Token.OperatorType.AND) {
                        Etrue = (LinkedList) semanticStack.peek();
                        backpatch(Etrue, quadruple.getNextQuad());
                    }
                }
                semanticStack.push(token);
                break;
            case 45:
                et = (etype) semanticStack.pop();
                if (token.getOpType() == Token.OperatorType.AND) {
                    if (et != etype.RELATIONAL) {
                        throw SemanticError.eTypeMismatch(et);
                    }
                    List Efalse2 = (LinkedList) semanticStack.pop();
                    List Etrue2 = (LinkedList) semanticStack.pop();
                    Token id43 = (Token) semanticStack.pop();
                    List Efalse1 = (LinkedList) semanticStack.pop();
                    List Etrue1 = (LinkedList) semanticStack.pop();

                    Efalse = merge(Efalse1, Efalse2);
                    Etrue = Etrue2;

                    semanticStack.push(Etrue);
                    semanticStack.push(Efalse);
                    semanticStack.push(etype.RELATIONAL);
                } else {
                    if (et != etype.ARITHMETIC) {
                        throw SemanticError.eTypeMismatch(et);
                    }
                    SymbolTableEntry idseccc = (SymbolTableEntry) semanticStack.pop();
                    Token op = (Token) semanticStack.pop();
                    SymbolTableEntry idfirrr = (SymbolTableEntry) semanticStack.pop();

                    if ((typecheck(idfirrr, idseccc) != 0) && (op.getType().equals("mod"))) {
                        throw SemanticError.modError(idfirrr.getType().toString(), idseccc.getType().toString());
                    }

                    if ((typecheck(idfirrr, idseccc)) == 0) {
                        if (op.getType().equals("mod")) {
                            SymbolTableEntry $$TEMP1 = create("TEMP1", TokenType.INTEGER);
                            gen("mov", idfirrr, $$TEMP1);
                            SymbolTableEntry $$TEMP2 = create("TEMP2", TokenType.INTEGER);
                            gen("move", $$TEMP1, $$TEMP2);
                            gen("sub", $$TEMP2, idseccc, $$TEMP1);
                            gen("bge", $$TEMP1, idseccc, quadruple.getNextQuad() - 2);
                            semanticStack.push($$TEMP2);
                        } else if (op.getType().equals("/")) {
                            SymbolTableEntry $$TEMP1 = create("TEMP1", TokenType.REAL);
                            gen("ltof", idfirrr, $$TEMP1);
                            SymbolTableEntry $$TEMP2 = create("TEMP2", TokenType.REAL);
                            gen("ltof", idseccc, $$TEMP2);
                            SymbolTableEntry $$TEMP3 = create("TEMP3", TokenType.REAL);
                            gen("fdiv", $$TEMP1, $$TEMP2, $$TEMP3);
                            semanticStack.push($$TEMP2);
                        } else {
                            $$TEMP = create("TEMP1", TokenType.INTEGER);
                            gen(getString(token), idfirrr, idseccc, $$TEMP);
                            semanticStack.push($$TEMP);
                        }
                    }

                    if ((typecheck(idfirrr, idseccc)) == 1) {
                        if (op.getType().equals("div")) {
                            SymbolTableEntry $$TEMP1 = create("TEMP1", TokenType.INTEGER);
                            gen("ftol", idfirrr, $$TEMP1);
                            SymbolTableEntry $$TEMP2 = create("TEMP2", TokenType.INTEGER);
                            gen("ftol", idseccc, $$TEMP2);
                            SymbolTableEntry $$TEMP3 = create("TEMP3", TokenType.INTEGER);
                            gen("div", $$TEMP1, $$TEMP2, $$TEMP3);
                            semanticStack.push($$TEMP2);
                        } else {
                            $$TEMP = create("TEMP", TokenType.REAL);
                            gen("f" + getString(token), idfirrr, idseccc, $$TEMP);
                            semanticStack.push($$TEMP);
                        }
                    }

                    if ((typecheck(idfirrr, idseccc)) == 2) {
                        if (op.getType().equals("div")) {
                            SymbolTableEntry $$TEMP1 = create("TEMP1", TokenType.INTEGER);
                            gen("ftol", idfirrr, $$TEMP1);
                            SymbolTableEntry $$TEMP2 = create("TEMP2", TokenType.INTEGER);
                            gen("div", $$TEMP1, idseccc, $$TEMP2);
                            semanticStack.push($$TEMP2);
                        } else {
                            SymbolTableEntry $$TEMP1 = create("TEMP1", TokenType.REAL);
                            gen("ltof" + token.getType(), idseccc, $$TEMP1);
                            SymbolTableEntry $$TEMP2 = create("TEMP2", TokenType.REAL);
                            gen("f" + getString(token), idfirrr, $$TEMP1, $$TEMP2);
                            semanticStack.push($$TEMP2);
                        }
                    }

                    if ((typecheck(idfirrr, idseccc)) == 3) {
                        if (op.getType().equals("div")) {
                            SymbolTableEntry $$TEMP1 = create("TEMP1", TokenType.INTEGER);
                            gen("ftol", idseccc, $$TEMP1);
                            SymbolTableEntry $$TEMP2 = create("TEMP2", TokenType.INTEGER);
                            gen("div", idfirrr, $$TEMP1, $$TEMP2);
                            semanticStack.push($$TEMP2);
                        } else {
                            SymbolTableEntry $$TEMP1 = create("TEMP1", TokenType.REAL);
                            gen("ltof" + token.getType(), idfirrr, $$TEMP1);
                            SymbolTableEntry $$TEMP2 = create("TEMP2", TokenType.REAL);
                            gen("f" + getString(token), $$TEMP1, idseccc, $$TEMP2);
                            semanticStack.push($$TEMP2);
                        }
                    }
                    semanticStack.push(etype.ARITHMETIC);
                }
                break;
            case 46:
                if (token.getType() == TokenType.IDENTIFIER) {
                    if (global) {
                        id = globalTable.lookup(token.getValue());
                    } else {
                        id = localTable.lookup(token.getValue());
                    }
                    if (id == null) {
                        throw SemanticError.undeclaredVariable(token.getValue());
                    } else {
                        semanticStack.push(id);
                    }
                }

                if ((token.getType() == TokenType.INTCONSTANT)
                        || (token.getType() == TokenType.REALCONSTANT)) {
                    id = constantTable.lookup(token.getValue());
                    ConstantEntry entry;
                    if (id == null) {
                        if (token.getType() == TokenType.INTCONSTANT) {
                            entry = new ConstantEntry(token.getValue(), TokenType.INTEGER);
                        } else {
                            entry = new ConstantEntry(token.getValue(), TokenType.REAL);
                        }
                        constantTable.insert(entry);
                        semanticStack.push(entry);
                    }
                }
                break;

            case 47:
                et = (etype) semanticStack.pop();
                if (et != etype.RELATIONAL) {
                    throw SemanticError.eTypeMismatch(et);
                }

                Etrue = (LinkedList) semanticStack.pop();
                Efalse = (LinkedList) semanticStack.pop();

                semanticStack.push(Etrue);
                semanticStack.push(Efalse);
                semanticStack.push(etype.RELATIONAL);
                break;

            case 48:
                SymbolTableEntry offset2 = (SymbolTableEntry) semanticStack.pop();
                if (offset2 != null) {
                    SymbolTableEntry id4 = (SymbolTableEntry) semanticStack.pop();
                    SymbolTableEntry eType = (SymbolTableEntry) semanticStack.pop();

                    if (offset2.isFunction()) {
                        execute(52, token);
                    } else {
                        $$TEMP = create("TEMP", id4.getType());
                        gen("load", id4, offset2, $$TEMP);
                        semanticStack.push($$TEMP);
                    }
                }
                semanticStack.push(etype.ARITHMETIC);
                break;
            case 49:
                et = (etype) semanticStack.pop();
                if (et != etype.ARITHMETIC) {
                    throw SemanticError.eTypeMismatch(et);
                }

                SymbolTableEntry id49 = (SymbolTableEntry) semanticStack.peek();
                semanticStack.push(et);

                if (!id49.isFunction()) {
                    throw SemanticError.typeMismatch(id49);
                }

                paramCount.push(0);
                nextParam.push(id49.getPI());
                break;
            case 50:
                Stack<SymbolTableEntry> reverseStack = new Stack<SymbolTableEntry>();
                while (!semanticStack.empty()) {
                    SymbolTableEntry tempid = (SymbolTableEntry) semanticStack.pop();
                    reverseStack.push(tempid);
                }

                while (!reverseStack.empty()) {
                    SymbolTableEntry tempid = (SymbolTableEntry) reverseStack.pop();
                    gen("param", tempid);
                    localMemory++;
                }

                int pc50 = paramCount.pop();
                SymbolTableEntry id50 = (SymbolTableEntry) semanticStack.pop();
                if (pc50 > id50.getNumParams()) {
                    throw SemanticError.wrongNumParams();
                }
                gen("call", id50, pc50);
                nextParam.pop();
                $$TEMP = create("TEMP", id50.getType());
                gen("move", ((FunctionEntry) id50).getResult(), $$TEMP);
                semanticStack.push($$TEMP);
                semanticStack.push(etype.ARITHMETIC);
                break;
            case 51:
                SymbolTableEntry id51 = (SymbolTableEntry) semanticStack.pop();
                if (id51.getName() == "read") {
                    SA51Read();
                } else if (id51.getName() == "write") {
                    SA51Write();
                } else {
                    if (paramCount.peek() != id51.getNumParams()) {
                        throw SemanticError.wrongNumParams();
                    }
                    Stack<SymbolTableEntry> reverseStack51 = new Stack<SymbolTableEntry>();
                    while (!semanticStack.empty()) {
                        SymbolTableEntry tempid = (SymbolTableEntry) semanticStack.pop();
                        reverseStack51.push(tempid);
                    }

                    while (!reverseStack51.empty()) {
                        SymbolTableEntry id511 = reverseStack51.pop();
                        gen("param", id511);
                        localMemory++;
                    }
                }
                gen("call", id51, paramCount.pop());
                break;
            case 52:
                et = (etype) semanticStack.pop();
                SymbolTableEntry id52 = (SymbolTableEntry) semanticStack.pop();
                if (!(id52.isFunction())) {
                    throw SemanticError.typeMismatch(id52);
                }

                if (id52.getNumParams() > 0) {
                    throw SemanticError.wrongNumParams();
                }

                gen("call", id52, 0);
                $$TEMP = create("TEMP", id52.getType());
                semanticStack.push($$TEMP);
                semanticStack.push(etype.ARITHMETIC);
                break;
            case 54:
                SymbolTableEntry proc = (SymbolTableEntry) semanticStack.peek();
                if (!(proc.isProcedure())) {
                    throw SemanticError.notAProcedure(proc);
                }
                break;
            case 55:
                backpatch(globalStore, globalMemory);
                gen("free", globalMemory);
                gen("PROCEND");
                break;
            case 56:
                gen("PROCBEGIN", "main");
                globalStore = quadruple.getNextQuad();
                gen("alloc", "_");
                break;

        }
    }

    public void SA51Write() {
        Stack<SymbolTableEntry> reverseStack = new Stack<SymbolTableEntry>();
        while (!semanticStack.empty()) {
            SymbolTableEntry tempid = (SymbolTableEntry) semanticStack.pop();
            reverseStack.push(tempid);
        }

        while (!reverseStack.empty()) {
            SymbolTableEntry id = (SymbolTableEntry) reverseStack.pop();
            gen("print", id.getName() + "=");
            if (id.getType() == TokenType.REAL) {
                gen("foutp", id);
            } else {
                gen("outp", id);
            }
            gen("newl");
        }
        paramCount.pop();
    }

    public void SA51Read() {
        Stack<SymbolTableEntry> reverseStack = new Stack<SymbolTableEntry>();
        while (!semanticStack.empty()) {
            SymbolTableEntry tempid = (SymbolTableEntry) semanticStack.pop();
            reverseStack.push(tempid);
        }

        while (!reverseStack.empty()) {
            SymbolTableEntry id = (SymbolTableEntry) reverseStack.pop();
            if (id.getType() == TokenType.REAL) {
                gen("finp", id);
            } else {
                gen("inp", id);
            }
        }
        paramCount.pop();
    }

    public VariableEntry create(String name, TokenType type) throws SymbolTableError {
        VariableEntry $$NAME = new VariableEntry(name, -globalMemory, type);
        globalTable.insert($$NAME);
        globalMemory++;
        return $$NAME;
    }

    public void gen(String tviCode) {
        String[] quad = {tviCode, null, null, null};
        quadruple.addQuad(quad);
    }

    public void gen(String tviCode, SymbolTableEntry op) {
        String[] quad = {tviCode, op.getName(), null, null};
        quadruple.addQuad(quad);
    }

    public void gen(String tviCode, int in) {
        String[] quad = {tviCode, String.valueOf(in), null, null};
        quadruple.addQuad(quad);
    }

    //36
    public void gen(String tviCode, SymbolTableEntry op, int in) {
        String[] quad = {tviCode, op.getName(), Integer.toString(in), null};
        quadruple.addQuad(quad);
    }

    private void gen(String tviCode, String op) {
        String[] quad = {tviCode, op, null, null};
        quadruple.addQuad(quad);
    }

    //31
    //FIXXOIEJFOIWEFJOIEF
    private void gen(String tviCode, SymbolTableEntry op1, SymbolTableEntry op2) {
        String[] quad = {tviCode, op1.getName(), op2.getName(), null};
        quadruple.addQuad(quad);
    }

    //45
    //FIXEOWIFJWEOIFJWE
    private void gen(String tviCode, SymbolTableEntry op1, SymbolTableEntry op2, int in) {
        String[] quad = {tviCode, op1.getName(), op2.getName(), Integer.toString(in)};
        quadruple.addQuad(quad);
    }

    //FJWEOIFJWOEIFJWEIOFJWOEIFJWOEIFJWEOIFJOWJ
    private void gen(String tviCode, SymbolTableEntry op1, SymbolTableEntry op2, SymbolTableEntry op3) {
        String[] quad = {tviCode, op1.getName(), op2.getName(), op3.getName()};
        quadruple.addQuad(quad);
    }

    //33
    //FIXOIWJFOIWEJF
    private void gen(String tviCode, SymbolTableEntry op1, int in,
            SymbolTableEntry op2) {
        String[] quad = {tviCode, op1.getName(), Integer.toString(in), op2.getName()};
        quadruple.addQuad(quad);
    }

    //39
    //OIWEJFOIEWJ
    private void gen(String tviCode, SymbolTableEntry op1, SymbolTableEntry op2,
            String s) {
        String[] quad = {tviCode, op1.getName(), op2.getName(), s};
        quadruple.addQuad(quad);
    }

    public int typecheck(Token id1, Token id2) {
        if (id1.getType().equals(INTEGER) && id2.getType().equals(INTEGER)) {
            return 0;
        } else if (id1.getType().equals(REAL) && id2.getType().equals(REAL)) {
            return 1;
        } else if (id1.getType().equals(REAL) && id2.getType().equals(INTEGER)) {
            return 2;
        } else {
            return 3;
        }
    }

    public int typecheck(SymbolTableEntry id1, SymbolTableEntry id2) {
        if (id1.getType().equals(INTEGER) && id2.getType().equals(INTEGER)) {
            return 0;
        } else if (id1.getType().equals(REAL) && id2.getType().equals(REAL)) {
            return 1;
        } else if (id1.getType().equals(REAL) && id2.getType().equals(INTEGER)) {
            return 2;
        } else {
            return 3;
        }
    }

    public void backpatch(int p, int i) {
        quadruple.setField(i, 1, Integer.toString(p));
    }

    public void backpatch(List<Integer> p, int i) {
        for (Integer x : p) {
            String[] quad = quadruple.getQuad(x);
            for (int j = 0; j < quad.length; j++) {
                quadruple.setField(x, j, Integer.toString(i));
            }
        }
    }

    public void semanticStackDump() {
        for (Object obj : semanticStack) {
            System.out.println(obj + " ");
        }
    }

    public List makeList(int i) {
        List<Integer> list = new LinkedList<Integer>();
        list.add(i);
        return list;
    }

    public List merge(List p1, List p2) {
        List<Integer> list = new LinkedList<Integer>();
        list.addAll(p1);
        list.addAll(p2);
        return list;
    }

    public String getString(Token token) {
        TokenType type = token.getType();
        String value = token.getValue();
        String str = "";

        if (type == TokenType.ADDOP) {
            switch (value) {
                case "+":
                    str = "add";
                    break;
                case "-":
                    str = "sub";
                    break;
            }
        } else if (type == TokenType.MULOP) {
            switch (value) {
                case "*":
                    str = "mul";
                    break;
                case "/":
                    str = "div";
                    break;
            }
        } else if (type == TokenType.RELOP) {
            switch (value) {
                case "=":
                    str = "beq";
                    break;
                case "<>":
                    str = "bne";
                    break;
                case "<":
                    str = "blt";
                    break;
                case ">":
                    str = "bgt";
                    break;
                case "<=":
                    str = "ble";
                    break;
                case ">=":
                    str = "bge";
                    break;
            }
        }

        return str;
    }
}
