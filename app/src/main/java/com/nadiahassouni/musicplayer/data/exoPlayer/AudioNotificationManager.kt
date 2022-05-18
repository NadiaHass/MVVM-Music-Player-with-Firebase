package com.nadiahassouni.musicplayer.data.exoPlayer

import android.content.Context
import android.support.v4.media.session.MediaSessionCompat

class AudioNotificationManager(
    private val context : Context ,
    sessionToken : MediaSessionCompat.Token ,
    private val newSongCallBack : () -> Unit
) {
}