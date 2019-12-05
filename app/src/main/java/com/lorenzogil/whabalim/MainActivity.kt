package com.lorenzogil.whabalim

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.TextView
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        populateUI()
    }

    fun populateUI() {
        findViewById<TextView>(R.id.tvDetection).apply {
            if (hasWhatsappDatabases()) {
                text = getString(R.string.whatsapp_detected)
            } else {
                text = getString(R.string.whatsapp_undetected)
            }
        }
    }

    fun hasWhatsappDatabases(): Boolean {
        val root = Environment.getExternalStorageDirectory().absolutePath
        val dir = File(root + "/WhatsApp/Databases")
        if (dir.exists() && dir.isDirectory()) {
            return true
        } else {
            return false
        }
    }
}
