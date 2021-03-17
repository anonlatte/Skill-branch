package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*

class User(
    private val firstName: String,
    private val lastName: String?,
    email: String? = null,
    rawPhone: String? = null,
    meta: Map<String, Any>? = null,
) {

    constructor(
        firstName: String,
        lastName: String?,
        email: String,
        password: String
    ) : this(firstName, lastName, email, meta = mapOf("auth" to "password")) {
        passwordHash = encrypt(password)
    }

    constructor(
        firstName: String,
        lastName: String?,
        rawPhone: String
    ) : this(firstName, lastName, rawPhone = rawPhone, meta = mapOf("auth" to "sms")) {
        val code = generateAccessCode()
        accessCode = code
        sendAccessCodeToUser(rawPhone, code)
    }

    private val initials: String
        get() = listOfNotNull(firstName, lastName).map {
            it.first().toUpperCase()
        }.joinToString(" ")

    private val fullName: String
        get() = listOfNotNull(firstName, lastName).joinToString(" ").capitalize(Locale.getDefault())

    private var phone: String? = null
        set(value) {
            field = value?.replace(Regex("[^+\\d]"), "")
        }

    private var _login: String? = null
    var login: String
        set(value) {
            _login = value.toLowerCase(Locale.getDefault())
        }
        get() = _login!!

    private var salt: String? = null
    private lateinit var passwordHash: String

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    var accessCode: String? = null
        set(value) {
            field = value
            if (value != null) {
                passwordHash = encrypt(value)
            }
        }
    val userInfo: String

    init {
        check(firstName.isNotBlank()) { "FirstName must not be blank" }
        check(!email.isNullOrBlank() || !rawPhone.isNullOrBlank()) {
            "Email or phone must not be null or blank"
        }

        phone = rawPhone
        login = email ?: phone!!

        userInfo = """
            firstName: $firstName
            lastName: $lastName
            login: $login
            fullName: $fullName
            initials: $initials
            email: $email
            phone: $phone
            meta: $meta
        """.trimIndent()
    }

    private fun sendAccessCodeToUser(rawPhone: String, code: String) {
        println("$code ---> $rawPhone")
    }

    fun generateAccessCode(): String = (0..5).map {
        POSSIBLE_CHARS.random()
    }.joinToString("")

    private fun encrypt(password: String): String {
        if (salt.isNullOrEmpty()) {
            salt = ByteArray(16).also { SecureRandom().nextBytes(it) }.toString()
        }
        return salt.plus(password).md5()
    }

    fun checkPassword(pass: String): Boolean {
        println("Checking passwordHash is $passwordHash")
        return encrypt(pass) == passwordHash
    }

    fun changePassword(oldPass: String, newPass: String) {
        if (checkPassword(oldPass)) {
            passwordHash = encrypt(newPass)
            if (!accessCode.isNullOrEmpty()) {
                accessCode = newPass
            }
            println("Password $oldPass has been change to $newPass")
        } else {
            throw IllegalArgumentException(
                "The entered password doesn't match the current password"
            )
        }
    }

    companion object Factory {
        private const val POSSIBLE_CHARS =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

        fun makeUser(
            fullName: String,
            email: String? = null,
            password: String? = null,
            phone: String? = null
        ): User {
            val (firstName, lastName) = fullName.fullNameToPair()

            return if (!phone.isNullOrBlank()) {
                User(firstName, lastName, phone)
            } else if (!email.isNullOrBlank() && !password.isNullOrBlank()) {
                User(firstName, lastName, email, password)
            } else {
                throw IllegalArgumentException("")
            }
        }

        private fun String.fullNameToPair(): Pair<String, String?> = split(" ").filter {
            it.isNotBlank()
        }.run {
            when (size) {
                1 -> first() to null
                2 -> first() to last()
                else -> throw IllegalArgumentException("")
            }
        }
    }

    private fun String.md5(): String {
        val messageDigest = MessageDigest.getInstance("MD5")
        val digest = messageDigest.digest(toByteArray())
        val hexString = BigInteger(1, digest).toString(16)
        return hexString.padStart(32, '0')
    }
}
