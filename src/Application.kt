package com.example

import com.example.data.checkIfPasswordIsCorrect
import com.example.routes.*
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.basic
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.gson.gson
import io.ktor.routing.Routing

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {


    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }
    install(Authentication){
        authenticate()
    }
    install(Routing) {
        authRoute()
        postRoute()
        userRoute()
        commentRoute()
        notificationRoute()
    }

}

private fun Authentication.Configuration.authenticate() {
    basic {
        realm = "MyServer"
        validate { credentials ->
            if(checkIfPasswordIsCorrect(credentials.name, credentials.password)) {
                UserIdPrincipal(credentials.name)
            }
            else {
                null
            }
        }
    }
}

