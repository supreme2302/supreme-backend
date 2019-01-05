package com.supreme.spa.backend.vue.models

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class Auth {
    var id: Int? = 0
    var username: String? = null
    var email: String? = null
    var password: String? = null
    var confirmPassword: String? = null
    private val passwordEncoder = BCryptPasswordEncoder()

    fun checkConfirm(password: String, confirm: String): Boolean {
        return password == confirm
    }

    fun checkPassword(pass: String): Boolean {
        return passwordEncoder.matches(pass, this.password)
    }

    fun saltHash() {
        this.password = passwordEncoder.encode(password)
    }
}
