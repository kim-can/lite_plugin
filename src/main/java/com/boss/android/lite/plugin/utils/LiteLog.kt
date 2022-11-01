package com.boss.android.lite.plugin.utils

import com.intellij.notification.NotificationType
import com.intellij.notification.NotificationType.INFORMATION
import java.time.LocalDateTime

fun blog(msg: String) = log(msg, INFORMATION)

private fun log(msg: String, type: NotificationType) {
    val timedMsg = "${LocalDateTime.now()} : $msg"
    println(timedMsg)

}
