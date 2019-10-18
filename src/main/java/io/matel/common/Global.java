package io.matel.common;

import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class Global {
    public static final String SECRET = "SomeSecretForJWTGeneration";
    public static final int EXPIRATION_TIME = 864_000_000; // 10 days
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";

    public static final int RUNNING_STATE = 1; // 1 = computeticks ; 2 = loadmerges

    private List<Integer> freqList;
    private long idTick;
    public boolean isOnline = false;
    public boolean readonlyComputation = false;
    public boolean readonlyData = true;
    public boolean random = false;
    public boolean hasCompletedLoading = false;
    volatile public int numContracts = 0;
    volatile public int completedContracts = 0;

    public static final int MAX_TICKS_SIZE = 10;
    public static final int MAX_CANDLES_SIZE = 10;


    public synchronized long getIdTick(boolean increment) {
        if (increment) {
            return ++idTick;
        } else {
            return idTick;
        }
    }

    public void setIdTick(long idTick) {
        this.idTick = idTick;
    }

    public static double round(double value, int decimals) {
        double base = Math.pow(10, decimals);
        return Math.round(value * base) / base;
    }

    public synchronized List<Integer> getFreqList() {
        if (freqList == null) {
            freqList = new ArrayList<>();
            freqList.add(0);
            freqList.add(1);
            freqList.add(5);
            freqList.add(15);
            freqList.add(30);
            freqList.add(60);
            freqList.add(420);
            freqList.add(1380);
            freqList.add(6900);
            freqList.add(35000);
            freqList.add(100000);
        }
        return freqList;
    }


}
