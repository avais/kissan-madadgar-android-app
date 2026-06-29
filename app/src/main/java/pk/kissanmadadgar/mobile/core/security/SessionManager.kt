package pk.kissanmadadgar.mobile.core.security

import android.content.Context
import android.content.SharedPreferences
import pk.kissanmadadgar.mobile.domain.model.UserRole

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("kissan_secure_prefs", Context.MODE_PRIVATE)

    fun saveAuthToken(token: String) {
        prefs.edit().putString("auth_token", token).apply()
    }

    fun getAuthToken(): String? {
        return prefs.getString("auth_token", null)
    }

    fun saveUserPhone(phone: String) {
        prefs.edit().putString("user_phone", phone).apply()
    }

    fun getUserPhone(): String? {
        return prefs.getString("user_phone", null)
    }

    fun saveUserRole(role: UserRole) {
        prefs.edit().putString("user_role", role.name).apply()
    }

    fun getUserRole(): UserRole? {
        val roleStr = prefs.getString("user_role", null) ?: return null
        return try {
            UserRole.valueOf(roleStr)
        } catch (e: Exception) {
            null
        }
    }

    fun saveUserId(userId: String) {
        prefs.edit().putString("user_id", userId).apply()
    }

    fun getUserId(): String? {
        return prefs.getString("user_id", null)
    }

    fun saveUserName(name: String) {
        prefs.edit().putString("user_name", name).apply()
    }

    fun getUserName(): String? {
        return prefs.getString("user_name", null)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun setContactAuthorized(authorized: Boolean) {
        prefs.edit().putBoolean("is_contact_authorized", authorized).apply()
    }

    fun isContactAuthorized(): Boolean {
        return prefs.getBoolean("is_contact_authorized", false)
    }

    fun saveUserAddress(address: String) {
        prefs.edit().putString("user_address", address).apply()
    }

    fun getUserAddress(): String? {
        return prefs.getString("user_address", null)
    }

    fun saveUserCnic(cnic: String) {
        prefs.edit().putString("user_cnic", cnic).apply()
    }

    fun getUserCnic(): String? {
        return prefs.getString("user_cnic", null)
    }

    fun saveUserDistrict(district: String) {
        prefs.edit().putString("user_district", district).apply()
    }

    fun getUserDistrict(): String? {
        return prefs.getString("user_district", null)
    }

    fun saveGuestToken(token: String) {
        prefs.edit().putString("guest_token", token).apply()
    }

    fun getGuestToken(): String? {
        return prefs.getString("guest_token", null)
    }
}
