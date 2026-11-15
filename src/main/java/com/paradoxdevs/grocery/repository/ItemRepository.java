package com.paradoxdevs.grocery.repository;

import com.paradoxdevs.grocery.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}