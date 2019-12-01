package io.matel.app.state;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.ZonedDateTime;

@Entity
public class LogProcessorState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;
    public long idTick;
    public int freq;
    public long idcontract;
    public int offset_;
    public ZonedDateTime timestamp;

    public LogProcessorState(){}

    public LogProcessorState(long idcontract, int freq, int offset){
        this.idcontract = idcontract;
        this.freq = freq;
        this.offset_ = offset;
    }

    public boolean isNewCandle0;
    public boolean smallCandleNoiseRemoval;
    public boolean flowSizeGreaterThan4;

    public boolean isMaxDetect;
    public boolean isEventTypeMaxDetect;
    public boolean isMinDetect;
    public boolean isEventTypeMinDetect;
    public boolean isEventTypeMinDetectCancel;
    public boolean isEventTypeMaxDetectCancel;
    public boolean isEventTypeMaxConfirm;
    public boolean isEventTypeMinConfirm;


    public double low0;
    public double high0;
    public double low1;
    public double high1;
    public double low2;
    public double high2;
    public double low3;
    public double high3;
    public double low4;
    public double high4;

    public double high0MinusLow0;
    public double abnormalHeightLevel;
    public double closeAverage;




    public boolean isLow2GreatherThanMin;
    public boolean isHigh2LessThanMax;

    public boolean low0LessThanMaxValid;
    public boolean low0LessThanMinValue;
    public boolean high0GreaterThanMaxValue;
    public boolean high0GreatherThanMinValid;
    public double maxValue;
    public double maxValid;
    public boolean maxTrend;

    public double minValue;
    public double minValid;
    public boolean minTrend;

    public boolean colorGreaterThanMinus1;
    public boolean colorLessThan1;


    public double max;
    public double min;
    public int getColorMax;
    public int getColorMin;

    public int color0;


    public boolean isHigh1LessThanOrEqualHigh2;
    public boolean isHigh3LessThanOrEqualHigh2;
    public boolean isHigh4LessThanOrEqualHigh2;

    public boolean isLow1GreaterThanOrEqualLow2;
    public boolean isLow3GreaterThanOrEqualLow2;
    public boolean isLow4GreaterThanOrEqualLow2;

}
