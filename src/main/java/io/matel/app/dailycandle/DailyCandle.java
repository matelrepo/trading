package io.matel.app.dailycandle;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Entity
public class DailyCandle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String symbol;
    private LocalDate date;
    private double open;
    private double high;
    private double low;
    private double close;
    private double volume;

    @CreationTimestamp
    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime createdOn;

    @UpdateTimestamp
    @Column(nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime updatedOn;


    public DailyCandle(String symbol, LocalDate date, double open, double high, double low, double close, double volume) {
        this.symbol = symbol;
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }

    public DailyCandle() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getOpen() {
        return open;
    }

    public void setOpen(double open) {
        this.open = open;
    }

    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public double getLow() {
        return low;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public double getClose() {
        return close;
    }

    public void setClose(double close) {
        this.close = close;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    @Override
    public String toString() {
        return "Stocks{" +
                "id=" + id +
                ", date=" + date +
                ", open=" + open +
                ", high=" + high +
                ", low=" + low +
                ", close=" + close +
                ", volume=" + volume +
                '}';
    }
}
