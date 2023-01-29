package main

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class BCryptPassword {

    static void main(String[] args) {
        def encoder = new BCryptPasswordEncoder()
        def encoded = encoder.encode(args[0])
        println "${args[0]} => ${encoded}"
    }

}
