package com.example.fivestones

import android.app.Application
import com.example.fivestones.data.PreferencesRepository
import com.example.fivestones.data.SoundPlayer

class FiveStonesApplication : Application() {
    val repository: PreferencesRepository by lazy { PreferencesRepository(this) }
    val soundPlayer: SoundPlayer by lazy { SoundPlayer(this) }
}
