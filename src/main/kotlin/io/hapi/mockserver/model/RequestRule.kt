package io.hapi.mockserver.model

import com.fasterxml.jackson.annotation.JsonProperty

data class RequestRule(
    val id: String = "",
    val name: String = "",
    val method: String = "*",
    val path: String = "*",
    val statusCode: Int = 200,
    val responseBody: String? = null,
    val responseHeaders: Map<String, String> = emptyMap(),
    val delay: Long = 0,
    val enabled: Boolean = true,
    val priority: Int = 0
) 