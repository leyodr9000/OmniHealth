package com.example.omnihealth.data.export

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import androidx.core.content.FileProvider
import com.example.omnihealth.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 周复盘图片导出。
 *
 * 用 Canvas 直接画，不依赖截图库：内容固定、体积小、不需要 Activity。
 * 目的只有一个——让用户能把结论存下来或发给别人，而不是只留在 App 里。
 */
object ReportImageExporter {

    private const val WIDTH = 1080
    private const val HEIGHT = 1350

    fun export(
        context: Context,
        summary: String,
        evidence: String,
        actions: List<String>,
        fiberDays: Int,
    ): File? = runCatching {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.parseColor("#0A0D14"))

        val title = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F1F4F9")
            textSize = 64f
            isFakeBoldText = true
        }
        val body = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#A7B2C4")
            textSize = 38f
        }
        val accent = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#6EA8FF")
            textSize = 36f
            isFakeBoldText = true
        }

        var y = 140f
        canvas.drawText(context.getString(R.string.review_image_title), 80f, y, title)
        y += 60f
        canvas.drawText(
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            80f,
            y,
            body,
        )

        y += 90f
        drawWrapped(canvas, summary, 80f, y, WIDTH - 160f, accent)
        y += 150f
        drawWrapped(canvas, evidence, 80f, y, WIDTH - 160f, body)
        y += 120f
        drawWrapped(canvas, context.getString(R.string.review_fiber, fiberDays), 80f, y, WIDTH - 160f, body)

        y += 160f
        actions.forEach { action ->
            drawWrapped(canvas, "· $action", 80f, y, WIDTH - 160f, body)
            y += 70f
        }

        y += 60f
        val footer = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#6B7686")
            textSize = 28f
        }
        drawWrapped(canvas, context.getString(R.string.review_image_footer), 80f, y, WIDTH - 160f, footer)

        val dir = File(context.getExternalFilesDir(null), "exports").apply { mkdirs() }
        val file = File(dir, "omnihealth-review-${System.currentTimeMillis()}.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        file
    }.getOrNull()

    fun share(context: Context, file: File) {
        val uri = runCatching {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        }.getOrNull() ?: return
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        runCatching {
            context.startActivity(
                Intent.createChooser(intent, context.getString(R.string.review_share_title)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }

    private fun drawWrapped(
        canvas: Canvas,
        text: String,
        x: Float,
        startY: Float,
        maxWidth: Float,
        paint: Paint,
    ) {
        val words = text.split(" ")
        var line = ""
        var y = startY
        words.forEach { word ->
            val candidate = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(candidate) <= maxWidth) {
                line = candidate
            } else {
                canvas.drawText(line, x, y, paint)
                y += paint.textSize * 1.5f
                line = word
            }
        }
        if (line.isNotEmpty()) canvas.drawText(line, x, y, paint)
    }
}
