package io.matel.app.domain;

import javax.persistence.*;

@Entity
@Table(name = "histo")
public class Historical {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private long cid;

    @Column(nullable = false)
    private long tid;

    @Column(nullable = false)
    private int freq;

    @Column(nullable = false)
    private double open;

    @Column(nullable = false)
    private double high;

    @Column(nullable = false)
    private double low;

    @Column(nullable = false)
    private double close;

    public Historical(){}

    public Historical(long cid, long tid, int freq, double open, double high, double low, double close){
        this.cid = cid;
        this.tid = tid;
        this.freq = freq;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;

    }

    public long getCid() {
        return cid;
    }

    public void setCid(long cid) {
        this.cid = cid;
    }

    public long getTid() {
        return tid;
    }

    public void setTid(long tid) {
        this.tid = tid;
    }

    public int getFreq() {
        return freq;
    }

    public void setFreq(int freq) {
        this.freq = freq;
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

    @Override
    public String toString() {
        return "Historical{" +
                "id=" + id +
                ", cid=" + cid +
                ", tid=" + tid +
                ", freq=" + freq +
                ", open=" + open +
                ", high=" + high +
                ", low=" + low +
                ", close=" + close +
                '}';
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
