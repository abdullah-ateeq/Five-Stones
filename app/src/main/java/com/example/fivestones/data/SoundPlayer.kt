package com.example.fivestones.data

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.fivestones.R

enum class SoundEffect { STONE_PLACED, WIN, DRAW }

/** Low-latency playback of the three short effects. Sounds load once for the app's lifetime. */
class SoundPlayer(context: Context) {

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(3)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    private val soundIds: Map<SoundEffect, Int> = mapOf(
        SoundEffect.STONE_PLACED to soundPool.load(context, R.raw.stone_place, 1),
        SoundEffect.WIN to soundPool.load(context, R.raw.win, 1),
        SoundEffect.DRAW to soundPool.load(context, R.raw.draw, 1),
    )

    fun play(effect: SoundEffect) {
        val id = soundIds[effect] ?: return
        val volume = when (effect) {
            SoundEffect.STONE_PLACED -> 0.55f
            SoundEffect.WIN -> 0.6f
            SoundEffect.DRAW -> 0.5f
        }
        soundPool.play(id, volume, volume, 1, 0, 1f)
    }
}
