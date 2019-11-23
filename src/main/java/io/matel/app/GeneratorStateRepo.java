package io.matel.app;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GeneratorStateRepo extends JpaRepository<GeneratorState, Long> {
    GeneratorState findByIdcontract(long idcontract);
}

