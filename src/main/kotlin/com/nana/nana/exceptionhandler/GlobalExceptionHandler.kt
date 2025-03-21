package com.nana.nana.exceptionhandler

import com.nana.nana.domain.feature.assemblysync.SyncStartupListener
import com.nana.nana.domain.feature.log.LogLPSyncRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler(
    private val logLPSyncRepository: LogLPSyncRepository,
    private val applicationScope: CoroutineScope
) {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(e: IllegalArgumentException): ResponseEntity<String> {
        return handleError(e, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(e: NoSuchElementException): ResponseEntity<String> {
        return handleError(e, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(SecurityException::class)
    fun handleUnauthorized(e: SecurityException): ResponseEntity<String> {
        return handleError(e, HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneralException(e: Exception): ResponseEntity<String> {
        return handleError(e, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    private fun handleError(e: Exception, status: HttpStatus): ResponseEntity<String> {
        val message = e.message ?: "알 수 없는 에러 발생"
        logger.error("에러 발생: $message")

        applicationScope.launch {
            try {
                newSuspendedTransaction {
                    logLPSyncRepository.logLPSyncError(message, "")
                }
            } catch (ex: Exception) {
                logger.error("로그 저장 중 에러 발생: ${ex.message}")
            }
        }

        return ResponseEntity(message, status)
    }
}