package io.matel.app.repo;

import io.matel.app.domain.ContractBasic;
import io.matel.app.domain.TimeSales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeSalesRepo extends JpaRepository<TimeSales, Long> {
}
