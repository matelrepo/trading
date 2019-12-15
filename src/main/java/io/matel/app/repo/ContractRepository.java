package io.matel.app.repo;

import io.matel.app.domain.ContractBasic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<ContractBasic, Long> {
    List<ContractBasic> findByActiveAndTypeOrderByIdcontract(boolean active, String type);
    ContractBasic findByIdcontract(long idcontract);
}
