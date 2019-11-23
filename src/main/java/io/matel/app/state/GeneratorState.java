package io.matel.app.state;

import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.ZonedDateTime;
import java.util.Random;

@Entity
public class GeneratorState {

    @Id
    private long idcontract;
    private long idtick;

    @Column(nullable = false, columnDefinition= "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime timestamp;
    private boolean isConnected;
    private int speed;
    private boolean isRandomGenerator = true;
    private double ask = -1;
    private double bid = -1;
    private int volume = 0;
    private int totalVolume =0;
    private double lastPrice = -1;
    private int triggerUp =0;
    private int triggerDown = 0;
    private int color =0;

    @UpdateTimestamp
    @Column(nullable = false, columnDefinition= "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime updatedOn;

    public GeneratorState() { }

    public GeneratorState(long idcontract, boolean isRandomGenerator, int speed){
        this(idcontract,isRandomGenerator);
        this.speed = speed;
    }

    public GeneratorState(long idcontract, boolean isRandomGenerator) {
        this.idcontract = idcontract;
        this.isRandomGenerator = isRandomGenerator;
        Random rand = new Random();
        speed = rand.nextInt(10000);
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

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
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

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(int totalVolume) {
        this.totalVolume = totalVolume;
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

    @Override
    public String toString() {
        return "GeneratorState{" +
                "idcontract=" + idcontract +
                ", idtick=" + idtick +
                ", timestamp=" + timestamp +
                ", isConnected=" + isConnected +
                ", speed=" + speed +
                ", isRandomGenerator=" + isRandomGenerator +
                ", ask=" + ask +
                ", bid=" + bid +
                ", volume=" + volume +
                ", totalVolume=" + totalVolume +
                ", lastPrice=" + lastPrice +
                ", triggerUp=" + triggerUp +
                ", triggerDown=" + triggerDown +
                ", color=" + color +
                ", updatedOn=" + updatedOn +
                '}';
    }

}
