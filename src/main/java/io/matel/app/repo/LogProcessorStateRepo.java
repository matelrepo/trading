package io.matel.app.repo;

import io.matel.app.state.LogProcessorState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogProcessorStateRepo extends JpaRepository<LogProcessorState, Long> {
}
