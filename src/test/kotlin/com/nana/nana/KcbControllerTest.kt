package com.nana.nana

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

@SpringBootTest
@AutoConfigureMockMvc
class KcbControllerTest(@Autowired val mockMvc: MockMvc) {

    @Test
    fun testPhonePopup2() {
        val requestJson = """
            {
              "SITE_NAME": "테스트사이트",
              "RETURN_URL": "http://localhost:8080/kcb/callback",
              "RQST_CAUS_CD": "01",
              "CHNL_CD": "01",
              "MSG_TP_CD": "10",
              "SMS_TMPL_TP": "2",
              "RTN_MSG": "key1=12345",
              "CP_CD": "V68410000000",
              "SERVER_IP": "192.168.0.6",
              "TEL_COM_CD": "",
              "TEL_NO": "",
              "SVC_NAME": "IDS_HS_POPUP_START",
              "BUILD_NO": "2.3.2"
            }
        """.trimIndent()

        mockMvc.perform(post("/kcb/phone_popup2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk())
            .andDo { print("성공") }
    }
}