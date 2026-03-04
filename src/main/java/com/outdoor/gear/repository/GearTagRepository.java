package com.outdoor.gear.repository;

import com.outdoor.gear.entity.GearTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GearTagRepository extends JpaRepository<GearTag, Long> {

    List<GearTag> findByType(String type);

    Optional<GearTag> findByNameAndType(String name, String type);
}
