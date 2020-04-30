package io.matel.app.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name ="contracts")
public class ContractBasic implements Cloneable {

    @Id
    @Column(unique = true)
    private long idcontract;

    private Integer conid;

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

    private LocalDate expiration;
    
    private LocalDate firstNotice;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false, columnDefinition= "TEXT")
    private String flowType = "MID";

    @Column(nullable = false)
    private int fusion;

    private String type;
    private String category;

    private long cloneid = -1;

    public ContractBasic(){}

    public ContractBasic(long idcontract, String title, String secType, String exchange, String currency, String symbol, double tickSize, int rounding, String multiplier,
                         LocalDate expiration, LocalDate firstNotice, boolean active, String flowType, int fusion) {
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

    public Object clone()throws CloneNotSupportedException{
        return (ContractBasic)super.clone();
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

    public LocalDate getExpiration() {
        return expiration;
    }

    public void setExpiration(LocalDate expiration) {
        this.expiration = expiration;
    }

    public LocalDate getFirstNotice() {
        return firstNotice;
    }

    public void setFirstNotice(LocalDate firstNotice) {
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
                ", conid=" + conid +
                ", title='" + title + '\'' +
                ", secType='" + secType + '\'' +
                ", exchange='" + exchange + '\'' +
                ", currency='" + currency + '\'' +
                ", symbol='" + symbol + '\'' +
                ", tickSize=" + tickSize +
                ", rounding=" + rounding +
                ", multiplier='" + multiplier + '\'' +
                ", expiration=" + expiration +
                ", firstNotice=" + firstNotice +
                ", active=" + active +
                ", flowType='" + flowType + '\'' +
                ", fusion=" + fusion +
                ", type='" + type + '\'' +
                ", category='" + category + '\'' +
                '}';
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getConid() {
        return conid;
    }

    public void setConid(Integer conid) {
        this.conid = conid;
    }


    public long getCloneid() {
        return cloneid;
    }

    public void setCloneid(long cloneid) {
        this.cloneid = cloneid;
    }
}

