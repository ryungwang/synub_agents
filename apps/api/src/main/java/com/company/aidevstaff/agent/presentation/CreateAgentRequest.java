package com.company.aidevstaff.agent.presentation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateAgentRequest(
        @NotBlank
        @Pattern(regexp = "^[a-zA-Z0-9-]{2,64}$", message = "ID는 영문, 숫자, 하이픈 2~64자만 사용할 수 있습니다")
        String id,
        @NotBlank
        String team,
        @NotBlank
        String name,
        @NotBlank
        String role,
        @Min(0)
        @Max(100)
        Integer qualityScore
) {
    public int effectiveQualityScore() {
        return qualityScore == null ? 90 : qualityScore;
    }
}
