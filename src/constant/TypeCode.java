package constant;

public class TypeCode {
    public static final int BEGIN = 1;
    public static final int END = 2;
    public static final int INTEGER = 3;
    public static final int IF = 4;
    public static final int THEN = 5;
    public static final int ELSE = 6;
    public static final int FUNCTION = 7;
    public static final int READ = 8;
    public static final int WRITE = 9;
    public static final int IDENTIFIER = 10;
    public static final int CONSTANT = 11;
    public static final int EQUAL = 12;
    public static final int NOT_EQUAL = 13;
    public static final int LESS_EQUAL = 14;
    public static final int LESS = 15;
    public static final int GREATER_EQUAL = 16;
    public static final int GREATER = 17;
    public static final int SUBTRACT = 18;
    public static final int MULTIPLY = 19;
    public static final int ASSIGN = 20;
    public static final int BRACKET_LEFT = 21;
    public static final int BRACKET_RIGHT = 22;
    public static final int SEMICOLON = 23;
    public static final int EOLN = 24;
    public static final int EOF = 25;


    public static boolean isTypeCode(int code) {
        return 1 <= code && code <= 25;
    }
}
