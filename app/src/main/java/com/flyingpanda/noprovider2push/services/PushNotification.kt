package com.flyingpanda.noprovider2push.services

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import androidx.core.os.bundleOf

/**
 * THIS FUNC IS USED TO PUSH NOTIFICATIONS TO OTHER APPS
 * It is called from the thread in WebSocketService
 */

/**
 * Function to notify client
 */
fun notifyClient(context: Context, clientPackage: String, message: String){
    val db = MessagingDatabase(context)
    val service = db.getServiceName(clientPackage)
    if (service.isBlank()) {
        Log.w("notifyClient","No service found for $clientPackage")
        return
    }
    val gHandlerThread = HandlerThread(clientPackage)
    gHandlerThread.start()

    val gConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName,
                                        service: IBinder) {
            val gService = Messenger(service)
            Log.i("notifyClient","Remote service connected")
            try {
                val msg = Message.obtain(
                        null,
                        TYPE_MESSAGE, 0, 0
                )
                msg.data = bundleOf("json" to message)
                gService.send(msg)
                Log.i("notifyClient","Notification sent")
            } catch(e: RemoteException) {
                Log.e("notifyClient","Something went wrong", e)
            } finally {
                context.unbindService(this)
                gHandlerThread.quit()
            }
        }

        override fun onServiceDisconnected(className: ComponentName) {
            gHandlerThread.quit()
        }
    }

    val intent = Intent()
    intent.component = ComponentName(clientPackage, service)
    context.bindService(intent, gConnection, Context.BIND_AUTO_CREATE)
}