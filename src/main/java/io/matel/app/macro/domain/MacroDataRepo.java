package io.matel.app.macro.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MacroDataRepo extends JpaRepository<MacroData, MacroDataKey> {
    List<MacroData> findByCode(String code);
}
