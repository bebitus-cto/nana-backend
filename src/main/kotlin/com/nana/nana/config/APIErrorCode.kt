package com.nana.nana.config

object OpenAssemblyResultCode {

    // 에러 코드
    object Error {
        const val MISSING_REQUIRED_VALUES = "ERROR-300"
        const val INVALID_API_KEY = "ERROR-290"
        const val DAILY_TRAFFIC_LIMIT_EXCEEDED = "ERROR-337"
        const val SERVICE_NOT_FOUND = "ERROR-310"
        const val INVALID_REQUEST_LOCATION_TYPE = "ERROR-333"
        const val EXCEEDS_MAX_REQUEST_LIMIT = "ERROR-336"
        const val SERVER_ERROR = "ERROR-500"
        const val DATABASE_CONNECTION_ERROR = "ERROR-600"
        const val SQL_QUERY_ERROR = "ERROR-601"
        const val CERTIFICATE_EXPIRED = "ERROR-990"
    }

    // 정보 코드
    object Ok {
        const val SUCCESS = "INFO-000"
        const val API_KEY_USAGE_RESTRICTED = "INFO-300"
        const val NO_DATA_FOUND = "INFO-200"
    }
}