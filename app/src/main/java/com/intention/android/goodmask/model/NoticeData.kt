package com.intention.android.goodmask.model

import java.sql.Timestamp

data class NoticeData(
    var title: String,
    var content: String,
    var time: Timestamp?,
    var read: Boolean
)