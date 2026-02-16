package com.mapleraid.manner.adapter.in.web.dto;

import com.mapleraid.manner.domain.EvaluationContext;
import com.mapleraid.manner.domain.MannerTag;

import java.util.List;

public record MannerEvaluationRequestDto(
        String targetUserId,
        EvaluationContext context,
        List<MannerTag> tags
) {
}
