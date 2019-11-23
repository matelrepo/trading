package io.matel.app.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface TickRepository extends JpaRepository<Tick, Long> {
    Tick findTopByIdcontractOrderByTimestampDesc(long idcontract);
    Tick findTopByOrderByIdDesc();

    @Query(value = "SELECT * FROM tick WHERE id > :idTick and idcontract= :idcontract", nativeQuery = true)
    List<Tick> getTicksGreatherThanTickByIdContract(long idcontract, long idTick);

    @Transactional
    @Modifying
    @Query("update Tick t set t.close =  t.close + :factor where t.idcontract = :idcontract")
    int updateHistoricalTicks(@Param("factor") double factor,
                                   @Param("idcontract") long idcontract);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM tick WHERE idcontract = :idcontract and id > :idTick", nativeQuery = true)
    void deleteIncorrectTicks(@Param("idcontract") long idcontract, @Param("idTick") long idTick);
}
