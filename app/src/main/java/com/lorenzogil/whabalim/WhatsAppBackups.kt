package com.lorenzogil.whabalim

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File
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
        val value = backups.value
        var result = 0L
        if (value != null) {
            result = backupsCleaner.getSize(days, value)
        }
        return result
    }

    fun deleteBackups(days: Int): Int {
        val backupsCleaner = BackupsCleaner()
        val value = backups.value
        var result = 0
        if (value != null) {
            result = backupsCleaner.deleteBackups(days, value)
            backups.value = backupsCleaner.getBackupFiles()
        }
        return result
    }
}