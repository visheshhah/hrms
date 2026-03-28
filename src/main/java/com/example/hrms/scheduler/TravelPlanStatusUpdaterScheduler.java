package com.example.hrms.scheduler;

import com.example.hrms.entities.TravelPlan;
import com.example.hrms.enums.TravelStatus;
import com.example.hrms.repositories.TravelPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TravelPlanStatusUpdaterScheduler {

    private final TravelPlanRepository travelPlanRepository;

    //@Scheduled(cron = "0 0 0 * * *")
    @Scheduled(cron = "0 */1 * * * ?")
    @Transactional
    public void updateTravelPlanStatus() {

        LocalDate today = LocalDate.now();

        List<TravelPlan> travelPlans = travelPlanRepository.findTravels();

        for (TravelPlan travelPlan : travelPlans) {

            TravelStatus newStatus;

            if (today.isAfter(travelPlan.getEndDate())) {
                newStatus = TravelStatus.COMPLETED;

            } else if (
                    (today.isEqual(travelPlan.getStartDate()) || today.isAfter(travelPlan.getStartDate()))
                            &&
                            (today.isEqual(travelPlan.getEndDate()) || today.isBefore(travelPlan.getEndDate()))
            ) {
                newStatus = TravelStatus.ACTIVE;

            } else {
                newStatus = TravelStatus.DRAFT;
            }

            if (!travelPlan.getStatus().equals(newStatus)) {
                travelPlan.setStatus(newStatus);
            }
        }
    }
}