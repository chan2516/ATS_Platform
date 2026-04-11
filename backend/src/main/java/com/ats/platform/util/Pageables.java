package com.ats.platform.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class Pageables {

	public static final int MAX_PAGE_SIZE = 100;

	private Pageables() {
	}

	/** Ensures sane bounds and default sort by createdAt descending when unsorted. */
	public static PageRequest normalize(Pageable pageable) {
		int size = Math.min(Math.max(pageable.getPageSize(), 1), MAX_PAGE_SIZE);
		Sort sort = pageable.getSort().isSorted()
				? pageable.getSort()
				: Sort.by(Sort.Direction.DESC, "createdAt");
		return PageRequest.of(pageable.getPageNumber(), size, sort);
	}
}
