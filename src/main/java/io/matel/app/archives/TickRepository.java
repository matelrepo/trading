//package io.matel.app.repo;
//
//import io.matel.app.domain.Tick;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//@Repository
//public interface TickRepository extends JpaRepository<Tick, Long> {
////    Tick findTopByIdcontractOrderByTimestampDesc(long idcontract);
////    Tick findTopByOrderByIdDesc();
//
////    @Query(value = "SELECT * FROM tick WHERE id > :idTick and idcontract= :idcontract ORDER BY timestamp", nativeQuery = true)
////    List<Tick> getTicksGreatherThanTickByIdContractByOrderByTimestamp(long idcontract, long idTick);
//
////    @Transactional
////    @Modifying
////    @Query("update Tick t set t.close =  t.close + :factor where t.idcontract = :idcontract")
////    int updateHistoricalTicks(@Param("factor") double factor,
////                                   @Param("idcontract") long idcontract);
////
////    @Transactional
////    @Modifying
////    @Query(value = "DELETE FROM tick WHERE idcontract = :idcontract and id > :idTick", nativeQuery = true)
////    void deleteIncorrectTicks(@Param("idcontract") long idcontract, @Param("idTick") long idTick);
//}
