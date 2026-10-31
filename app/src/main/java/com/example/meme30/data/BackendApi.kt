package com.example.meme30.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.LruCache
import com.example.meme30.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
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
    private val memeImageRequests = Semaphore(permits = 2)
    private val memeImageCache = object : LruCache<String, Bitmap>(
        (Runtime.getRuntime().maxMemory() / 16 / 1024).toInt()
    ) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int =
            bitmap.byteCount / 1024
    }

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

    suspend fun getMemeImage(memeId: String, maxDimensionPx: Int = 480): Bitmap =
        withContext(Dispatchers.IO) {
            val cacheKey = "$memeId:$maxDimensionPx"
            memeImageCache.get(cacheKey)?.let { return@withContext it }

            memeImageRequests.withPermit {
                memeImageCache.get(cacheKey)?.let { return@withPermit it }

                val encodedId = Uri.encode(memeId)
                val bytes = getBytes("$baseUrl/id_memea=$encodedId")
                val bounds = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
                val options = BitmapFactory.Options().apply {
                    inSampleSize = calculateInSampleSize(
                        width = bounds.outWidth,
                        height = bounds.outHeight,
                        maxDimensionPx = maxDimensionPx
                    )
                }
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
                    ?: throw IOException("The backend returned an invalid meme image.")
                memeImageCache.put(cacheKey, bitmap)
                bitmap
            }
        }

    fun videoStreamUrl(videoId: String): String {
        val encodedId = Uri.encode(videoId)
        return "$baseUrl/android_id_videa=$encodedId"
    }

    private fun get(path: String): String =
        String(getBytes(path), StandardCharsets.UTF_8)

    private fun calculateInSampleSize(
        width: Int,
        height: Int,
        maxDimensionPx: Int
    ): Int {
        var sampleSize = 1
        while (
            width / sampleSize > maxDimensionPx ||
            height / sampleSize > maxDimensionPx
        ) {
            sampleSize *= 2
        }
        return sampleSize
    }

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
