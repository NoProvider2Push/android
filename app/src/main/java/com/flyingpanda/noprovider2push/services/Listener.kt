package com.flyingpanda.noprovider2push.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.google.gson.Gson
import io.ktor.util.*
import java.net.BindException

val listeningPort = 51515

class Listener: Service(){
    override fun onCreate(){
        try {
            val context = this
            Log.i("Listener", "Started")
            embeddedServer(Netty, 51515) {
                install(ContentNegotiation) {
                    gson {}
                }
                routing {
                    get("/") {
                        call.respond("ok")
                    }
                    post("/{clientPackage}/{...}") {
                        call.respond("ok")
                        val clientPackage = call.parameters["clientPackage"]
                        Log.i("Listener", "Received request to $clientPackage")
                        val parameters = call.receiveParameters().toMap()
                        clientPackage?.let { notifyClient(context, clientPackage, Gson().toJson(parameters)) }
                    }
                }
            }.start(wait = false)
        }catch (e: BindException){
            Log.i("Listener","Trying to bind again")
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}

