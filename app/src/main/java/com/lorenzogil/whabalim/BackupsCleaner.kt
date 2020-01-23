package com.lorenzogil.whabalim

import android.os.Environment
import java.io.File
import java.util.logging.Logger
import kotlin.collections.ArrayList

class BackupsCleaner {

    fun hasWhatsApp(): Boolean {
        val dir = getDatabasesDir()
        return (dir.exists() && dir.isDirectory)
    }

    private companion object {
        private const val DATABASES_DIR = "/WhatsApp/Databases/"
    }

    private fun getDatabasesDir (): File {
        val root = Environment.getExternalStorageDirectory().absolutePath
        return File(root + DATABASES_DIR)
    }

    fun getBackupFiles(): ArrayList<File> {
        val result = ArrayList<File> ()
        val dir = getDatabasesDir()
        dir.walk().forEach {
            if (it.isFile) {
                result.add(it)
            }
        }
        return result
    }

    fun getThreshold(days: Int) : Int {
        return days * 24 * 60 * 60 * 1000
    }

    fun deleteBackups(amount: Int, backups: ArrayList<File>): Int {
        val logger = Logger.getLogger(BackupsCleaner::class.java.name)
        var result = 0
        logger.info("Keeping " + amount + " backups from " + backups.size)
        val toDelete = backups.sortedBy { it.lastModified() }.dropLast(amount)
        toDelete.forEach( {
            if (it.exists()) {
                if (it.absoluteFile.delete()) {
                    logger.info("Successfully deleting file " + it.name)
                    result += 1
                } else {
                    if (it.exists()) {
                        logger.warning("Error while deleting the file " + it.name)
                    } else {
                        logger.severe("File does not exists " + it.name)
                    }
                }
            } else {
                logger.warning("File does not exist " + it.name)
            }
        })
        return result
    }

}