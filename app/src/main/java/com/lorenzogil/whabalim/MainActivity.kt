package com.lorenzogil.whabalim

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import java.util.logging.Logger

class MainActivity : AppCompatActivity() {

    private companion object {
        private const val DATABASES_DIR = "/WhatsApp/Databases/"
    }

    private lateinit var wab: WhatsAppBackups

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        ActivityCompat.requestPermissions(this, permissions, 0)

        wab = ViewModelProviders.of(this)[WhatsAppBackups::class.java]
        wab.hasWhatsApp.observe(this, Observer { hasWhatsApp ->
            val msg: String
            if (hasWhatsApp) {
                msg = getString(R.string.whatsapp_detected)
            } else {
                msg = getString(R.string.whatsapp_undetected)
            }
            findViewById<TextView>(R.id.tvDetection).setText(msg)
        })
        wab.backups.observe(this, Observer { backups ->
            val logger = Logger.getLogger(MainActivity::class.java.name)
            val dbs = backups.map{it.name}.joinToString("\n")
            val size = wab.size()
            logger.info("The backups have changed. Updating the UI: " + size)
            findViewById<TextView>(R.id.tvDatabases).setText(dbs)
            findViewById<TextView>(R.id.tvSize).setText(sizeString(size))
        })

        // https://developer.android.com/topic/libraries/architecture/workmanager
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
        wab.deleteBackups(findViewById<SeekBar>(R.id.sbDays).progress)
    }
}
