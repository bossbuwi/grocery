package com.paradoxdevs.grocery.repository;

import com.paradoxdevs.grocery.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long> {
}
