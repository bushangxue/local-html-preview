package com.example.localhtmlpreview

import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.zip.ZipInputStream

object SafeZip {
    fun unzip(input: InputStream, root: Path, maxFiles: Int = 20_000, maxBytes: Long = 500L * 1024 * 1024) {
        Files.createDirectories(root); var count = 0; var total = 0L
        ZipInputStream(input.buffered()).use { zip ->
            while (true) {
                val e = zip.nextEntry ?: break
                if (++count > maxFiles) error("文件数量超过 $maxFiles")
                val name = e.name.replace('\\', '/')
                require(name.isNotBlank() && !name.startsWith("/") && !name.contains('\u0000')) { "非法 ZIP 路径" }
                require(name.split('/').none { it == ".." }) { "检测到路径穿越" }
                val out = root.resolve(name).normalize()
                require(out.startsWith(root)) { "检测到路径穿越" }
                if (e.isDirectory) {
                    Files.createDirectories(out)
                    zip.closeEntry()
                    continue
                }
                Files.createDirectories(out.parent)
                Files.newOutputStream(out, StandardOpenOption.CREATE_NEW).use { sink ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) { val n = zip.read(buffer); if (n <= 0) break; total += n; require(total <= maxBytes) { "解压后大小超过限制" }; sink.write(buffer, 0, n) }
                }
                zip.closeEntry()
            }
        }
    }
}
