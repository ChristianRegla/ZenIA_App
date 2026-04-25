package com.zenia.app.util

import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "1. Phone - Light", device = "spec:width=360dp,height=800dp,dpi=480", showBackground = true)
@Preview(name = "2. Phone - Small", device = "spec:width=320dp,height=480dp,dpi=480", showBackground = true)
@Preview(name = "3. Foldable", device = "spec:width=673dp,height=841dp,dpi=480", showBackground = true)
@Preview(name = "4. Tablet", device = "spec:width=1280dp,height=800dp,dpi=480", showBackground = true)
annotation class DevicePreviews
