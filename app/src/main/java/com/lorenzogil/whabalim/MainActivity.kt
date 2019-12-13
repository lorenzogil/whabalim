package com.lorenzogil.whabalim

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProviders

class MainActivity : AppCompatActivity() {

    private companion object {
        private const val DATABASES_DIR = "/WhatsApp/Databases/"
    }

    private lateinit var wab: WhatsAppBackups

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        wab = ViewModelProviders.of(this)[WhatsAppBackups::class.java]

        val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        ActivityCompat.requestPermissions(this, permissions, 0).apply {
            populateUI()
        }

        // https://developer.android.com/topic/libraries/architecture/workmanager
    }

    private fun populateUI() {
        val backups = wab.backups()
        findViewById<TextView>(R.id.tvDetection).apply {
            text = if (backups.size > 0) {
                getString(R.string.whatsapp_detected)
            } else {
                getString(R.string.whatsapp_undetected)
            }
        }

        val dbs = backups.map{it.name}.joinToString("\n")
        val size = backups.map{it.length()}.sum()
        findViewById<TextView>(R.id.tvDatabases).setText(dbs + " " + sizeString(size))
    }

    private fun sizeString (size: Long) : String {
        val space : Double
        val unit : String
        val kilobyte = 1024
        val megabyte = 1024L * kilobyte
        val gigabyte = 1024 * megabyte
        if (size > gigabyte) {
            space = size.toDouble() / gigabyte
            unit = "GB"
        } else if (size > megabyte) {
            space = size.toDouble() / megabyte
            unit = "MB"
        } else if (size > kilobyte) {
            space = size.toDouble() / kilobyte
            unit = "KB"
        } else {
            space = size.toDouble()
            unit = "Bytes"
        }
        return "%.2f %s".format(space, unit)
    }

    fun onDeleteClicked(view: View) {
        wab.deleteBackups(2)
    }
}
