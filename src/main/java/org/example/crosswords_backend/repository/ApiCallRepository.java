package org.example.crosswords_backend.repository;

import org.example.crosswords_backend.model.ApiCall;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApiCallRepository extends JpaRepository<ApiCall, Integer> {

    List<ApiCall> findByCrosswordIdOrderByCreatedOnAsc(int crosswordId);
}
