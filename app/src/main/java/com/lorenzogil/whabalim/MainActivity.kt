package com.lorenzogil.whabalim

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.work.*
import java.util.concurrent.TimeUnit
import java.util.logging.Logger


class MainActivity : AppCompatActivity() {

    private companion object {
        private const val DATABASES_DIR = "/WhatsApp/Databases/"
        private const val REQUEST_READ_EXTERNAL_STORAGE = 1
    }

    private lateinit var wab: WhatsAppBackups
    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferences = getSharedPreferences(getString(R.string.preferences_file), Context.MODE_PRIVATE)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(this, permissions, REQUEST_READ_EXTERNAL_STORAGE)
        } else {
            populateUI()
        }

    }

    private fun populateUI () {
        val defaultDays = resources.getInteger(R.integer.default_days)
        val days = preferences.getInt(getString(R.string.preference_days), defaultDays)

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

        val isScheduled = preferences.getBoolean(getString(R.string.preference_is_scheduled), false)
        val swScheduler = findViewById<Switch>(R.id.swScheduler)
        swScheduler.isChecked = isScheduled
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_READ_EXTERNAL_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    populateUI()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    // TODO: tell the user this permission is required otherwise this app is useless
                }
                return
            }
            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }

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
        val result = wab.deleteBackups(findViewById<SeekBar>(R.id.sbDays).progress)
        val msg: String
        if (result == 0) {
            msg = "No backups were removed"
        } else if (result == 1) {
            msg = "1 backup was removed"
        } else {
            msg = result.toString() + " backups were removed"
        }
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    fun onSwitchClicked(view: View) {
        val logger = Logger.getLogger(MainActivity::class.java.name)
        val switch = view as Switch
        val isScheduled: Boolean
        if (switch.isChecked) {
            logger.info("Enabling the daily removal of backups")
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "cleaner",
                ExistingPeriodicWorkPolicy.REPLACE,
                PeriodicWorkRequestBuilder<BackupsCleanerWorker>(
                    1,
                    TimeUnit.DAYS
                ).build()
            )
            isScheduled = true
        } else {
            logger.info("Disabling the daily removal of backups")
            WorkManager.getInstance(this).cancelUniqueWork("cleaner")
            isScheduled = false
        }
        with (preferences.edit()) {
            putBoolean(getString(R.string.preference_is_scheduled), isScheduled)
            commit()
        }
    }
}
