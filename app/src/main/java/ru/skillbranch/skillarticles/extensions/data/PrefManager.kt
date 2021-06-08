package ru.skillbranch.skillarticles.extensions.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import ru.skillbranch.skillarticles.App
import ru.skillbranch.skillarticles.data.delegates.PrefDelegate

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PrefManager(context: Context = App.applicationContext()) {
    val dataStore = context.dataStore
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(javaClass.canonicalName, "error ${throwable.message}")
    }
    internal val scope = CoroutineScope(SupervisorJob() + exceptionHandler)
    var isBigText by PrefDelegate(false)
    var isDarkMode by PrefDelegate(false)
}

