package com.nana.nana.domain.feature.auth.kcb

import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import java.io.File
import java.io.FileNotFoundException

@Component
class LicenseFileProvider {

    private val logger = LoggerFactory.getLogger(LicenseFileProvider::class.java)

    fun getLicenseFilePath(): String {
        val resourcePath = "kcb_license/V68410000000_IDS_01_PROD_AES_license.dat"
        val resource = ClassPathResource(resourcePath)
        if (!resource.exists()) {
            throw FileNotFoundException("License file not found in classpath: $resourcePath")
        }

        // 임시 디렉토리에 복사 (OkCert가 OS 레벨 파일로 접근 가능하도록)
        val tempFile = File(System.getProperty("java.io.tmpdir"), "kcb_license.dat")
        logger.debug("Copying license file to temp: ${tempFile.absolutePath}")

        resource.inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        logger.debug("License file copied to: ${tempFile.absolutePath} (Size: ${tempFile.length()} bytes)")
        return tempFile.absolutePath
    }
}