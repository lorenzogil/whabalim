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

    private var backupFiles = ArrayList<File>()

    init {
        if (hasWhatsApp()) {
            backupFiles = loadBackupFiles()
        }
    }

    fun backups () : ArrayList<File> = backupFiles

    private fun getDatabasesDir (): File {
        val root = Environment.getExternalStorageDirectory().absolutePath
        return File(root + DATABASES_DIR)
    }

    private fun hasWhatsApp(): Boolean {
        val dir = getDatabasesDir()
        return (dir.exists() && dir.isDirectory)
    }

    private fun loadBackupFiles(): ArrayList<File> {
        val files = ArrayList<File>()
        val dir = getDatabasesDir()
        dir.walk().forEach {
            if (it.isFile) {
                files.add(it)
            }
        }
        return files
    }

    fun deleteBackups(days: Int) {
        val now = Date().time
        val threshold = days * 24 * 60 * 60 * 1000
        val logger = Logger.getLogger(WhatsAppBackups::class.java.name)
        backupFiles.forEach {
            if (it.exists()) {
                if ((now - it.lastModified()) > threshold) {
                    if (it.absoluteFile.delete()) {
                        logger.info("Successfully removing file " + it.name)
                    } else {
                        if (it.exists()) {
                            logger.warning("Error while removing the file " + it.name)
                        } else {
                            logger.severe("The file does not exists " + it.name)
                        }
                    }
                }
            } else {
                logger.warning("The file does not exist anymore " + it.name)
            }
        }
    }
}