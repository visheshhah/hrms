package com.example.hrms.repositories;

import com.example.hrms.entities.ConfigureGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigureGameRepository extends JpaRepository<ConfigureGame, Long> {
    ConfigureGame findByGameGameName(String gameName);


    boolean existsByGame_IdAndIsActiveFalse(Long gameId);
}
