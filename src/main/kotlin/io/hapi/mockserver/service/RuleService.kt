package io.hapi.mockserver.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.hapi.mockserver.model.RequestRule
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Service
class RuleService(
    @Value("\${mock-server.rules.directory:rules}")
    private val rulesDirectory: String
) {
    private val logger = LoggerFactory.getLogger(RuleService::class.java)
    private val rules = ConcurrentHashMap<String, RequestRule>()
    private val idCounter = AtomicInteger(1)
    private val objectMapper = ObjectMapper().registerModule(JavaTimeModule())

    init {
        createRulesDirectory()
        loadRulesFromFile()
    }

    private fun createRulesDirectory() {
        val directory = File(rulesDirectory)
        if (!directory.exists()) {
            directory.mkdirs()
            logger.info("Created rules directory: ${directory.absolutePath}")
        } else {
            logger.info("Using existing rules directory: ${directory.absolutePath}")
        }
    }

    private fun loadRulesFromFile() {
        val rulesFile = File(rulesDirectory, "rules.json")
        if (rulesFile.exists()) {
            try {
                val rulesList = objectMapper.readValue(rulesFile, Array<RequestRule>::class.java).toList()
                rulesList.forEach { rule ->
                    rules[rule.id] = rule
                    // Update counter to avoid ID conflicts
                    val ruleIdNumber = rule.id.replace("rule_", "").toIntOrNull()
                    if (ruleIdNumber != null && ruleIdNumber >= idCounter.get()) {
                        idCounter.set(ruleIdNumber + 1)
                    }
                }
                logger.info("Loaded ${rulesList.size} rules from file")
            } catch (e: Exception) {
                logger.error("Failed to load rules from file: ${e.message}", e)
            }
        } else {
            logger.info("No rules file found, starting with empty rules")
        }
    }

    private fun saveRulesToFile() {
        val rulesFile = File(rulesDirectory, "rules.json")
        try {
            val rulesList = rules.values.toList()
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(rulesFile, rulesList)
            logger.info("Saved ${rulesList.size} rules to file")
        } catch (e: Exception) {
            logger.error("Failed to save rules to file: ${e.message}", e)
        }
    }

    fun addRule(rule: RequestRule): RequestRule {
        val ruleWithId = if (rule.id.isBlank()) {
            rule.copy(id = "rule_${idCounter.getAndIncrement()}")
        } else {
            rule
        }
        rules[ruleWithId.id] = ruleWithId
        saveRulesToFile()
        logger.info("Added rule: ${ruleWithId.name} (${ruleWithId.method} ${ruleWithId.path})")
        return ruleWithId
    }

    fun removeRule(ruleId: String): Boolean {
        val removed = rules.remove(ruleId) != null
        if (removed) {
            saveRulesToFile()
        }
        logger.info("Removed rule: $ruleId -> $removed")
        return removed
    }

    fun getAllRules(): List<RequestRule> {
        val allRules = rules.values.sortedBy { it.priority }
        logger.info("Retrieved ${allRules.size} rules")
        return allRules
    }

    fun findMatchingRule(method: String, path: String): RequestRule? {
        // Strip /mock prefix from path for rule matching
        val pathForMatching = if (path.startsWith("/mock")) {
            path.substring(5) // Remove "/mock" prefix
        } else {
            path
        }
        
        val matchingRule = rules.values
            .filter { it.enabled }
            .filter { rule ->
                (rule.method == "*" || rule.method.equals(method, ignoreCase = true)) &&
                (rule.path == "*" || pathForMatching.matches(rule.path.toRegex()) || pathForMatching == rule.path)
            }
            .maxByOrNull { it.priority }
        
        logger.info("Finding matching rule for $method $path (matching against: $pathForMatching) -> ${matchingRule?.name ?: "none"}")
        return matchingRule
    }

    fun clearAllRules() {
        logger.info("Clearing all rules")
        rules.clear()
        saveRulesToFile()
    }
} 