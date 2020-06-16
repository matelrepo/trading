package io.matel.app.config.Ibconfig;

import com.ib.client.TickAttrib;
import com.ib.client.TickAttribBidAsk;
import com.ib.client.TickAttribLast;

public interface IbClient {

    void tickPrice(int tickerId, int field, double price, TickAttrib attrib);

    void tickSize(int tickerId, int field, int size);

    void tickByTickAllLast(int reqId, int tickType, long time, double price, int size, TickAttribLast tickAttribLast, String exchange, String specialConditions);

    void tickByTickBidAsk(int reqId, long time, double bidPrice, double askPrice, int bidSize, int askSize, TickAttribBidAsk tickAttribBidAsk);

}
