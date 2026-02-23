package com.workintech.s17d2.rest;

import com.workintech.s17d2.model.Experience;

public record DeveloperRequest(
        Integer id,
        String name,
        Double salary,
        Experience experience
) {}