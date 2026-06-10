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
        WHERE a.user = :user AND (:activeOnly IS NULL OR a.active = :activeOnly)
        ORDER BY g.displayOrder ASC, a.displayOrder ASC, a.name ASC
    """)
    List<Asset> findByUserWithGroup(@Param("user") User user, @Param("activeOnly") Boolean activeOnly);

    @Query("""
        SELECT a FROM Asset a
        JOIN FETCH a.group
        WHERE a.group = :group AND a.user = :user
        ORDER BY a.displayOrder ASC, a.name ASC
    """)
    List<Asset> findByGroupAndUserOrderByDisplayOrderAscNameAsc(
            @Param("group") AssetGroup group,
            @Param("user") User user);

    Optional<Asset> findByIdAndUser(Long id, User user);
}
