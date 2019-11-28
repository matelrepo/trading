package io.matel.app.repo;

import io.matel.app.state.LogProcessorData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogProcessorDataRepo extends JpaRepository<LogProcessorData, Long> {
}
