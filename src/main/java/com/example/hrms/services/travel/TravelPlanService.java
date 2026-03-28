package com.example.hrms.services.travel;

import com.example.hrms.dtos.notification.CreateNotificationDto;
import com.example.hrms.dtos.travel.*;
import com.example.hrms.entities.*;
import com.example.hrms.enums.ERole;
import com.example.hrms.enums.NotificationType;
import com.example.hrms.enums.ReferenceType;
import com.example.hrms.enums.TravelStatus;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.repositories.EmployeeRepository;
import com.example.hrms.repositories.EmployeeTravelRepository;
import com.example.hrms.repositories.TravelPlanRepository;
import com.example.hrms.repositories.UserRepository;
import com.example.hrms.services.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.security.InvalidParameterException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TravelPlanService {
    private final EmployeeRepository employeeRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final EmployeeTravelRepository employeeTravelRepository;
    private final NotificationService notificationService;

    public TravelPlanResponseDto createTravelPlan(TravelPlanDto travelPlanDto, Long creatorId) {

        User creator = userRepository.findById(creatorId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Employee creatorEmployee = creator.getEmployee();

        if(travelPlanDto.getStartDate().isAfter(travelPlanDto.getEndDate())) {
            throw new IllegalArgumentException("Start date should be after end date");
        }

        TravelPlan travelPlanEntity = new TravelPlan();
        List<EmployeeTravel> participants = new ArrayList<>();

        travelPlanEntity.setTitle(travelPlanDto.getTitle());
        travelPlanEntity.setDescription(travelPlanDto.getDescription());
        travelPlanEntity.setStartDate(travelPlanDto.getStartDate());
        travelPlanEntity.setEndDate(travelPlanDto.getEndDate());
        travelPlanEntity.setSourceLocation(travelPlanDto.getSourceLocation());
        travelPlanEntity.setDestinationLocation(travelPlanDto.getDestinationLocation());
        travelPlanEntity.setCreatedByEmployee(creatorEmployee);
        travelPlanEntity.setIsInternational(travelPlanDto.getIsInternational());

        String destinationLocation = travelPlanDto.getDestinationLocation();
        LocalDate startDate = travelPlanDto.getStartDate();
        LocalDate endDate = travelPlanDto.getEndDate();
        List<Employee> employees = new ArrayList<>();

        for(EmployeeTravelDto employee: travelPlanDto.getEmployees()) {
            Employee employeeEntity = employeeRepository.findById(employee.getEmployeeId()).orElseThrow(() -> new ResourceNotFoundException("EMPLOYEE NOT FOUND"));
            EmployeeTravel employeeTravelEntity = new EmployeeTravel();
            employeeTravelEntity.setEmployee(employeeEntity);
            validateTravelConflict(travelPlanDto.getStartDate(), travelPlanDto.getEndDate(), employeeEntity, null);
            employeeTravelEntity.setTravelPlan(travelPlanEntity);
            employees.add(employeeEntity);
            participants.add(employeeTravelEntity);
        }
        travelPlanEntity.setEmployeeTravels(participants);

        travelPlanEntity = travelPlanRepository.save(travelPlanEntity);

        //
        for(Employee employee: employees) {
            sendNotification(creatorEmployee, employee, employee.getFirstName(), destinationLocation, startDate, endDate, travelPlanEntity.getId());
        }
        //

        return modelMapper.map(travelPlanEntity, TravelPlanResponseDto.class);
    }

    private void sendNotification(Employee sender, Employee reciever, String name, String destination, LocalDate startDate, LocalDate endDate, Long travelPlanId) {
        CreateNotificationDto createNotificationDto = new CreateNotificationDto();
        createNotificationDto.setSender(sender);
        createNotificationDto.setReceiver(reciever);
        createNotificationDto.setTitle("Travel Plan Created");
        createNotificationDto.setReferenceId(travelPlanId);
        createNotificationDto.setReferenceType(ReferenceType.TRAVEL_PLAN);

        String message = "Hello %s, you are scheduled to visit %s starting from %s to %s.".formatted(name, destination, startDate, endDate);
        createNotificationDto.setMessage(message);
        createNotificationDto.setNotificationType(NotificationType.TRAVEL);
        notificationService.createNotification(createNotificationDto);
    }

//    public TravelPlanResponseDto createTravelPlan(TravelPlanDto travelPlanDto, Long creatorId) {
//
//        Employee creator = employeeRepository.findById(creatorId).orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
//
//        if(travelPlanDto.getStartDate().isAfter(travelPlanDto.getEndDate())) {
//            throw new IllegalArgumentException("Start date should be after end date");
//        }
//
//        TravelPlan travelPlanEntity = new TravelPlan();
//        List<EmployeeTravel> participants = new ArrayList<>();
//
//        //travelPlanEntity = modelMapper.map(travelPlanDto, TravelPlan.class);
//        travelPlanEntity.setTitle(travelPlanDto.getTitle());
//        travelPlanEntity.setDescription(travelPlanDto.getDescription());
//        travelPlanEntity.setStartDate(travelPlanDto.getStartDate());
//        travelPlanEntity.setEndDate(travelPlanDto.getEndDate());
//        travelPlanEntity.setSourceLocation(travelPlanDto.getSourceLocation());
//        travelPlanEntity.setDestinationLocation(travelPlanDto.getDestinationLocation());
//        travelPlanEntity.setCreatedByEmployee(creator);
//        travelPlanEntity.setIsInternational(travelPlanDto.getIsInternational());
//
//        for(EmployeeTravelDto employee: travelPlanDto.getEmployees()) {
//            Employee employeeEntity = employeeRepository.findById(employee.getEmployeeId()).orElseThrow(() -> new ResourceNotFoundException("EMPLOYEE NOT FOUND"));
//            EmployeeTravel employeeTravelEntity = new EmployeeTravel();
//            employeeTravelEntity.setEmployee(employeeEntity);
//            employeeTravelEntity.setTravelPlan(travelPlanEntity);
//            participants.add(employeeTravelEntity);
//        }
//        travelPlanEntity.setEmployeeTravels(participants);
//
//        travelPlanEntity = travelPlanRepository.save(travelPlanEntity);
//
//        return modelMapper.map(travelPlanEntity, TravelPlanResponseDto.class);
//    }

    @Transactional
    public TravelPlanResponseDto updateTravelPlan(Long travelPlanId , UpdateTravelPlanDto travelPlanDto, Long creatorId)  {
        TravelPlan travelPlanEntity = travelPlanRepository.findById(travelPlanId).orElseThrow(() -> new ResourceNotFoundException("Travel Plan not found"));
        employeeRepository.findById(creatorId).orElseThrow(() -> new ResourceNotFoundException("Employee not found"));


        if(!travelPlanDto.getTitle().isEmpty()) {
            travelPlanEntity.setTitle(travelPlanDto.getTitle());
        }
        if(!travelPlanDto.getDescription().isEmpty()) {
            travelPlanEntity.setDescription(travelPlanDto.getDescription());
        }
        if(!travelPlanDto.getSourceLocation().isEmpty()) {
            travelPlanEntity.setSourceLocation(travelPlanDto.getSourceLocation());
        }
        if(!travelPlanDto.getDestinationLocation().isEmpty()) {
            travelPlanEntity.setDestinationLocation(travelPlanDto.getDestinationLocation());
        }

        if(travelPlanDto.getStartDate() != null && travelPlanDto.getEndDate() != null) {
            if(travelPlanDto.getStartDate().isAfter(travelPlanDto.getEndDate())) {
                throw new InvalidParameterException("Start date cannot be after end date");
            }
            travelPlanEntity.setStartDate(travelPlanDto.getStartDate());
            travelPlanEntity.setEndDate(travelPlanDto.getEndDate());
        }

        if(travelPlanDto.getStartDate() != null) {
            if(travelPlanDto.getStartDate().isAfter(travelPlanEntity.getEndDate())) {
                throw new InvalidParameterException("Start date cannot be after end date");
            }
            travelPlanEntity.setStartDate(travelPlanDto.getStartDate());
        }

        if(travelPlanDto.getEndDate() != null) {
            if(travelPlanDto.getEndDate().isBefore(travelPlanEntity.getStartDate())) {
                throw new InvalidParameterException("End date cannot be before start date");
            }
            travelPlanEntity.setEndDate(travelPlanDto.getEndDate());
        }

        boolean isDateChanged = travelPlanDto.getStartDate() != null || travelPlanDto.getEndDate() != null;

        if (isDateChanged) {
            for (EmployeeTravel et : travelPlanEntity.getEmployeeTravels()) {
                validateTravelConflict(
                        travelPlanEntity.getStartDate(),
                        travelPlanEntity.getEndDate(),
                        et.getEmployee(),
                        travelPlanEntity.getId()
                );
            }
        }

        if(!travelPlanDto.getIsInternational().equals(travelPlanEntity.getIsInternational())) {
            travelPlanEntity.setIsInternational(travelPlanDto.getIsInternational());
        }

        List<EmployeeTravel> existingEmployees = travelPlanEntity.getEmployeeTravels();

        Set<Long> existingEmployeeIds = existingEmployees.stream().map(employeeTravel -> employeeTravel.getEmployee().getId()).collect(Collectors.toSet());
        Set<Long> newEmployeeIds = travelPlanDto.getEmployeeIds() == null
                ? Set.of()
                : new HashSet<>(travelPlanDto.getEmployeeIds());

        Set<Long> toAdd = new HashSet<>(newEmployeeIds);
        toAdd.removeAll(existingEmployeeIds);

        Set<Long> toRemove = new HashSet<>(existingEmployeeIds);
        toRemove.removeAll(newEmployeeIds);

        if (!toRemove.isEmpty()) {
            employeeTravelRepository.deleteByTravelPlanAndEmployeeIdIn(travelPlanEntity, toRemove);
        }

        if (!toAdd.isEmpty()) {
            List<Employee> employeesToAdd = employeeRepository.findAllById(toAdd);

            for (Employee employee : employeesToAdd) {
                validateTravelConflict(
                        travelPlanEntity.getStartDate(),
                        travelPlanEntity.getEndDate(),
                        employee,
                        travelPlanEntity.getId()
                );

                EmployeeTravel employeeTravel = new EmployeeTravel();
                employeeTravel.setEmployee(employee);
                employeeTravel.setTravelPlan(travelPlanEntity);
                employeeTravelRepository.save(employeeTravel);
            }
        }


        travelPlanEntity = travelPlanRepository.save(travelPlanEntity);
        return modelMapper.map(travelPlanEntity, TravelPlanResponseDto.class);
    }

    public void deleteTravelPlan(Long travelPlanId, Long userId) throws AccessDeniedException {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isHr = user.getRoles().stream().anyMatch(role -> role.getName()== ERole.ROLE_HR);
        if(!isHr) {
            throw new AccessDeniedException("You are not allowed to access this resource");
        }
        TravelPlan travelPlanEntity = travelPlanRepository.findById(travelPlanId).orElseThrow(() -> new ResourceNotFoundException("TRAVEL PLAN NOT FOUND"));
        travelPlanEntity.setIsActive(Boolean.FALSE);
        travelPlanEntity.setDeletedAt(Instant.now());
        travelPlanEntity.setDeletedBy(user.getEmployee());
        travelPlanRepository.save(travelPlanEntity);

    }

    public TravelPlanResponseDto getTravelPlanById(Long travelPlanId){
        TravelPlan travelPlan = travelPlanRepository.findById(travelPlanId).orElseThrow(() -> new ResourceNotFoundException("TRAVEL PLAN NOT FOUND"));
        return  modelMapper.map(travelPlan, TravelPlanResponseDto.class);
    }

    public List<TravelPlanResponseDto> getAllTravelPlans(){
        List<TravelPlan> travelPlans = travelPlanRepository.findTravels();
        return travelPlans.stream()
                .map(travelPlan -> modelMapper.map(travelPlan, TravelPlanResponseDto.class))
                .toList();
    }

    public List<TravelPlanResponseDto> getAllTravelPlansByStatus(String status){
        TravelStatus travelStatus;
        String statusLower = status.toLowerCase();
        switch (statusLower) {
            case "cancelled":
                travelStatus = TravelStatus.CANCELLED;
                break;
            case "draft":
                travelStatus = TravelStatus.DRAFT;
                break;
            case "completed":
                travelStatus = TravelStatus.COMPLETED;
                break;
            default:
                travelStatus = TravelStatus.ACTIVE;
                break;
        }

        List<TravelPlan> travelPlans = travelPlanRepository.findTravelsByStatus(travelStatus);
        return travelPlans.stream()
                .map(travelPlan -> modelMapper.map(travelPlan, TravelPlanResponseDto.class))
                .toList();
    }

    public List<TravelPlanResponseDto> getTravelPlansByEmployee(Long userId){
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<EmployeeTravel> employeeTravels = employeeTravelRepository.findByEmployeeId(user.getEmployee().getId());
        List<TravelPlan> travelPlans = employeeTravels.stream()
                .map(EmployeeTravel::getTravelPlan)
                .toList();
        return travelPlans.stream()
                .map(travelPlan -> modelMapper.map(travelPlan, TravelPlanResponseDto.class))
                .toList();

    }

    public List<EmployeeDto> getTravelPlanParticipants(Long travelPlanId){
        TravelPlan travelPlan = travelPlanRepository.findById(travelPlanId).orElseThrow(() -> new ResourceNotFoundException("TRAVEL PLAN NOT FOUND"));
        List<EmployeeTravel> employeeTravels = travelPlan.getEmployeeTravels();
        List<Employee> employees = employeeTravels
                .stream()
                .map(EmployeeTravel::getEmployee)
                .toList();
        return employees.stream()
                .map(employee -> {
                    EmployeeDto employeeDto = new EmployeeDto();
                    employeeDto.setId(employee.getId());
                    employeeDto.setFirstName(employee.getFirstName());
                    employeeDto.setLastName(employee.getLastName());
                    employeeDto.setDesignation(employee.getDesignation());
                    employeeDto.setDepartment(employee.getDepartment().getDepartmentName());
                    return employeeDto;

                })
                .toList();
    }

    public List<TravelPlanResponseDto> getTravelPlansByEmployeeAndStatus(Long userId, String status) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        TravelStatus travelStatus;
        String statusLower = status.toLowerCase();
        switch (statusLower) {
            case "cancelled":
                travelStatus = TravelStatus.CANCELLED;
                break;
            case "draft":
                travelStatus = TravelStatus.DRAFT;
                break;
            case "completed":
                travelStatus = TravelStatus.COMPLETED;
                break;
            default:
                travelStatus = TravelStatus.ACTIVE;
                break;
        }

        List<EmployeeTravel> employeeTravels = employeeTravelRepository.findByEmployeeIdAndStatus(user.getEmployee().getId(), travelStatus);
        List<TravelPlan> travelPlans = employeeTravels.stream()
                .map(EmployeeTravel::getTravelPlan)
                .toList();
        return travelPlans.stream()
                .map(travelPlan -> modelMapper.map(travelPlan, TravelPlanResponseDto.class))
                .toList();
    }

    private void validateTravelConflict(
            LocalDate startDate,
            LocalDate endDate,
            Employee employee,
            Long currentTravelPlanId
    ) {
        List<EmployeeTravel> employeeTravels = employeeTravelRepository.findExistingTravels(employee);

        for (EmployeeTravel et : employeeTravels) {

            if (currentTravelPlanId != null &&
                    et.getTravelPlan().getId().equals(currentTravelPlanId)) {
                continue;
            }

            checkBookingConflicts(et.getTravelPlan(), startDate, endDate);
        }
    }

    private void checkBookingConflicts(TravelPlan travelPlan, LocalDate newStartDate, LocalDate newEndDate) {
        LocalDate travelStartDate = travelPlan.getStartDate();
        LocalDate travelEndDate = travelPlan.getEndDate();

        if (!travelEndDate.isBefore(newStartDate) &&
                !travelStartDate.isAfter(newEndDate)){
                throw new IllegalStateException("Employee is being overbooked");
            }
    }

    public TravelPlanDetailDto getTravelPlanDetailById(Long travelPlanId){
        TravelPlan travelPlan = travelPlanRepository.findById(travelPlanId).orElseThrow(() -> new ResourceNotFoundException("TRAVEL PLAN NOT FOUND"));
        if(!travelPlan.getIsActive()){
            throw new IllegalStateException("Travel Plan is Deleted.");
        }
        List<EmployeeTravel> employeeTravels = travelPlan.getEmployeeTravels();
        List<Employee> employees = employeeTravels
                .stream()
                .map(EmployeeTravel::getEmployee)
                .toList();
        List<EmployeeDto> participants = employees.stream()
                .map(employee -> {
                    EmployeeDto employeeDto = new EmployeeDto();
                    employeeDto.setId(employee.getId());
                    employeeDto.setFirstName(employee.getFirstName());
                    employeeDto.setLastName(employee.getLastName());
                    employeeDto.setDesignation(employee.getDesignation());
                    employeeDto.setDepartment(employee.getDepartment().getDepartmentName());
                    return employeeDto;

                })
                .toList();
        TravelPlanDetailDto travelPlanDetailDto = new TravelPlanDetailDto();
        travelPlanDetailDto.setParticipants(participants);
        travelPlanDetailDto.setTravelPlanId(travelPlanId);
        travelPlanDetailDto.setStartDate(travelPlan.getStartDate());
        travelPlanDetailDto.setEndDate(travelPlan.getEndDate());
        travelPlanDetailDto.setTitle(travelPlan.getTitle());
        travelPlanDetailDto.setDescription(travelPlan.getDescription());
        travelPlanDetailDto.setStatus(travelPlan.getStatus());
        travelPlanDetailDto.setSourceLocation(travelPlan.getSourceLocation());
        travelPlanDetailDto.setDestinationLocation(travelPlan.getDestinationLocation());
        travelPlanDetailDto.setIsInternational(travelPlan.getIsInternational());
        travelPlanDetailDto.setCreatedAt(travelPlan.getCreatedAt());
        travelPlanDetailDto.setCreatedById(travelPlan.getCreatedByEmployee().getId());
        return travelPlanDetailDto;

    }

    public List<TravelPlanResponseDto> getTravelPlansByEmployeeId(Long employeeId){
        Employee employee = employeeRepository.findById(employeeId).orElseThrow(() -> new ResourceNotFoundException("Employee NOT FOUND"));

        List<EmployeeTravel> employeeTravels = employeeTravelRepository.findByEmployeeId(employee.getId());
        List<TravelPlan> travelPlans = employeeTravels.stream()
                .map(EmployeeTravel::getTravelPlan)
                .toList();
        return travelPlans.stream()
                .map(travelPlan -> modelMapper.map(travelPlan, TravelPlanResponseDto.class))
                .toList();

    }

    public List<TravelPlanResponseDto> getTravelPlansByEmployeeIdAndStatus(Long employeeId, String status) {
        Employee employee = employeeRepository.findById(employeeId).orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        TravelStatus travelStatus;
        String statusLower = status.toLowerCase();
        switch (statusLower) {
            case "cancelled":
                travelStatus = TravelStatus.CANCELLED;
                break;
            case "draft":
                travelStatus = TravelStatus.DRAFT;
                break;
            case "completed":
                travelStatus = TravelStatus.COMPLETED;
                break;
            default:
                travelStatus = TravelStatus.ACTIVE;
                break;
        }

        List<EmployeeTravel> employeeTravels = employeeTravelRepository.findByEmployeeIdAndStatus(employee.getId(), travelStatus);
        List<TravelPlan> travelPlans = employeeTravels.stream()
                .map(EmployeeTravel::getTravelPlan)
                .toList();
        return travelPlans.stream()
                .map(travelPlan -> modelMapper.map(travelPlan, TravelPlanResponseDto.class))
                .toList();
    }
}
//public List<TravelPlanResponseDto> getTravelPlansByEmployeeId(Long userId, String status){
//    User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
//    //List<TravelPlan> travelPlans = employeeTravelRepository.findByEmployeeId(user.getEmployee().getId()).orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
//    List<TravelPlan>
//
//    if(status.equals("active")) {
//
//    }else if(status.equals("upcoming")){
//
//    }else if(status.equals("completed")){
//
//    }else {
//
//    }
//
//    return travelPlans.stream()
//            .map(travelPlan -> modelMapper.map(travelPlan, TravelPlanResponseDto.class))
//            .toList();
//
//}
//}