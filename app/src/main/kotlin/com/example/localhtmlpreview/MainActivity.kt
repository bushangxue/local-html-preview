package com.example.localhtmlpreview

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity
import java.io.File
import java.nio.file.Files
import java.util.UUID
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    private val executor = Executors.newSingleThreadExecutor()
    private val picker = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.OpenDocument()) { uri -> if (uri != null) import(uri) }
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32, 48, 32, 32) }
        val title = TextView(this).apply { text = "本地 HTML 预览器"; textSize = 24f }; root.addView(title)
        val help = TextView(this).apply { text = "选择 HTML 或 ZIP，内容只在本机隔离预览。"; setPadding(0, 16, 0, 24) }; root.addView(help)
        val choose = Button(this).apply { text = "选择文件"; setOnClickListener { picker.launch(arrayOf("text/html", "application/zip", "application/x-zip-compressed", "*/*")) } }; root.addView(choose)
        setContentView(root)
    }
    private fun import(uri: Uri) { Toast.makeText(this, "正在导入…", Toast.LENGTH_SHORT).show(); executor.execute { try {
        val id = UUID.randomUUID().toString(); val project = File(filesDir, "projects/$id/site"); project.mkdirs(); val name = uri.lastPathSegment ?: "upload"
        contentResolver.openInputStream(uri)!!.use { input -> if (name.lowercase().endsWith(".zip")) SafeZip.unzip(input, project.toPath()) else Files.copy(input, project.resolve("index.html").toPath()) }
        val entry = findEntry(project) ?: error("未找到 index.html")
        runOnUiThread { startActivity(Intent(this, PreviewActivity::class.java).putExtra("root", project.absolutePath).putExtra("entry", entry)) }
    } catch (e: Exception) { runOnUiThread { Toast.makeText(this, "导入失败：${e.message}", Toast.LENGTH_LONG).show() } } } }
    private fun findEntry(root: File): String? { val direct = File(root, "index.html"); if (direct.isFile) return "index.html"; val candidates = root.walkTopDown().filter { it.isFile && it.extension.lowercase() in setOf("html", "htm") }.toList(); return candidates.singleOrNull()?.relativeTo(root)?.path?.replace(File.separatorChar, '/') }
    override fun onDestroy() { executor.shutdownNow(); super.onDestroy() }
}
