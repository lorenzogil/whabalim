package com.lorenzogil.whabalim

import android.os.Environment
import androidx.lifecycle.ViewModel
import java.io.File
import java.util.*
import java.util.logging.Logger
import kotlin.collections.ArrayList

class WhatsAppBackups : ViewModel() {

    private companion object {
        private const val DATABASES_DIR = "/WhatsApp/Databases/"
    }

    private var backupFiles = ArrayList<BackupFile>()

    init {
        if (hasWhatsApp()) {
            backupFiles = loadBackupFiles()
        }
    }

    fun backups () : ArrayList<BackupFile> = backupFiles

    private fun getDatabasesDir (): File {
        val root = Environment.getExternalStorageDirectory().absolutePath
        return File(root + DATABASES_DIR)
    }

    private fun hasWhatsApp(): Boolean {
        val dir = getDatabasesDir()
        return (dir.exists() && dir.isDirectory)
    }

    private fun loadBackupFiles(): ArrayList<BackupFile> {
        val files = ArrayList<BackupFile>()
        val dir = getDatabasesDir()
        dir.walk().forEach {
            if (it.isFile) {
                files.add(BackupFile(it.name, it.length()))
            }
        }
        return files
    }

    /*
    public fun getDatabases(days: Int): ArrayList<String> {
        val databases = ArrayList<String>()
        val now = Date().time
        val threshold = days * 24 * 60 * 60 * 1000
        val dir = getDatabasesDir()
        dir.walk().forEach {
            if (it.isFile) {
                if ((now - it.lastModified()) > threshold) {
                    Logger.getLogger(MainActivity::class.java.name)
                        .warning("Seeing file " + it.name)
                    databases.add(it.name)
                }
            }
        }
        return databases
    }
    */
}