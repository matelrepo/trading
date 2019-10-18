package io.matel.trader.domain;

import io.matel.trader.tools.FlowType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name ="contracts")
public class ContractBasic {

    @Id
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

    @Column(nullable = false)
    private String expiration;
    
    @Column(nullable = false)
    private String firstNotice;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false, columnDefinition= "TEXT")
    private FlowType flowType = FlowType.MID;

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
        this.flowType = FlowType.valueOf(flowType);
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

    public FlowType getFlowType() {
        return flowType;
    }

    public void setFlowType(FlowType flowType) {
        this.flowType = flowType;
    }

    public int getFusion() {
        return fusion;
    }

    public void setFusion(int fusion) {
        this.fusion = fusion;
    }
}

