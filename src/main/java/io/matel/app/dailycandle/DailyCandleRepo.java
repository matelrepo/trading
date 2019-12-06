package io.matel.app.dailycandle;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DailyCandleRepo extends JpaRepository<DailyCandle, Long> {

    DailyCandle findTopByOrderByDateDesc();
}
