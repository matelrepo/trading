//package io.matel.app.domain;
//
//import io.matel.app.state.ProcessorState;
//
//import javax.persistence.*;
//import java.time.OffsetDateTime;
//
//@Entity
//public class Event implements  Cloneable {
//
//    public Object clone()throws CloneNotSupportedException{
//        return super.clone();
//    }
//
//    public Event(){}
//
//    public Event(long idcontract, int freq){
//        this.idcontract = idcontract;
//        this.freq = freq;
//    }
//
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private long id;
//
//    private long idTick;
////    private long idCandle;
//
//    @Enumerated(EnumType.STRING)
//    private EventType eventType;
//
//    @OneToOne(mappedBy = "event")
//    private ProcessorState processorState;
//
//    public void setIdcontract(long idcontract) {
//        this.idcontract = idcontract;
//    }
//
//    public void setFreq(int freq) {
//        this.freq = freq;
//    }
//
//    private long idcontract;
//    private int freq;
////    private double value = 0;
////    private double target = 0;
////    private boolean isTradable = false;
//
//
//    @Column(nullable = false , columnDefinition="TIMESTAMP WITH TIME ZONE")
//    private OffsetDateTime timestampTick;
////    @Column(nullable = false , columnDefinition="TIMESTAMP WITH TIME ZONE")
////    private OffsetDateTime timestampCandle;
//
//    public long getIdCandle() {
//        return idCandle;
//    }
//
//    public void setIdCandle(long idCandle) {
//        this.idCandle = idCandle;
//    }
//
//    public long getIdTick() {
//        return idTick;
//    }
//
//    public void setIdTick(long idTick) {
//        this.idTick = idTick;
//    }
//
//    public long getIdcontract() {
//        return idcontract;
//    }
//
//    public int getFreq() {
//        return freq;
//    }
//
//    public OffsetDateTime getTimestampTick() {
//        return timestampTick;
//    }
//
//    public boolean isTradable() {
//        return isTradable;
//    }
//
//    public void setTradable(boolean tradable) {
//        isTradable = tradable;
//    }
//
//    public double getValue(){
//        return value;
//    }
//
//    public void setValue(double value){
//        this.value = value;
//    }
//
//    public double getTarget(){
//        return target;
//    }
//
//    public void setTarget(double target){
//        this.target = target;
//    }
//
//    public OffsetDateTime getTimestampCandle() {
//        return timestampCandle;
//    }
//
//    public void setTimestampCandle(OffsetDateTime timestamp_candle) {
//        this.timestampCandle = timestamp_candle;
//    }
//
//    public void setTimestampTick(OffsetDateTime timestamp) {
//        this.timestampTick = timestamp;
//    }
//
//}
