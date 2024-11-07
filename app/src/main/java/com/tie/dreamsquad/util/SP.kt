package com.tie.dreamsquad.utils

import android.content.Context
import android.preference.PreferenceManager

object SP {

    // Constants for SharedPreferences keys
    const val USER_ID = "USER_ID"
    const val USER_MOBILE = "USER_MOBILE"
    const val USER_NAME = "USER_NAME"
    const val USER_EMAIL = "USER_EMAIL"
    const val USER_PROFILE_PIC = "USER_PROFILE_PIC"
    const val USER_IS_VERIFIED = "USER_IS_VERIFIED"
    const val USER_STATUS = "USER_STATUS"
    const val USER_CREATED_AT = "USER_CREATED_AT"
    const val LOGIN_STATUS = "LOGIN_STATUS"
    const val USER_OTP = "USER_OTP"

    // Save String data in SharedPreferences
    fun savePreferences(mContext: Context, key: String, value: String?) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext)
        val editor = sharedPreferences.edit()
        editor.putString(key, value).apply()
    }

    // Retrieve String data from SharedPreferences
    fun getPreferences(context: Context, key: String): String? {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPreferences.getString(key, "")
    }



    // Remove all data from SharedPreferences (for logout, clear user data)
    fun removeAllSharedPreferences(mContext: Context) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext)
        val editor = sharedPreferences.edit()
        editor.clear().apply()
    }


}
