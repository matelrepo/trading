package io.matel.app.state;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.matel.app.domain.EventType;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@IdClass(ProcessorStateKey.class)
public class ProcessorState {

    @Id
    private long idcontract;

    @Id
    private int freq;


    private ZonedDateTime timestamp;
    private int color = 0;
    private boolean minTrend = false;
    private boolean maxTrend = false;
    private double maxValue = Double.MAX_VALUE;
    private double maxValid = Double.MAX_VALUE;
    private double max = Double.MAX_VALUE;
    private double minValue = Double.MIN_VALUE;
    private double minValid = Double.MIN_VALUE;
    private double min = Double.MIN_VALUE;
    private ZonedDateTime lastDayOfQuarter;

    @Transient
    private Map<EventType, Boolean> activeEvents = new HashMap<>();

    @Column(nullable = false, columnDefinition= "TEXT")
    private String events= "";

    public ProcessorState(long idcontract, int freq) {
        this.idcontract = idcontract;
        this.freq = freq;
        for (EventType type : EventType.values()) {
            activeEvents.put(type, false);
        }
    }

    public ProcessorState() {
        for (EventType type : EventType.values()) {
            activeEvents.put(type, false);
        }
    }


    public long getIdcontract() {
        return idcontract;
    }

    public int getFreq() {
        return freq;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public boolean isMinTrend() {
        return minTrend;
    }

    public void setMinTrend(boolean minTrend) {
        this.minTrend = minTrend;
    }

    public boolean isMaxTrend() {
        return maxTrend;
    }

    public void setMaxTrend(boolean maxTrend) {
        this.maxTrend = maxTrend;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    public double getMaxValid() {
        return maxValid;
    }

    public void setMaxValid(double maxValid) {
        this.maxValid = maxValid;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getMinValue() {
        return minValue;
    }

    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }

    public double getMinValid() {
        return minValid;
    }

    public void setMinValid(double minValid) {
        this.minValid = minValid;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    @JsonIgnore
    public Map<EventType, Boolean> getActiveEvents() {
        return activeEvents;
    }

    @JsonIgnore
    public void setActiveEvents(Map<EventType, Boolean> activeEvents) {
        this.activeEvents = activeEvents;
    }

    public void setEvents(String events){
        this.events = events;
        for (String s : events.split(",")) {
            activeEvents.put(EventType.valueOf(s), true);
        }
    }

    public String getEvents(){
        events = "";
        activeEvents.forEach((eventType, isTrue) ->{
            if(isTrue)
                events = events + ',' + eventType;
        });
        if(events.charAt(0) == ',')
            events = events.substring(1);

        return events;
    }

    @JsonIgnore
    public void setType(EventType type) {
        switch (type) {
            case MAX_ADV:
                activeEvents.put(EventType.MAX_ADV, true);
                break;
            case MAX_ADV_CANCEL:
                activeEvents.put(EventType.MAX_ADV, false);
                break;
            case MAX_DETECT:
                activeEvents.put(EventType.MAX_DETECT, true);
                activeEvents.put(EventType.MAX_DETECT_CANCEL, false);
                activeEvents.put(EventType.MAX_ADV, false);
                break;
            case MAX_CONFIRM:
                activeEvents.put(EventType.MIN_CONFIRM, false);
                activeEvents.put(EventType.MAX_DETECT, false);
                activeEvents.put(EventType.MAX_CONFIRM, true);
                break;
            case MAX_DETECT_CANCEL:
                activeEvents.put(EventType.MAX_DETECT, false);
                activeEvents.put(EventType.MAX_DETECT_CANCEL, true);
                break;
            case MAX_CONFIRM_CANCEL:
                activeEvents.put(EventType.MAX_CONFIRM, false);
                activeEvents.put(EventType.MAX_CONFIRM_CANCEL, true);
                break;
            case MIN_ADV:
                activeEvents.put(EventType.MIN_ADV, true);
                break;
            case MIN_ADV_CANCEL:
                activeEvents.put(EventType.MIN_ADV, false);
                break;
            case MIN_DETECT:
                activeEvents.put(EventType.MIN_DETECT, true);
                activeEvents.put(EventType.MIN_DETECT_CANCEL, false);
                activeEvents.put(EventType.MIN_ADV, false);
                break;
            case MIN_CONFIRM:
                activeEvents.put(EventType.MAX_CONFIRM, false);
                activeEvents.put(EventType.MIN_CONFIRM, true);
                activeEvents.put(EventType.MIN_DETECT, false);
                break;
            case MIN_DETECT_CANCEL:
                activeEvents.put(EventType.MIN_DETECT, false);
                activeEvents.put(EventType.MIN_DETECT_CANCEL, true);
                break;
            case MIN_CONFIRM_CANCEL:
                activeEvents.put(EventType.MIN_CONFIRM_CANCEL, true);
                activeEvents.put(EventType.MIN_CONFIRM, false);
                break;
            default:
                break;
        }
        getEvents();
    }

    @Override
    public String toString() {
        return "ProcessorData{" +
                "idcontract=" + idcontract +
                ", freq=" + freq +
                ", timestamp=" + timestamp +
                ", color=" + color +
                ", minTrend=" + minTrend +
                ", maxTrend=" + maxTrend +
                ", maxValue=" + maxValue +
                ", maxValid=" + maxValid +
                ", max=" + max +
                ", minValue=" + minValue +
                ", minValid=" + minValid +
                ", min=" + min +
                ", activeEvents=" + activeEvents +
                ", events='" + events + '\'' +
                '}';
    }

    public ZonedDateTime getLastDayOfQuarter() {
        return lastDayOfQuarter;
    }

    public void setLastDayOfQuarter(ZonedDateTime lastDayOfQuarter) {
        this.lastDayOfQuarter = lastDayOfQuarter;
    }
}
