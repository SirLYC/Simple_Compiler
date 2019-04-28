package tas;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

class WordAnalyzer {
    private static final Set<Character> wordBackSymbol = new HashSet<>();
    private static final Set<Character> operators = new HashSet<>();
    private static final int BEGIN = 1;
    private static final int END = 2;
    private static final int INTEGER = 3;
    private static final int IF = 4;
    private static final int THEN = 5;
    private static final int ELSE = 6;
    private static final int FUNCTION = 7;
    private static final int READ = 8;
    private static final int WRITE = 9;
    private static final int IDENTIFIER = 10;
    private static final int CONSTANT = 11;
    private static final int EUQAL = 12;
    private static final int NOT_EQUAL = 13;
    private static final int LESS_EQUAL = 14;
    private static final int LESS = 15;
    private static final int GREATER_EQUAL = 16;
    private static final int GREATER = 17;
    private static final int SUBTRACT = 18;
    private static final int MULTIPLY = 19;
    private static final int ASSIGN = 20;
    private static final int BRACKET_LEFT = 21;
    private static final int BRACKET_RIGHT = 22;
    private static final int SEMICOLON = 23;
    private static final int EOLN = 24;
    private static final int EOF = 25;
    private static final String ERROR_ILLEGAL_SYMBOL = "illegal symbol: \"%s\".";
    private static final String ERROR_COLON_NOT_MATCH = "\":\" doesn't match.";
    private static final String ERROR_OPERATOR = "illegal operator \"%s\".";
    private static final String ERROR_SYMBOL_TOO_LONG = "symbol \"%s\" too long(>=16).";
    private static final String ERROR_NUMBER_TOO_LONG = "number \"%s\" too long(>=16).";

    static {
        wordBackSymbol.add('=');
        wordBackSymbol.add('-');
        wordBackSymbol.add('*');
        wordBackSymbol.add('(');
        wordBackSymbol.add(')');
        wordBackSymbol.add('<');
        wordBackSymbol.add('>');
        wordBackSymbol.add(':');
        wordBackSymbol.add(';');
        operators.addAll(wordBackSymbol);
        operators.remove(';');
    }

    private final String filename;
    private final String outputFilename;
    private final String outputErrorFile;
    private int state;
    private int currentLine;
    private StringBuilder word = new StringBuilder();

    WordAnalyzer(String filename) {
        this.filename = filename;
        int index = filename.lastIndexOf(".");
        String name;
        if (index == -1) {
            name = filename;
        } else {
            name = filename.substring(0, index);
        }
        outputFilename = name + ".dyd";
        outputErrorFile = name + ".err";
    }

    void run() throws IOException {
        try (
                PrintWriter stdout = new PrintWriter(outputFilename);
                PrintWriter stderr = new PrintWriter(outputErrorFile)
        ) {
            currentLine = 0;
            Scanner scanner = new Scanner(new File(filename).getAbsoluteFile());
            while (scanner.hasNext()) {
                currentLine++;
                String next = scanner.nextLine();
                for (int i = 0; i < next.length(); i++) {
                    char c = next.charAt(i);
                    processCharacter(c, stdout, stderr);
                }
                processCharacter(' ', stdout, stderr);
                writeSymbol("EOLN", EOLN, stdout);
            }
            writeSymbol("EOF ", EOF, stdout);

            stdout.flush();
            stderr.flush();
        }
    }

