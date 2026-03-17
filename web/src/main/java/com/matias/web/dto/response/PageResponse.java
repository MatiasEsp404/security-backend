package com.matias.web.dto.response;

import lombok.Getter;

import java.util.List;

@Getter
public class PageResponse<T> {
    private final List<T> content;
    private final int pageNumber;
    private final int pageSize;
    private final long totalElements;
    private final int totalPages;
    private final boolean first;
    private final boolean last;

    public PageResponse(List<T> content, int pageNumber, int pageSize, 
                       long totalElements, int totalPages, boolean first, boolean last) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.first = first;
        this.last = last;
    }

    public boolean isEmpty() {
        return content.isEmpty();
    }

    public boolean hasContent() {
        return !isEmpty();
    }

    public boolean hasNext() {
        return !last;
    }

    public boolean hasPrevious() {
        return !first;
    }
}
