package org.example.crosswords_backend.dto;

import java.util.List;

// The answer to a new submission: the saved submission, the grid that was submitted, and which cells were right.
// submittedGrid lets the frontend show Claude's letters. For a player it is just their own grid back.
// cellMarks uses the grid's row format: '+' correct, '-' wrong, '.' empty, '#' black.
// It reveals which letters are correct, so it is only sent back to the one who submitted.
public record SubmissionResultDto(SubmissionDto submission, List<String> submittedGrid, List<String> cellMarks) {
}
