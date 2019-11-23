package io.matel.app.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class Global {
    public static final boolean ONLINE = false;
    public static final boolean RANDOM = true;
    public static final boolean READ_ONLY_TICKS = false;
    public static final boolean READ_ONLY_CANDLES = false;
    public static final boolean UPDATE_MACRO = false;


    public static final String SECRET = "SomeSecretForJWTGeneration";
    public static final int TOKEN_EXPIRATION_TIME = 864_000_000; // 10 days
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final int EXECUTOR_THREADS = 10;
    public static final int STARTING_PRICE = 10000;
    public static final int RUNNING_STATE = 1; // 1 = computeticks ; 2 = loadmerges
    public static final int MAX_LENGTH_TICKS = 10;
    public static final int MAX_LENGTH_FLOW = 100;
    public static final int MAX_TICKS_SIZE_SAVING = 1000;
    public static final int MAX_CANDLES_SIZE_SAVING = 1000;
    public static final int[] FREQUENCIES = {0, 1, 5, 15, 30, 60, 240, 1380, 6900,35000,100000};



    private long idTick;
    private long idCandle;


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

    public synchronized long getIdCandle(boolean increment) {
        if (increment) {
            return ++idCandle;
        } else {
            return idCandle;
        }
    }

    public void setIdCandle(long idCandle) {
        this.idCandle = idCandle;
    }

}
