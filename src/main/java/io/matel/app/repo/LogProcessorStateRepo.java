package io.matel.app.repo;

import io.matel.app.state.LogProcessorState;
import io.matel.app.state.ProcessorState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogProcessorStateRepo extends JpaRepository<LogProcessorState, Long> {
    List<LogProcessorState> findTop500ByIdcontractAndFreqOrderByIdDesc(long idcontract, int freq);
    List<LogProcessorState> findByIdcontract(long idcontract);



}
