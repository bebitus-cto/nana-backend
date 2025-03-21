package com.nana.nana.domain.feature.assemblysync

import com.nana.nana.domain.feature.assemblysync.OpenAssemblyConfig.ResultOk.LAW_PROPOSAL_FULL_DETAIL
import com.nana.nana.domain.feature.assemblysync.OpenAssemblyConfig.ResultOk.MEMBERS_DETAIL
import com.nana.nana.domain.feature.assemblysync.OpenAssemblyConfig.ResultOk.ENDED_LEGISLATION
import com.nana.nana.domain.feature.assemblysync.OpenAssemblyConfig.ResultOk.LAW_PROPOSAL_OVERVIEW
import com.nana.nana.domain.feature.assemblysync.OpenAssemblyConfig.ResultOk.PENDING_LAW_PROPOSAL
import com.nana.nana.domain.feature.assemblysync.OpenAssemblyConfig.ResultOk.PROGRESS_LEGISLATION
import com.nana.nana.domain.feature.assemblysync.OpenAssemblyConfig.ResultOk.PROPOSER_DETAIL
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.util.UriComponentsBuilder

/**
 * API 설정 정보
 */
object OpenAssemblyConfig {

    private val logger: Logger = LoggerFactory.getLogger(OpenAssemblyConfig::class.java)

    private const val API_KEY = "fe07cf484bdc495d9aa19e8355935bd4"

    object ResultOk {
        const val MEMBERS_DETAIL = "ALLNAMEMBER"
        const val LAW_PROPOSAL_FULL_DETAIL = "ALLBILL"
        const val LAW_PROPOSAL_OVERVIEW = "TVBPMBILL11"
        const val PROGRESS_LEGISLATION = "nknalejkafmvgzmpt"
        const val ENDED_LEGISLATION = "nohgwtzsamojdozky"
        const val PENDING_LAW_PROPOSAL = "nwbqublzajtcqpdae"
        const val PROPOSER_DETAIL = "nzmimeepazxkubdpn"
    }

    object PAGE {
        const val COACTOR = "https://likms.assembly.go.kr/bill/coactorListPopup.do?billId="
    }

    object Url {
        const val MEMBERS_DETAIL_URL = MEMBERS_DETAIL

        const val PROGRESS_LEGISLATION_URL = PROGRESS_LEGISLATION
        const val ENDED_LEGISLATION_URL = ENDED_LEGISLATION
        const val LAW_PROPOSAL_FULL_DETAIL_URL = LAW_PROPOSAL_FULL_DETAIL
        const val LAW_PROPOSAL_OVERVIEW_URL = LAW_PROPOSAL_OVERVIEW
        const val PENDING_LAW_PROPOSAL_URL = PENDING_LAW_PROPOSAL
        const val PROPOSER_DETAIL_URL = PROPOSER_DETAIL
    }

    fun getOpenAssemblyBaseUriBuilder(url: String): UriComponentsBuilder {
        return UriComponentsBuilder.fromUriString(url)
            .queryParam("KEY", API_KEY)
            .queryParam("Type", "json")
    }

    fun UriComponentsBuilder.toDecodedUriString(): String =
        build(false).toUriString()
}