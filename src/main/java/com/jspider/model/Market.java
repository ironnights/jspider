package com.jspider.model;

import java.util.List;

public record Market(
        String name,
        List<Runner> runners
) {}