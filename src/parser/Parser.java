package parser;

import constant.TypeCode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.*;

import static constant.TypeCode.*;

class Parser {
    private final String dydFilename;
    private final String errFilename;
    private final String varTableFilename;
    private final String procFilename;
    private final String sourceFilename;

    private Scanner sourceScanner;
    private PrintWriter errorPw;

    private String currentWord;
    private int currentWordType;
    private int currentLine;
    private final StringBuilder currentLineSb = new StringBuilder();
    private int currentLevel;
    private final Set<Variable> variableSet = new HashSet<>();
    private final List<Variable> variableList = new ArrayList<>();
    private final Set<Procedure> procedureSet = new HashSet<>();
    private final List<Procedure> procedureList = new ArrayList<>();
    private Procedure currentProc;
    private final List<String> wordList = new ArrayList<>();
    private final List<Integer> typeList = new ArrayList<>();
    private int currentIndex;

    private static final String PROGRAM_ERROR_DYD = "dyd file error!";

    Parser(String filename) {
        int index = filename.lastIndexOf(".");
        String name;
        if (index == -1) {
            name = filename;
        } else {
            name = filename.substring(0, index);
        }

        dydFilename = name + ".dyd";
        errFilename = name + ".err";
        varTableFilename = name + ".var";
        procFilename = name + ".pro";
        sourceFilename = name + ".pas";
    }

    private void programErrorAndExit(String reason) {
        System.err.println(reason);
        System.exit(-1);
    }

    private void printParseError(String reason) {
        String info = String.format("***LINE:%d  %s", currentLine, reason);
        System.err.println(info);
        errorPw.println(info);
    }

    private void advance() {
        currentIndex++;
        if (currentIndex < wordList.size()) {
            currentWord = wordList.get(currentIndex);
            currentWordType = typeList.get(currentIndex);
        } else {
            currentWord = null;
        }

        if (currentWord != null) {
            String string = currentLineSb.toString();
            while (!string.startsWith(currentWord) && sourceScanner.hasNextLine()) {
                string = sourceScanner.nextLine();
                currentLineSb.delete(0, currentLineSb.length());
                currentLineSb.append(string);
                removeLineSpaceStart();
                string = currentLineSb.toString();
                currentLine++;
            }

            if (!string.startsWith(currentWord)) {
                programErrorAndExit("源文件与dyd文件内容不匹配: " + string + ": " + currentWord);
            }
            currentLineSb.delete(0, currentWord.length());
            removeLineSpaceStart();
        }
    }

    private void removeLineSpaceStart() {
        while (currentLineSb.length() > 0 && Character.isWhitespace(currentLineSb.charAt(0))) {
            currentLineSb.delete(0, 1);
        }
    }

    void checkHasError() {
        File errFile = new File(errFilename);
        if (errFile.exists()) try {
            Scanner scanner = new Scanner(errFile.getAbsoluteFile());
            int count = 0;
            while (scanner.hasNext()) {
                scanner.nextLine();
                count++;
            }
            if (count > 0) {
                programErrorAndExit("有" + count + "个错误, 具体查看" + errFilename + "文件");
            }
        } catch (FileNotFoundException e) {
            programErrorAndExit("打开错误文件失败");
        }
    }

