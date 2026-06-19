package org.example.notificationservice.admin;

import org.example.notificationservice.admin.dto.AdminStatsResponse;
import org.example.notificationservice.admin.dto.AdminUserResponse;
import org.example.notificationservice.google.GoogleAccountRepository;
import org.example.notificationservice.mail.MailAccountRepository;
import org.example.notificationservice.repository.NotificationRepository;
import org.example.notificationservice.user.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final MailAccountRepository mailAccountRepository;
    private final GoogleAccountRepository googleAccountRepository;
    private final NotificationRepository notificationRepository;

    public AdminController(
            UserRepository userRepository,
            MailAccountRepository mailAccountRepository,
            GoogleAccountRepository googleAccountRepository,
            NotificationRepository notificationRepository
    ) {
        this.userRepository = userRepository;
        this.mailAccountRepository = mailAccountRepository;
        this.googleAccountRepository = googleAccountRepository;
        this.notificationRepository = notificationRepository;
    }

    @GetMapping("/stats")
    public AdminStatsResponse getStats() {
        return new AdminStatsResponse(
                userRepository.count(),
                userRepository.countByRole("ADMIN"),
                mailAccountRepository.count(),
                googleAccountRepository.count(),
                notificationRepository.count()
        );
    }

    @GetMapping("/users")
    public List<AdminUserResponse> getUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> new AdminUserResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getRole()
                ))
                .toList();
    }
}