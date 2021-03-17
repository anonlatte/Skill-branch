package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting

object UserHolder {
    private val map = mutableMapOf<String, User>()

    fun registerUser(
        fullName: String,
        email: String,
        password: String
    ) = User.makeUser(fullName, email, password).also { map[it.login] = it }

    fun loginUser(
        login: String,
        password: String
    ): String? {
        return map[login.formatLogin()]?.let {
            if (it.checkPassword(password)) {
                it.userInfo
            } else {
                null
            }
        }
    }

    fun registerUserByPhone(
        fullName: String,
        phone: String
    ) = User.makeUser(fullName = fullName, phone = phone).also { map[it.login] = it }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun clearHolder() {
        map.clear()
    }

    fun requestAccessCode(login: String) {
        map[login.formatLogin()]?.updateAccessCode()
    }

    private fun String.formatLogin(): String = if (first() == '+') {
        replace(Regex("[^+\\d]"), "").trim()
    } else {
        this
    }

    private fun User.updateAccessCode() {
        accessCode = generateAccessCode()
    }
}