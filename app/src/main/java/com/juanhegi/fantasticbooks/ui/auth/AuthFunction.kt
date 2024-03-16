package com.juanhegi.fantasticbooks.ui.auth

class AuthFunction {
    fun isValidEmail(email: String): String {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return if (!(email.matches(emailPattern.toRegex()) && email.isNotBlank())) {
            "Error en el email"
        } else {
            ""
        }

    }

    fun isValidPassword(password: String): String {
        val minPasswordLength = 6
        return if (password.length >= minPasswordLength && password.isNotBlank()) {
            ""
        } else {
            "Error en la contraseña, mínimo 6 carácteres"
        }
    }

    fun isValidRepeatPassword(password: String, password2: String): String {
        return if (password == password2) {
            ""
        } else {
            "Las contraseñas no son iguales"
        }
    }


}