package io.matel.trader.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
@Table(name = "tick")
public class Tick {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private long timestamp;

    @Column(nullable = false)
    private double close;

    @Column(nullable = false)
    private long idcontract;

    @Column(nullable = false)
    private int triggerUp;

    @Column(nullable = false)
    private int triggerDown;

    @JsonIgnore
    private boolean discarded;


    public Tick(){}

    public Tick(long idcontract ,long timestamp, double close) {
        this.timestamp = timestamp;
        this.close = close;
        this.idcontract = idcontract;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
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
}
