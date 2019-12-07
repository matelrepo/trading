package io.matel.app;


import io.matel.app.domain.Tick;

public class LoadingErrorHandler {

    long idcontract;
    Tick lastTickByContract;
    long numTicksBreaking;
    long lastCandleId;
    long minTickIdBreaking;
    boolean errorDetected = false;

    @Override
    public String toString() {
        return "LoadingErrorHandler{" +
                "idcontract=" + idcontract +
                ", lastTickByContract=" + lastTickByContract +
                ", numTicksBreaking=" + numTicksBreaking +
                ", lastCandleId=" + lastCandleId +
                ", minTickIdBreaking=" + minTickIdBreaking +
                ", errorDetected=" + errorDetected +
                '}';
    }
}

