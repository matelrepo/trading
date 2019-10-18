package io.matel.trader.domain;


import io.matel.common.Global;

import javax.persistence.*;

@Entity
@Table(name = "candle")
public class Candle {

    @Id
    private long id;

    @Column(nullable = false)
    private long idtick;

    @Column(nullable = false)
    private long timestamp;

    @Column(nullable = false)
    private double open;

    @Column(nullable = false)
    private double high;

    @Column(nullable = false)
    private double low;

    @Column(nullable = false)
    private double close;

    @Column(nullable = false)
    private long idcontract;

    @Column(nullable = false)
    private int freq;

    @Column(nullable = false)
    private long childTimestamp;

    @Column(nullable = false)
    private int color = 0;

    private boolean newCandle;
    private long speed = 1000;

    public Candle() { }

    public Candle(long timestamp, double lastPrice, double close, ContractBasic contract, int freq, long childTimestamp) {
        if (lastPrice > 0) {
            double tickSize = contract.getTickSize();
            int rounding = contract.getRounding();
            boolean isUpTick = close > lastPrice;
            double adjust = isUpTick ? -tickSize : tickSize;
            this.open = Global.round(close + adjust, rounding);

            if (lastPrice >= close) {
                this.high = this.open;
                this.low = close;
            } else {
                this.high = close;
                this.low = this.open;
            }

        } else {
            this.open = close;
            this.high = close;
            this.low = close;
        }
        this.close = close;
        this.timestamp = timestamp;
        this.idcontract = contract.getIdcontract();
        this.childTimestamp = childTimestamp;
        this.freq = freq;
        this.newCandle = true;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getIdtick() {
        return idtick;
    }

    public void setIdtick(long idtick) {
        this.idtick = idtick;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
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

    public long getIdcontract() {
        return idcontract;
    }

    public void setIdcontract(long idcontract) {
        this.idcontract = idcontract;
    }

    public int getFreq() {
        return freq;
    }

    public void setFreq(int freq) {
        this.freq = freq;
    }

    public long getChildTimestamp() {
        return childTimestamp;
    }

    public void setChildTimestamp(long childTimestamp) {
        this.childTimestamp = childTimestamp;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public boolean isNewCandle() {
        return newCandle;
    }

    public void setNewCandle(boolean newCandle) {
        this.newCandle = newCandle;
    }

    public long getSpeed() {
        return speed;
    }

    public void setSpeed(long speed) {
        this.speed = speed;
    }
}

