package io.matel.app.portfolio;

import com.ib.client.Contract;

public class Position {

    Contract contract;
    double quantity =0;
    double marketPrice = 0;
    double marketValue = 0;
    double averageCost = 0;
    double unrealizedPnl = 0;
    double realizedPnl = 0;
    double dailyPnl =0;

    boolean connected = false;

    public Position(Contract contract, double quantity, double marketPrice, double marketValue, double averageCost, double unrealizedPnl, double realizedPnl) {
        this.contract = contract;
        this.quantity = quantity;
        this.marketPrice = marketPrice;
        this.marketValue = marketValue;
        this.averageCost = averageCost;
        this.unrealizedPnl = unrealizedPnl;
        this.realizedPnl = realizedPnl;
    }

    public void updatePosition(int conid, double quantity, double marketPrice, double marketValue, double averageCost,
                               double unrealizedPnl, double realizedPnl) {
        this.contract = contract;
        this.quantity = quantity;
        this.marketPrice = marketPrice;
        this.marketValue = marketValue;
        this.averageCost = averageCost;
        this.unrealizedPnl = unrealizedPnl;
        this.realizedPnl = realizedPnl;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public void setUnrealizedPnl(double unrealizedPnl) {
        this.unrealizedPnl = unrealizedPnl;
    }

    public void setRealizedPnl(double realizedPnl) {
        this.realizedPnl = realizedPnl;
    }

    public void setDailyPnl(double dailyPnl) {
        this.dailyPnl = dailyPnl;
    }

    public Contract getContract() {
        return contract;
    }

    public double getQuantity() {
        return quantity;
    }

    public double getMarketPrice() {
        return marketPrice;
    }

    public double getMarketValue() {
        return marketValue;
    }

    public double getAverageCost() {
        return averageCost;
    }

    public double getUnrealizedPnl() {
        return unrealizedPnl;
    }

    public double getRealizedPnl() {
        return realizedPnl;
    }

    @Override
    public String toString() {
        return "Position{" +
                "contract=" + contract.conid() +
                ", quantity=" + quantity +
                ", marketPrice=" + marketPrice +
                ", marketValue=" + marketValue +
                ", averageCost=" + averageCost +
                ", unrealizedPnl=" + unrealizedPnl +
                ", realizedPnl=" + realizedPnl +
                '}';
    }
}
