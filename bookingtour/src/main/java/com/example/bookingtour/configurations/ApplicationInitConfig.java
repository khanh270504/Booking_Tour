package com.example.bookingtour.configurations;

import com.example.bookingtour.entities.Role;
import com.example.bookingtour.entities.User;
import com.example.bookingtour.enums.UserStatus;
import com.example.bookingtour.repositories.RoleRepository;
import com.example.bookingtour.repositories.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;
    UserRepository userRepository;
    RoleRepository roleRepository;

    @NonFinal
    @Value("${admin.username}")
    protected String adUsername;

    @NonFinal
    @Value("${admin.password}")
    protected String adPassword;

    @EventListener(ApplicationReadyEvent.class)
    public void initAdmin() {
        String adminUsername = adUsername;
        String adminPassword = passwordEncoder.encode(adPassword);


        if (userRepository.findByEmail(adminUsername).isEmpty()) {
            log.info("Tao tk Admin");


            Role adminRole = roleRepository.findById("ADMIN")
                    .orElseGet(() -> {
                        Role role = Role.builder()
                                .roleName("ADMIN")
                                .description("Quản trị hệ thống")
                                .build();
                        return roleRepository.save(role);
                    });

            User adminUser = User.builder()
                    .email(adminUsername)
                    .password(adminPassword)
                    .userCode("Admin")
                    .status(UserStatus.ACTIVE)
                    .role(adminRole)
                    .build();

            userRepository.save(adminUser);

        } else {
            log.info("Admin da ton tai");
        }
    }
}
