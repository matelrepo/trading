package io.matel.app.repo;

import io.matel.app.state.ProcessorState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface ProcessorStateRepo extends JpaRepository<ProcessorState, Long> {

    List<ProcessorState> findByIdTick(long idTick);


}
