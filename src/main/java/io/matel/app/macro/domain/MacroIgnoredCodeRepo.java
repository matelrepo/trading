package io.matel.app.macro.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MacroIgnoredCodeRepo extends JpaRepository<MacroIgnoredCode, String> {

    List<MacroIgnoredCode> findAll();

}