    private void processCharacter(char c, PrintWriter stdout, PrintWriter stderr) {
        switch (state) {
            case 0:
                if (isAlpha(c)) {
                    state = 1;
                    word.append(c);
                } else if (Character.isDigit(c)) {
                    state = 3;
                    word.append(c);
                } else if (c == '=') {
                    state = 5;
                    analyzeSymbol(c, stdout, stderr);
                } else if (c == '-') {
                    state = 6;
                    analyzeSymbol(c, stdout, stderr);
                } else if (c == '*') {
                    state = 7;
                    analyzeSymbol(c, stdout, stderr);
                } else if (c == '(') {
                    state = 8;
                    analyzeSymbol(c, stdout, stderr);
                } else if (c == ')') {
                    state = 9;
                    analyzeSymbol(c, stdout, stderr);
                } else if (c == '<') {
                    state = 10;
                    word.append(c);
                } else if (c == '>') {
                    state = 14;
                    word.append(c);
                } else if (c == ':') {
                    state = 17;
                    word.append(c);
                } else if (c == ';') {
                    state = 20;
                    analyzeSymbol(c, stdout, stderr);
                } else if (Character.isWhitespace(c)) {
                    analyzeSymbol(word.toString(), stdout, stderr);
                } else {
                    state = 21;
                    word.append(c);
                }
                break;
            case 1:
                if (isAlpha(c) || Character.isDigit(c)) {
                    state = 1;
                    word.append(c);
                } else if (wordBackSymbol.contains(c) || Character.isWhitespace(c)) {
                    analyzeSymbol(word.toString(), stdout, stderr);
                    if (wordBackSymbol.contains(c)) {
                        processCharacter(c, stdout, stderr);
                    }
                } else {
                    state = 2;
                    word.append(c);
                }
                break;
            case 2:
                // error: illegal word
                if (wordBackSymbol.contains(c) || Character.isWhitespace(c)) {
                    writeError(String.format(ERROR_ILLEGAL_SYMBOL, word.toString()), stderr);
                    word.delete(0, word.length());
                    state = 0;
                    if (wordBackSymbol.contains(c)) {
                        processCharacter(c, stdout, stderr);
                    }
                } else if (isAlpha(c) || Character.isDigit(c)) {
                    word.append(c);
                }
                break;
            case 3:
                if (wordBackSymbol.contains(c) || Character.isWhitespace(c)) {
                    analyzeSymbol(word.toString(), stdout, stderr);
                    if (wordBackSymbol.contains(c)) {
                        processCharacter(c, stdout, stderr);
                    }
                } else {
                    word.append(c);
                }
                break;
            case 4:
                if (wordBackSymbol.contains(c) || Character.isWhitespace(c)) {
                    writeError(String.format(ERROR_ILLEGAL_SYMBOL, word.toString()), stderr);
                    word.delete(0, word.length());
                    state = 0;
                    if (wordBackSymbol.contains(c)) {
                        processCharacter(c, stdout, stderr);
                    }
                } else {
                    word.append(c);
                }
                break;
            case 10:
                if (c == '=') {
                    state = 11;
                    analyzeSymbol(word.append('=').toString(), stdout, stderr);
                } else if (c == '>') {
                    state = 12;
                    analyzeSymbol(word.append('>').toString(), stdout, stderr);
                } else if (operators.contains(c)) {
                    state = 13;
                    word.append(c);
                } else {
                    analyzeSymbol(word.toString(), stdout, stderr);
                    if (!Character.isWhitespace(c)) {
                        processCharacter(c, stdout, stderr);
                    }
                }
                break;
            case 14:
                if (c == '=') {
                    state = 15;
                    analyzeSymbol(word.append('=').toString(), stdout, stderr);
                } else if (operators.contains(c)) {
                    word.append(c);
                    state = 16;
                } else {
                    analyzeSymbol(word.toString(), stdout, stderr);
                    if (!Character.isWhitespace(c)) {
                        processCharacter(c, stdout, stderr);
                    }
                }
                break;
            case 13:
            case 16:
                if (operators.contains(c)) {
                    word.append(c);
                } else {
                    writeError(String.format(ERROR_OPERATOR, word.toString()), stderr);
                    word.delete(0, word.length());
                    state = 0;
                    if (!Character.isWhitespace(c)) {
                        processCharacter(c, stdout, stderr);
                    }
                }
                break;
            case 17:
                if (c == '=') {
                    state = 18;
                    analyzeSymbol(word.append('=').toString(), stdout, stderr);
                } else {
                    state = 19;
                    writeError(ERROR_COLON_NOT_MATCH, stderr);
                    state = 0;
                    if (!Character.isWhitespace(c)) {
                        processCharacter(c, stdout, stderr);
                    }
                }
                break;
            case 21:
                if (wordBackSymbol.contains(c) || Character.isWhitespace(c)) {
                    writeError(String.format(ERROR_ILLEGAL_SYMBOL, word.toString()), stderr);
                    word.delete(0, word.length());
                    state = 0;
                    if (!Character.isWhitespace(c)) {
                        processCharacter(c, stdout, stderr);
                    }
                } else {
                    word.append(c);
                }
                break;
        }
    }

