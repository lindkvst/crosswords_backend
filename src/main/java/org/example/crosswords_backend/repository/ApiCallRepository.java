package org.example.crosswords_backend.repository;

import org.example.crosswords_backend.dto.ApiUsageSummaryDto;
import org.example.crosswords_backend.model.ApiCall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ApiCallRepository extends JpaRepository<ApiCall, Integer> {

    List<ApiCall> findByCrosswordIdOrderByCreatedOnAsc(int crosswordId);

    // JPQL works on entities and fields, not tables. "select new" builds one DTO per group directly in the query,
    // so the database does the counting instead of loading every row into Java.
    @Query("""
            select new org.example.crosswords_backend.dto.ApiUsageSummaryDto(
                c.apiName, count(c), sum(c.durationMs), sum(c.inputTokens), sum(c.outputTokens), sum(c.estimatedWh))
            from ApiCall c
            group by c.apiName
            order by c.apiName
            """)
    List<ApiUsageSummaryDto> summarizeByApi();
}
