package io.matel.app.macro.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CountryPrefRepo extends JpaRepository<CountryPref, Long> {
    List<CountryPref> findAllByType(String type);

}
