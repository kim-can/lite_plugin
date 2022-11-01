package com.boss.android.lite.plugin.file

import javax.swing.Icon

data class FileListModel(val icon: Icon, val title:String,val type : FileType)

enum class FileType{
    Lite,
    LiteActivity,
    LiteFragment,
    LiteDialogFragment
}

data class XMLModel(val xmlName:String,val xmlBinding:String)