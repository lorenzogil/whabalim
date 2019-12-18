package com.lorenzogil.whabalim

import android.os.Environment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File
import java.util.*
import java.util.logging.Logger
import kotlin.collections.ArrayList

class WhatsAppBackups : ViewModel() {

    private companion object {
        private const val DATABASES_DIR = "/WhatsApp/Databases/"
    }

    val hasWhatsApp = MutableLiveData<Boolean> ()
    val backups = MutableLiveData<ArrayList<File>> ()

    init {
        val dir = getDatabasesDir()
        val hasWA = (dir.exists() && dir.isDirectory)
        hasWhatsApp.value = hasWA
        if (hasWA){
            loadBackupFiles()
        }
    }

    fun size(): Long {
        val result = backups.value?.map{it.length()}?.sum()
        if (result == null) {
            return 0L
        } else {
            return result
        }
    }

    fun size(days: Int): Long {
        val now = Date().time
        val threshold = getThreshold(days)
        val result = backups.value?.filter{
            it.exists() && ((now - it.lastModified()) < threshold)
        }?.map{it.length()}?.sum()
        if (result == null) {
            return 0L
        } else {
            return result
        }
    }

    private fun getDatabasesDir (): File {
        val root = Environment.getExternalStorageDirectory().absolutePath
        return File(root + DATABASES_DIR)
    }

    private fun loadBackupFiles() {
        val newList = ArrayList<File> ()
        val dir = getDatabasesDir()
        dir.walk().forEach {
            if (it.isFile) {
                newList.add(it)
            }
        }
        backups.value = newList
    }

    private fun getThreshold(days: Int) : Int {
        return days * 24 * 60 * 60 * 1000
    }

    fun deleteBackups(days: Int) {
        val now = Date().time
        val threshold = getThreshold(days)
        val logger = Logger.getLogger(WhatsAppBackups::class.java.name)
        logger.info("Deleting backups older than " + days + " days")
        backups.value?.forEach {
            if (it.exists()) {
                if ((now - it.lastModified()) > threshold) {
                    if (it.absoluteFile.delete()) {
                        logger.info("Successfully deleting file " + it.name)
                    } else {
                        if (it.exists()) {
                            logger.warning("Error while deleting the file " + it.name)
                        } else {
                            logger.severe("File does not exists " + it.name)
                        }
                    }
                } else {
                    logger.info("File not selected to be deleted " + it.name)
                }
            } else {
                logger.warning("File does not exist " + it.name)
            }
        }
        loadBackupFiles()
    }
}