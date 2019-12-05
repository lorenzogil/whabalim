package com.lorenzogil.whabalim

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.TextView
import androidx.core.app.ActivityCompat
import java.io.File
import java.util.logging.Logger

class MainActivity : AppCompatActivity() {

    val DATABASES_DIR = "/WhatsApp/Databases/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        ActivityCompat.requestPermissions(this, permissions, 0).apply {
            populateUI()
        }
    }

    private fun populateUI() {
        val hasDBs = hasDatabases()
        findViewById<TextView>(R.id.tvDetection).apply {
            if (hasDatabases()) {
                text = getString(R.string.whatsapp_detected)
            } else {
                text = getString(R.string.whatsapp_undetected)
            }
        }
        if (!hasDBs) {
            return
        }

        val dbs = getDatabases().joinToString("\n")
        findViewById<TextView>(R.id.tvDatabases).apply {
            text = dbs
        }
    }

    private fun getDatabasesDir (): File {
        val root = Environment.getExternalStorageDirectory().absolutePath
        return File(root + DATABASES_DIR)
    }

    private fun hasDatabases(): Boolean {
        val dir = getDatabasesDir()
        return (dir.exists() && dir.isDirectory())
    }

    private fun getDatabases(): ArrayList<String> {
        val databases = ArrayList<String>()
        val dir = getDatabasesDir()
        dir.walk().forEach {
            if (it.isFile) {
                Logger.getLogger(MainActivity::class.java.name).warning("Seeing file " + it.name)
                databases.add(it.name)
            }
        }
        return databases
    }
}
