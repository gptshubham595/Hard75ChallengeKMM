package com.shubham.hard75kmm

import androidx.compose.ui.window.ComposeUIViewController
import com.shubham.hard75kmm.di.initKoin

fun MainViewController() = ComposeUIViewController({
    initKoin()
}) {
    App()
}