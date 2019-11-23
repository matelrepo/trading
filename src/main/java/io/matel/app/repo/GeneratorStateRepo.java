package io.matel.app.repo;

import io.matel.app.state.GeneratorState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GeneratorStateRepo extends JpaRepository<GeneratorState, Long> {
    GeneratorState findByIdcontract(long idcontract);
}

