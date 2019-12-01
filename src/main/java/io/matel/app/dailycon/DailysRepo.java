package io.matel.app.dailycon;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DailysRepo extends JpaRepository<Dailys, Long> {
}
