package io.matel.trader.repository;

import io.matel.trader.domain.Tick;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TickRepository extends JpaRepository<Tick, Long> {
}
