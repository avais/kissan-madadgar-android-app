package pk.kissanmadadgar.mobile.data.remote

import retrofit2.Response

data class ApiErrorResponse(
    val message: String?
)

suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Result<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception("سرور سے موصول ہونے والا جواب خالی تھا (Response body was null)."))
            }
        } else {
            val errorBody = response.errorBody()?.string()
            val errorMessage = if (!errorBody.isNullOrEmpty()) {
                try {
                    com.google.gson.Gson().fromJson(errorBody, ApiErrorResponse::class.java).message
                        ?: "سرور کی خرابی: ${response.code()}"
                } catch (e: Exception) {
                    errorBody
                }
            } else {
                "سرور کی خرابی: ${response.code()}"
            }
            Result.failure(Exception(errorMessage))
        }
    } catch (e: Exception) {
        Result.failure(Exception("نیٹ ورک کنکشن میں خرابی: ${e.localizedMessage ?: "نامعلوم وجہ"}", e))
    }
}
