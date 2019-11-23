package io.matel.app.domain;

import io.matel.app.ProcessorData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcessorDataRepository extends JpaRepository<ProcessorData, ProcessorDataId> {
    List<ProcessorData> findByIdcontract(long idcontract);
}
