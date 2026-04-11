package com.ats.platform;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.nio.file.Path;

/**
 * Pins datasource to in-memory H2 so tests do not pick up {@code SPRING_DATASOURCE_URL} from the
 * developer shell (e.g. after running against Docker Postgres).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class AbstractSpringBootIntegrationTest {

	@DynamicPropertySource
	static void testDatasource(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url",
				() -> "jdbc:h2:mem:testdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH");
		registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
		registry.add("spring.datasource.username", () -> "sa");
		registry.add("spring.datasource.password", () -> "");
		registry.add("spring.flyway.enabled", () -> "false");
		registry.add("app.storage.local.root-directory",
				() -> Path.of("target", "integration-resumes").toAbsolutePath().normalize().toString());
	}
}
