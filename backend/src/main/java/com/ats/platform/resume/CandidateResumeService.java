package com.ats.platform.resume;

import com.ats.platform.api.BadRequestException;
import com.ats.platform.api.NotFoundException;
import com.ats.platform.api.job.dto.MyApplicationResponse;
import com.ats.platform.config.ResumeUploadProperties;
import com.ats.platform.domain.JobApplication;
import com.ats.platform.domain.JobPosting;
import com.ats.platform.domain.UserRole;
import com.ats.platform.repository.JobApplicationRepository;
import com.ats.platform.security.UserPrincipal;
import com.ats.platform.storage.LocalResumeStorage;
import com.ats.platform.util.MatchReasonsJson;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

@Service
public class CandidateResumeService {

	private final JobApplicationRepository jobApplicationRepository;
	private final LocalResumeStorage localResumeStorage;
	private final ResumeTextExtractor resumeTextExtractor;
	private final AtsMatchScoringService atsMatchScoringService;
	private final ResumeUploadProperties uploadProperties;

	public CandidateResumeService(
			JobApplicationRepository jobApplicationRepository,
			LocalResumeStorage localResumeStorage,
			ResumeTextExtractor resumeTextExtractor,
			AtsMatchScoringService atsMatchScoringService,
			ResumeUploadProperties uploadProperties) {
		this.jobApplicationRepository = jobApplicationRepository;
		this.localResumeStorage = localResumeStorage;
		this.resumeTextExtractor = resumeTextExtractor;
		this.atsMatchScoringService = atsMatchScoringService;
		this.uploadProperties = uploadProperties;
	}

	@Transactional
	public MyApplicationResponse uploadResume(UserPrincipal principal, Long applicationId, MultipartFile file) {
		if (principal.getRole() != UserRole.CANDIDATE) {
			throw new BadRequestException("Only candidates can upload resumes");
		}
		if (file == null || file.isEmpty()) {
			throw new BadRequestException("File is required");
		}
		if (file.getSize() > uploadProperties.getMaxSizeBytes()) {
			throw new BadRequestException("File exceeds maximum size of " + uploadProperties.getMaxSizeBytes() + " bytes");
		}
		String contentType = normalizeContentType(file.getContentType());
		if (contentType == null || uploadProperties.getAllowedContentTypes().stream()
				.noneMatch(ct -> ct.equalsIgnoreCase(contentType))) {
			throw new BadRequestException("Unsupported file type. Allowed: PDF and DOCX.");
		}

		JobApplication app = jobApplicationRepository.findDetailById(applicationId)
				.orElseThrow(() -> new NotFoundException("Application not found"));
		if (!Objects.equals(app.getCandidate().getId(), principal.getId())) {
			throw new NotFoundException("Application not found");
		}

		byte[] bytes;
		try {
			bytes = file.getBytes();
		}
		catch (IOException e) {
			throw new BadRequestException("Could not read uploaded file");
		}

		String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "resume";

		String parsedText;
		try {
			parsedText = resumeTextExtractor.extract(bytes, filename);
		}
		catch (IOException | TikaException | SAXException e) {
			throw new BadRequestException("Could not extract text from resume. Use a valid PDF or DOCX.");
		}

		JobPosting job = app.getJobPosting();
		AtsMatchScoringService.MatchResult match = atsMatchScoringService.score(
				job.getTitle(),
				job.getDescription(),
				parsedText);

		String previousKey = app.getResumeStorageKey();
		String storageKey;
		try {
			storageKey = localResumeStorage.store(
					principal.getId(),
					applicationId,
					filename,
					new ByteArrayInputStream(bytes));
		}
		catch (IOException e) {
			throw new BadRequestException("Could not store file");
		}

		localResumeStorage.deleteIfExists(previousKey);

		app.setResumeFile(
				storageKey,
				filename,
				contentType,
				Instant.now(),
				parsedText,
				match.score0to100(),
				MatchReasonsJson.write(match.reasons()));

		app = jobApplicationRepository.save(app);
		return MyApplicationResponse.fromEntity(app);
	}

	private static String normalizeContentType(String raw) {
		if (raw == null) {
			return null;
		}
		int semi = raw.indexOf(';');
		String base = semi > 0 ? raw.substring(0, semi) : raw;
		return base.trim().toLowerCase(Locale.ROOT);
	}
}
