package com.example.localhtmlpreview

import java.io.BufferedInputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import kotlin.concurrent.thread

class LocalServer(private val root: Path) {
    private val token = UUID.randomUUID().toString().replace("-", "")
    private var socket: ServerSocket? = null
    private var worker: Thread? = null
    fun start(): String {
        socket = ServerSocket(0, 20, java.net.InetAddress.getByName("127.0.0.1"))
        worker = thread(name = "local-html-server") { while (true) try { socket?.accept()?.let { handle(it) } } catch (_: Exception) { break } }
        return "http://127.0.0.1:${socket!!.localPort}/index.html?token=$token"
    }
    fun stop() { socket?.close(); worker?.interrupt(); socket = null }
    private fun handle(client: Socket) {
        client.use { c ->
            val input = BufferedInputStream(c.getInputStream()); val line = readLine(input) ?: return
            val path = line.split(" ").getOrNull(1) ?: return
            val query = path.substringAfter('?', "")
            if (!query.split('&').contains("token=$token")) return response(c.getOutputStream(), 403, "text/plain", "Forbidden".toByteArray())
            val clean = path.substringBefore('?').removePrefix("/").ifBlank { "index.html" }
            val file = root.resolve(clean).normalize()
            if (!file.startsWith(root) || !Files.isRegularFile(file)) return response(c.getOutputStream(), 404, "text/plain", "Not found".toByteArray())
            val bytes = Files.readAllBytes(file)
            response(c.getOutputStream(), 200, mime(file), bytes)
        }
    }
    private fun readLine(input: BufferedInputStream): String? { val b = StringBuilder(); var p = -1; while (true) { val n = input.read(); if (n < 0) return null; if (p == 13 && n == 10) return b.dropLast(1).toString(); b.append(n.toChar()); p = n; if (b.length > 8192) return null } }
    private fun response(out: OutputStream, code: Int, type: String, body: ByteArray) { out.write("HTTP/1.1 $code OK\r\nContent-Type: $type\r\nContent-Length: ${body.size}\r\nCache-Control: no-store\r\nConnection: close\r\n\r\n".toByteArray()); out.write(body); out.flush() }
    private fun mime(p: Path) = when (p.fileName.toString().substringAfterLast('.', "").lowercase()) { "html","htm" -> "text/html; charset=utf-8"; "js","mjs" -> "text/javascript; charset=utf-8"; "css" -> "text/css; charset=utf-8"; "json" -> "application/json"; "svg" -> "image/svg+xml"; "png" -> "image/png"; "jpg","jpeg" -> "image/jpeg"; "gif" -> "image/gif"; "webp" -> "image/webp"; "woff" -> "font/woff"; "woff2" -> "font/woff2"; "wasm" -> "application/wasm"; else -> "application/octet-stream" }
}
