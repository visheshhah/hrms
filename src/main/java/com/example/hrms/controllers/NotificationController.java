package com.example.hrms.controllers;

import com.example.hrms.dtos.notification.NotificationDetailDto;
import com.example.hrms.dtos.notification.NotificationResponseDto;
import com.example.hrms.entities.MyUserDetails;
import com.example.hrms.services.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notification")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponseDto>> getUnreadNotifications(@AuthenticationPrincipal MyUserDetails myUserDetails) {
        return new ResponseEntity<>(notificationService.getUnreadNotificationsOfToday(myUserDetails.getId()), HttpStatus.OK);
    }

    @PatchMapping("/{notificationId}/mark")
    public void markAsRead(@PathVariable Long notificationId, @AuthenticationPrincipal MyUserDetails myUserDetails) {
        notificationService.mardAsRead(notificationId, myUserDetails.getId());
    }

    @GetMapping("/all")
    public ResponseEntity<List<NotificationResponseDto>> getAllNotificationOfUser(@AuthenticationPrincipal MyUserDetails myUserDetails) {
        return new ResponseEntity<>(notificationService.getAllNotifications(myUserDetails.getId()), HttpStatus.OK);
    }

    @GetMapping("/{notificationId}")
    public ResponseEntity<NotificationDetailDto> getNotificationById(@PathVariable("notificationId") Long notificationId) {
        return new ResponseEntity<>(notificationService.getNotificationById(notificationId), HttpStatus.OK);
    }
}
