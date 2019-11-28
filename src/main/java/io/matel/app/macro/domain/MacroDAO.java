package io.matel.app.macro.domain;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public class MacroDAO {

    private String code;
    private LocalDate date;
    private double current;
    private double previous;
    private String country;
    private String dataName;
    private ZonedDateTime createdOn;
    private ZonedDateTime updatedOn;

    public MacroDAO(String code, LocalDate date, double current, double previous, String country, String dataName, ZonedDateTime createdOn, ZonedDateTime updatedOn) {
        this.code = code;
        this.date = date;
        this.current = current;
        this.previous = previous;
        this.country = country;
        this.dataName = dataName;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
    }

    public MacroDAO() {
    }

    @Override
    public String toString() {
        return "MacroDAO{" +
                "code='" + code + '\'' +
                ", date=" + date +
                ", current=" + current +
                ", previous=" + previous +
                ", country='" + country + '\'' +
                ", dataName='" + dataName + '\'' +
                ", createdOn=" + createdOn +
                ", updatedOn=" + updatedOn +
                '}';
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getCurrent() {
        return current;
    }

    public void setCurrent(double current) {
        this.current = current;
    }

    public double getPrevious() {
        return previous;
    }

    public void setPrevious(double previous) {
        this.previous = previous;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDataName() {
        return dataName;
    }

    public void setDataName(String dataName) {
        this.dataName = dataName;
    }

    public ZonedDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(ZonedDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public ZonedDateTime getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(ZonedDateTime updatedOn) {
        this.updatedOn = updatedOn;
    }
}
