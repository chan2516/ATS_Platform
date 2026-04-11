package com.ats.platform.storage;

import com.ats.platform.config.ResumeStorageProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Filesystem-backed storage for development. Swap for S3/Cloudinary behind the same interface later.
 */
@Component
public class LocalResumeStorage {

	private final ResumeStorageProperties properties;

	public LocalResumeStorage(ResumeStorageProperties properties) {
		this.properties = properties;
	}

	@PostConstruct
	void ensureRoot() throws IOException {
		Files.createDirectories(properties.getRootDirectory());
	}

	/**
	 * @return opaque storage key (path relative to root, POSIX-style for consistency)
	 */
	public String store(Long userId, Long applicationId, String originalFilename, InputStream in) throws IOException {
		String safeName = sanitizeFilename(originalFilename);
		String relative = userId + "/" + applicationId + "/" + UUID.randomUUID() + "_" + safeName;
		Path target = properties.getRootDirectory().resolve(relative);
		Files.createDirectories(target.getParent());
		Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
		return relative.replace('\\', '/');
	}

	public void deleteIfExists(String storageKey) {
		if (storageKey == null) {
			return;
		}
		Path p = properties.getRootDirectory().resolve(storageKey);
		try {
			Files.deleteIfExists(p);
		}
		catch (IOException ignored) {
			// best-effort cleanup
		}
	}

	public byte[] read(String storageKey) throws IOException {
		Path p = properties.getRootDirectory().resolve(storageKey);
		return Files.readAllBytes(p);
	}

	private static String sanitizeFilename(String name) {
		if (name == null || name.isBlank()) {
			return "resume.bin";
		}
		String stripped = name.replaceAll("[^a-zA-Z0-9._-]", "_");
		return stripped.length() > 200 ? stripped.substring(0, 200) : stripped;
	}
}
