package ru.balbekovad.yadrocontactstestapp

import android.net.Uri

data class Contact(
    val id: String,
    val name: String,
    val phone: String,
    val imageURL: String
) {
    val uri: Uri get() = Uri.fromParts("tel", phone, null)
}