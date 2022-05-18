package com.nadiahassouni.musicplayer.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.nadiahassouni.musicplayer.data.model.Singer
import com.nadiahassouni.musicplayer.data.model.Song
import kotlinx.coroutines.tasks.await

class AudioDatabase {

    private val firestore = FirebaseFirestore.getInstance()
    private val songsCollection = firestore.collection("songs")
    private val singersCollection = firestore.collection("sings")

    suspend fun getAllSongs() : List<Song>{
        return try {
            songsCollection.get().await().toObjects(Song::class.java)
        }catch (e : Exception){
            emptyList()
        }
    }

    suspend fun getAllSingers() : List<Singer>{
        return try {
            singersCollection.get().await().toObjects(Singer::class.java)
        }catch (e : Exception){
            emptyList()
        }
    }
}