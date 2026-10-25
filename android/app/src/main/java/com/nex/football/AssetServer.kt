package com.nex.football

import android.content.res.AssetManager
import java.io.BufferedReader
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

/**
 * Tiny loopback HTTP server that serves the app's assets from http://127.0.0.1:<port>/.
 * Serving over a real http origin (rather than file://) keeps web storage, fetch and
 * service-worker behaviour identical to the browser build, with no extra dependencies.
 */
class AssetServer(private val assets: AssetManager) {

    var port: Int = 0
        private set
    private var server: ServerSocket? = null
    @Volatile private var running = false

    fun start() {
        val socket = ServerSocket(0)
        port = socket.localPort
        server = socket
        running = true
        thread(isDaemon = true) {
            while (running) {
                try {
                    val client = socket.accept()
                    thread(isDaemon = true) { handle(client) }
                } catch (e: Exception) { if (running) continue else break }
            }
        }
    }

    fun stop() {
        running = false
        try { server?.close() } catch (e: Exception) {}
    }

    private fun handle(client: Socket) {
        try {
            val input = client.getInputStream().bufferedReader()
            val requestLine = input.readLine() ?: return
            var line: String?
            do { line = input.readLine() } while (!line.isNullOrEmpty())
            val path = requestLine.split(" ").getOrNull(1) ?: "/"
            val clean = path.substringBefore("?").trimStart('/').ifEmpty { "index.html" }
            val stream: InputStream? = try { assets.open(clean) } catch (e: Exception) { null }
            val out: OutputStream = client.getOutputStream()
            if (stream == null) {
                val body = "Not found".toByteArray()
                out.write("HTTP/1.1 404 Not Found\r\nContent-Type: text/plain\r\nContent-Length: ${body.size}\r\nConnection: close\r\n\r\n".toByteArray())
                out.write(body)
            } else {
                val body = stream.readBytes()
                val type = when {
                    clean.endsWith(".html") -> "text/html; charset=utf-8"
                    clean.endsWith(".js") -> "application/javascript"
                    clean.endsWith(".css") -> "text/css"
                    clean.endsWith(".json") || clean.endsWith(".webmanifest") -> "application/json"
                    clean.endsWith(".png") -> "image/png"
                    clean.endsWith(".jpg") || clean.endsWith(".jpeg") -> "image/jpeg"
                    clean.endsWith(".webp") -> "image/webp"
                    clean.endsWith(".svg") -> "image/svg+xml"
                    else -> "application/octet-stream"
                }
                out.write("HTTP/1.1 200 OK\r\nContent-Type: $type\r\nContent-Length: ${body.size}\r\nCache-Control: no-store\r\nConnection: close\r\n\r\n".toByteArray())
                out.write(body)
            }
            out.flush()
        } catch (e: Exception) {
        } finally {
            try { client.close() } catch (e: Exception) {}
        }
    }
}
