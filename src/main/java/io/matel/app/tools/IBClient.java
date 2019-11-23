package io.matel.app.tools;

import com.ib.client.TickAttrib;

public interface IBClient {

    public void tickPrice(int tickerId, int field, double price, TickAttrib attrib);

    public void tickSize(int tickerId, int field, int size);

    public void reconnectMktData();


}
