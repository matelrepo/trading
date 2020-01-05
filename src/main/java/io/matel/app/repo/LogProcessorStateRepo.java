package io.matel.app.repo;

import io.matel.app.state.LogProcessorState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogProcessorStateRepo extends JpaRepository<LogProcessorState, Long> {
    List<LogProcessorState> findTop500ByIdcontractAndFreqOrderByIdDesc(long idcontract, int freq);

}
