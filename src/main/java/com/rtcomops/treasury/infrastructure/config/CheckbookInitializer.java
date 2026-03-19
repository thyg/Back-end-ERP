package com.rtcomops.treasury.infrastructure.config;

import com.rtcomops.treasury.domain.model.Checkbook;
import com.rtcomops.treasury.domain.port.out.CheckbookRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Initializes the system fictitious checkbook at application startup.
 * This checkbook is used for received checks that don't have a real checkbook.
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-30
 */
@Configuration
public class CheckbookInitializer {

    private static final Logger log = LoggerFactory.getLogger(CheckbookInitializer.class);

    /**
     * System checkbook constants.
     */
    public static final String SYSTEM_CHECKBOOK_PREFIX = "SYSTEM-FICTIF";
    public static final String SYSTEM_CHECKBOOK_IBAN = "SYSTEM";
    public static final String CHECK_NUMBER_PREFIX = "CHQ-REC-";
    public static final String TYPE_FICTIF = "FICTIF";
    public static final String TYPE_REEL = "REEL";
    public static final String STATUS_ACTIVE = "ACTIVE";

    private final CheckbookRepositoryPort checkbookRepository;

    public CheckbookInitializer(CheckbookRepositoryPort checkbookRepository) {
        this.checkbookRepository = checkbookRepository;
    }

    @Bean
    ApplicationRunner initializeSystemCheckbook() {
        return args -> {
            checkbookRepository.findByIsSystemTrue()
                .flatMap(this::ensureCorrectType)
                .switchIfEmpty(createSystemCheckbook())
                .subscribe(
                    checkbook -> log.info("System checkbook initialized: {} (ID: {}, Type: {})",
                        checkbook.getPrefix(), checkbook.getId(), checkbook.getType()),
                    error -> log.error("Error initializing system checkbook", error)
                );
        };
    }

    /**
     * Ensures the system checkbook has the correct FICTIF type.
     * Fixes any existing system checkbook that may have been created with wrong type.
     */
    private Mono<Checkbook> ensureCorrectType(Checkbook checkbook) {
        if (!TYPE_FICTIF.equals(checkbook.getType())) {
            log.warn("System checkbook has incorrect type: {}. Correcting to FICTIF...", checkbook.getType());
            checkbook.setType(TYPE_FICTIF);
            checkbook.setUpdatedAt(LocalDateTime.now());
            return checkbookRepository.save(checkbook)
                .doOnSuccess(cb -> log.info("System checkbook type corrected to FICTIF"));
        }
        return Mono.just(checkbook);
    }

    private Mono<Checkbook> createSystemCheckbook() {
        log.info("Creating system fictitious checkbook...");

        Checkbook systemCheckbook = Checkbook.builder()
            .id(UUID.randomUUID())
            .bankAccountId(null)
            .iban(SYSTEM_CHECKBOOK_IBAN)
            .prefix(SYSTEM_CHECKBOOK_PREFIX)
            .startNumber(null)
            .endNumber(null)
            .currentNumber(null)
            .status(STATUS_ACTIVE)
            .type(TYPE_FICTIF)
            .isSystem(true)
            .nextSequence(1L)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        return checkbookRepository.save(systemCheckbook)
            .doOnSuccess(cb -> log.info("System checkbook created successfully with ID: {}", cb.getId()));
    }

    /**
     * Generates the next check number for received checks.
     * Format: CHQ-REC-000001, CHQ-REC-000002, etc.
     *
     * @param sequence the current sequence number
     * @return formatted check number
     */
    public static String generateReceivedCheckNumber(long sequence) {
        return CHECK_NUMBER_PREFIX + String.format("%06d", sequence);
    }
}
