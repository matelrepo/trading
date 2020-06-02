package io.matel.app;

import com.fasterxml.jackson.databind.JsonNode;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDate;

@Entity
public class Daily {

    public Daily() {

    }

    public Daily(String code, JsonNode node, boolean extended){
        this.code = code;
        this.date = LocalDate.parse(node.get("date").asText());
        this.open = node.get("open").asDouble();
        this.high =  node.get("high").asDouble();
        this.low = node.get("low").asDouble();
        this.close = node.get("close").asDouble();
        this.adj_close =  node.get("adjusted_close").asDouble();
        this.volume = node.get("volume").asLong();
        if(extended) {
            this.code =  node.get("code").asText();
            this.name = node.get("name").asText();
            this.exchange = node.get("exchange_short_name").asText();
            this.marketCapitalization = node.get("MarketCapitalization").asLong();
            this.ema50 = node.get("ema_50d").asDouble();
            this.ema250 = node.get("ema_200d").asDouble();
            this.high250 = node.get("hi_250d").asDouble();
            this.low250 = node.get("lo_250d").asDouble();
            this.avgvol14 = node.get("avgvol_14d").asDouble();
            this.avgvol50 = node.get("avgvol_50d").asDouble();
            this.avgvol200 = node.get("avgvol_200d").asDouble();
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long id;
    String code;
    String name;
    String exchange;
    LocalDate date;
    double open, high, low, close, adj_close;
    long marketCapitalization;
    long volume;
    double ema50, ema250,high250,low250;
    double avgvol14, avgvol50,avgvol200;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
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

    public double getAdj_close() {
        return adj_close;
    }

    public void setAdj_close(double adj_close) {
        this.adj_close = adj_close;
    }

    public long getVolume() {
        return volume;
    }

    public void setVolume(long volume) {
        this.volume = volume;
    }

    public double getEma50() {
        return ema50;
    }

    public void setEma50(double ema50) {
        this.ema50 = ema50;
    }

    public double getEma250() {
        return ema250;
    }

    public void setEma250(double ema250) {
        this.ema250 = ema250;
    }

    public double getHigh250() {
        return high250;
    }

    public void setHigh250(double high250) {
        this.high250 = high250;
    }

    public double getLow250() {
        return low250;
    }

    public void setLow250(double low250) {
        this.low250 = low250;
    }

    public double getAvgvol14() {
        return avgvol14;
    }

    public void setAvgvol14(double avgvol14) {
        this.avgvol14 = avgvol14;
    }

    public double getAvgvol50() {
        return avgvol50;
    }

    public void setAvgvol50(double avgvol50) {
        this.avgvol50 = avgvol50;
    }

    public double getAvgvol200() {
        return avgvol200;
    }

    public void setAvgvol200(double avgvol200) {
        this.avgvol200 = avgvol200;
    }


    public long getMarketCapitalization() {
        return marketCapitalization;
    }

    public void setMarketCapitalization(long marketCapitalization) {
        this.marketCapitalization = marketCapitalization;
    }

    @Override
    public String toString() {
        return "Daily{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", exchange='" + exchange + '\'' +
                ", date=" + date +
                ", open=" + open +
                ", high=" + high +
                ", low=" + low +
                ", close=" + close +
                ", adj_close=" + adj_close +
                ", volume=" + volume +
                ", ema50=" + ema50 +
                ", ema250=" + ema250 +
                ", high250=" + high250 +
                ", low250=" + low250 +
                ", avgvol14=" + avgvol14 +
                ", avgvol50=" + avgvol50 +
                ", avgvol200=" + avgvol200 +
                '}';
    }
}
