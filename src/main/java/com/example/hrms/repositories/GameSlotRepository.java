package com.example.hrms.repositories;

import com.example.hrms.entities.Game;
import com.example.hrms.entities.GameSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface GameSlotRepository extends JpaRepository<GameSlot,Long> {
    boolean existsByGameAndSlotDate(Game game, LocalDate slotDate);

    //Get all slots where gameId is this and slotDate is this
    @Query("""
        SELECT gs FROM GameSlot gs
        WHERE gs.game.id = :gameId AND gs.slotDate = :today AND gs.isFinalized = false 
    """)
    List<GameSlot> findByGameIdAndSlotDate(Long gameId, LocalDate today);


    @Query("""
        SELECT gs FROM GameSlot gs
        WHERE gs.slotDate = :today AND gs.isFinalized = false 
    """)
    List<GameSlot> findBySlotDateAndIsFinalizedFalse(LocalDate today);

}
