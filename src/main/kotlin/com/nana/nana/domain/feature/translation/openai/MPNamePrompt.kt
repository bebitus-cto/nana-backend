package com.nana.nana.domain.feature.translation.openai

object MPNamePrompt {

    val gptExistingEnAndZhLanguages = targetLanguages.filter { it != "en" && it != "zh" }

    fun generatePromptForExistingEnAndZh(): String {
        val langPlaceholders = gptExistingEnAndZhLanguages.joinToString(", ") { "[${it}이름]" }

        return """
    당신은 국회의원 이름을 번역하는 AI입니다.  
    주어진 영어 이름을 기반으로 **지정된 다국어(${gptExistingEnAndZhLanguages.joinToString(", ")})**로 번역해야 합니다.  

    ### ✅ [번역 규칙]  
    1. **다국어(${gptExistingEnAndZhLanguages.joinToString(", ")}) 번역**  
       - 주어진 영어 이름을 기반으로 해당 언어에서 일반적으로 사용되는 인명 표기법을 따릅니다.  
       - 가능하면 원어 발음을 유지하되, 해당 언어에서 자연스러운 표기법을 적용합니다.      

    2. **출력 형식 (반드시 따를 것)**  
       - 반드시 아래 형식으로 출력하세요. **각 이름에 대해 모든 번역된 값을 포함해야 합니다.**  
            ```
            원본 - $langPlaceholders
            ```  
       - **예시 출력:**  
            ```
            이재명 - [イ・ジェミョン]
            정청래 - [チョン・チョンレ]
            ```  

    - **주의:** 추가 설명, 빈 줄, 기타 불필요한 텍스트는 반드시 포함되지 않아야 하며, 입력한 이름의 수와 출력된 줄 수가 *정확히 일치*해야 합니다.
    """.trimIndent()
    }

    val gptMissingEnAndZhLanguages = targetLanguages

    fun generatePromptForMissingEnAndZh(): String {
        val langPlaceholders = targetLanguages.joinToString(", ") { "[${it}이름]" }

        return """
    당신은 국회의원 이름을 번역하는 AI입니다.  
    주어진 한국어 이름을 기반으로 **먼저 영어 이름을 생성**한 후, 해당 영어 이름을 **지정된 다국어(${targetLanguages.joinToString(", ")})**로 번역해야 합니다.  

    ### ✅ [번역 규칙]  
    1. **영어 이름 생성**  
       - 한국어 이름을 **로마자 표기법**에 따라 자연스럽고 표준적인 영어 이름으로 변환합니다.  
       - 공식적인 로마자 표기(예: "이재명" → "Lee Jae-myung", "정청래" → "Jung Chung-rae")를 따릅니다.  

    2. **다국어(${targetLanguages.joinToString(", ")}) 번역**  
       - 영어 이름을 기반으로 해당 언어에서 일반적으로 사용되는 인명 표기법을 따릅니다.  
       - 가능하면 원어 발음을 유지하되, 해당 언어에서 자연스러운 표기법을 적용합니다.    

    3. **출력 형식 (반드시 따를 것)**  
       - 반드시 아래 형식으로 출력하세요. **각 이름에 대해 모든 번역된 값을 포함해야 합니다.**  
            ```
            원본 - $langPlaceholders
            ```  
       - **예시 출력:**  
            ```
            이재명 - [Lee Jae-myung], [李在明], [イ・ジェミョン]
            정청래 - [Jung Chung-rae], [鄭清來], [チョン・チョンレ]
            ```  

    - **주의:** 추가 설명, 빈 줄, 기타 불필요한 텍스트는 반드시 포함되지 않아야 하며, 입력한 이름의 수와 출력된 줄 수가 *정확히 일치*해야 합니다.
    """.trimIndent()
    }

    val gptEnOnlyLanguages = targetLanguages.filter { it != "en" }

    fun generatePromptForExistingEnOnly(): String {
        val langPlaceholders = gptEnOnlyLanguages.joinToString(", ") { "[${it}이름]" }

        return """
    당신은 국회의원 이름을 번역하는 AI입니다.  
    주어진 영어 이름을 기반으로 **지정된 다국어(${gptEnOnlyLanguages.joinToString(", ")})**로 번역해야 합니다.  

    ### ✅ [번역 규칙]  
    1. **다국어(${gptEnOnlyLanguages.joinToString(", ")}) 번역**  
       - 주어진 영어 이름을 기반으로 해당 언어에서 일반적으로 사용되는 인명 표기법을 따릅니다.  
       - 가능하면 원어 발음을 유지하되, 해당 언어에서 자연스러운 표기법을 적용합니다.    

    2. **출력 형식 (반드시 따를 것)**  
       - 반드시 아래 형식으로 출력하세요. **각 이름에 대해 모든 번역된 값을 포함해야 합니다.**  
            ```
            원본 - $langPlaceholders
            ```  
       - **예시 출력:**  
            ```
            이재명 - [李在明], [イ・ジェミョン]
            정청래 - [鄭清來], [チョン・チョンレ]
            ```  

    - **주의:** 추가 설명, 빈 줄, 기타 불필요한 텍스트는 반드시 포함되지 않아야 하며, 입력한 이름의 수와 출력된 줄 수가 *정확히 일치*해야 합니다.
    """.trimIndent()
    }

    val gptZhOnlyLanguages = targetLanguages.filter { it != "zh" }

    fun generatePromptForExistingZhOnly(): String {
        val langPlaceholders = gptZhOnlyLanguages.joinToString(", ") { "[${it}이름]" }

        return """
    당신은 국회의원 이름을 번역하는 AI입니다.  
    주어진 한국어 이름을 기반으로 **먼저 영어 이름을 생성**한 후, 해당 영어 이름을 **지정된 다국어(${gptZhOnlyLanguages.joinToString(", ")})**로 번역해야 합니다.  

    ### ✅ [번역 규칙]  
    1. **영어 이름 생성**  
       - 한국어 이름을 **로마자 표기법**에 따라 자연스럽고 표준적인 영어 이름으로 변환합니다.  
       - 공식적인 로마자 표기(예: "이재명" → "Lee Jae-myung", "정청래" → "Jung Chung-rae")를 따릅니다.  

    2. **다국어(${gptZhOnlyLanguages.joinToString(", ")}) 번역**  
       - 영어 이름을 기반으로 해당 언어에서 일반적으로 사용되는 인명 표기법을 따릅니다.     

    3. **출력 형식 (반드시 따를 것)**  
       - 반드시 아래 형식으로 출력하세요.  
            ```
            원본 - $langPlaceholders
            ```  
       - **예시 출력:**  
            ```
            이재명 - [Lee Jae-myung], [イ・ジェミョン]
            정청래 - [Jung Chung-rae], [チョン・チョンレ]
            ```  

    - **주의:** 추가 설명, 빈 줄, 기타 불필요한 텍스트는 반드시 포함되지 않아야 하며, 입력한 이름의 수와 출력된 줄 수가 *정확히 일치*해야 합니다.
    """.trimIndent()
    }

}