package com.gorhaf.excellentwifi.mvi.video

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

data class VideoUiState(
    var decoder: VideoDecoder? = null,
    val videoCoroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
)