    private void analyzeSymbol(char c, PrintWriter stdout, PrintWriter stderr) {
        int type = -1;
        switch (c) {
            case '=':
                type = EUQAL;
                break;
            case '<':
                type = LESS;
                break;
            case '>':
                type = GREATER;
                break;
            case '-':
                type = SUBTRACT;
                break;
            case '*':
                type = MULTIPLY;
                break;
            case '(':
                type = BRACKET_LEFT;
                break;
            case ')':
                type = BRACKET_RIGHT;
                break;
            case ';':
                type = SEMICOLON;
                break;
        }
        if (type == -1) {
            writeError(String.format(ERROR_ILLEGAL_SYMBOL, String.valueOf(c)), stderr);
        } else {
            writeSymbol(c, type, stdout);
        }
        state = 0;
    }

    private void analyzeSymbol(String symbol, PrintWriter stdout, PrintWriter stderr) {
        try {
            if (symbol.isEmpty()) {
                state = 0;
                return;
            } else if (symbol.length() >= 16) {
                if (state == 3) {
                    writeError(String.format(ERROR_NUMBER_TOO_LONG, symbol), stderr);
                } else {
                    writeError(String.format(ERROR_SYMBOL_TOO_LONG, symbol), stderr);
                }
                state = 0;
                return;
            }

            switch (state) {
                case 1:
                    int type;
                    switch (symbol) {
                        case "begin":
                            type = BEGIN;
                            break;
                        case "end":
                            type = END;
                            break;
                        case "integer":
                            type = INTEGER;
                            break;
                        case "if":
                            type = IF;
                            break;
                        case "then":
                            type = THEN;
                            break;
                        case "else":
                            type = ELSE;
                            break;
                        case "function":
                            type = FUNCTION;
                            break;
                        case "read":
                            type = READ;
                            break;
                        case "write":
                            type = WRITE;
                            break;
                        default:
                            type = IDENTIFIER;
                            break;
                    }
                    writeSymbol(symbol, type, stdout);
                    break;
                case 3:
                    writeSymbol(symbol, CONSTANT, stdout);
                    break;
                case 11:
                    writeSymbol(symbol, LESS_EQUAL, stdout);
                    break;
                case 12:
                    writeSymbol(symbol, NOT_EQUAL, stdout);
                    break;
                case 14:
                    writeSymbol(symbol, GREATER_EQUAL, stdout);
                    break;
                case 18:
                    writeSymbol(symbol, ASSIGN, stdout);
                    break;
                case 20:
                    writeSymbol(symbol, SEMICOLON, stdout);
                    break;
            }

            state = 0;

        } finally {
            word.delete(0, word.length());
        }
    }

    private void writeSymbol(char symbol, int type, PrintWriter stdout) {
        stdout.println(String.format("%16c %2d", symbol, type));
    }

    private void writeSymbol(String symbol, int type, PrintWriter stdout) {
        stdout.println(String.format("%16s %2d", symbol, type));
    }

    private void writeError(String reason, PrintWriter stderr) {
        String s = String.format("***LINE:%d  %s", currentLine, reason);
        System.err.println(s);
        stderr.println(s);
    }

    private boolean isAlpha(char c) {
        return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z');
    }
}
