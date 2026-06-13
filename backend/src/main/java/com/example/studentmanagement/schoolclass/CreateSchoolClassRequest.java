package com.example.studentmanagement.schoolclass;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateSchoolClassRequest(
		@NotBlank @Size(max = 100) String name,
		@Min(1900) @Max(2200) int entryYear,
		@NotNull Long homeroomTeacherId) {
}
