package com.ats.platform.resume;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Deterministic v1 scorer: token overlap / Jaccard between job text and resume text.
 * Embeddings / LLM scoring can plug in later with the same facade.
 */
@Service
public class AtsMatchScoringService {

	private static final Pattern NON_WORD = Pattern.compile("[^a-z0-9]+");
	private static final Set<String> STOPWORDS = Set.of(
			"the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "of", "as", "by",
			"with", "from", "is", "are", "was", "were", "be", "been", "being", "have", "has", "had",
			"do", "does", "did", "will", "would", "could", "should", "may", "might", "must", "shall",
			"this", "that", "these", "those", "it", "its", "we", "you", "they", "them", "our", "your",
			"their", "who", "whom", "which", "what", "when", "where", "why", "how", "all", "each",
			"every", "both", "few", "more", "most", "other", "some", "such", "no", "nor", "not",
			"only", "same", "so", "than", "too", "very", "just", "also", "into", "about", "over",
			"any", "can", "here", "there", "if", "then", "else", "my", "me", "i", "he", "she", "his",
			"her", "us", "up", "out", "down", "off", "per", "via", "within", "without", "among",
			"looking", "seeking", "join", "team", "work", "years", "year", "experience", "skills",
			"ability", "strong", "good", "great", "excellent", "required", "preferred", "plus");

	public MatchResult score(String jobTitle, String jobDescription, String resumeText) {
		String jobBlob = (jobTitle != null ? jobTitle : "") + "\n" + (jobDescription != null ? jobDescription : "");
		Set<String> jobTerms = tokenize(jobBlob);
		Set<String> resumeTerms = tokenize(resumeText != null ? resumeText : "");

		if (jobTerms.isEmpty() && resumeTerms.isEmpty()) {
			return new MatchResult(0, List.of("Not enough text in job posting and resume to compute a score."));
		}
		if (jobTerms.isEmpty()) {
			return new MatchResult(0, List.of("Job posting has no scorable keywords after normalization."));
		}

		Set<String> intersection = new HashSet<>(jobTerms);
		intersection.retainAll(resumeTerms);

		Set<String> union = new HashSet<>(jobTerms);
		union.addAll(resumeTerms);

		int jaccardPercent = union.isEmpty()
				? 0
				: (int) Math.round(100.0 * intersection.size() / union.size());

		// Blend Jaccard with recall against job requirements so “hits all job keywords” weighs strongly.
		int recallPercent = (int) Math.round(100.0 * intersection.size() / jobTerms.size());
		int blended = (int) Math.round(0.5 * jaccardPercent + 0.5 * recallPercent);
		int score = Math.min(100, Math.max(0, blended));

		List<String> reasons = new ArrayList<>();
		reasons.add("Overlap: " + intersection.size() + " shared terms out of " + union.size()
				+ " unique terms across job + resume (Jaccard " + jaccardPercent + "%).");
		reasons.add("Coverage: " + intersection.size() + " of " + jobTerms.size()
				+ " job keywords appear in the resume (" + recallPercent + "%).");

		List<String> topMatches = intersection.stream()
				.sorted(Comparator.comparingInt(String::length).reversed())
				.limit(8)
				.collect(Collectors.toList());
		if (!topMatches.isEmpty()) {
			reasons.add("Sample matched keywords: " + String.join(", ", topMatches) + ".");
		}

		return new MatchResult(score, reasons.stream().limit(5).collect(Collectors.toList()));
	}

	private static Set<String> tokenize(String text) {
		String normalized = Normalizer.normalize(text.toLowerCase(Locale.ROOT), Normalizer.Form.NFKD);
		normalized = NON_WORD.matcher(normalized).replaceAll(" ");
		Set<String> out = new HashSet<>();
		for (String raw : normalized.split("\\s+")) {
			String t = raw.trim();
			if (t.length() < 2) {
				continue;
			}
			if (STOPWORDS.contains(t)) {
				continue;
			}
			if (t.chars().allMatch(Character::isDigit)) {
				continue;
			}
			out.add(t);
		}
		return out;
	}

	public record MatchResult(int score0to100, List<String> reasons) {
	}
}
