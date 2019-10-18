package io.matel.trader.tools;

import com.ib.client.TickAttrib;

public interface IBClient {

    public void tickPrice(int tickerId, int field, double price, TickAttrib attrib);

    public void reconnectMktData();


}
