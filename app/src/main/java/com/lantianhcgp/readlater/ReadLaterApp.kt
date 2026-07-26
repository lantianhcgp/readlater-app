package com.lantianhcgp.readlater

import android.app.Application
import android.util.Log
import com.lantianhcgp.readlater.debug.DebugApiServer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import com.lantianhcgp.readlater.data.db.dao.ArticleDao
import com.lantianhcgp.readlater.data.db.dao.TagDao

@HiltAndroidApp
class ReadLaterApp : Application() {

    @Inject lateinit var articleDao: ArticleDao
    @Inject lateinit var tagDao: TagDao

    private var debugServer: DebugApiServer? = null

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            try {
                debugServer = DebugApiServer(
                    port = 8080,
                    articleDao = articleDao,
                    tagDao = tagDao
                )
                debugServer?.start()
                Log.i("ReadLaterApp", "Debug API available at http://localhost:8080")
            } catch (e: Exception) {
                Log.e("ReadLaterApp", "Failed to start debug server: ${e.message}")
            }
        }
    }

    override fun onTerminate() {
        debugServer?.stop()
        super.onTerminate()
    }
}
