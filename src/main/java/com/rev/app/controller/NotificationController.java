package com.rev.app.controller;

import com.rev.app.entity.User;
import com.rev.app.service.UserService;
import com.rev.app.service.NotificationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;

@Controller
public class NotificationController {


@Autowired
private NotificationService notificationService;

@Autowired
private UserService userService;


// ⭐ GLOBAL METHOD → Load unread notification count for navbar
@ModelAttribute
public void loadNotificationCount(Model model, Principal principal) {

    if (principal != null) {

        String email = principal.getName();

        Optional<User> userOpt = userService.findByEmail(email);

        if (userOpt.isPresent()) {

            User user = userOpt.get();

            long unreadCount = notificationService.getUnreadCount(user.getUserId());

            model.addAttribute("unreadCount", unreadCount);
        }
    }
}


// ================= VIEW NOTIFICATIONS =================
@GetMapping("/notifications")
public String notifications(Model model, Principal principal) {

    if (principal == null) {
        return "redirect:/login";
    }

    String email = principal.getName();

    Optional<User> userOpt = userService.findByEmail(email);

    if (userOpt.isPresent()) {

        User user = userOpt.get();

        model.addAttribute(
                "notifications",
                notificationService.getUserNotifications(user.getUserId())
        );
    }

    return "notifications";
}


// ================= MARK NOTIFICATION AS READ =================
@PostMapping("/notifications/read/{id}")
public String markAsRead(@PathVariable Long id) {

    notificationService.markAsRead(id);

    return "redirect:/notifications";
}


}
