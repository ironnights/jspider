package com.jspider.model;

import java.util.List;

public record Sport(
        String name,
        String family,
        List<Region> regions
) {}