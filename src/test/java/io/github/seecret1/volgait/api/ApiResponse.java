package io.github.seecret1.volgait.api;

import com.fasterxml.jackson.databind.JsonNode;

public record ApiResponse(

        int statusCode,

        JsonNode body,

        String rawBody

) { }
