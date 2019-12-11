package io.matel.app.repo;

import io.matel.app.state.ProcessorState;
import io.matel.app.state.ProcessorStateKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcessorStateRepo extends JpaRepository<ProcessorState, ProcessorStateKey> {
    List<ProcessorState> findByIdcontract(long idcontract);

}
