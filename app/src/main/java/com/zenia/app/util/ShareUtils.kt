package com.zenia.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.FileProvider
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.suspendCancellableCoroutine

object ShareUtils {

    suspend fun captureComposableAsBitmap(
        view: View,
        context: Context,
        width: Int = 1080,
        height: Int = 1920,
        content: @Composable () -> Unit
    ): Bitmap = suspendCancellableCoroutine { continuation ->
        try {
            val composeView = ComposeView(context).apply {
                setContent { content() }
            }

            composeView.setViewTreeLifecycleOwner(view.findViewTreeLifecycleOwner())
            composeView.setViewTreeViewModelStoreOwner(view.findViewTreeViewModelStoreOwner())
            composeView.setViewTreeSavedStateRegistryOwner(view.findViewTreeSavedStateRegistryOwner())

            val rootView = view.rootView as? ViewGroup
            if (rootView != null) {
                composeView.alpha = 0f
                rootView.addView(composeView, ViewGroup.LayoutParams(width, height))

                continuation.invokeOnCancellation {
                    rootView.removeView(composeView)
                }

                composeView.post {
                    try {
                        if (continuation.isActive) {
                            composeView.measure(
                                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
                            )
                            composeView.layout(0, 0, width, height)

                            val bitmap = createBitmap(width, height)
                            val canvas = Canvas(bitmap)
                            composeView.draw(canvas)

                            rootView.removeView(composeView)
                            continuation.resume(bitmap)
                        } else {
                            rootView.removeView(composeView)
                        }
                    } catch (e: Exception) {
                        rootView.removeView(composeView)
                        if (continuation.isActive) {
                            continuation.resumeWithException(e)
                        }
                    }
                }
            } else {
                if (continuation.isActive) {
                    continuation.resumeWithException(IllegalStateException("No se pudo obtener el RootView"))
                }
            }
        } catch (e: Exception) {
            if (continuation.isActive) {
                continuation.resumeWithException(e)
            }
        }
    }

    suspend fun shareBitmap(context: Context, bitmap: Bitmap) {
        withContext(Dispatchers.IO) {
            try {
                val cachePath = File(context.cacheDir, "images")
                cachePath.mkdirs()
                val file = File(cachePath, "zenia_streak_${System.currentTimeMillis()}.png")

                val stream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                stream.flush()
                stream.close()

                val imageUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )

                withContext(Dispatchers.Main) {
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        setDataAndType(imageUri, "image/png")
                        putExtra(Intent.EXTRA_STREAM, imageUri)
                        putExtra(Intent.EXTRA_TEXT, "¡Cuidando mi salud mental un día a la vez con ZenIA! 🌱")
                    }
                    val chooser = Intent.createChooser(shareIntent, "Compartir racha")
                    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(chooser)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}