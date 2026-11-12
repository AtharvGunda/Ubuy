package com.ubuy.config;

import com.ubuy.entity.Product;
import com.ubuy.entity.Role;
import com.ubuy.entity.User;
import com.ubuy.repository.ProductRepository;
import com.ubuy.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           ProductRepository productRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Safe idempotent check for admin user
        userRepository.findByEmail("admin@ubuy.com").ifPresentOrElse(
            user -> {
                // If existing admin user has a legacy placeholder password, update with valid BCrypt hash
                if (!passwordEncoder.matches("admin123", user.getPassword())) {
                    user.setPassword(passwordEncoder.encode("admin123"));
                    userRepository.save(user);
                }
            },
            () -> {
                User admin = new User();
                admin.setName("Admin");
                admin.setEmail("admin@ubuy.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(Role.ADMIN);
                userRepository.save(admin);
            }
        );

        // Safe idempotent check for sample products
        if (productRepository.count() == 0) {
            productRepository.save(new Product(null, "Classic White T-Shirt", "100% cotton, unisex fit",
                    new BigDecimal("19.99"), 150, "Apparel"));
            productRepository.save(new Product(null, "Wireless Earbuds", "Bluetooth 5.0, 24h battery life",
                    new BigDecimal("49.99"), 80, "Electronics"));
            productRepository.save(new Product(null, "Stainless Steel Bottle", "750ml, keeps drinks cold for 24h",
                    new BigDecimal("24.99"), 200, "Kitchen"));
        }
    }
}

