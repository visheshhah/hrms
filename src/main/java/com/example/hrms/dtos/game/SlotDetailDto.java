package com.example.hrms.dtos.game;

import com.example.hrms.dtos.travel.EmployeeDto;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
public class SlotDetailDto {
    private Long slotId;
    private String gameName;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private int maxPlayers;
    private List<ParticipantsDto> participants;

}
