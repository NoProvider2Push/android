package com.flyingpanda.noprovider2push.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.flyingpanda.noprovider2push.R
import com.flyingpanda.noprovider2push.services.Listener
import com.flyingpanda.noprovider2push.services.MessagingDatabase


class MainActivity : AppCompatActivity() {

    private lateinit var listView : ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        startListener(this)

        listView = findViewById<ListView>(R.id.applications_list)
        val db = MessagingDatabase(this)
        var appList = db.listApps()
        db.close()
        listView.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                appList
        )
        listView.setOnItemLongClickListener(
                fun(parent: AdapterView<*>, v: View, position: Int, id: Long): Boolean {
                    val alert: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(
                            this@MainActivity)
                    alert.setTitle("Unregistering")
                    alert.setMessage("Are you sure to unregister ${appList[position]} ?")
                    alert.setPositiveButton("YES") { dialog, which ->
                        val db = MessagingDatabase(this)
                        db.forceUnregisterApp(appList[position])
                        appList = db.listApps()
                        db.close()
                        listView.adapter = ArrayAdapter(
                                this,
                                android.R.layout.simple_list_item_1,
                                appList
                        )
                        dialog.dismiss()
                    }
                    alert.setNegativeButton("NO") { dialog, which -> dialog.dismiss() }
                    alert.show()
                    return true
                }
        )
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if(hasFocus) {
            val db = MessagingDatabase(this)
            val appList = db.listApps()
            db.close()
            listView.adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_list_item_1,
                    appList
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun startListener(context: Context){
        val serviceIntent = Intent(context, Listener::class.java)
        context.startService(serviceIntent)
    }
}
