package io.hapi.mockserver.controller

import io.hapi.mockserver.service.RequestRecordingService
import io.hapi.mockserver.service.RuleService
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.concurrent.TimeUnit

@RestController
@RequestMapping("/mock")
class MockController(
    private val requestRecordingService: RequestRecordingService,
    private val ruleService: RuleService
) {
    private val logger = LoggerFactory.getLogger(MockController::class.java)

    @RequestMapping(
        method = [RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, 
                 RequestMethod.PATCH, RequestMethod.HEAD, RequestMethod.OPTIONS, RequestMethod.TRACE],
        path = ["/**"]
    )
    fun handleRequest(
        request: HttpServletRequest,
        @RequestBody(required = false) body: String?
    ): ResponseEntity<String> {
        
        logger.info("Received request: ${request.method} ${request.requestURI}")
        
        // Record the request
        val recordedRequest = requestRecordingService.recordRequest(
            method = request.method,
            path = request.requestURI,
            queryParams = request.parameterMap.mapValues { it.value.toList() },
            headers = request.headerNames.toList().associateWith { request.getHeaders(it).toList() },
            body = body,
            contentType = request.contentType,
            remoteAddress = request.remoteAddr,
            userAgent = request.getHeader("User-Agent")
        )
        
        logger.info("Recorded request with ID: ${recordedRequest.id}")

        // Find matching rule
        val matchingRule = ruleService.findMatchingRule(request.method, request.requestURI)
        
        return if (matchingRule != null) {
            logger.info("Found matching rule: ${matchingRule.name}")
            
            // Apply delay if specified
            if (matchingRule.delay > 0) {
                try {
                    TimeUnit.MILLISECONDS.sleep(matchingRule.delay)
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                }
            }
            
            // Build response with custom headers
            val responseHeaders = HttpHeaders()
            matchingRule.responseHeaders.forEach { (key, value) ->
                responseHeaders.add(key, value)
            }
            
            ResponseEntity.status(matchingRule.statusCode)
                .headers(responseHeaders)
                .body(matchingRule.responseBody ?: "Mock response for ${request.method} ${request.requestURI}")
        } else {
            logger.info("No matching rule found, returning default response")
            // Default response when no rule matches
            ResponseEntity.status(HttpStatus.OK)
                .header("X-Mock-Server", "No matching rule found")
                .body("Default mock response for ${request.method} ${request.requestURI}")
        }
    }
} 