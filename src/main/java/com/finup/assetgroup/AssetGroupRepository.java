package com.finup.assetgroup;

import com.finup.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssetGroupRepository extends JpaRepository<AssetGroup, Long> {
    List<AssetGroup> findByUserOrderByDisplayOrderAscNameAsc(User user);
    Optional<AssetGroup> findByIdAndUser(Long id, User user);
}
