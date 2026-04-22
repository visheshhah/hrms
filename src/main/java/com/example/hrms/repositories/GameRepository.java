package com.example.hrms.repositories;

import com.example.hrms.entities.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> findByIsActiveTrue();
    //Game findByGameName(String gameName);

    Optional<Game> findByIdAndIsActiveTrue(Long id);

    Optional<Game> findByGameName(String gameName);
}
