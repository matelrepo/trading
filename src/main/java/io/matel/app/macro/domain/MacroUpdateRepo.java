package io.matel.app.macro.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MacroUpdateRepo extends JpaRepository<MacroUpdate, Long> {

    MacroUpdate findTopByOrderByRefreshedDesc();
    List<MacroUpdate> findAllByCountry(String country);
    List<MacroUpdate> findDistinctByCode(String code);

}
