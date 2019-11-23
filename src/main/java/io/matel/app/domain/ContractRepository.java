package io.matel.app.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<ContractBasic, Long> {
    List<ContractBasic> findByActive(boolean active);
    ContractBasic findByIdcontract(long idcontract);
}
