package io.matel.app.macro.domain;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Entity
public class MacroUpdate {

    public MacroUpdate(){}

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private long id;
    private String code;

    private String country;
    private String dataName;

    @Column(columnDefinition= "TEXT")
    private String name;

    @Column(columnDefinition= "TEXT")
    private String description;

    @Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
    private LocalDateTime refreshed;
    private LocalDate fromDate;
    private LocalDate toDate;

    @CreationTimestamp
    @Column(nullable = false, updatable = false, columnDefinition= "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime createdOn;

    @UpdateTimestamp
    @Column(nullable = false, columnDefinition= "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime updatedOn;


    public MacroUpdate(String code, String name, String description, LocalDateTime refreshed, LocalDate fromDate, LocalDate toDate) {
        this.code = code;
        this.name = name;
        this.country = name.substring(0,name.indexOf("-")-1);
        this.dataName = name.substring(name.indexOf("-")+1, name.length());
        this.description = description;
        this.refreshed = refreshed;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getRefreshed() {
        return refreshed;
    }

    public void setRefreshed(LocalDateTime refreshed) {
        this.refreshed = refreshed;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    @Override
    public String toString() {
        return "SgeUpdate{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", refreshed_at=" + refreshed +
                ", from_date=" + fromDate +
                ", to_date=" + toDate +
                '}';
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
}
