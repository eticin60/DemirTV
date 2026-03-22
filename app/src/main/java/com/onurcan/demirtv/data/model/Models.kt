package com.onurcan.demirtv.data.model

data class Channel(
    val id: String,
    val name: String,
    val logoUrl: String?,
    val streamUrl: String,
    val category: String,
    val isChildSafe: Boolean = false
)

data class Profile(
    val id: Int,
    val name: String,
    val isChild: Boolean = false,
    val avatarRes: Int
)
