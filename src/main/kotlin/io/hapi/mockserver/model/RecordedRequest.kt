package io.hapi.mockserver.model

import java.time.LocalDateTime

data class RecordedRequest(
    val id: String,
    val timestamp: LocalDateTime,
    val method: String,
    val path: String,
    val queryParams: Map<String, List<String>>,
    val headers: Map<String, List<String>>,
    val body: String?,
    val contentType: String?,
    val remoteAddress: String?,
    val userAgent: String?
) 