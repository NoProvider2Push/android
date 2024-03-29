package org.unifiedpush.distributor.noprovider2push.activities

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import org.unifiedpush.distributor.noprovider2push.R
import org.unifiedpush.distributor.noprovider2push.services.Listener
import org.unifiedpush.distributor.noprovider2push.services.MessagingDatabase
import org.unifiedpush.distributor.noprovider2push.services.sendUnregistered


class MainActivity : AppCompatActivity() {

    private lateinit var listView : ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        startListener(this)
        setListView()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if(hasFocus) {
            setListView()
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        }else{
            context.startService(serviceIntent)
        }
    }

    private fun setListView(){
        listView = findViewById<ListView>(R.id.applications_list)
        val db = MessagingDatabase(this)
        val tokenList = db.listTokens().toMutableList()
        val appList = emptyArray<String>().toMutableList()
        tokenList.forEach {
            appList.add(db.getPackageName(it))
        }
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
                    alert.setPositiveButton("YES") { dialog, _ ->
                        sendUnregistered(this, tokenList[position])
                        val db = MessagingDatabase(this)
                        db.unregisterApp(tokenList[position])
                        db.close()
                        tokenList.removeAt(position)
                        appList.removeAt(position)
                        dialog.dismiss()
                    }
                    alert.setNegativeButton("NO") { dialog, _ -> dialog.dismiss() }
                    alert.show()
                    return true
                }
        )
    }
}
