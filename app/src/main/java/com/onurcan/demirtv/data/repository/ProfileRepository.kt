package com.onurcan.demirtv.data.repository

import com.onurcan.demirtv.data.model.Profile

object ProfileRepository {
    fun getProfiles(): List<Profile> = listOf(
        Profile(1, "Demir TV", isChild = false, avatarRes = 0)
    )
}
