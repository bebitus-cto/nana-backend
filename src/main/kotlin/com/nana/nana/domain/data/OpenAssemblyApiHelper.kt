package com.nana.nana.domain.data

import com.nana.nana.config.OpenAssemblyResultCode.Ok.SUCCESS
import com.nana.nana.domain.data.OpenAssemblyAPICommonKey.CODE
import com.nana.nana.domain.data.OpenAssemblyAPICommonKey.HEAD
import com.nana.nana.domain.data.OpenAssemblyAPICommonKey.ITEMS
import com.nana.nana.domain.data.OpenAssemblyAPICommonKey.ITEM_COUNT
import com.nana.nana.domain.data.OpenAssemblyAPICommonKey.MESSAGE
import com.nana.nana.domain.data.OpenAssemblyAPICommonKey.RESULT
import com.nana.nana.util.secondOrNull
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun <R, T> OpenAssemblyBaseApiResponse<R>.handleOpenAssemblyApiResponse(
    onSuccess: (itemTotalCount: Int, pagedItems: List<R>?) -> T,
    onError: (errorResult: OpenAssemblyResult) -> T
): T {
    val logger: Logger = LoggerFactory.getLogger(OpenAssemblyBaseApiResponse::class.java)
    val successItems = getSuccess()
    val errorResult = getError()

    return when {
        // 성공 조건: successItems가 있고 크기가 2이며 결과 코드가 SUCCESS인 경우
        successItems != null && successItems.count() == 2 -> {
            val head = successItems.firstOrNull()?.head
            val listTotalCount = head?.firstOrNull()?.listTotalCount
            val successResult = head?.secondOrNull()?.successResult
            val items = successItems.secondOrNull()?.items

            when {
                listTotalCount != null && successResult != null && !items.isNullOrEmpty() && successResult.code == SUCCESS -> {
                    onSuccess(listTotalCount, items) // 성공 처리
                }

                successResult != null -> {
                    onError(successResult) // 실패 처리 (head에 에러 정보 포함)
                }

                errorResult != null -> {
                    onError(errorResult) // 실패 처리 (errorResult 포함)
                }

                else -> {
                    logger.warn("알 수 없는 데이터 형태: successItems: $successItems, errorResult: $errorResult ")
                    throw IllegalStateException("열린 국회정보 API 호출 예상치 못한 에러 발생")
                }
            }
        }

        errorResult != null -> {
            onError(errorResult)
        }

        else -> {
            throw IllegalStateException("열린 국회정보 API 호출 예상치 못한 에러 발생")
        }
    }
}

@Serializable
abstract class OpenAssemblyBaseApiResponse<R> {
    protected abstract val successResult: List<OpenAssemblySuccessResponse<R>>?
    protected abstract val errorResult: OpenAssemblyResult?

    fun getSuccess(): List<OpenAssemblySuccessResponse<R>>? = successResult
    fun getError(): OpenAssemblyResult? = errorResult
}

/**
 * 열린국회정보 API는 모두 같은 형태
 * Json 표준 구조를 따르지 않는 레거시 시스템으로 성공할 때 오는 데이터 구조
 * @property head 첫번째 아이템은 head만 있고 items는 null
 * @property items 두번째 아이템은 items만 있고 head는 null
 */
@Serializable
data class OpenAssemblySuccessResponse<R>(
    @SerialName(HEAD)
    val head: List<OpenAssemblyHead>? = null,
    @SerialName(ITEMS)
    val items: List<R>? = null
)

@Serializable
data class OpenAssemblyHead(
    @SerialName(ITEM_COUNT)
    val listTotalCount: Int? = null,
    @SerialName(RESULT)
    val successResult: OpenAssemblyResult? = null
)

@Serializable
data class OpenAssemblyResult(
    @SerialName(CODE)
    val code: String,
    @SerialName(MESSAGE)
    val message: String? = null
)

object OpenAssemblyAPICommonKey {
    const val ITEM_COUNT = "list_total_count"
    const val ITEMS = "row"

    const val HEAD = "head"
    const val RESULT = "RESULT"

    const val CODE = "CODE"
    const val MESSAGE = "MESSAGE"
}