package io.matel.app.macro.domain;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Entity
@IdClass(MacroDataKey.class)
public class MacroData {
    @Id
    private String code;
    @Id
    private LocalDate date;
    private double value;

    @CreationTimestamp
    @Column(nullable = false, updatable = false, columnDefinition= "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime createdOn;

    @UpdateTimestamp
    @Column(nullable = false, columnDefinition= "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime updatedOn;

    public MacroData(){};

    public MacroData(String code, String date, String value){
        this.code = code;
        this.date = LocalDate.parse(date);
        this.value = Double.valueOf(value);
    };

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "MacroData{" +
                "code='" + code + '\'' +
                ", date=" + date +
                ", value=" + value +
                '}';
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }


}


