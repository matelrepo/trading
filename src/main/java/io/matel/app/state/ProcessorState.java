package io.matel.app.state;

import com.fasterxml.jackson.annotation.JsonIgnore;
//import io.matel.app.domain.Event;
import io.matel.app.config.tools.Utils;
import io.matel.app.domain.ContractBasic;
import io.matel.app.domain.EventType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
public class ProcessorState implements  Cloneable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private int color = 0;
    private boolean minTrend = false;
    private boolean maxTrend = false;
    private double maxValue = Double.MAX_VALUE;
    private double maxValid = Double.MAX_VALUE;
    private double max = Double.MAX_VALUE;
    private double minValue = Double.MIN_VALUE;
    private double minValid = Double.MIN_VALUE;
    private double min = Double.MIN_VALUE;
    private LocalDate lastDayOfQuarter;
    private double open, high, low, close;
    private boolean checkpoint = false;
    private double averageClose=0;
    private double abnormalHeight =0;
    private double value = 0;
    private double target = 0;
    private boolean isTradable = false;

    @Column(nullable = false , columnDefinition="TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime timestampCandle = OffsetDateTime.now();

    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    public OffsetDateTime getUpdatedOn() {
        return updatedOn;
    }

    public String getEvtype() {
        return Evtype;
    }

    public void setEvtype(String type) {
        this.Evtype = type;
    }

    private String Evtype ="NONE";

    @CreationTimestamp
    @Column(nullable = false, updatable = false, columnDefinition= "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime createdOn;

    @UpdateTimestamp
    @Column(nullable = false, columnDefinition= "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime updatedOn;

    private long idCandle;


    public long getIdTick() {
        return idTick;
    }

    private long idTick;
    private long idcontract;
    private int freq;

    public void setIdTick(long idTick) {
        this.idTick = idTick;
    }

    public int getFreq(){
        return freq;
    }

    public long getIdcontract(){
        return idcontract;
    }

    public void setTimestampTick(OffsetDateTime timestampTick) {
        this.timestampTick = timestampTick;
    }

    public OffsetDateTime getTimestampTick() {
        return  this.timestampTick;
    }

    @Column(nullable = false , columnDefinition="TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime timestampTick = OffsetDateTime.now();

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @Transient
    private Map<EventType, Boolean> activeEvents = new HashMap<>();
    @Transient
    private Map<EventType, Boolean> activeEventsTradable = new HashMap<>();


    @Column(nullable = false, columnDefinition= "TEXT")
    private String eventsList = "";
    @Column(nullable = false, columnDefinition= "TEXT")
    private String eventsTradableList = "";

    @Transient
    ContractBasic contract;

//    public Event getEvent() {
//        return event;
//    }

//    @OneToOne(cascade = CascadeType.ALL)
//    @JoinColumn(name = "event_id", referencedColumnName = "id")
//    Event event;

    public ProcessorState(ContractBasic contract, int freq) {
        this.idcontract = contract.getIdcontract();
        this.contract= contract;
        this.freq = freq;
//        event = new Event(idcontract, freq);
        for (EventType type : EventType.values()) {
            activeEvents.put(type, false);
            activeEventsTradable.put(type, false);

        }
    }
//    public void setEvent(Event event){
//        this.event = event;
//    }

    public void setContract(ContractBasic contract){
        this.contract = contract;
    }

    public ProcessorState() {
        for (EventType type : EventType.values()) {
            activeEvents.put(type, false);
            activeEventsTradable.put(type, false);
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


    public boolean isTradable() {
        return isTradable;
    }

    public void setTradable(boolean tradable) {
        isTradable = tradable;
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

    public OffsetDateTime getTimestampCandle() {
        return timestampCandle;
    }

    public void setTimestampCandle(OffsetDateTime timestamp_candle) {
        this.timestampCandle = timestamp_candle;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getOpen() {
        return open;
    }

    public void setOpen(double open) {
        this.open = Utils.round(open, contract.getRounding());
    }

    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = Utils.round(high, contract.getRounding());
    }

    public double getLow() {
        return low;
    }

    public void setLow(double low) {
        this.low = Utils.round(low, contract.getRounding());
    }

    public double getClose() {
        return close;
    }

    public void setClose(double close) {
        this.close = Utils.round(close, contract.getRounding());
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
        this.maxValue = Utils.round(maxValue, contract.getRounding());
    }

    public double getMaxValid() {
        return maxValid;
    }

    public void setMaxValid(double maxValid) {
        this.maxValid = Utils.round(maxValid, contract.getRounding());
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = Utils.round(max, contract.getRounding());
    }

    public double getMinValue() {
        return minValue;
    }

    public void setMinValue(double minValue) {
        this.minValue = Utils.round(minValue, contract.getRounding());
    }

    public double getMinValid() {
        return minValid;
    }

    public void setMinValid(double minValid) {
        this.minValid = Utils.round(minValid, contract.getRounding());
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = Utils.round(min, contract.getRounding());
    }

    @JsonIgnore
    public Map<EventType, Boolean> activeEvents() {
        return activeEvents;
    }


    public void setActiveEvents(String events){
        this.eventsList = events;
        if(!this.eventsList.equals("")) {
            for (String s : events.split(",")) {
                if (s != null) {
                    activeEvents.put(EventType.valueOf(s), true);
                    activeEventsTradable.put(EventType.valueOf(s), true);

                }
            }
        }
    }

    public String getEventsList(){
        return eventsList;
    }

    public void setActiveEventsTradable(String events){
        this.eventsTradableList = events;
        if(!this.eventsTradableList.equals("")) {
            for (String s : events.split(",")) {
                if (s != null) {
                    activeEvents.put(EventType.valueOf(s), true);
                    activeEventsTradable.put(EventType.valueOf(s), true);

                }
            }
        }
    }

    public String getEventsTradableList(){
        return eventsTradableList;
    }

    public String getActiveEvents(){
        eventsList = "";
        activeEvents.forEach((eventType, isTrue) ->{
            if(isTrue)
                eventsList = eventsList + ',' + eventType;
        });
        if(eventsList.length()>0)
        if(eventsList.charAt(0) == ',')
            eventsList = eventsList.substring(1);

        return eventsList;
    }

    public String getActiveEventsTradable(){
        eventsTradableList = "";
        activeEventsTradable.forEach((eventType, isTrue) ->{
            if(isTrue)
                eventsTradableList = eventsTradableList + ',' + eventType;
        });
        if(eventsTradableList.length()>0)
            if(eventsTradableList.charAt(0) == ',')
                eventsTradableList = eventsTradableList.substring(1);

            if(eventsTradableList.equals(""))
                eventsTradableList = EventType.NONE + "";
        return eventsTradableList;
    }

    @JsonIgnore
    public void setType(EventType type, boolean tradable) {
        eventType = type;
        switch (type) {
            case MAX_ADV:
               // if(tradable)
                activeEvents.put(EventType.MAX_ADV, true);
                activeEventsTradable.put(EventType.MAX_ADV, isTradable);
                break;
            case MAX_ADV_CANCEL:
               // if(tradable)
                    activeEvents.put(EventType.MAX_ADV, false);
                activeEventsTradable.put(EventType.MAX_ADV, false);

                break;
            case MAX_DETECT:
              //  if(tradable) {
                    activeEvents.put(EventType.MAX_DETECT, true);
                    activeEvents.put(EventType.MAX_DETECT_CANCEL, false);
                    activeEvents.put(EventType.MAX_ADV, false);
                activeEventsTradable.put(EventType.MAX_DETECT, isTradable);
                activeEventsTradable.put(EventType.MAX_DETECT_CANCEL, false);
                activeEventsTradable.put(EventType.MAX_ADV, false);
               // }
                value = maxValue;
                target = maxValid;
                break;
            case MAX_CONFIRM:
              //  if(tradable) {
                    activeEvents.put(EventType.MIN_CONFIRM, false);
                    activeEvents.put(EventType.MAX_DETECT, false);
                    activeEvents.put(EventType.MAX_CONFIRM, true);
                activeEventsTradable.put(EventType.MIN_CONFIRM, false);
                activeEventsTradable.put(EventType.MAX_DETECT, false);
                activeEventsTradable.put(EventType.MAX_CONFIRM, isTradable);
              //  }
                value = maxValue;
                target = maxValid;
                break;
            case MAX_DETECT_CANCEL:
               // if(tradable) {
                    activeEvents.put(EventType.MAX_DETECT, false);
                    activeEvents.put(EventType.MAX_DETECT_CANCEL, true);
                activeEventsTradable.put(EventType.MAX_DETECT, false);
                activeEventsTradable.put(EventType.MAX_DETECT_CANCEL, isTradable);
               // }
                value = maxValue;
                target = maxValid;
                break;
            case MAX_CONFIRM_CANCEL:
               // if(tradable) {
                    activeEvents.put(EventType.MAX_CONFIRM, false);
                    activeEvents.put(EventType.MAX_CONFIRM_CANCEL, true);
                activeEventsTradable.put(EventType.MAX_CONFIRM, false);
                activeEventsTradable.put(EventType.MAX_CONFIRM_CANCEL,isTradable);
               // }
                value = maxValue;
                target = maxValid;
                break;
            case MIN_ADV:
               // if(tradable)
                activeEvents.put(EventType.MIN_ADV, true);
                activeEventsTradable.put(EventType.MIN_ADV, isTradable);

                break;
            case MIN_ADV_CANCEL:
               // if(tradable)
                activeEvents.put(EventType.MIN_ADV, false);
                activeEventsTradable.put(EventType.MIN_ADV, false);

                break;
            case MIN_DETECT:
              //  if(tradable) {
                    activeEvents.put(EventType.MIN_DETECT, true);
                    activeEvents.put(EventType.MIN_DETECT_CANCEL, false);
                    activeEvents.put(EventType.MIN_ADV, false);
                activeEventsTradable.put(EventType.MIN_DETECT,isTradable);
                activeEventsTradable.put(EventType.MIN_DETECT_CANCEL, false);
                activeEventsTradable.put(EventType.MIN_ADV, false);
               // }
                value = minValue;
                target = minValid;
                break;
            case MIN_CONFIRM:
               // if(tradable) {
                    activeEvents.put(EventType.MAX_CONFIRM, false);
                    activeEvents.put(EventType.MIN_CONFIRM, true);
                    activeEvents.put(EventType.MIN_DETECT, false);
                activeEventsTradable.put(EventType.MAX_CONFIRM, false);
                activeEventsTradable.put(EventType.MIN_CONFIRM, isTradable);
                activeEventsTradable.put(EventType.MIN_DETECT, false);
               // }
                value = minValue;
                target = minValid;
                break;
            case MIN_DETECT_CANCEL:
               // if(tradable) {
                    activeEvents.put(EventType.MIN_DETECT, false);
                    activeEvents.put(EventType.MIN_DETECT_CANCEL, true);
                activeEventsTradable.put(EventType.MIN_DETECT, false);
                activeEventsTradable.put(EventType.MIN_DETECT_CANCEL, isTradable);
               // }
                value = minValue;
                target = minValid;
                break;
            case MIN_CONFIRM_CANCEL:
               // if(tradable) {
                    activeEvents.put(EventType.MIN_CONFIRM_CANCEL, true);
                    activeEvents.put(EventType.MIN_CONFIRM, false);
                activeEventsTradable.put(EventType.MIN_CONFIRM_CANCEL, isTradable);
                activeEventsTradable.put(EventType.MIN_CONFIRM, false);
               // }
                value = minValue;
                target = minValid;
                break;
            default:
                break;
        }
        getActiveEvents();
        getActiveEventsTradable();

    }


    public LocalDate getLastDayOfQuarter() {
        return lastDayOfQuarter;
    }

    public void setLastDayOfQuarter(LocalDate lastDayOfQuarter) {
        this.lastDayOfQuarter = lastDayOfQuarter;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType event) {
        this.eventType = event;
    }

    @Override
    public String toString() {
        return "ProcessorState{" +
                "color=" + color +
                ", open=" + open +
                ", high=" + high +
                ", low=" + low +
                ", close=" + close +
                ", value=" + value +
                ", target=" + target +
                ", isTradable=" + isTradable +
                ", timestampCandle=" + timestampCandle +
                ", idCandle=" + idCandle +
                ", idTick=" + idTick +
                ", idcontract=" + idcontract +
                ", freq=" + freq +
                ", timestampTick=" + timestampTick +
                ", eventsList='" + eventsList + '\'' +
                ", eventsTradableList='" + eventsTradableList + '\'' +
                '}';
    }

    public double getAverageClose() {
        return averageClose;
    }

    public void setAverageClose(double averageClose) {
        this.averageClose = averageClose;
    }

    public double getAbnormalHeight() {
        return abnormalHeight;
    }

    public void setAbnormalHeight(double abnormalHeight) {
        this.abnormalHeight = abnormalHeight;
    }

    public boolean isCheckpoint() {
        return checkpoint;
    }

    public void setCheckpoint(boolean checkpoint) {
        this.checkpoint = checkpoint;
    }


}
