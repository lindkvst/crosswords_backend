package org.example.crosswords_backend.dto;

import java.util.List;

// The answer to a new submission: the saved submission, plus which cells were right.
// cellMarks uses the grid's row format: '+' correct, '-' wrong, '.' empty, '#' black.
// It reveals which letters are correct, so it is only sent back to the one who submitted.
public record SubmissionResultDto(SubmissionDto submission, List<String> cellMarks) {
}
