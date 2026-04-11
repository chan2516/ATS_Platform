package com.ats.platform.resume;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AtsMatchScoringServiceTest {

	private final AtsMatchScoringService scorer = new AtsMatchScoringService();

	@Test
	void highOverlap_producesStrongScoreAndReasons() {
		AtsMatchScoringService.MatchResult r = scorer.score(
				"Java Engineer",
				"We need Java Spring Boot and PostgreSQL.",
				"Professional Java developer with Spring Boot and PostgreSQL experience.");
		assertThat(r.score0to100()).isBetween(40, 100);
		assertThat(r.reasons()).isNotEmpty();
	}

	@Test
	void noOverlap_producesLowScore() {
		AtsMatchScoringService.MatchResult r = scorer.score(
				"Rust Systems",
				"wasm embedded",
				"marketing sales retail");
		assertThat(r.score0to100()).isLessThan(50);
	}
}
