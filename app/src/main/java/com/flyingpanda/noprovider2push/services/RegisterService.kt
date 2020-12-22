package com.flyingpanda.noprovider2push.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.*
import android.util.Log
import androidx.core.os.bundleOf
import kotlin.concurrent.thread

/**
 * THIS SERVICE IS USED BY OTHER APPS TO REGISTER
 */

class RegisterService : Service() {
    /** Keeps track of all current registered clients.  */
    private val db = MessagingDatabase(this)
    private lateinit var settings : SharedPreferences

    /**
     * Handler of incoming messages from clients.
     */
    private class IncomingHandler(var service: RegisterService) : Handler(Looper.getMainLooper()) {

        override fun handleMessage(msg: Message) {
            Log.i("test","${msg.what}")
            when (msg.what) {
                TYPE_CONNECTOR_REGISTER ->{
                    val uid = msg.sendingUid
                    val msgData = msg.data
                    thread(start = true) {
                        registerApp(msgData, uid)
                        Log.i("RegisterService","Registration is finished")
                    }.join()
                    sendInfo(msg)
                }
                TYPE_CONNECTOR_UNREGISTER ->{
                    val uid = msg.sendingUid
                    val msgData = msg.data
                    thread(start = true) {
                        unregisterApp(msgData, uid)
                        Log.i("RegisterService","Unregistration is finished")
                    }
                    simpleAnswer(msg, TYPE_CONNECTOR_UNREGISTER_ACKNOWLEDGE)
                }
                else -> super.handleMessage(msg)
            }
        }

        private fun simpleAnswer(msg: Message, what: Int) {
            try {
                msg.replyTo?.send(Message.obtain(null, what, 0, 0))
            } catch (e: RemoteException) {
            }
        }

        private fun unregisterApp(msg: Bundle, clientUid: Int) {
            val clientPackageName = msg.getString("package").toString()
            // we only trust unregistered demands from the uid who registered the app
            if (service.db.strictIsRegistered(clientPackageName, clientUid)) {
                Log.i("RegisterService","Unregistering $clientPackageName uid: $clientUid")
                service.db.unregisterApp(clientPackageName, clientUid)
            }
        }

        private fun registerApp(msg: Bundle,clientUid: Int) {
            val clientPackageName = msg.getString("package").toString()
            if (clientPackageName.isBlank()) {
                Log.w("RegisterService","Trying to register an app without packageName")
                return
            }
            Log.i("RegisterService","registering $clientPackageName uid: $clientUid")
            // The app is registered with the same uid : we re-register it
            // the client may need to create a new app in the server
            if (service.db.strictIsRegistered(clientPackageName, clientUid)) {
                Log.i("RegisterService","$clientPackageName already registered : unregistering to register again")
                unregisterApp(msg,clientUid)
            }
            // The app is registered with a new uid.
            // User should unregister this app manually
            // to avoid an app to impersonate another one
            if (service.db.isRegistered(clientPackageName)) {
                Log.w("RegisterService","$clientPackageName already registered with a different uid")
                return
            }

            val clientService = msg.getString("service").toString()
            if (clientService.isBlank()) {
                Log.w("RegisterService","Cannot find the service for $clientPackageName")
                return
            }
            Log.i("RegisterService","registering : $clientPackageName $clientUid $clientService")
            service.db.registerApp(clientPackageName, clientUid, clientService)
        }

        private fun sendInfo(msg: Message){
            val clientPackageName = msg.data?.getString("package").toString()
            Log.i("RegisterService","$clientPackageName is asking for its token and url")
            // we only trust unregistered demands from the uid who registered the app
            if (service.db.strictIsRegistered(clientPackageName, msg.sendingUid)) {
                // db.getToken also remove the token in the db
                try {
                    val address = service.settings?.getString("address","")
                    val endpoint = service.settings?.getString("proxy","") +
                            "/$address:$listeningPort/$clientPackageName/"
                    val answer = Message.obtain(null, TYPE_CONNECTOR_REGISTER_SUCCESS, 0, 0)
                    answer.data = bundleOf("endpoint" to endpoint)
                    msg.replyTo?.send(answer)
                } catch (e: RemoteException) {
                }
            }else{
                Log.w("RegisterService","Client isn't registered or has a different uid")
            }
        }

    }

    /**
     * Target we publish for clients to send messages to gHandler.
     */
    private val gMessenger = Messenger(IncomingHandler(this))

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    override fun onBind(intent: Intent): IBinder? {
        settings = getSharedPreferences("Config", Context.MODE_PRIVATE)
        return gMessenger.binder
    }

    override fun onDestroy() {
        db.close()
        super.onDestroy()
    }

}