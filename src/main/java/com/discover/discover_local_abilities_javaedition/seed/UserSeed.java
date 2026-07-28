package com.discover.discover_local_abilities_javaedition.seed;

import com.discover.discover_local_abilities_javaedition.repository.UserRepository;
import com.discover.discover_local_abilities_javaedition.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Seeds the database with dummy users for local development/testing.
 *
 * Activate with the "seed" profile, e.g.:
 *   mvn spring-boot:run -Dspring-boot.run.profiles=seed
 * or set SPRING_PROFILES_ACTIVE=seed
 *
 * Optional properties (application-seed.yml or CLI args):
 *   seed.user-count=20
 *   seed.clear=false
 */
@Component
@Profile("userSeed")
public class UserSeed implements CommandLineRunner {

    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${seed.user-count:20}")
    private int userCount;

    @Value("${seed.clear:true}")
    private boolean clear;

    private static final List<String> FIRST_NAMES = List.of(
            "Aleksandar", "Marija", "Stefan", "Elena", "Nikola",
            "Ana", "Luka", "Sara", "Filip", "Milena",
            "Ivan", "Maja", "Petar", "Jovana", "Darko",
            "Kristina", "Bojan", "Teodora", "Viktor", "Ivana"
    );

    private static final List<String> LAST_NAMES = List.of(
            "Petrov", "Nikolov", "Stojanovic", "Ivanovic", "Dimitrov",
            "Trajkov", "Blazevski", "Ristovski", "Gjorgjevski", "Markovic",
            "Kovacevic", "Milovanovic", "Todorovic", "Jovanovic", "Stankovic"
    );

    private static final List<String> DOMAINS = List.of(
            "gmail.com", "yahoo.com", "outlook.com", "hotmail.com", "proton.me"
    );

    private static final String DESTINATION = "Skopje";

    private static final double LAT_MIN = 41.979054;
    private static final double LAT_MAX = 42.016448;
    private static final double LON_MIN = 21.406731;
    private static final double LON_MAX = 21.44377;

    public UserSeed(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (clear) {
            userRepository.deleteAll();
            entityManager.createNativeQuery("ALTER SEQUENCE users_id_seq RESTART WITH 1")
                    .executeUpdate();
        }

        System.out.printf("Generating %d dummy user(s)...%n%n", userCount);

        int created = 0;
        for (int i = 1; i <= userCount; i++) {
            User user = buildUser(i);
            try {
                userRepository.save(user);
                created++;
                System.out.printf("  ✅ [%3d/%d] %-10s %-20s %s%n",
                        i, userCount, user.getName(), user.getSurname(), user.getEmail());
            } catch (Exception exc) {
                System.out.printf("  ❌ [%3d/%d] Failed (%s): %s%n",
                        i, userCount, user.getEmail(), exc.getMessage());
            }
        }

        System.out.printf("%nDone — %d/%d users inserted.%n", created, userCount);
    }

    private User buildUser(int uid) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        String first = FIRST_NAMES.get(random.nextInt(FIRST_NAMES.size()));
        String last = LAST_NAMES.get(random.nextInt(LAST_NAMES.size()));
        double latitude = round(LAT_MIN + (LAT_MAX - LAT_MIN) * random.nextDouble());
        double longitude = round(LON_MIN + (LON_MAX - LON_MIN) * random.nextDouble());

        User user = new User();
        user.setName(first);
        user.setSurname(last);
        user.setEmail(fakeEmail(first, last, uid));
        user.setDestination(DESTINATION);
        user.setLatitude(latitude);
        user.setLongitude(longitude);
        return user;
    }

    private String fakeEmail(String first, String last, int uid) {
        String domain = DOMAINS.get(ThreadLocalRandom.current().nextInt(DOMAINS.size()));
        return "%s.%s%d@%s".formatted(first.toLowerCase(), last.toLowerCase(), uid, domain);
    }

    private double round(double value) {
        return Math.round(value * 1_000_000.0) / 1_000_000.0;
    }
}