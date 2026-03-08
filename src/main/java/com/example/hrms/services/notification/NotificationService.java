package com.example.hrms.services.notification;

import com.example.hrms.dtos.notification.CreateNotificationDto;
import com.example.hrms.dtos.notification.NotificationDetailDto;
import com.example.hrms.dtos.notification.NotificationResponseDto;
import com.example.hrms.entities.*;
import com.example.hrms.enums.NotificationType;
import com.example.hrms.enums.ReferenceType;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.repositories.EmployeeRepository;
import com.example.hrms.repositories.NotificationRepository;
import com.example.hrms.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    public Long createNotification(CreateNotificationDto createNotificationDto) {
        Employee sender = employeeRepository.findById(createNotificationDto.getSender().getId()).orElseThrow(() -> new ResourceNotFoundException("Sender does not exist"));
        Employee receiver = employeeRepository.findById(createNotificationDto.getReceiver().getId()).orElseThrow(() -> new ResourceNotFoundException("Reciver does not exist"));


        Notification notification = new Notification();
        notification.setTitle(createNotificationDto.getTitle());
        notification.setMessage(createNotificationDto.getMessage());
        notification.setNotificationType(createNotificationDto.getNotificationType());
        notification.setSender(sender);
        notification.setReceiver(receiver);
        notification.setReferenceId(createNotificationDto.getReferenceId());
        notification.setReferenceType(createNotificationDto.getReferenceType());
        notificationRepository.save(notification);
        return notification.getId();
    }

    public void mardAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(() -> new ResourceNotFoundException("Notification does not exist"));
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User does not exist"));
        if(!notification.getReceiver().getId().equals(user.getEmployee().getId())) {
            throw new AccessDeniedException("You are not allowed to perform this action");
        }
        notification.setReadStatus(true);
        notificationRepository.save(notification);
    }

    public List<NotificationResponseDto> getUnreadNotificationsOfToday(Long userId){
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User does not exist"));
        Employee employee = user.getEmployee();

        List<Notification> notifications = notificationRepository.findByEmployeeAndDate(employee, LocalDate.now());
        return notifications.stream()
                .map(notification -> {
                    NotificationResponseDto notificationResponseDto = new NotificationResponseDto();
                    notificationResponseDto.setId(notification.getId());
                    notificationResponseDto.setTitle(notification.getTitle());
                    notificationResponseDto.setMessage(notification.getMessage());
                    notificationResponseDto.setReferenceType(notification.getReferenceType());
                    notificationResponseDto.setCreatedAt(notification.getCreatedAt());
                    if(notification.getSender() != null) {
                        Employee sender = notification.getSender();
                        notificationResponseDto.setSender(sender.getFirstName() + " " + sender.getLastName());
                    }
                    if(notification.getReferenceId() != null){
                        notificationResponseDto.setReferenceId(notification.getReferenceId());
                    }
                    return notificationResponseDto;
                }).toList();

    }

    public List<NotificationResponseDto> getAllNotifications(Long userId){
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User does not exist"));
        Employee employee = user.getEmployee();

        List<Notification> notifications = notificationRepository.findAllNotificationByEmployee(employee);
        return notifications.stream()
                .map(notification -> {
                    NotificationResponseDto notificationResponseDto = new NotificationResponseDto();
                    notificationResponseDto.setId(notification.getId());
                    notificationResponseDto.setTitle(notification.getTitle());
                    notificationResponseDto.setMessage(notification.getMessage());
                    notificationResponseDto.setReferenceType(notification.getReferenceType());
                    notificationResponseDto.setCreatedAt(notification.getCreatedAt());
                    if(notification.getSender() != null) {
                        Employee sender = notification.getSender();
                        notificationResponseDto.setSender(sender.getFirstName() + " " + sender.getLastName());
                    }
                    if(notification.getReferenceId() != null){
                        notificationResponseDto.setReferenceId(notification.getReferenceId());
                    }
                    return notificationResponseDto;
                }).toList();
    }

    public NotificationDetailDto getNotificationById(Long notificationId){
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(() -> new ResourceNotFoundException("Notification does not exist"));
        NotificationDetailDto notificationDetailDto = new NotificationDetailDto();
        Employee sender = notification.getSender();
        notificationDetailDto.setId(notification.getId());
        notificationDetailDto.setTitle(notification.getTitle());
        notificationDetailDto.setMessage(notification.getMessage());
        notificationDetailDto.setReferenceType(notification.getReferenceType());
        notificationDetailDto.setCreatedAt(notification.getCreatedAt());
        if(notification.getReferenceId() != null){
            notificationDetailDto.setReferenceId(notification.getReferenceId());
        }
        notificationDetailDto.setSender(sender.getFirstName() + " " + sender.getLastName());
        return notificationDetailDto;
    }

    //
    public void notifyExpenseSubmitted(Employee employee, Employee hr, Expense expense, TravelPlan travelPlan) {

        Notification notification = new Notification();

        notification.setSender(employee);
        notification.setReceiver(hr);

        notification.setTitle("Expense Submitted");

        notification.setNotificationType(NotificationType.TRAVEL);

        notification.setReferenceType(ReferenceType.EXPENSE);
        notification.setReferenceId(expense.getId());

        String message = "%s submitted an expense of %s for travel to %s."
                .formatted(
                        employee.getFirstName(),
                        expense.getAmount(),
                        travelPlan.getDestinationLocation()
                );

        notification.setMessage(message);

        notificationRepository.save(notification);
    }

    public void notifyExpenseRejected(Employee hr, Employee employee, Expense expense) {

        Notification notification = new Notification();

        notification.setSender(hr);
        notification.setReceiver(employee);

        notification.setTitle("Expense Rejected");

        notification.setNotificationType(NotificationType.TRAVEL);
        notification.setReferenceType(ReferenceType.EXPENSE);
        notification.setReferenceId(expense.getId());

        String message = "Your expense of %s has been rejected."
                .formatted(expense.getAmount());

        notification.setMessage(message);

        notificationRepository.save(notification);
    }

    public void notifyExpenseApproved(Employee hr, Employee employee, Expense expense) {

        Notification notification = new Notification();

        notification.setSender(hr);
        notification.setReceiver(employee);

        notification.setTitle("Expense Approved");

        notification.setNotificationType(NotificationType.TRAVEL);
        notification.setReferenceType(ReferenceType.EXPENSE);
        notification.setReferenceId(expense.getId());

        String message = "Your expense of %s has been approved."
                .formatted(expense.getAmount());

        notification.setMessage(message);

        notificationRepository.save(notification);
    }

    public void notifySlotBooking(Employee receiver, SlotBooking slotBooking, GameSlot gameSlot) {
        Notification notification = new Notification();

        notification.setSender(null);
        notification.setReceiver(receiver);

        notification.setTitle("Slot Booked");

        notification.setNotificationType(NotificationType.GAME);
        notification.setReferenceType(ReferenceType.GAME_SLOT);
        notification.setReferenceId(slotBooking.getId());

        String message = "Your booking for slot for %s has been confirmed which is scheduled at %s - %s on %s"
                .formatted(gameSlot.getGame().getGameName(),
                            gameSlot.getStartTime(),
                            gameSlot.getEndTime(),
                            gameSlot.getSlotDate());

        notification.setMessage(message);

        notificationRepository.save(notification);

    }

    public void notifyRegistrationRejected(Employee receiver, GameSlot gameSlot) {
        Notification notification = new Notification();

        notification.setSender(null);
        notification.setReceiver(receiver);

        notification.setTitle("Slot Registration Rejected");

        notification.setNotificationType(NotificationType.GAME);
        notification.setReferenceType(ReferenceType.GAME_SLOT);
        notification.setReferenceId(null);

        String message = "Your slot registration for %s has been rejected which is scheduled at %s - %s on %s"
                .formatted(gameSlot.getGame().getGameName(),
                        gameSlot.getStartTime(),
                        gameSlot.getEndTime(),
                        gameSlot.getSlotDate());

        notification.setMessage(message);

        notificationRepository.save(notification);

    }

    public void notifyRegistrationMade(Employee sender, Employee receiver, GameSlot gameSlot) {
        Notification notification = new Notification();

        notification.setSender(sender);
        notification.setReceiver(receiver);

        notification.setTitle("Slot Registered");

        notification.setNotificationType(NotificationType.GAME);
        notification.setReferenceType(ReferenceType.GAME_SLOT);
        notification.setReferenceId(gameSlot.getId());

        String message = "Your interest for the slot for %s which is scheduled at %s - %s on %s has been registered"
                .formatted(gameSlot.getGame().getGameName(),
                        gameSlot.getStartTime(),
                        gameSlot.getEndTime(),
                        gameSlot.getSlotDate());

        notification.setMessage(message);

        notificationRepository.save(notification);

    }

    public void notifySlotBookingCancelled(Employee sender, Employee receiver, GameSlot gameSlot) {
        Notification notification = new Notification();

        notification.setSender(sender);
        notification.setReceiver(receiver);

        notification.setTitle("Slot Booking Cancelled");

        notification.setNotificationType(NotificationType.GAME);
        notification.setReferenceType(ReferenceType.GAME_SLOT);
        notification.setReferenceId(gameSlot.getId());

        String message = "Your booking for the slot for %s which is scheduled at %s - %s on %s has been cancelled"
                .formatted(gameSlot.getGame().getGameName(),
                        gameSlot.getStartTime(),
                        gameSlot.getEndTime(),
                        gameSlot.getSlotDate());

        notification.setMessage(message);

        notificationRepository.save(notification);
    }

    public void notifySlotRegistrationCancelled(Employee sender, Employee receiver, GameSlot gameSlot) {
        Notification notification = new Notification();

        notification.setSender(sender);
        notification.setReceiver(receiver);

        notification.setTitle("Slot Registration Cancelled");

        notification.setNotificationType(NotificationType.GAME);
        notification.setReferenceType(ReferenceType.GAME_SLOT);
        notification.setReferenceId(gameSlot.getId());

        String message = "Your registration for the slot for %s which is scheduled at %s - %s on %s has been cancelled"
                .formatted(gameSlot.getGame().getGameName(),
                        gameSlot.getStartTime(),
                        gameSlot.getEndTime(),
                        gameSlot.getSlotDate());

        notification.setMessage(message);

        notificationRepository.save(notification);
    }

}
