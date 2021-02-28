package com.example

import com.example.data.checkIfPasswordIsCorrect
import com.example.routes.authRoute
import com.example.routes.commentRoute
import com.example.routes.postRoute
import com.example.routes.userRoute
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*

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

