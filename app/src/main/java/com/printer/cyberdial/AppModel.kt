package com.printer.cyberdial

data class AppModel(
    val name: String,
    val iconResId: Int,
    val isFolder: Boolean = false,
    val folderApps: List<AppModel>? = null,
    val packageName: String? = null
)