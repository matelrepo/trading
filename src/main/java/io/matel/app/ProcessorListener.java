package io.matel.app;

import io.matel.app.state.ProcessorState;

public interface ProcessorListener {

    void notifyEvent(ProcessorState state);
}
