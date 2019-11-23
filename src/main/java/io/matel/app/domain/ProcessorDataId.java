package io.matel.app.domain;

import java.io.Serializable;
import java.util.Objects;

public class ProcessorDataId implements Serializable {

    private long idcontract;
    private int freq;

    public ProcessorDataId() { }

    public ProcessorDataId(long idcontract, int freq) {
        this.idcontract = idcontract;
        this.freq = freq;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcessorDataId that = (ProcessorDataId) o;
        return idcontract == that.idcontract &&
                freq == that.freq;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idcontract, freq);
    }
}
