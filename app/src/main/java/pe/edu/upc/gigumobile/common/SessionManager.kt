package pe.edu.upc.gigumobile.common

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("GigUSession", Context.MODE_PRIVATE)

    fun clearSession() {
        sharedPreferences.edit().clear().apply()
    }

    fun saveToken(token: String) {
        sharedPreferences.edit().putString("token", token).apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString("token", null)
    }

    fun setTermsAccepted(accepted: Boolean) {
        sharedPreferences.edit().putBoolean("terms_accepted", accepted).apply()
    }

    fun areTermsAccepted(): Boolean {
        return sharedPreferences.getBoolean("terms_accepted", false)
    }
}

