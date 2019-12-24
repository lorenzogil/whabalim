package com.lorenzogil.whabalim

import android.os.Environment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File
import java.util.*
import java.util.logging.Logger
import kotlin.collections.ArrayList

class WhatsAppBackups : ViewModel() {

    val hasWhatsApp = MutableLiveData<Boolean> ()
    val backups = MutableLiveData<ArrayList<File>> ()

    init {
        val backupsCleaner = BackupsCleaner()
        val hasWA = backupsCleaner.hasWhatsApp()
        hasWhatsApp.value = hasWA
        if (hasWA){
            backups.value = backupsCleaner.getBackupFiles()
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
        val backupsCleaner = BackupsCleaner()
        val now = Date().time
        val threshold = backupsCleaner.getThreshold(days)
        val result = backups.value?.filter{
            it.exists() && ((now - it.lastModified()) < threshold)
        }?.map{it.length()}?.sum()
        if (result == null) {
            return 0L
        } else {
            return result
        }
    }

    fun deleteBackups(days: Int) {
        val backupsCleaner = BackupsCleaner()
        val value = backups.value
        if (value != null) {
            backupsCleaner.deleteBackups(days, value)
            backups.value = backupsCleaner.getBackupFiles()
        }
    }
}