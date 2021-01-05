package com.flyingpanda.noprovider2push.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import com.flyingpanda.noprovider2push.R
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.net.BindException

const val listeningPort = 51515

class Listener: Service(){

    private var isServiceStarted = false
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate(){
        super.onCreate()
        Log.i("Listener","Starting")
        val notification = createNotification()
        startForeground(51515, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startService()
        // by returning this we make sure the service is restarted if the system kills the service
        return START_STICKY
    }

    private fun httpServer(port: Int){
        try {
            val context = this
            Log.i("Listener", "Started")
            embeddedServer(Netty, port) {
                install(ContentNegotiation) {
                    gson {}
                }
                routing {
                    get("/") {
                        call.respond("ok")
                    }
                    post("/{application}/{token}/{...}") {
                        call.respond("ok")
                        val application = call.parameters["application"]
                        val token = call.parameters["token"]!!
                        Log.i("Listener", "Received request to $application")
                        val parameters = call.receiveText()
                        application?.let {
                            sendMessage(context, application, token, parameters)
                        }
                    }
                }
            }.start(wait = false)
        }catch (e: BindException){
            Log.i("Listener","Trying to bind again")
        }
    }

    private fun createNotification(): Notification {
        val notificationChannelId = "NoProvider2Push.Listener"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;
            val channel = NotificationChannel(
                    notificationChannelId,
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_LOW
            ).let {
                it.description = "NoProvider2Push Listener"
                it
            }
            notificationManager.createNotificationChannel(channel)
        }

        val builder: Notification.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(
                this,
                notificationChannelId
        ) else Notification.Builder(this)

        return builder
                .setContentTitle("NoProvider2Push")
                .setContentText("Listening")
                .setSmallIcon(R.drawable.ic_launcher_notification)
                .setTicker("Listening")
                .setPriority(Notification.PRIORITY_LOW) // for under android 26 compatibility
                .build()
    }

    private fun startService() {
        if (isServiceStarted) return
        isServiceStarted = true

        // we need this lock so our service gets not affected by Doze Mode
        wakeLock =
                (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                    newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EndlessService::lock").apply {
                        acquire()
                    }
                }
        httpServer(listeningPort)
    }
}

