package io.hapi.mockserver.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.hapi.mockserver.model.RecordedRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Service
class RequestRecordingService(
    @Value("\${mock-server.recording.directory:requests}")
    private val recordingDirectory: String
) {
    private val logger = LoggerFactory.getLogger(RequestRecordingService::class.java)
    private val idCounter = AtomicInteger(1)
    private val recordedRequests = ConcurrentHashMap<String, RecordedRequest>()
    private val objectMapper = ObjectMapper().registerModule(JavaTimeModule())

    init {
        createRecordingDirectory()
        loadRequestsFromFile()
    }

    private fun createRecordingDirectory() {
        val directory = File(recordingDirectory)
        if (!directory.exists()) {
            directory.mkdirs()
            logger.info("Created recording directory: ${directory.absolutePath}")
        } else {
            logger.info("Using existing recording directory: ${directory.absolutePath}")
        }
    }

    private fun loadRequestsFromFile() {
        val requestsFile = File(recordingDirectory, "requests.json")
        if (requestsFile.exists()) {
            try {
                val requestsList = objectMapper.readValue(requestsFile, Array<RecordedRequest>::class.java).toList()
                requestsList.forEach { request ->
                    recordedRequests[request.id] = request
                    // Update counter to avoid ID conflicts
                    val requestIdNumber = request.id.replace("req_", "").split("_").firstOrNull()?.toIntOrNull()
                    if (requestIdNumber != null && requestIdNumber >= idCounter.get()) {
                        idCounter.set(requestIdNumber + 1)
                    }
                }
                logger.info("Loaded ${requestsList.size} recorded requests from file")
            } catch (e: Exception) {
                logger.error("Failed to load requests from file: ${e.message}", e)
            }
        } else {
            logger.info("No requests file found, starting with empty requests")
        }
    }

    private fun saveRequestsToFile() {
        val requestsFile = File(recordingDirectory, "requests.json")
        try {
            val requestsList = recordedRequests.values.toList()
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(requestsFile, requestsList)
            logger.info("Saved ${requestsList.size} requests to file")
        } catch (e: Exception) {
            logger.error("Failed to save requests to file: ${e.message}", e)
        }
    }

    fun recordRequest(
        method: String,
        path: String,
        queryParams: Map<String, List<String>>,
        headers: Map<String, List<String>>,
        body: String?,
        contentType: String?,
        remoteAddress: String?,
        userAgent: String?
    ): RecordedRequest {
        val timestamp = LocalDateTime.now()
        val id = "req_${idCounter.getAndIncrement()}_${timestamp.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"))}"
        
        logger.info("Recording request: $method $path")
        
        val recordedRequest = RecordedRequest(
            id = id,
            timestamp = timestamp,
            method = method,
            path = path,
            queryParams = queryParams,
            headers = headers,
            body = body,
            contentType = contentType,
            remoteAddress = remoteAddress,
            userAgent = userAgent
        )

        recordedRequests[id] = recordedRequest
        saveRequestToFile(recordedRequest)
        saveRequestsToFile() // Save the in-memory list too
        
        logger.info("Successfully recorded request with ID: $id")
        return recordedRequest
    }

    private fun saveRequestToFile(request: RecordedRequest) {
        val timestamp = request.timestamp.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"))
        val safePath = request.path.replace("/", "_").replace("?", "_").replace("&", "_")
        val fileName = "${timestamp}_${request.method}_${safePath}.json"
        
        val file = File(recordingDirectory, fileName)
        
        try {
            val jsonContent = buildString {
                appendLine("{")
                appendLine("  \"id\": \"${request.id}\",")
                appendLine("  \"timestamp\": \"${request.timestamp}\",")
                appendLine("  \"method\": \"${request.method}\",")
                appendLine("  \"path\": \"${request.path}\",")
                appendLine("  \"queryParams\": {")
                request.queryParams.forEach { (key, values) ->
                    appendLine("    \"$key\": [${values.joinToString(",") { "\"$it\"" }}],")
                }
                appendLine("  },")
                appendLine("  \"headers\": {")
                request.headers.forEach { (key, values) ->
                    appendLine("    \"$key\": [${values.joinToString(",") { "\"$it\"" }}],")
                }
                appendLine("  },")
                appendLine("  \"body\": ${if (request.body != null) "\"${request.body.replace("\"", "\\\"")}\"" else "null"},")
                appendLine("  \"contentType\": ${if (request.contentType != null) "\"${request.contentType}\"" else "null"},")
                appendLine("  \"remoteAddress\": ${if (request.remoteAddress != null) "\"${request.remoteAddress}\"" else "null"},")
                appendLine("  \"userAgent\": ${if (request.userAgent != null) "\"${request.userAgent}\"" else "null"}")
                appendLine("}")
            }
            
            file.writeText(jsonContent)
            logger.info("Saved request to file: ${file.absolutePath}")
        } catch (e: Exception) {
            logger.error("Failed to save request to file: ${e.message}", e)
        }
    }

    fun getAllRecordedRequests(): List<RecordedRequest> {
        val requests = recordedRequests.values.sortedByDescending { it.timestamp }
        logger.info("Retrieved ${requests.size} recorded requests")
        return requests
    }

    fun getRecordedRequest(id: String): RecordedRequest? {
        val request = recordedRequests[id]
        logger.info("Retrieved recorded request: $id -> ${request != null}")
        return request
    }

    fun clearRecordedRequests() {
        logger.info("Clearing all recorded requests")
        recordedRequests.clear()
        saveRequestsToFile()
        // Optionally clear files too
        val directory = File(recordingDirectory)
        if (directory.exists()) {
            directory.listFiles()?.forEach { 
                if (it.name != "requests.json") {
                    it.delete() 
                }
            }
            logger.info("Cleared all request files from directory")
        }
    }
} 