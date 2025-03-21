package com.nana.nana.domain.feature.auth.kcb

import kcb.module.v3.OkCert
import kcb.module.v3.exception.OkCertException
import kcb.org.json.JSONObject
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.io.File

@Controller
@RequestMapping("/auth")
class KcbController(
    licenseFileProvider: LicenseFileProvider,
    private val kcbService: KcbService,
    @Value("\${kcb.cpCd}") private val cdCd: String,
    @Value("\${kcb.returnUrl}") private val returnUrl: String,
    @Value("\${kcb.target}") private val target: String,
    @Value("\${kcb.svcName}") private val svcName: String
) {
    private val logger = LoggerFactory.getLogger(KcbController::class.java)
    private val licensePath = licenseFileProvider.getLicenseFilePath()

    @PostMapping("/kcb/verification")
    fun kcbVerification(@RequestBody request: Map<String, String>): String {
        // 1) 라이선스 파일 검사
        val file = File(licensePath)
        if (!file.exists()) {
            logger.error("KCB 라이선스 파일이 존재하지 않음: $licensePath")
            throw Exception("라이선스 파일이 존재하지 않습니다.")
        }

        val email = request["email"] ?: throw Exception("email 파라미터가 존재하지 않습니다.")

        // 2) OkCert 호출 로직
        val paramJson = """
        {
          "SITE_NAME": "NaNa",
          "RETURN_URL": "$returnUrl",
          "RQST_CAUS_CD": "01",
          "CHNL_CD": "01",
          "MSG_TP_CD": "10",
          "SMS_TMPL_TP": "2",
          "CP_CD": "$cdCd",
          "SVC_NAME": "IDS_HS_POPUP_START",
          "BUILD_NO": "2.3.2",
          "RETURN_MSG": "$email"
        }
    """.trimIndent()

        return try {
            val resultJson = OkCert().callOkCert(target, cdCd, svcName, licensePath, paramJson)
            val jsonObject = JSONObject(resultJson)
            val resultCode = jsonObject.optString("RSLT_CD", "")
            val mdlTkn = jsonObject.optString("MDL_TKN", "")

            if (resultCode == "B000" && mdlTkn.isNotEmpty()) {
                val redirectUrl =
                    "/auth/phone_popup?CP_CD=$cdCd&MDL_TKN=$mdlTkn&POPUP_URL=https://safe.ok-name.co.kr/CommonSvl"
                logger.debug("리다이렉트 URL: $redirectUrl")

                // 스프링 부트에서 "redirect:" 접두어를 사용하면 302 응답이 자동 생성됩니다.
                return "redirect:$redirectUrl"
            } else {
                val rsltMsg = jsonObject.optString("RSLT_MSG", "")
                throw Exception("$resultCode : $rsltMsg")
            }
        } catch (e: OkCertException) {
            logger.error("OkCertException occurred. Code: ${e.code}, Message: ${e.message}", e)
            throw Exception("Error Code: ${e.code}, Message: ${e.message}")
        } catch (e: Exception) {
            logger.error("Unexpected error in OkCert call", e)
            throw Exception(e.message ?: "Unknown error")
        }
    }

    @GetMapping("/phone_popup")
    fun phonePopup(
        @RequestParam("CP_CD") cpCd: String,
        @RequestParam("MDL_TKN") mdlTkn: String,
        @RequestParam("POPUP_URL") popupUrl: String,
        model: Model
    ): String {
        model.addAttribute("CP_CD", cpCd)
        model.addAttribute("MDL_TKN", mdlTkn)
        model.addAttribute("POPUP_URL", popupUrl)
        return "auth/phone_popup"
    }

    @GetMapping("/kcb/result/callback")
    suspend fun kcbCallback(@RequestParam("mdl_tkn") mdlTkn: String) {
        val paramJson = JSONObject().apply {
            put("MDL_TKN", mdlTkn)
        }.toString()

        return try {
            val resultJson = OkCert().callOkCert(target, cdCd, "IDS_HS_POPUP_RESULT", licensePath, paramJson)
            val jsonObject = JSONObject(resultJson)

            newSuspendedTransaction {
                logger.debug("KCB 유저 인증 Raw 콜백: $jsonObject")

                val updatedUser = kcbService.updateKCBUserInfo(
                    email = jsonObject.optString("RETURN_MSG", ""),
                    kcbName = jsonObject.optString("RSLT_NAME", ""),
                    kcbBirthDate = jsonObject.optString("RSLT_BIRTHDAY", ""),
                    kcbSexCode = jsonObject.optString("RSLT_SEX_CD", ""),
                    kcbNationality = jsonObject.optString("RSLT_NTV_FRNR_CD", ""),
                    kcbPhoneNumber = jsonObject.optString("TEL_NO", "")
                )

                logger.debug("KCB 유저 인증 콜백: $updatedUser")
            }

        } catch (e: OkCertException) {
            logger.error("OkCert 처리 중 예외 발생: ${e.message}")
        } catch (e: Exception) {
            logger.error("KCB 콜백 처리 중 예외 발생: ${e.message}")
        }
    }
}