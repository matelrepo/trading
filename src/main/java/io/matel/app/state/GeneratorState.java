package io.matel.app.state;

import io.matel.app.config.Global;
import io.matel.app.config.tools.Utils;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

@Entity
public class GeneratorState {

    @Id
    private long idcontract;
    private long idtick;

    @Column(nullable = false, columnDefinition= "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime timestamp = OffsetDateTime.now().minusMinutes(5);

    @Transient
    private long speed = 500L;
    @Transient
    private double speedMultiplier = 10;
    private boolean isRandomGenerator = true;

    private int triggerUp =0;
    private int triggerDown = 0;
    private int color =0;

    private double lastPrice = Global.STARTING_PRICE; //ok
    private double dailyMark = 10000; //ok
    private double weeklyMark = 10000; //ok
    private double monthlyMark = 10000; //ok
    private double yearlyMark = 10000; //ok
    private double changeValue; //ok
    private double changePerc; //ok
    @Transient
private boolean liveQuote = true;
    private int previousVolume = 0;

    @Transient
    private double ask = -1; //ok

    @Transient
    private double bid = -1; //ok

    @Transient
    private double askQuantity; //ok

    @Transient
    private double bidQuantity; //ok

    @Transient
    private int tickQuantity = 0; //ok

    private int dailyVolume =0; //ok
    private double high=Double.MIN_VALUE; //ok
    private double low=Double.MAX_VALUE; //ok

    @Transient
    private int marketDataStatus = 0;


    @UpdateTimestamp
    @Column(nullable = false, columnDefinition= "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime updatedOn;

    public GeneratorState() { }

    public GeneratorState(long idcontract, boolean isRandomGenerator, int speed){
        this(idcontract,isRandomGenerator);
    }

    public GeneratorState(long idcontract, boolean isRandomGenerator) {
        this.idcontract = idcontract;
        this.isRandomGenerator = isRandomGenerator;
    }

    public long getIdcontract() {
        return idcontract;
    }

    public void setIdcontract(long idcontract) {
        this.idcontract = idcontract;
    }

    public long getIdtick() {
        return idtick;
    }

    public void setIdtick(long idtick) {
        this.idtick = idtick;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public long getSpeed() {
        return this.speed;
    }

    public void setSpeed(long speed) {
        this.speed = speed;
    }

    public boolean isRandomGenerator() {
        return isRandomGenerator;
    }

    public void setRandomGenerator(boolean randomGenerator) {
        isRandomGenerator = randomGenerator;
    }

    public double getAsk() {
        return ask;
    }

    public void setAsk(double ask) {
        this.ask = ask;
    }

    public double getBid() {
        return bid;
    }

    public void setBid(double bid) {
        this.bid = bid;
    }

    public int getTickQuantity() {
        return tickQuantity;
    }

    public void setTickQuantity(int tickQuantity) {
        this.tickQuantity = tickQuantity;
    }

    public int getDailyVolume() {
        return dailyVolume;
    }

    public void setDailyVolume(int dailyVolume) {
        this.dailyVolume = dailyVolume;
    }

    public double getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(double lastPrice) {
        this.lastPrice = lastPrice;
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

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public double getDailyMark() {
        return dailyMark;
    }

    public void setDailyMark(double dailyMark) {
        this.dailyMark = dailyMark;
    }

    public double getWeeklyMark() {
        return weeklyMark;
    }

    public void setWeeklyMark(double weeklyMark) {
        this.weeklyMark = weeklyMark;
    }

    public double getMonthlyMark() {
        return monthlyMark;
    }

    public void setMonthlyMark(double monthlyMark) {
        this.monthlyMark = monthlyMark;
    }

    public double getChangeValue() {
        return changeValue;
    }

    public void setChangeValue(double changeValue) {
        this.changeValue = changeValue;
    }

    public double getChangePerc() {
        return changePerc;
    }

    public void setChangePerc(double changePerc) {
        this.changePerc = Utils.round(changePerc,5);
    }

    public double getAskQuantity() {
        return askQuantity;
    }

    public void setAskQuantity(double askQuantity) {
        this.askQuantity = askQuantity;
    }

    public double getBidQuantity() {
        return bidQuantity;
    }

    public void setBidQuantity(double bidQuantity) {
        this.bidQuantity = bidQuantity;
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

    @Override
    public String toString() {
        return "GeneratorState{" +
                "idcontract=" + idcontract +
                ", idtick=" + idtick +
                ", timestamp=" + timestamp +
                ", speed=" + speed +
                ", speedMultiplier=" + speedMultiplier +
                ", isRandomGenerator=" + isRandomGenerator +
                ", triggerUp=" + triggerUp +
                ", triggerDown=" + triggerDown +
                ", color=" + color +
                ", lastPrice=" + lastPrice +
                ", dailyMark=" + dailyMark +
                ", weeklyMark=" + weeklyMark +
                ", monthlyMark=" + monthlyMark +
                ", yearlyMark=" + yearlyMark +
                ", changeValue=" + changeValue +
                ", changePerc=" + changePerc +
                ", previousVolume=" + previousVolume +
                ", ask=" + ask +
                ", bid=" + bid +
                ", askQuantity=" + askQuantity +
                ", bidQuantity=" + bidQuantity +
                ", tickQuantity=" + tickQuantity +
                ", dailyVolume=" + dailyVolume +
                ", high=" + high +
                ", low=" + low +
      //          ", marketDataStatus=" + marketDataStatus +
                ", updatedOn=" + updatedOn +
                '}';
    }

    public double getYearlyMark() {
        return yearlyMark;
    }

    public void setYearlyMark(double yearlyMark) {
        this.yearlyMark = yearlyMark;
    }

    public int getPreviousVolume() {
        return previousVolume;
    }

    public void setPreviousVolume(int previousVolume) {
        this.previousVolume = previousVolume;
    }

    public int getMarketDataStatus() {
        return marketDataStatus;
    }

    public void setMarketDataStatus(int marketDataErrorCode) {
        this.marketDataStatus = marketDataErrorCode;
    }


    public ZonedDateTime getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(ZonedDateTime updatedOn) {
        this.updatedOn = updatedOn;
    }

    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    public void setSpeedMultiplier(double speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
    }

    public boolean isLiveQuote() {
        return liveQuote;
    }

    public void setLiveQuote(boolean liveQuote) {
        this.liveQuote = liveQuote;
    }
}
