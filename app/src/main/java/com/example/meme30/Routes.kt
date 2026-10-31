package com.example.meme30

import android.net.Uri
import com.example.meme30.data.Meme
import com.example.meme30.data.Video

object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val MEMES = "memes"
    const val MEME_DETAIL = "meme/{memeId}?creator={creator}"
    const val VIDEO_DETAIL = "video/{videoId}?title={title}&creator={creator}"

    fun memeDetail(meme: Meme): String =
        "meme/${Uri.encode(meme.id)}?creator=${Uri.encode(meme.creator)}"

    fun videoDetail(video: Video): String =
        "video/${Uri.encode(video.id)}" +
            "?title=${Uri.encode(video.title)}&creator=${Uri.encode(video.creator)}"
}