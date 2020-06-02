package io.matel.app;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DailyRepo extends JpaRepository<Daily, Long> {
    //List<MacroData> findByCode(String code);

}