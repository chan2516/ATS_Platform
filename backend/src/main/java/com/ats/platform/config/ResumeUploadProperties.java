package com.ats.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.resume")
public class ResumeUploadProperties {

	private long maxSizeBytes = 5L * 1024 * 1024;

	private List<String> allowedContentTypes = List.of(
			"application/pdf",
			"application/vnd.openxmlformats-officedocument.wordprocessingml.document");

	public long getMaxSizeBytes() {
		return maxSizeBytes;
	}

	public void setMaxSizeBytes(long maxSizeBytes) {
		this.maxSizeBytes = maxSizeBytes;
	}

	public List<String> getAllowedContentTypes() {
		return allowedContentTypes;
	}

	public void setAllowedContentTypes(List<String> allowedContentTypes) {
		this.allowedContentTypes = allowedContentTypes;
	}
}
