package io.matel.app.repo;

import io.matel.app.state.ProcessorState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ProcessorStateRepo extends JpaRepository<ProcessorState, Long> {

    ProcessorState findTopByIdcontractOrderByIdDesc(long idcontract);
    List<ProcessorState> findByIdTick(long idTick);
  //  ProcessorState findTopByIdcontractAndFreqOrderByIdDesc(long idcontract, int freq);

}
