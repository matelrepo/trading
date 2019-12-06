package io.matel.app.macro.domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class MacroDataKey implements Serializable {
    private String code;
    private LocalDate date;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MacroDataKey that = (MacroDataKey) o;
        return Objects.equals(code, that.code) &&
                Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, date);
    }
}
