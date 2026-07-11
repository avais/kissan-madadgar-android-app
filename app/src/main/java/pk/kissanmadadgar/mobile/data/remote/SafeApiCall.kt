package pk.kissanmadadgar.mobile.data.remote

import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

data class ApiErrorResponse(
    val message: String?
)

/**
 * Maps a network-layer exception to a clean, generic Urdu message — never the exception's own
 * text, which for things like ConnectException is a raw "Failed to connect to /<ip>:<port>"
 * string that means nothing to a farmer and shouldn't be shown to one. The real exception is
 * still attached as the cause (see safeApiCall below) and logged, so nothing is lost for
 * debugging — only what reaches the UI is sanitized.
 */
private fun friendlyNetworkErrorMessage(e: Exception): String = when (e) {
    is UnknownHostException,
    is ConnectException -> "انٹرنیٹ کنکشن دستیاب نہیں ہے۔ براہ کرم اپنا انٹرنیٹ کنکشن چیک کر کے دوبارہ کوشش کریں۔"
    is SocketTimeoutException -> "سرور سے رابطہ کرنے میں زیادہ وقت لگ گیا۔ براہ کرم دوبارہ کوشش کریں۔"
    is SSLException -> "محفوظ کنکشن قائم کرنے میں خرابی پیش آئی۔ براہ کرم دوبارہ کوشش کریں۔"
    is IOException -> "نیٹ ورک میں خرابی پیش آئی۔ براہ کرم اپنا انٹرنیٹ کنکشن چیک کریں۔"
    else -> "غیر متوقع خرابی پیش آئی۔ براہ کرم دوبارہ کوشش کریں۔"
}

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
        android.util.Log.e("safeApiCall", "Network call failed", e)
        Result.failure(Exception(friendlyNetworkErrorMessage(e), e))
    }
}
