package com.rtcomops.treasury.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Persistence entity for Checkbook.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "treasury", name = "checkbooks")
public class CheckbookEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column("bank_account_id")
    private UUID bankAccountId;

    @Column("iban")
    private String iban;

    @Column("prefix")
    private String prefix;

    @Column("start_number")
    private Integer startNumber;

    @Column("end_number")
    private Integer endNumber;

    @Column("number_of_pages")
    private Integer numberOfPages;

    @Column("current_number")
    private Integer currentNumber;

    @Column("status")
    private String status;

    @Column("type")
    @Builder.Default
    private String type = "REEL";

    @Column("is_system")
    @Builder.Default
    private Boolean isSystem = false;

    @Column("next_sequence")
    private Long nextSequence;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Override
    public UUID getId() {
        return this.id;
    }

    @Override
    public boolean isNew() {
        return this.isNew;
    }

    public CheckbookEntity markNotNew() {
        this.isNew = false;
        return this;
    }
}
