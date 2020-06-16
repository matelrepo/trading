package io.matel.app.repo;

import io.matel.app.domain.GlobalSettings;
import io.matel.app.domain.TimeSales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GlobalSettingsRepo extends JpaRepository<GlobalSettings, Long> {

    List<GlobalSettings> findAllByIdcontract(Long idcontract);
}
