package io.hapi.mockserver.model

import java.time.LocalDateTime
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class RecordedRequest @JsonCreator constructor(
    @JsonProperty("id") val id: String = "",
    @JsonProperty("timestamp") val timestamp: String = "",
    @JsonProperty("method") val method: String = "",
    @JsonProperty("path") val path: String = "",
    @JsonProperty("headers") val headers: Map<String, String> = emptyMap(),
    @JsonProperty("body") val body: String? = null
) 