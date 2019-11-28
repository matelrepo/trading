package io.matel.app.macro.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MacroDataRepo extends JpaRepository<MacroData, Long> {
    List<MacroData> findByCode(String code);


    @Query(value = "SELECT f.code, f.date, f.current, f.previous, u.country FROM (\n" +
            "SELECT a.code, a.date, a.value as current, b.value as previous FROM (\n" +
            "SELECT * from (\n" +
            "SELECT\n" +
            "   code, date, value,\n" +
            "   DENSE_RANK () OVER ( \n" +
            "      PARTITION BY code\n" +
            "      ORDER BY date DESC\n" +
            "   ) date_rank \n" +
            "FROM\n" +
            "   macro_data order by date desc\n" +
            "\t) t1 WHERE date_rank =1\n" +
            "\t) a\n" +
            "\tLEFT JOIN(\n" +
            "\tSELECT * FROM (\n" +
            "\tSELECT * from (\n" +
            "SELECT\n" +
            "   code, date, value,\n" +
            "   DENSE_RANK () OVER ( \n" +
            "      PARTITION BY code\n" +
            "      ORDER BY date DESC\n" +
            "   ) date_rank \n" +
            "FROM\n" +
            "   macro_data order by date desc\n" +
            "\t) t1 WHERE date_rank =2\n" +
            "\t\t\n" +
            "\t\t) c) b ON a.code = b.code order by a.date desc ) f\n" +
            "LEFT JOIN macro_update u on f.code = u.code WHERe country LIKE ('Germany')", nativeQuery = true)
    List<MacroDAO> findMat();
}
