package com.example.meme30.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.meme30.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

data class Meme(
    val id: String,
    val channelId: String,
    val creator: String
)

data class Video(
    val id: String,
    val title: String,
    val channelId: String,
    val creator: String
)

object BackendApi {
    private val baseUrl = BuildConfig.BACKEND_BASE_URL.trim().trimEnd('/')
    const val MEME_PAGE_SIZE = 20

    suspend fun getMemes(offset: Int): List<Meme> = withContext(Dispatchers.IO) {
        require(offset >= 0) { "Meme offset cannot be negative." }
        val response = get("$baseUrl/more_memes?n=$offset")
        val items = JSONArray(response)
        List(items.length()) { index ->
            val item = items.getJSONObject(index)
            Meme(
                id = item.getString("id"),
                channelId = item.getString("id_kanala"),
                creator = item.getString("name")
            )
        }
    }

    suspend fun getVideos(): List<Video> = withContext(Dispatchers.IO) {
        val response = get("$baseUrl/videos?n=0")
        val items = JSONArray(response)
        List(items.length()) { index ->
            val item = items.getJSONObject(index)
            Video(
                id = item.getString("id"),
                title = item.getString("ime"),
                channelId = item.getString("id_kanala"),
                creator = item.getString("name")
            )
        }
    }

    suspend fun getMemeImage(memeId: String): Bitmap = withContext(Dispatchers.IO) {
        val encodedId = Uri.encode(memeId)
        val bytes = getBytes("$baseUrl/id_memea=$encodedId")
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: throw IOException("The backend returned an invalid meme image.")
    }

    fun videoStreamUrl(videoId: String): String {
        val encodedId = Uri.encode(videoId)
        return "$baseUrl/android_id_videa=$encodedId"
    }

    private fun get(path: String): String =
        String(getBytes(path), StandardCharsets.UTF_8)

    private fun getBytes(path: String): ByteArray {
        val connection = URL(path).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 15_000
        connection.readTimeout = 30_000
        try {
            val statusCode = connection.responseCode
            if (statusCode !in 200..299) {
                throw IOException("Backend request failed with HTTP $statusCode.")
            }
            return connection.inputStream.use { it.readBytes() }
        } finally {
            connection.disconnect()
        }
    }
}
