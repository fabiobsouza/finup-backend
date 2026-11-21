package com.finup.entry;

import com.finup.asset.Asset;
import com.finup.user.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MonthlyEntryRepository extends JpaRepository<MonthlyEntry, Long> {

    @Query("""
        SELECT e FROM MonthlyEntry e
        JOIN FETCH e.asset a
        JOIN FETCH a.group g
        WHERE e.user.id = :userId AND e.year = :year AND e.month = :month
        ORDER BY a.displayOrder ASC, a.name ASC
    """)
    List<MonthlyEntry> findByUserAndYearAndMonthFetch(
            @Param("userId") Long userId,
            @Param("year") int year,
            @Param("month") int month);

    Optional<MonthlyEntry> findByAssetAndYearAndMonth(Asset asset, Short year, Short month);

    List<MonthlyEntry> findByAssetAndUserOrderByYearAscMonthAsc(Asset asset, User user);

    boolean existsByAsset(Asset asset);

    /** Lançamento mais recente do ativo. */
    Optional<MonthlyEntry> findFirstByAssetOrderByYearDescMonthDesc(Asset asset);

    /** Lançamentos do ano + dezembro do ano anterior (base da variação anual). */
    @Query("""
        SELECT e FROM MonthlyEntry e
        JOIN FETCH e.asset a
        JOIN FETCH a.group g
        WHERE e.user.id = :userId
          AND (e.year = :year OR (e.year = :previousYear AND e.month = 12))
    """)
    List<MonthlyEntry> findForAnnualReport(
            @Param("userId") Long userId,
            @Param("year") int year,
            @Param("previousYear") int previousYear);

    @Query("SELECT DISTINCT e.year FROM MonthlyEntry e WHERE e.user.id = :userId ORDER BY e.year")
    List<Short> findDistinctYears(@Param("userId") Long userId);

    @Query("""
        SELECT e FROM MonthlyEntry e
        JOIN FETCH e.asset a
        JOIN FETCH a.group g
        WHERE e.user.id = :userId
          AND (e.year > :fromYear OR (e.year = :fromYear AND e.month >= :fromMonth))
        ORDER BY e.year ASC, e.month ASC
    """)
    List<MonthlyEntry> findByUserFromPeriod(
            @Param("userId") Long userId,
            @Param("fromYear") int fromYear,
            @Param("fromMonth") int fromMonth
    );

    /** Diagnostic: all entries for a specific period — used by dashboard for current month */
    @Query("""
        SELECT e FROM MonthlyEntry e
        JOIN FETCH e.asset a
        JOIN FETCH a.group g
        WHERE e.user.id = :userId AND e.year = :year AND e.month = :month
        ORDER BY g.name ASC, a.name ASC
    """)
    List<MonthlyEntry> findByUserIdAndPeriod(
            @Param("userId") Long userId,
            @Param("year") int year,
            @Param("month") int month
    );
}
