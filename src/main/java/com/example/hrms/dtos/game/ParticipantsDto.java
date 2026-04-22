package com.example.hrms.dtos.game;

import com.example.hrms.enums.SlotBookingStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParticipantsDto {
        private Long employeeId;
        private String name;
        private String department;
        private SlotBookingStatus status;


}
