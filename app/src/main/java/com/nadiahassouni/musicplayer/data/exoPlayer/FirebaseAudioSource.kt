package com.nadiahassouni.musicplayer.data.exoPlayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import androidx.core.net.toUri
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.nadiahassouni.musicplayer.data.firebase.AudioDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FirebaseAudioSource {

    private val audioDatabase : AudioDatabase = AudioDatabase()

    var songs = emptyList<MediaMetadataCompat>()


    suspend fun getSongsFromFirebase() = withContext(Dispatchers.IO){
        state = State.STATE_INITIALIZING
        val allSongs = audioDatabase.getAllSongs()
        songs = allSongs.map {
            MediaMetadataCompat.Builder()
                .putString(METADATA_KEY_ARTIST , it.singer )
                .putString(METADATA_KEY_MEDIA_ID , it.audioId)
                .putString(METADATA_KEY_TITLE , it.title)
                .putString(METADATA_KEY_DISPLAY_TITLE, it.title)
                .putString(METADATA_KEY_MEDIA_URI , it.audioUrl )
                .putString(METADATA_KEY_DISPLAY_ICON_URI , it.imageUrl)
                .putString(METADATA_KEY_ALBUM_ART_URI , it.imageUrl)
                .putString(METADATA_KEY_DISPLAY_TITLE , it.singer)
                .putString(METADATA_KEY_DISPLAY_DESCRIPTION , it.singer)
                .build()
        }
        state = State.STATE_INITIALIZED
    }

    fun convertMediaMetadataToMediaSource(dataSourceFactory : DefaultDataSourceFactory) : ConcatenatingMediaSource{
        val concatenatingMediaSource = ConcatenatingMediaSource()
        songs.forEach {
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(it.getString(METADATA_KEY_MEDIA_URI)))
            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        return  concatenatingMediaSource
    }

    fun convertMediaMetaDataToMediaItems() = songs.map {
        val description = MediaDescriptionCompat.Builder()
            .setMediaUri(it.getString(METADATA_KEY_MEDIA_URI).toUri())
            .setTitle(it.description.title)
            .setSubtitle(it.description.subtitle)
            .setMediaId(it.description.mediaId)
            .setIconUri(it.description.iconUri)
            .build()
        MediaBrowserCompat.MediaItem(description , FLAG_PLAYABLE)
    }

    //check if audios finished downloading from firebase
    private val onReadyListener = mutableListOf<(Boolean) -> Unit>()

    private var state : State = State.STATE_INITIALIZING

    //whenever state is changed we will trigger the setter
    set(value){
        if(value == State.STATE_INITIALIZED || value == State.STATE_ERROR){
            // used by only one thread at a time
            synchronized(onReadyListener){
                field = value
                onReadyListener.forEach{ listener ->
                    listener(state == State.STATE_INITIALIZED)  //true
                }
            }
        }else{
            field = value
        }
    }

    fun whenReady(action : (Boolean)-> Unit) : Boolean {
        return if (state == State.STATE_CREATED || state == State.STATE_INITIALIZING){
            onReadyListener += action
            false
        }else{
            action(state == State.STATE_INITIALIZED)
            true
        }
    }

}

enum class State {
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR
}