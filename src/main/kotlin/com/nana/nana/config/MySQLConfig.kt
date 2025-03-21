package com.nana.nana.config

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.Slf4jSqlDebugLogger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

/**
 * Exposed와 MySQL 데이터베이스를 연결하는 설정 클래스
 */
object MySQLConfig {

    private val logger = LoggerFactory.getLogger(MySQLConfig::class.java)

    fun connect() {
        Database.connect(
            url = "jdbc:mysql://localhost:3306/nana_database?serverTimezone=Asia/Seoul&useLegacyDatetimeCode=false",
            driver = "com.mysql.cj.jdbc.Driver",
            user = "root",
            password = "2025wkdbdlsdlehlwK!"
        )
        DatabaseConfig {
            sqlLogger = Slf4jSqlDebugLogger
        }

        logger.info("===  데이터베이스 연결 성공  ===")
    }
}

/**
 * DatabaseInitializer
 * - 애플리케이션 시작 시 MySQL 데이터베이스 연결 초기화
 */
@Component
class DatabaseInitializer : ApplicationListener<ApplicationReadyEvent> {

    private val logger = LoggerFactory.getLogger(DatabaseInitializer::class.java)

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        MySQLConfig.connect()
        logger.info("===  데이터베이스 초기화 성공  ===")
    }
}