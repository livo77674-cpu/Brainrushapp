package com.brainrush.app

import android.app.Application
import com.brainrush.app.data.preferences.BrainRushDataStore
import com.brainrush.app.data.preferences.brainRushDataStore
import com.brainrush.app.data.repository.BrainRushRepository
import com.brainrush.app.data.repository.OfflineBrainRushRepository

class BrainRushApplication : Application() {
    lateinit var repository: BrainRushRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = OfflineBrainRushRepository(BrainRushDataStore(applicationContext.brainRushDataStore))
    }
}
