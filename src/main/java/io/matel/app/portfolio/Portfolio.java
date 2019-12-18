package io.matel.app.portfolio;

import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class Portfolio {

    double initMarginReq =0;
    double maintMarginReq = 0;
    double netLiquidation = 0;
    double dailyPnl =0;
    double realizedPnl = 0;
    double unrealizedPnl = 0;
    ZonedDateTime lastUpdate;
    Map<Integer, Position> positions = new HashMap<>();

    public double getInitMarginReq() {
        return initMarginReq;
    }

    public void setInitMarginReq(double initMarginReq) {
        this.initMarginReq = initMarginReq;
    }

    public double getMaintMarginReq() {
        return maintMarginReq;
    }

    public void setMaintMarginReq(double maintMarginReq) {
        this.maintMarginReq = maintMarginReq;
    }

    public double getNetLiquidation() {
        return netLiquidation;
    }

    public void setNetLiquidation(double netLiquidation) {
        this.netLiquidation = netLiquidation;
    }

    public double getDailyPnl() {
        return dailyPnl;
    }

    public void setDailyPnl(double dailyPnl) {
        this.dailyPnl = dailyPnl;
    }

    public double getRealizedPnl() {
        return realizedPnl;
    }

    public void setRealizedPnl(double realizedPnl) {
        this.realizedPnl = realizedPnl;
    }

    public double getUnrealizedPnl() {
        return unrealizedPnl;
    }

    public void setUnrealizedPnl(double unrealizedPnl) {
        this.unrealizedPnl = unrealizedPnl;
    }

    public ZonedDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(ZonedDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Map<Integer, Position> getPositions() {
        return positions;
    }

    public void setPositions(Map<Integer, Position> positions) {
        this.positions = positions;
    }

    @Override
    public String toString() {
        return "Portfolio{" +
                "initMarginReq=" + initMarginReq +
                ", maintMarginReq=" + maintMarginReq +
                ", netLiquidation=" + netLiquidation +
                ", dailyPnl=" + dailyPnl +
                ", realizedPnl=" + realizedPnl +
                ", unrealizedPnl=" + unrealizedPnl +
                ", lastUpdate=" + lastUpdate +
                '}';
    }
}
