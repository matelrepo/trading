package io.matel.app.state;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.matel.app.domain.EventType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
public class ProcessorState implements  Cloneable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private long idTick;
    private long idCandle;
    private long idcontract;
    private int freq;

    @Column(nullable = false , columnDefinition="TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime timestamp;
    @Column(nullable = false , columnDefinition="TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime timestamp_candle;

    private int color = 0;
    private boolean minTrend = false;
    private boolean maxTrend = false;
    private double maxValue = Double.MAX_VALUE;
    private double maxValid = Double.MAX_VALUE;
    private double max = Double.MAX_VALUE;
    private double minValue = Double.MIN_VALUE;
    private double minValid = Double.MIN_VALUE;
    private double min = Double.MIN_VALUE;
    private double value = 0;
    private double target = 0;
    private LocalDate lastDayOfQuarter;
    private boolean isTradable = false;
    private double open, high, low, close;


    @Enumerated(EnumType.STRING)
    private EventType event;

    @Transient
    private Map<EventType, Boolean> activeEvents = new HashMap<>();

    @Column(nullable = false, columnDefinition= "TEXT")
    private String events= "";

    public boolean isCheckpoint() {
        return checkpoint;
    }

    public void setCheckpoint(boolean checkpoint) {
        this.checkpoint = checkpoint;
    }

    private boolean checkpoint = false;

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

    public Object clone()throws CloneNotSupportedException{
        return super.clone();
    }


    public long getIdCandle() {
        return idCandle;
    }

    public void setIdCandle(long idCandle) {
        this.idCandle = idCandle;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getIdTick() {
        return idTick;
    }

    public void setIdTick(long idTick) {
        this.idTick = idTick;
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

    public int getFreq() {
        return freq;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
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
    public Map<EventType, Boolean> activeEvents() {
        return activeEvents;
    }

    public void setActiveEvents(String events){
        this.events = events;
        if(!this.events.equals("")) {
            for (String s : events.split(",")) {
                if (s != null)
                    activeEvents.put(EventType.valueOf(s), true);
            }
        }
    }

    public String getEvents(){
        return events;
    }

    public String getActiveEvents(){
        events = "";
        activeEvents.forEach((eventType, isTrue) ->{
            if(isTrue)
                events = events + ',' + eventType;
        });
        if(events.length()>0)
        if(events.charAt(0) == ',')
            events = events.substring(1);

        return events;
    }

    @JsonIgnore
    public void setType(EventType type) {
        event = type;
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
                value = maxValue;
                target = maxValid;
                break;
            case MAX_CONFIRM:
                activeEvents.put(EventType.MIN_CONFIRM, false);
                activeEvents.put(EventType.MAX_DETECT, false);
                activeEvents.put(EventType.MAX_CONFIRM, true);
                value = maxValue;
                target = maxValid;
                break;
            case MAX_DETECT_CANCEL:
                activeEvents.put(EventType.MAX_DETECT, false);
                activeEvents.put(EventType.MAX_DETECT_CANCEL, true);
                value = maxValue;
                target = maxValid;
                break;
            case MAX_CONFIRM_CANCEL:
                activeEvents.put(EventType.MAX_CONFIRM, false);
                activeEvents.put(EventType.MAX_CONFIRM_CANCEL, true);
                value = maxValue;
                target = maxValid;
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
                value = minValue;
                target = minValid;
                break;
            case MIN_CONFIRM:
                activeEvents.put(EventType.MAX_CONFIRM, false);
                activeEvents.put(EventType.MIN_CONFIRM, true);
                activeEvents.put(EventType.MIN_DETECT, false);
                value = minValue;
                target = minValid;
                break;
            case MIN_DETECT_CANCEL:
                activeEvents.put(EventType.MIN_DETECT, false);
                activeEvents.put(EventType.MIN_DETECT_CANCEL, true);
                value = minValue;
                target = minValid;
                break;
            case MIN_CONFIRM_CANCEL:
                activeEvents.put(EventType.MIN_CONFIRM_CANCEL, true);
                activeEvents.put(EventType.MIN_CONFIRM, false);
                value = minValue;
                target = minValid;
                break;
            default:
                break;
        }
        getActiveEvents();
    }


    public LocalDate getLastDayOfQuarter() {
        return lastDayOfQuarter;
    }

    public void setLastDayOfQuarter(LocalDate lastDayOfQuarter) {
        this.lastDayOfQuarter = lastDayOfQuarter;
    }

    public boolean isTradable() {
        return isTradable;
    }

    public void setTradable(boolean tradable) {
        isTradable = tradable;
    }

    public EventType getEvent() {
        return event;
    }

    public double getValue(){
        return value;
    }

    public void setValue(double value){
        this.value = value;
    }

    public double getTarget(){
        return target;
    }

    public void setTarget(double target){
        this.target = target;
    }

    public void setEvent(EventType event) {
        this.event = event;
    }

    @Override
    public String toString() {
        return "ProcessorState{" +
                "id=" + id +
                ", idTick=" + idTick +
                ", idCandle=" + idCandle +
                ", idcontract=" + idcontract +
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
                ", value=" + value +
                ", target=" + target +
                ", lastDayOfQuarter=" + lastDayOfQuarter +
                ", isTradable=" + isTradable +
                ", open=" + open +
                ", high=" + high +
                ", low=" + low +
                ", close=" + close +
                ", event=" + event +
                ", activeEvents=" + activeEvents +
                ", events='" + events + '\'' +
                '}';
    }

    public ZonedDateTime getTimestamp_candle() {
        return timestamp_candle;
    }

    public void setTimestamp_candle(ZonedDateTime timestamp_candle) {
        this.timestamp_candle = timestamp_candle;
    }

        public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
