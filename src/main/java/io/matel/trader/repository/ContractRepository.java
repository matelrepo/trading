package io.matel.trader.repository;

import io.matel.trader.domain.ContractBasic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractRepository extends JpaRepository<ContractBasic, Long> {
}
