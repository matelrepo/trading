package io.matel.app.config;

import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;

@Configuration
public class Global {
    public static final boolean ONLINE = false;
    public static final boolean RANDOM = false;
    public static final boolean READ_ONLY_TICKS = true;
    public static final boolean READ_ONLY_CANDLES = true;
    public static final boolean COMPUTE_DEEP_HISTORICAL = false;

    public static final String PORT = "5432";
    public static final String ACCOUNT_NUMBER ="U2629343";
    //public static final String ACCOUNT_NUMBER ="U6916961";

    public static final int STARTING_PRICE = 2267;
    public static final int MAX_LENGTH_TICKS = 10;
    public static final int MAX_LENGTH_CANDLE = 100;
    public static final int MAX_TICKS_SIZE_SAVING = 1000;
    public static final int MAX_CANDLES_SIZE_SAVING = 5000;
    public static final int[] FREQUENCIES = {0, 1, 5, 15, 60, 240, 480, 1380, 6900, 35000, 100000, 300000};
    public static final ZoneId ZONE_ID = ZoneId.of("Europe/Paris");

    public static boolean send_email = true;
    public static long startIdCandle = 0;
    public static long startIdTick = 0;
    public static boolean hasCompletedLoading = false;

    private long idTick;
    private long idCandle;


    public static final String SECRET = "SomeSecretForJWTGeneration";
    public static final int TOKEN_EXPIRATION_TIME = 864_000_000; // 10 days
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final int EXECUTOR_THREADS = Runtime.getRuntime().availableProcessors();


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
