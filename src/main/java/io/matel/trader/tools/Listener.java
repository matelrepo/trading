package io.matel.trader.tools;


import io.matel.trader.domain.Candle;

public interface Listener {

    public void process(Candle candle);

    public void end();
}

