package io.matel.app.domain;

import io.matel.app.config.Global;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

@Entity
public class TimeSales implements Cloneable {


    @Id
    private long id;
    private long idcontract;
    private long time;
    private  double price;
    private int size;
    private double bid;
//    private double previousBid;
    private double ask;
//    private double previousAsk;
    private String way;
    private boolean newTrade = false;
    private boolean start = true;
    private double cumul =0;

    public void setWay(double bid, double ask){
        if( price <=  bid){
            this.way ="SELL";
        }else if(price >= ask){
            this.way = "BUY";
        } else{
            this.way="NEUTRAL";
        }
    }

    public void incrementId(){
        this.id++;
    }


    public Object clone()throws CloneNotSupportedException{
        return super.clone();
    }

    public long getIdcontract() {
        return idcontract;
    }

    public void setIdcontract(long idcontract) {
        this.idcontract = idcontract;
    }

    public void setStart(boolean start) {
        this.start = start;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
        this.cumul = this.cumul + size;
    }

    public double getBid() {
        return bid;
    }

    public void setBid(double bid) {
        this.bid = bid;
    }

    public double getAsk() {
        return ask;
    }

    public void setAsk(double ask) {
        this.ask = ask;
    }

    public String getWay() {
        return way;
    }

    public void setWay(String way) {
        this.way = way;
    }

    public boolean isNewTrade() {
        return newTrade;
    }

    public void setNewTrade(boolean newTrade) {
        this.newTrade = newTrade;
    }

    @Override
    public String toString() {
        return   id + " " + DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(Instant.ofEpochSecond(time).atZone(Global.ZONE_ID).toLocalDateTime())
                + " " + getWay() + " " + size + "    " + price +  " (" +bid + " " + ask + ")";

    }

    public double getCumul() {
        return cumul;
    }

    public void setCumul(double cumul) {
        this.cumul = cumul;
    }
}