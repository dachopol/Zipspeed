package com.example.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

data class EndpointHealthResult(
    val id: String,
    val name: String,
    val target: String,
    val url: String,
    val latencyMs: Int? = null,
    val statusCode: Int? = null,
    val isReachable: Boolean = false
)

class EndpointHealthChecker {
    private val client = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(3, TimeUnit.SECONDS)
        .callTimeout(4, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private data class Endpoint(
        val id: String,
        val name: String,
        val target: String,
        val url: String
    )

    private val endpoints = listOf(
        Endpoint("cloudflare", "Cloudflare", "1.1.1.1", "https://1.1.1.1/cdn-cgi/trace"),
        Endpoint("google", "Google", "www.google.com", "https://www.google.com/generate_204"),
        Endpoint("youtube", "YouTube", "www.youtube.com", "https://www.youtube.com/generate_204"),
        Endpoint("wikipedia", "Wikipedia", "en.wikipedia.org", "https://en.wikipedia.org/wiki/Special:BlankPage")
    )

    suspend fun checkAll(): List<EndpointHealthResult> = withContext(Dispatchers.IO) {
        endpoints.map { endpoint ->
            val startNs = System.nanoTime()
            runCatching {
                val request = Request.Builder()
                    .url(endpoint.url)
                    .header("User-Agent", "Zipspeed/1.0")
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->
                    val elapsedMs = ((System.nanoTime() - startNs) / 1_000_000L)
                        .coerceAtLeast(1L)
                        .coerceAtMost(Int.MAX_VALUE.toLong())
                        .toInt()
                    EndpointHealthResult(
                        id = endpoint.id,
                        name = endpoint.name,
                        target = endpoint.target,
                        url = endpoint.url,
                        latencyMs = elapsedMs,
                        statusCode = response.code,
                        isReachable = response.isSuccessful || response.code in 300..399
                    )
                }
            }.getOrElse {
                EndpointHealthResult(
                    id = endpoint.id,
                    name = endpoint.name,
                    target = endpoint.target,
                    url = endpoint.url,
                    latencyMs = null,
                    statusCode = null,
                    isReachable = false
                )
            }
        }
    }
}
