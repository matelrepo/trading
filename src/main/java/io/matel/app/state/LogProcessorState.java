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
    public ZonedDateTime timestamp;

    public LogProcessorState(){}

    public LogProcessorState(long idcontract, int freq){
        this.idcontract = idcontract;
        this.freq = freq;
    }

    public boolean isNewCandle0;
    public boolean smallCandleNoiseRemoval;

    public boolean isMaxDetect;
    public boolean isEventTypeMaxDetect;
    public boolean isMinDetect;
    public boolean isEventTypeMinDetect;
    public boolean isEventTypeMinDetectCancel;
    public boolean isEventTypeMaxDetectCancel;
    public boolean isEventTypeMaxConfirm;
    public boolean isEventTypeMinConfirm;
    public boolean isTradable;


    public double close;
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

    @Override
    public String toString() {
        return "LogProcessorState{" +
                "id=" + id +
                ", idTick=" + idTick +
                ", freq=" + freq +
                ", idcontract=" + idcontract +
                ", timestamp=" + timestamp +
                ", isNewCandle0=" + isNewCandle0 +
                ", smallCandleNoiseRemoval=" + smallCandleNoiseRemoval +
                ", isMaxDetect=" + isMaxDetect +
                ", isEventTypeMaxDetect=" + isEventTypeMaxDetect +
                ", isMinDetect=" + isMinDetect +
                ", isEventTypeMinDetect=" + isEventTypeMinDetect +
                ", isEventTypeMinDetectCancel=" + isEventTypeMinDetectCancel +
                ", isEventTypeMaxDetectCancel=" + isEventTypeMaxDetectCancel +
                ", isEventTypeMaxConfirm=" + isEventTypeMaxConfirm +
                ", isEventTypeMinConfirm=" + isEventTypeMinConfirm +
                ", low0=" + low0 +
                ", high0=" + high0 +
                ", low1=" + low1 +
                ", high1=" + high1 +
                ", low2=" + low2 +
                ", high2=" + high2 +
                ", low3=" + low3 +
                ", high3=" + high3 +
                ", low4=" + low4 +
                ", high4=" + high4 +
                ", high0MinusLow0=" + high0MinusLow0 +
                ", abnormalHeightLevel=" + abnormalHeightLevel +
                ", closeAverage=" + closeAverage +
                ", isLow2GreatherThanMin=" + isLow2GreatherThanMin +
                ", isHigh2LessThanMax=" + isHigh2LessThanMax +
                ", low0LessThanMaxValid=" + low0LessThanMaxValid +
                ", low0LessThanMinValue=" + low0LessThanMinValue +
                ", high0GreaterThanMaxValue=" + high0GreaterThanMaxValue +
                ", high0GreatherThanMinValid=" + high0GreatherThanMinValid +
                ", maxValue=" + maxValue +
                ", maxValid=" + maxValid +
                ", maxTrend=" + maxTrend +
                ", minValue=" + minValue +
                ", minValid=" + minValid +
                ", minTrend=" + minTrend +
                ", colorGreaterThanMinus1=" + colorGreaterThanMinus1 +
                ", colorLessThan1=" + colorLessThan1 +
                ", max=" + max +
                ", min=" + min +
                ", getColorMax=" + getColorMax +
                ", getColorMin=" + getColorMin +
                ", color0=" + color0 +
                ", isHigh1LessThanOrEqualHigh2=" + isHigh1LessThanOrEqualHigh2 +
                ", isHigh3LessThanOrEqualHigh2=" + isHigh3LessThanOrEqualHigh2 +
                ", isHigh4LessThanOrEqualHigh2=" + isHigh4LessThanOrEqualHigh2 +
                ", isLow1GreaterThanOrEqualLow2=" + isLow1GreaterThanOrEqualLow2 +
                ", isLow3GreaterThanOrEqualLow2=" + isLow3GreaterThanOrEqualLow2 +
                ", isLow4GreaterThanOrEqualLow2=" + isLow4GreaterThanOrEqualLow2 +
                '}';
    }
}
