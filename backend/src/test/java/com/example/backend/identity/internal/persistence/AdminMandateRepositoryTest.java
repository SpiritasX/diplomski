package com.example.backend.identity.internal.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AdminMandateRepositoryTest {
    @Autowired
    private AdminMandateRepository adminMandateRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void returnsTrueForCurrentlyActiveMandate() {
        OffsetDateTime now =
                OffsetDateTime.parse("2026-08-16T20:00:00Z");

        String studentIndex = "IN 42/2022";

        jdbcTemplate.update("""
            INSERT INTO admin_mandates (
                admin_mandate_id,
                student_index,
                valid_from,
                valid_until
            )
            VALUES (
                SYS_GUID(),
                ?,
                ?,
                ?
            )
            """,
            studentIndex,
            now.minusDays(1),
            now.plusDays(1)
        );

        boolean active = adminMandateRepository.existsActiveMandateForStudent(
                                studentIndex,
                                now
                        );

        assertThat(active).isTrue();
    }

    @Test
    void returnsFalseForExpiredMandate() {
        OffsetDateTime now =
                OffsetDateTime.parse("2026-08-16T20:00:00Z");

        String studentIndex = "IN 42/2022";

        jdbcTemplate.update("""
        INSERT INTO admin_mandates (
            admin_mandate_id,
            student_index,
            valid_from,
            valid_until
        )
        VALUES (
            SYS_GUID(),
            ?,
            ?,
            ?
        )
        """,
                studentIndex,
                now.minusDays(2),
                now.minusDays(1)
        );

        boolean active =
                adminMandateRepository
                        .existsActiveMandateForStudent(
                                studentIndex,
                                now
                        );

        assertThat(active).isFalse();
    }

    @Test
    void returnsFalseWhenMandateExpiresExactlyAtNow() {
        OffsetDateTime now =
                OffsetDateTime.parse("2026-08-16T20:00:00Z");

        jdbcTemplate.update("""
        INSERT INTO admin_mandates (
            admin_mandate_id,
            student_index,
            valid_from,
            valid_until
        )
        VALUES (
            SYS_GUID(),
            ?,
            ?,
            ?
        )
        """,
                "IN 42/2022",
                now.minusDays(1),
                now
        );

        boolean active =
                adminMandateRepository
                        .existsActiveMandateForStudent(
                                "IN 42/2022",
                                now
                        );

        assertThat(active).isFalse();
    }
}
