//package io.matel.app.repo;
//
//
//import io.matel.app.domain.Candle;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//public interface CandleRepository extends JpaRepository<Candle, Long> {
//    List<Candle> findTop100ByIdcontractAndFreqOrderByTimestampDesc(long idcontract, int freq);
//    Candle findTopByOrderByIdDesc();
//    Candle findTopByIdcontractOrderByIdDesc(long idcontract);
//
//
//
////    @Transactional
////    @Modifying
////    @Query("update Candle c set c.close =  c.close + :factor, c.open = c.open + :factor, " +
////            "c.low =  c.low + :factor, c.high = c.high + :factor where c.idcontract = :idcontract")
////    int updateHistoricalCandles(@Param("factor") double factor,
////                              @Param("idcontract") long idcontract);
//
//
////    @Query(value = "SELECT COUNT(t2.mtick) FROM(" +
////    "SELECT t.mtick from(" +
////            "SELECT max(idtick) as mtick, freq FROM candle WHERE idcontract = :idcontract GROUP BY freq ORDER BY freq"+
////	") as t GROUP BY mtick"+
////	") as t2;", nativeQuery = true)
////    long countIdTickBreaks(@Param("idcontract") long idcontract); //should be =1
////
////    @Query(value = "SELECT MIN(t2.mtick) FROM(" +
////            "SELECT t.mtick from(" +
////            "SELECT max(idtick) as mtick, freq FROM candle WHERE idcontract = :idcontract GROUP BY freq ORDER BY freq" +
////            ") as t GROUP BY mtick" +
////            ") as t2;", nativeQuery = true)
////    long getSmallestIdTickBreak(@Param("idcontract") long idcontract);
//
////    @Transactional
////    @Modifying
////    @Query(value = "DELETE FROM Candle c WHERE c.idcontract = :idcontract and c.idTick > :idTick", nativeQuery = true)
////    int deleteIncorrectCandles(@Param("idcontract") long idcontract, @Param("idTick") long idTick);
//
//}
