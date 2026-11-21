package com.finup.asset;

import com.finup.assetgroup.AssetGroup;
import com.finup.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {

    @Query("""
        SELECT a FROM Asset a
        JOIN FETCH a.group g
        WHERE a.user = :user AND (:includeLiquidated = TRUE OR a.liquidatedAt IS NULL)
        ORDER BY g.displayOrder ASC, a.displayOrder ASC, a.name ASC
    """)
    List<Asset> findByUserWithGroup(
            @Param("user") User user,
            @Param("includeLiquidated") boolean includeLiquidated);

    /** Quantidade de ativos não liquidados por grupo: cada linha é [groupId, count]. */
    @Query("""
        SELECT a.group.id, COUNT(a) FROM Asset a
        WHERE a.user = :user AND a.liquidatedAt IS NULL
        GROUP BY a.group.id
    """)
    List<Object[]> countNotLiquidatedByGroup(@Param("user") User user);

    long countByGroup(AssetGroup group);

    Optional<Asset> findByIdAndUser(Long id, User user);
}
