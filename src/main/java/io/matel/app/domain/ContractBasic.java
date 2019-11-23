package io.matel.app.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name ="contracts")
public class ContractBasic {

    @Id
    @Column(unique = true)
    private long idcontract;

    private String title;

    private String secType;

    @Column(nullable = false)
    private String exchange;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private double tickSize;

    @Column(nullable = false)
    private int rounding;

    @Column(nullable = false)
    private String multiplier;

    private String expiration;
    
    private String firstNotice;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false, columnDefinition= "TEXT")
    private String flowType = "MID";

    @Column(nullable = false)
    private int fusion;

    public ContractBasic(){}

    public ContractBasic(long idcontract, String title, String secType, String exchange, String currency, String symbol, double tickSize, int rounding, String multiplier,
                         String expiration, String firstNotice, boolean active, String flowType, int fusion) {
        this.idcontract = idcontract;
        this.title = title;
        this.secType = secType;
        this.exchange = exchange;
        this.currency = currency;
        this.symbol = symbol;
        this.tickSize = tickSize;
        this.rounding = rounding;
        this.multiplier = multiplier;
        this.expiration = expiration;
        this.firstNotice = firstNotice;
        this.active = active;
        this.flowType = flowType;
        this.fusion = fusion;
    }

    public long getIdcontract() {
        return idcontract;
    }

    public void setIdcontract(long idcontract) {
        this.idcontract = idcontract;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSecType() {
        return secType;
    }

    public void setSecType(String secType) {
        this.secType = secType;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getTickSize() {
        return tickSize;
    }

    public void setTickSize(double tickSize) {
        this.tickSize = tickSize;
    }

    public int getRounding() {
        return rounding;
    }

    public void setRounding(int rounding) {
        this.rounding = rounding;
    }

    public String getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(String multiplier) {
        this.multiplier = multiplier;
    }

    public String getExpiration() {
        return expiration;
    }

    public void setExpiration(String expiration) {
        this.expiration = expiration;
    }

    public String getFirstNotice() {
        return firstNotice;
    }

    public void setFirstNotice(String firstNotice) {
        this.firstNotice = firstNotice;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getFlowType() {
        return flowType;
    }

    public void setFlowType(String flowType) {
        this.flowType = flowType;
    }

    public int getFusion() {
        return fusion;
    }

    public void setFusion(int fusion) {
        this.fusion = fusion;
    }

    @Override
    public String toString() {
        return "ContractBasic{" +
                "idcontract=" + idcontract +
                ", title='" + title + '\'' +
                ", secType='" + secType + '\'' +
                ", exchange='" + exchange + '\'' +
                ", currency='" + currency + '\'' +
                ", symbol='" + symbol + '\'' +
                ", tickSize=" + tickSize +
                ", rounding=" + rounding +
                ", multiplier='" + multiplier + '\'' +
                ", expiration='" + expiration + '\'' +
                ", firstNotice='" + firstNotice + '\'' +
                ", active=" + active +
                ", flowType='" + flowType + '\'' +
                ", fusion=" + fusion +
                '}';
    }
}

