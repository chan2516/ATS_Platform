package com.ats.platform.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public final class MatchReasonsJson {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private MatchReasonsJson() {
	}

	public static List<String> readList(String json) {
		if (json == null || json.isBlank()) {
			return List.of();
		}
		try {
			return MAPPER.readValue(json, new TypeReference<>() {
			});
		}
		catch (Exception e) {
			return List.of();
		}
	}

	public static String write(List<String> reasons) {
		try {
			return MAPPER.writeValueAsString(reasons);
		}
		catch (Exception e) {
			return "[]";
		}
	}
}