    void run() {
        checkHasError();
        Scanner dydScanner;
        try {
            dydScanner = new Scanner(new File(dydFilename));
            sourceScanner = new Scanner(new File(sourceFilename));
            errorPw = new PrintWriter(new FileOutputStream(errFilename), true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        int lineNumber = 0;
        while (dydScanner.hasNextLine()) {
            lineNumber++;
            String line = dydScanner.nextLine().trim();
            int index = line.indexOf(" ");
            if (index == -1) {
                programErrorAndExit(PROGRAM_ERROR_DYD);
            }
            String word = line.substring(0, index);
            if (word.isEmpty()) {
                programErrorAndExit(PROGRAM_ERROR_DYD);
            }
            String typeString = line.substring(index + 1).trim();
            int type = -1;
            try {
                type = Integer.parseInt(typeString);
            } catch (NumberFormatException e) {
                programErrorAndExit("dyd文件错误(line " + lineNumber + ")：不是有效的类型" + typeString);
            }

            if (!TypeCode.isTypeCode(type)) {
                programErrorAndExit("dyd文件错误(line " + lineNumber + ")：不是有效的类型" + typeString);
            }
            if (type != EOLN && type != EOF) {
                wordList.add(word);
                typeList.add(type);
            }
        }
        currentIndex = -1;

        if (!sourceScanner.hasNextLine()) {
            System.err.println("No content in source file.");
            return;
        }
        advance();
        program();
        errorPw.flush();
        errorPw.close();
        checkHasError();
        generateProcTable();
        generateVariableTable();

    }

    private void generateProcTable() {
        try (PrintWriter pw = new PrintWriter(procFilename)) {
            for (Procedure procedure : procedureList) {
                pw.println(String.format("%16s %16s %16d %16d %16d", procedure.name, procedure.type, procedure.level, procedure.firstVarOffset, procedure.lastVarOffset));
            }
        } catch (FileNotFoundException e) {
            programErrorAndExit("打开文件" + procFilename + "失败");
        }
    }

    private void generateVariableTable() {
        try (PrintWriter pw = new PrintWriter(varTableFilename)) {
            for (Variable variable : variableList) {
                pw.println(String.format("%16s %16s %16d %16d %16d",
                        variable.name, variable.proc.name, variable.kind, variable.level, variable.offset));
            }
        } catch (FileNotFoundException e) {
            programErrorAndExit("打开文件" + varTableFilename + "失败");
        }
    }

    // 程序
    private void program() {
        currentLevel = 0;
        currentProc = new Procedure("main", "void", 0, 0, 0, null);
        // <程序> => <分程序>
        // <分程序> => begin<说明语句表>;<执行语句表>end
        if (currentWordType == BEGIN) {
            advance();
        } else {
            printParseError("分程序起始缺少begin");
        }
        declareStatementTable();
        execStatementTable();
        if (currentWordType == END) {
            advance();
        } else {
            printParseError("分程序结束缺少end");
        }
        if (currentWord != null) {
            printParseError("非法符号的开始: " + currentWord);
        }
    }

    private void declareStatementTable() {
        // <说明语句表> => <说明语句>│<说明语句表>;<说明语句>
        // 消除左递归: <说明语句表> => <说明语句><$说明语句表>
        // <$说明语句表> => ;<说明语句><$说明语句表>│<null>
        declareStatement();
        $declareStatementTable();
    }

    private void $declareStatementTable() {
        // <$说明语句表> => ;<说明语句><$说明语句表>│<null>
        if (currentWordType == SEMICOLON) {
            advance();
            declareStatement();
            $declareStatementTable();
        }
    }

    private void declareStatement() {
        // <说明语句> => <变量说明>|<函数说明>
        if (currentWordType == INTEGER) {

            int nextType = nextWordType();
            if (nextType == FUNCTION) {
                // <函数说明> => integer function <标识符> (<参数>);<函数体>
                funcDeclare();
            } else if (nextType == IDENTIFIER) {
                // <变量说明> => integer <变量>
                // <变量> => 标识符
                varDeclare();
            } else {
                printParseError("说明语句错误");
            }
        }
    }

    private int nextWordType() {
        int index = currentIndex + 1;
        if (currentIndex >= typeList.size()) {
            printParseError("非法结尾");
        }
        return typeList.get(index);
    }

    private void funcDeclare() {
        // <函数说明> => integer function <标识符> (<参数>);<函数体>
        if (currentWordType != INTEGER) {
            printParseError("函数定义类型错：" + currentWord);
        }
        String returnType = currentWord;
        advance();
        if (currentWordType != FUNCTION) {
            printParseError("函数定义缺少关键字\"function\"");
        }
        advance();
        if (currentWordType != IDENTIFIER) {
            printParseError("函数定义缺少标识符");
        }
        Procedure procedure = new Procedure(currentWord, "integer", currentLevel + 1, 0, 0, currentProc);
        if (procedureSet.add(procedure)) {
            procedureList.add(procedure);
        } else {
            printParseError("函数: " + currentWord + " 重复定义");
        }
        currentProc = procedure;
        currentLevel++;

        // 将 (函数名: returnType) 加入变量表
        Variable variable = new Variable(currentWord, currentProc, 0, returnType, currentLevel, variableSet.size());
        variableSet.add(variable);
        variableList.add(variable);

        advance();

        if (currentWordType != BRACKET_LEFT) {
            printParseError("函数声明出错：缺少(");
        }
        advance();

        int paramStart = variableList.size();
        // <参数> => <变量>
        // <变量> => <标识符>
        if (currentWordType == IDENTIFIER) {
            Variable param = new Variable(currentWord, currentProc, 1, "integer", currentLevel, variableList.size());
            if (variableSet.add(param)) {
                variableList.add(param);
            } else {
                System.out.println("参数名不能与函数名相同: " + currentWord);
            }
        }
        int paramEnd = variableList.size();
        // 这里考虑的极小语言是函数一定有一个参数，否则报错
        currentProc.firstVarOffset = paramStart;
        currentProc.lastVarOffset = paramEnd;

        advance();
        if (currentWordType != BRACKET_RIGHT) {
            printParseError("函数声明出错：缺少)");
        }
        advance();
        if (currentWordType != SEMICOLON) {
            printParseError("函数声明出错：缺少;");
        }
        advance();

        funcBody();

        currentProc = currentProc.parent;
        currentLevel--;
    }


    private void funcBody() {
        // <函数体> => begin <说明语句表>；<执行语句表> end
        if (currentWordType != BEGIN) {
            printParseError("函数体开始缺少begin");
        }
        advance();
        declareStatementTable();
        execStatementTable();
        if (currentWordType != END) {
            printParseError("函数体结束缺少end");
        }
        advance();
    }


    private void varDeclare() {
        // <变量说明> => integer <变量>
        // integer、 <变量> 已验证
        // currentWord是变量标识
        if (currentWordType != INTEGER) {
            printParseError("变量声明类型错误：" + currentWord);
        }
        advance();
        if (currentWordType != IDENTIFIER) {
            printParseError("不是变量标识符：" + currentWord);
        }
        Variable variable = new Variable(currentWord, currentProc, 0,
                "integer", currentLevel, variableSet.size());
        if (variableSet.add(variable)) {
            variableList.add(variable);
        } else {
            printParseError("重复定义变量: " + currentWord);
        }
        var();
    }

    private void var() {
        if (currentWordType != IDENTIFIER) {
            printParseError("不是标识符: " + currentWord);
        }
        // 变量 -> 标识符
        Variable variable = getFromVarTable(currentWord);
        if (variable == null) {
            printParseError("变量: " + currentWord + " 未定义");
        }
        advance();
    }


    private Variable getFromVarTable(String identifier) {
        Procedure procedure = currentProc;
        int level = currentLevel;
        while (level >= 0) {
            Variable variable = new Variable(identifier, procedure, 0, "integer", level, 0);
            if (variableSet.contains(variable)) {
                return variableList.get(variableList.indexOf(variable));
            }
            level--;
            if (procedure != null) {
                procedure = procedure.parent;
            }
        }
        return null;
    }

    private void execStatementTable() {
        // <执行语句表 => <执行语句>│<执行语句表>;<执行语句>
        // 消除左递归：
        // <执行语句表> => <执行语句><$执行语句表>
        // <$执行语句表> => ;<执行语句><$执行语句表>│<null>
        execStatement();
        $execStatementTable();
    }

    private void execStatement() {
        // <执行语句> => <读语句>│<写语句>│<赋值语句>│<条件语句>
        if (currentWordType == READ) {
            readStatement();
        } else if (currentWordType == WRITE) {
            writeStatement();
        } else if (currentWordType == IDENTIFIER) {
            assignStatement();
        } else if (currentWordType == IF) {
            conditionStatement();
        } else {
            printParseError("非法符号开始：" + currentWord);
        }
    }

    private void $execStatementTable() {
        // <$执行语句表> => ;<执行语句><$执行语句表>│<null>
        if (currentWordType == SEMICOLON) {
            advance();
            execStatement();
            $execStatementTable();
        }
    }

    private void readStatement() {
        //<读语句> => read(<变量>)
        if (currentWordType != READ) {
            printParseError("读语句错误");
        }
        advance();
        if (currentWordType != BRACKET_LEFT) {
            printParseError("读语句缺少(");
        }
        advance();
        var();
        if (currentWordType != BRACKET_RIGHT) {
            printParseError("读语句缺少)");
        }
        advance();
    }

    private void writeStatement() {
        //<写语句> => write(<变量>)
        if (currentWordType != WRITE) {
            printParseError("读语句错误");
        }
        advance();
        if (currentWordType != BRACKET_LEFT) {
            printParseError("写语句缺少(");
        }
        advance();
        var();
        if (currentWordType != BRACKET_RIGHT) {
            printParseError("写语句缺少)");
        }
        advance();
    }

    private void assignStatement() {
        //<赋值语句> => <变量>:=<算术表达式>
        var();
        if (currentWordType != ASSIGN) {
            printParseError("赋值语句缺少\":=\"");
        }
        advance();
        calExpresion();
    }

    private void calExpresion() {
        // <算术表达式> => <算术表达式>-<项>│<项>
        // 消除左递归：
        // <算术表达式> => <项><$算术表达式>
        // <$算术表达式> => -<项><$算术表达式>|<null>
        term();
        $calExpression();
    }

    private void term() {
        // <项> => <项>*<因子>│<因子>
        // 消除左递归：
        // <项> => <因子><$项>
        // <$项> => *<因子><$项>│<null>
        factor();
        $term();
    }

    private void factor() {
        // <因子> => <变量>│<常数>│<函数调用>
        if (currentWordType == IDENTIFIER) {
            int nextWordType = nextWordType();
            if (nextWordType == BRACKET_LEFT) {
                funcCall();
            } else {
                var();
            }
        } else if (currentWordType == CONSTANT) {
            advance();
        } else {
            printParseError("需要常数、变量或函数调用");
        }
    }

    private void funcCall() {
        // <函数调用> => <标识符>(<算数表达式>)
        if (currentWordType != IDENTIFIER) {
            printParseError("不是函数调用标识符: " + currentWord);
        }

        if (!isDecalredProcedure()) {
            printParseError("没有声明的函数: " + currentWord);
        }
        advance();
        if (currentWordType != BRACKET_LEFT) {
            printParseError("函数调用出错，缺少(");
        }
        advance();
        calExpresion();
        if (currentWordType != BRACKET_RIGHT) {
            printParseError("函数调用出错，缺少)");
        }
        advance();
    }

    private boolean isDecalredProcedure() {
        int level = currentLevel;
        Procedure curProc = currentProc;
        Procedure procedure;
        while (level >= 0 && curProc != null) {
            procedure = new Procedure(currentWord, "integer", level, 0, 0, curProc.parent);
            if (procedureSet.contains(procedure)) {
                return true;
            }
            curProc = curProc.parent;
            level--;
        }
        return false;
    }

    private void $term() {
        // <$项> => *<因子><$项>│<null>
        if (currentWordType == MULTIPLY) {
            advance();
            factor();
            $term();
        }
    }

    private void $calExpression() {
        // <$算术表达式> => -<项><$算术表达式>|<null>
        if (currentWordType == SUBTRACT) {
            advance();
            term();
            $calExpression();
        }
    }

    private void conditionStatement() {
        // <条件语句> => if<条件表达式>then<执行语句>else<执行语句>
        if (currentWordType != IF) {
            printParseError("缺少if");
        }
        advance();
        conditionExpresion();
        if (currentWordType != THEN) {
            printParseError("缺少then");
        }
        advance();
        execStatement();
        if (currentWordType != ELSE) {
            printParseError("缺少else");
        }
        advance();
        execStatement();
    }

    private void conditionExpresion() {
        // <条件表达式> => <算术表达式><关系运算符><算术表达式>
        calExpresion();
        relationOperator();
        calExpresion();
    }

    private void relationOperator() {
        // <关系运算符> => <│<=│>│>=│=│<>
        // 对应code是12~17
        if (12 <= currentWordType && currentWordType <= 17) {
            advance();
        } else {
            printParseError("不是关系运算符：" + currentWord);
        }
    }
}
