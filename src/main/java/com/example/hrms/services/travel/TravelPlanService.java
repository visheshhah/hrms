package com.example.hrms.services.travel;

import com.example.hrms.dtos.travel.*;
import com.example.hrms.entities.Employee;
import com.example.hrms.entities.EmployeeTravel;
import com.example.hrms.entities.TravelPlan;
import com.example.hrms.entities.User;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.repositories.EmployeeRepository;
import com.example.hrms.repositories.EmployeeTravelRepository;
import com.example.hrms.repositories.TravelPlanRepository;
import com.example.hrms.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TravelPlanService {
    private final EmployeeRepository employeeRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final EmployeeTravelRepository employeeTravelRepository;

    public TravelPlanResponseDto createTravelPlan(TravelPlanDto travelPlanDto, Long creatorId) {

        User creator = userRepository.findById(creatorId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Employee creatorEmployee = creator.getEmployee();

        if(travelPlanDto.getStartDate().isAfter(travelPlanDto.getEndDate())) {
            throw new IllegalArgumentException("Start date should be after end date");
        }

        TravelPlan travelPlanEntity = new TravelPlan();
        List<EmployeeTravel> participants = new ArrayList<>();

        //travelPlanEntity = modelMapper.map(travelPlanDto, TravelPlan.class);
        travelPlanEntity.setTitle(travelPlanDto.getTitle());
        travelPlanEntity.setDescription(travelPlanDto.getDescription());
        travelPlanEntity.setStartDate(travelPlanDto.getStartDate());
        travelPlanEntity.setEndDate(travelPlanDto.getEndDate());
        travelPlanEntity.setSourceLocation(travelPlanDto.getSourceLocation());
        travelPlanEntity.setDestinationLocation(travelPlanDto.getDestinationLocation());
        travelPlanEntity.setCreatedByEmployee(creatorEmployee);
        travelPlanEntity.setIsInternational(travelPlanDto.getIsInternational());

        for(EmployeeTravelDto employee: travelPlanDto.getEmployees()) {
            Employee employeeEntity = employeeRepository.findById(employee.getEmployeeId()).orElseThrow(() -> new ResourceNotFoundException("EMPLOYEE NOT FOUND"));
            EmployeeTravel employeeTravelEntity = new EmployeeTravel();
            employeeTravelEntity.setEmployee(employeeEntity);
            employeeTravelEntity.setTravelPlan(travelPlanEntity);
            participants.add(employeeTravelEntity);
        }
        travelPlanEntity.setEmployeeTravels(participants);

        travelPlanEntity = travelPlanRepository.save(travelPlanEntity);

        return modelMapper.map(travelPlanEntity, TravelPlanResponseDto.class);
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

    public TravelPlanResponseDto updateTravelPlan(Long travelPlanId , UpdateTravelPlanDto travelPlanDto, Long creatorId)  {
        TravelPlan travelPlanEntity = travelPlanRepository.findById(travelPlanId).orElseThrow(() -> new ResourceNotFoundException("Travel Plan not found"));
        Employee creator = employeeRepository.findById(creatorId).orElseThrow(() -> new ResourceNotFoundException("Employee not found"));


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
        //TODO Pending




        travelPlanEntity = travelPlanRepository.save(travelPlanEntity);
        return modelMapper.map(travelPlanEntity, TravelPlanResponseDto.class);
    }

    public void deleteTravelPlan(Long travelPlanId) throws ResourceNotFoundException {
        TravelPlan travelPlanEntity = travelPlanRepository.findById(travelPlanId).orElseThrow(() -> new ResourceNotFoundException("TRAVEL PLAN NOT FOUND"));
        travelPlanEntity.setIsActive(Boolean.FALSE);

    }

    public TravelPlanResponseDto getTravelPlanById(Long travelPlanId){
        TravelPlan travelPlan = travelPlanRepository.findById(travelPlanId).orElseThrow(() -> new ResourceNotFoundException("TRAVEL PLAN NOT FOUND"));
        return  modelMapper.map(travelPlan, TravelPlanResponseDto.class);
    }

    public List<TravelPlanResponseDto> getAllTravelPlans(){
        List<TravelPlan> travelPlans = travelPlanRepository.findAll();
        return travelPlans.stream()
                .map(travelPlan -> modelMapper.map(travelPlan, TravelPlanResponseDto.class))
                .toList();
    }

    public List<TravelPlanResponseDto> getTravelPlansByEmployeeId(Long userId){
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<TravelPlan> travelPlans = employeeTravelRepository.findByEmployeeId(user.getEmployee().getId()).orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
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