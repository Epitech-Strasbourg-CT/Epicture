package com.dev.epicture.epicture.services.imgur.models

/**
 * basic imgur image model
 */
data class ImageModel(
    val id : String?,
    val link: String?,
    val mp4: String?,
    val title: String?,
    val description: String?,
    var favorite: Boolean?
) : SelectableModel()