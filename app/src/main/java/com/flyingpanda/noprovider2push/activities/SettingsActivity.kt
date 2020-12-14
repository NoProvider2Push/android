package com.flyingpanda.noprovider2push.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import com.flyingpanda.noprovider2push.R

class SettingsActivity : AppCompatActivity() {
    private var prefs: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("Config", Context.MODE_PRIVATE)
        setContentView(R.layout.activity_settings)
        val address = prefs?.getString("address", "")
        val gateway = prefs?.getString("gateway", "")
        findViewById<EditText>(R.id.settings_address_value).setText(address)
        findViewById<EditText>(R.id.settings_gateway_value).setText(gateway)
    }

    fun save(view: View){
        val address = findViewById<EditText>(R.id.settings_address_value).text.toString()
        val gateway = findViewById<EditText>(R.id.settings_gateway_value).text.toString()
        Log.i("save",address)
        val editor = prefs!!.edit()
        editor.putString("address", address)
        editor.putString("gateway", gateway)
        editor.commit()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}