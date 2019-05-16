package parser;

import java.util.Objects;

public class Variable {
    String name;
    Procedure proc;
    // 0: 变量 1: 形参
    int kind;
    String type;
    int level;
    int offset;

    Variable(String name, Procedure proc, int kind, String type, int level, int offset) {
        this.name = name;
        this.proc = proc;
        this.kind = kind;
        this.type = type;
        this.level = level;
        this.offset = offset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Variable variable = (Variable) o;
        return kind == variable.kind &&
                level == variable.level &&
                Objects.equals(name, variable.name) &&
                Objects.equals(proc, variable.proc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, proc, kind, level);
    }
}
