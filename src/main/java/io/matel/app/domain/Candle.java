package io.matel.app.domain;


import io.matel.app.config.tools.Utils;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

@Entity
@Table(name = "candle",
        indexes = {@Index(name = "index_date",  columnList="timestampTick", unique = false),
                @Index(name = "index_idcontract_freq",  columnList="idcontract, freq", unique = false)})
public class Candle implements  Cloneable {

    public Object clone()throws CloneNotSupportedException{
        return super.clone();
    }

    @Id
    @Column(unique = true)
    private long id;

    @Column(nullable = false)
    private long idtick;

    @Column(nullable = false , columnDefinition="TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime timestampCandle;

    @Column(nullable = false , columnDefinition="TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime timestampTick;

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

    private boolean newCandle;
    private int progress = 0;

    private double closeAverage;
    private double abnormalHeightLevel;
    private boolean bigCandle;

    private boolean smallCandleNoiseRemoval = false;

//    public String getContractType() {
//        return contractType;
//    }
//
//    private String contractType;
    private boolean checkpoint = false;

    @Column(nullable = false)
    private int volume = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false, columnDefinition= "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime createdOn;

    @UpdateTimestamp
    @Column(nullable = false, columnDefinition= "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime updatedOn;

    public Candle() { }

    public Candle(OffsetDateTime timestampCandle, OffsetDateTime timestampTick, double open, double high, double low, double close, long idcontract, int freq) {
        this.timestampCandle = timestampCandle;
        this.timestampTick = timestampTick;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.idcontract = idcontract;
        this.freq = freq;
//        if(idcontract>=10000){
//            contractType = "DAILY";
//        }else{
//            contractType = "LIVE";
//        }
    }


    public Candle(OffsetDateTime timestampCandle, OffsetDateTime timestampTick, double lastPrice, Double open, Double high, Double low, double close, ContractBasic contract, int freq) {
            if (lastPrice > 0) {
                double tickSize = contract.getTickSize();
                int rounding = contract.getRounding();
                boolean isUpTick = close > lastPrice;
                double adjust = isUpTick ? -tickSize : tickSize;
                if(freq>=1380)
                    adjust =0;
                this.open = Utils.round(close + adjust, rounding);
//                if(contract.getIdcontract()>=10000){
//                    contractType = "DAILY";
//                }else{
//                    contractType = "LIVE";
//                }

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
            this.timestampCandle = timestampCandle;
            this.timestampTick = timestampTick;
            this.idcontract = contract.getIdcontract();
            this.freq = freq;
            this.newCandle = true;
    }

    public Candle(long id, ContractBasic contract, int freq, long idtick, OffsetDateTime timestampCandle, OffsetDateTime timestampTick,
                  double open, double high, double low, double close,
                  int color, boolean newCandle, int progress,
                  int triggerDown, int triggerUp,
                  double abnormalHeightLevel, boolean bigCandle, double closeAverage,
                  ZonedDateTime createdOn, ZonedDateTime updatedOn, int volume){

        this.id = id;
        this.idcontract = contract.getIdcontract();
        this.freq = freq;
        this.idtick = idtick;
        this.timestampCandle = timestampCandle;
        this.timestampTick = timestampTick;
        this.open = Utils.round(open,contract.getRounding());
        this.high = Utils.round(high, contract.getRounding());
        this.low = Utils.round(low, contract.getRounding()) ;
        this.close = Utils.round(close, contract.getRounding());
        this.color = color;
        this.newCandle = newCandle;
        this.progress = progress;
        this.triggerDown = triggerDown;
        this.triggerUp = triggerUp;
        this.abnormalHeightLevel  =  abnormalHeightLevel;
        this.bigCandle = bigCandle;
        this.closeAverage = closeAverage;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.volume = volume;
//        if(idcontract>=10000){
//            contractType = "DAILY";
//        }else{
//            contractType = "LIVE";
//        }
    }


    public boolean isCheckpoint() {
        return checkpoint;
    }

    public void setCheckpoint(boolean checkpoint) {
        this.checkpoint = checkpoint;
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

//    public long getSpeed() {
//        return speed;
//    }
//
//    public void setSpeed(long speed) {
//        this.speed = speed;
//    }

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

//    public boolean isDiscarded() {
//        return discarded;
//    }
//
//    public void setDiscarded(boolean discarded) {
//        this.discarded = discarded;
//    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public OffsetDateTime getTimestampCandle() {
        return timestampCandle;
    }

    public void setTimestampCandle(OffsetDateTime timestampCandle) {
        this.timestampCandle = timestampCandle;
    }

    public OffsetDateTime getTimestampTick() {
        return timestampTick;
    }

    public void setTimestampTick(OffsetDateTime timestampTick) {
        this.timestampTick = timestampTick;
    }


    public boolean isSmallCandleNoiseRemoval() {
        return smallCandleNoiseRemoval;
    }

    public void setSmallCandleNoiseRemoval(boolean smallCandleNoiseRemoval) {
        this.smallCandleNoiseRemoval = smallCandleNoiseRemoval;
    }


    @Override
    public String toString() {
        return "Candle{" +
                "id=" + id +
                ", idtick=" + idtick +
                ", timestampCandle=" + timestampCandle +
                ", timestampTick=" + timestampTick +
                ", open=" + open +
                ", high=" + high +
                ", low=" + low +
                ", close=" + close +
                ", idcontract=" + idcontract +
                ", freq=" + freq +
                ", color=" + color +
                ", triggerUp=" + triggerUp +
                ", triggerDown=" + triggerDown +
                ", newCandle=" + newCandle +
                ", progress=" + progress +
                ", closeAverage=" + closeAverage +
                ", abnormalHeightLevel=" + abnormalHeightLevel +
                ", bigCandle=" + bigCandle +
                ", volume=" + volume +
                ", createdOn=" + createdOn +
                ", updatedOn=" + updatedOn +
                '}';
    }

    public double getCloseAverage() {
        return closeAverage;
    }

    public void setCloseAverage(double closeAverage) {
        this.closeAverage = closeAverage;
    }

    public double getAbnormalHeightLevel() {
        return abnormalHeightLevel;
    }

    public void setAbnormalHeightLevel(double abnormalHeightLevel) {
        this.abnormalHeightLevel = abnormalHeightLevel;
    }

    public boolean isBigCandle() {
        return bigCandle;
    }

    public void setBigCandle(boolean bigCandle) {
        this.bigCandle = bigCandle;
    }
}

