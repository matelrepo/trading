package io.matel.app.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;
import io.matel.app.tools.Utils;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.ZonedDateTime;

@Entity
@Table(name = "candle")
public class Candle {

    @Id
    @Column(unique = true)
    private long id;

    @Column(nullable = false)
    private long idtick;

    @Column(nullable = false , columnDefinition="TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime timestamp;

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
    private int color = 0;

    @Column(nullable = false)
    private int triggerUp;

    @Column(nullable = false)
    private int triggerDown;

    @JsonIgnore
    private boolean discarded;

    private boolean newCandle;
    private long speed = -1;
    private int progress = 0;

    @Column(nullable = false)
    private int volume = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false, columnDefinition= "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime createdOn;

    @UpdateTimestamp
    @Column(nullable = false, columnDefinition= "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime updatedOn;

    public Candle() { }

    public Candle(long idcontract ,long timestamp, double close) {
        this.timestamp = ZonedDateTime.now();
        this.close = close;
        this.idcontract = idcontract;
    }
    public Candle(ZonedDateTime timestamp, double lastPrice, double close, ContractBasic contract, int freq, ZonedDateTime childTimestamp) {
        if (lastPrice > 0) {
            double tickSize = contract.getTickSize();
            int rounding = contract.getRounding();
            boolean isUpTick = close > lastPrice;
            double adjust = isUpTick ? -tickSize : tickSize;
            this.open = Utils.round(close + adjust, rounding);

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
//        this.childTimestamp = childTimestamp;
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

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getTriggerUp() {
        return triggerUp;
    }

    public void setTriggerUp(int triggerUp) {
        this.triggerUp = triggerUp;
    }

    public int getTriggerDown() {
        return triggerDown;
    }

    public void setTriggerDown(int triggerDown) {
        this.triggerDown = triggerDown;
    }

    public boolean isDiscarded() {
        return discarded;
    }

    public void setDiscarded(boolean discarded) {
        this.discarded = discarded;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

//    public ZonedDateTime getChildTimestamp() {
//        return childTimestamp;
//    }
//
//    public void setChildTimestamp(ZonedDateTime childTimestamp) {
//        this.childTimestamp = childTimestamp;
//    }

    @Override
    public String toString() {
        return "Candle{" +
                "id=" + id +
                ", idtick=" + idtick +
                ", timestamp=" + timestamp +
                ", open=" + open +
                ", high=" + high +
                ", low=" + low +
                ", close=" + close +
                ", idcontract=" + idcontract +
                ", freq=" + freq +
//                ", childTimestamp=" + childTimestamp +
                ", color=" + color +
                ", triggerUp=" + triggerUp +
                ", triggerDown=" + triggerDown +
                ", discarded=" + discarded +
                ", newCandle=" + newCandle +
                ", speed=" + speed +
                ", progress=" + progress +
                '}';
    }
}

