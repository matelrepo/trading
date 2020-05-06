package io.matel.app.domain;

import io.matel.app.config.Global;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table(name = "tick")
public class Tick {

    @Id
    @Column(unique = true)
    private long id;

    @Column(nullable = false, columnDefinition= "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime timestamp;

    @Column(nullable = false)
    private double close;

    @Column(nullable = false)
    private long idcontract;

    @Column(nullable = false)
    private int triggerUp;

    @Column(nullable = false)
    private int triggerDown;

    @Column(nullable = false)
    private int volume =0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false, columnDefinition= "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime createdOn;

    @UpdateTimestamp
    @Column(nullable = false, columnDefinition= "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime updatedOn;

    public Tick(){}

    public Tick(long idtick,long idcontract , ZonedDateTime timestamp, double close) {
        this.id = idtick;
        this.timestamp = timestamp.withZoneSameLocal(Global.ZONE_ID);
        this.close = close;
        this.idcontract = idcontract;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
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

//    public boolean isDiscarded() {
//        return discarded;
//    }
//
//    public void setDiscarded(boolean discarded) {
//        this.discarded = discarded;
//    }

    @Override
    public String toString() {
        return "Tick{" +
                "id=" + id +
                ", timestamp=" + timestamp +
                ", close=" + close +
                ", idcontract=" + idcontract +
                ", triggerUp=" + triggerUp +
                ", triggerDown=" + triggerDown +
//                ", discarded=" + discarded +
                '}';
    }

//    public long getSpeed() {
//        return speed;
//    }
//
//    public void setSpeed(long speed) {
//        this.speed = speed;
//    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

}
