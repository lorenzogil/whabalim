package com.lorenzogil.whabalim

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.work.*
import java.util.concurrent.TimeUnit
import java.util.logging.Logger


class MainActivity : AppCompatActivity() {

    private companion object {
        private const val DATABASES_DIR = "/WhatsApp/Databases/"
    }

    private lateinit var wab: WhatsAppBackups
    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferences = getSharedPreferences(getString(R.string.preferences_file), Context.MODE_PRIVATE)
        val defaultDays = resources.getInteger(R.integer.default_days)
        val days = preferences.getInt(getString(R.string.preference_days), defaultDays)

        val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        ActivityCompat.requestPermissions(this, permissions, 0)

        val tvSize = findViewById<TextView>(R.id.tvSize)
        val skDays = findViewById<SeekBar>(R.id.sbDays)
        skDays.progress = days
        val tvDays = findViewById<TextView>(R.id.tvDays)

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
            tvSize.setText(sizeString(size))
        })

        tvDays.setText(getDaysMsg(skDays.progress))

        skDays.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvDays.setText(getDaysMsg(progress))
                tvSize.setText(sizeString(wab.size(progress)))
                with (preferences.edit()) {
                    putInt(getString(R.string.preference_days), progress)
                    commit()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "cleaner",
            ExistingPeriodicWorkPolicy.REPLACE,
            PeriodicWorkRequestBuilder<BackupsCleanerWorker>(
                1,
                TimeUnit.DAYS
            ).build()
        )
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

    private fun getDaysMsg (days: Int) : String {
        if (days == 1) {
            return "Keep %d day of backups".format(days)
        } else {
            return "Keep %d days of backups".format(days)
        }
    }

    fun onDeleteClicked(view: View) {
        wab.deleteBackups(findViewById<SeekBar>(R.id.sbDays).progress)
    }
}
