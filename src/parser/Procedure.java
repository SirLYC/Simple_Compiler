package parser;

import java.util.Objects;

public class Procedure {
    String name;
    String type;
    int level;
    int firstVarOffset;
    int lastVarOffset;
    Procedure parent;

    Procedure(String name, String type, int level, int firstVarOffset, int lastVarOffset, Procedure parent) {
        this.name = name;
        this.type = type;
        this.level = level;
        this.firstVarOffset = firstVarOffset;
        this.lastVarOffset = lastVarOffset;
        this.parent = parent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Procedure procedure = (Procedure) o;
        return level == procedure.level &&
                Objects.equals(name, procedure.name) &&
                Objects.equals(parent, procedure.parent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, level, parent);
    }
}
