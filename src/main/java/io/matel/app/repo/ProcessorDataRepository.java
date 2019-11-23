package io.matel.app.repo;

import io.matel.app.state.ProcessorData;
import io.matel.app.state.ProcessorDataId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcessorDataRepository extends JpaRepository<ProcessorData, ProcessorDataId> {
    List<ProcessorData> findByIdcontract(long idcontract);
}
