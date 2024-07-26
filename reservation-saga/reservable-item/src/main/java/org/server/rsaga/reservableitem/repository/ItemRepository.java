package org.server.rsaga.reservableitem.repository;

import org.server.rsaga.reservableitem.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByBusinessId(Long businessId);

    Optional<Item> findByIdAndBusinessId(Long id, Long businessId);
}