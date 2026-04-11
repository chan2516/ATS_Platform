package com.ats.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties(prefix = "app.storage.local")
public class ResumeStorageProperties {

	/**
	 * Root directory for candidate resume files (local dev; replace with S3-style storage in production).
	 */
	private Path rootDirectory = Path.of("data", "resumes");

	public Path getRootDirectory() {
		return rootDirectory;
	}

	public void setRootDirectory(Path rootDirectory) {
		this.rootDirectory = rootDirectory;
	}
}
