package com.zenia.app.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.ButtonDefaults
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.zenia.app.MainActivity

class SosWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFF1E1E1E))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Línea de Ayuda ZenIA",
                    style = TextStyle(
                        color = ColorProvider(day = Color.White, night = Color.White),
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = GlanceModifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        text = "Chat",
                        onClick = actionStartActivity<MainActivity>(
                            // Podríamos pasar un extra para que el MainActivity sepa a dónde ir
                        ),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = ColorProvider(day = Color(0xFF9C27B0), night = Color(0xFF9C27B0)),
                            contentColor = ColorProvider(day = Color.White, night = Color.White)
                        )
                    )

                    Spacer(modifier = GlanceModifier.width(8.dp))

                    Button(
                        text = "SOS",
                        onClick = actionStartActivity<MainActivity>(),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = ColorProvider(day = Color(0xFFE91E63), night = Color(0xFFE91E63)),
                            contentColor = ColorProvider(day = Color.White, night = Color.White)
                        )
                    )
                }
            }
        }
    }
}

class SosWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SosWidget()
}