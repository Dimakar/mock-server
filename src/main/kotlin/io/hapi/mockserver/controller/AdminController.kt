package io.hapi.mockserver.controller

import io.hapi.mockserver.model.RequestRule
import io.hapi.mockserver.service.RequestRecordingService
import io.hapi.mockserver.service.RuleService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin")
class AdminController(
    private val ruleService: RuleService,
    private val requestRecordingService: RequestRecordingService
) {

    // Rule management endpoints
    @GetMapping("/rules")
    fun getAllRules(): ResponseEntity<List<RequestRule>> {
        return ResponseEntity.ok(ruleService.getAllRules())
    }

    @PostMapping("/rules")
    fun addRule(@RequestBody rule: RequestRule): ResponseEntity<RequestRule> {
        val addedRule = ruleService.addRule(rule)
        return ResponseEntity.ok(addedRule)
    }

    @DeleteMapping("/rules/{ruleId}")
    fun removeRule(@PathVariable ruleId: String): ResponseEntity<Boolean> {
        val removed = ruleService.removeRule(ruleId)
        return ResponseEntity.ok(removed)
    }

    @DeleteMapping("/rules")
    fun clearAllRules(): ResponseEntity<Unit> {
        ruleService.clearAllRules()
        return ResponseEntity.ok().build()
    }

    // Request recording endpoints
    @GetMapping("/requests")
    fun getAllRecordedRequests(): ResponseEntity<List<io.hapi.mockserver.model.RecordedRequest>> {
        return ResponseEntity.ok(requestRecordingService.getAllRecordedRequests())
    }

    @GetMapping("/requests/{requestId}")
    fun getRecordedRequest(@PathVariable requestId: String): ResponseEntity<io.hapi.mockserver.model.RecordedRequest?> {
        val request = requestRecordingService.getRecordedRequest(requestId)
        return if (request != null) {
            ResponseEntity.ok(request)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/requests")
    fun clearRecordedRequests(): ResponseEntity<Unit> {
        requestRecordingService.clearRecordedRequests()
        return ResponseEntity.ok().build()
    }

    // Health check endpoint
    @GetMapping("/health")
    fun health(): ResponseEntity<Map<String, Any>> {
        val stats = mapOf(
            "status" to "UP",
            "rules" to ruleService.getAllRules().size,
            "recordedRequests" to requestRecordingService.getAllRecordedRequests().size
        )
        return ResponseEntity.ok(stats)
    }
} 