package com.workdna.civic.api.dto;

import jakarta.validation.constraints.NotNull;

public record LocationDto(
        @NotNull Double lat,
        @NotNull Double lng
) {}
