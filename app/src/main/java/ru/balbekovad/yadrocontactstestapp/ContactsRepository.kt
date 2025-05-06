package ru.balbekovad.yadrocontactstestapp

import android.content.Context
import android.provider.ContactsContract
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.adapters.ImmutableListAdapter
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

interface ContactsRepository {
    val contacts: StateFlow<ImmutableList<Contact>>
    suspend fun updateContacts()
    fun fetchAllContacts(): List<Contact>
}

class ContactsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : ContactsRepository {
    private val _contacts = MutableStateFlow<ImmutableList<Contact>>(persistentListOf())
    override val contacts: StateFlow<ImmutableList<Contact>>
        get() = _contacts

    override fun fetchAllContacts(): List<Contact> = buildList {
        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI
            ),
            "${ContactsContract.CommonDataKinds.Phone.TYPE} = ?",
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE.toString()
            ),
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
        )?.use { cursor ->
            val idxId = cursor.getColumnIndexOrThrow(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID
            )
            val idxName = cursor.getColumnIndexOrThrow(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
            )
            val idxNumber = cursor.getColumnIndexOrThrow(
                ContactsContract.CommonDataKinds.Phone.NUMBER
            )
            val idxPhoto = cursor.getColumnIndexOrThrow(
                ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI
            )

            while (cursor.moveToNext()) {
                this += Contact(
                    id = cursor.getString(idxId),
                    name = cursor.getString(idxName) ?: "",
                    phone = cursor.getString(idxNumber) ?: "",
                    imageURL = cursor.getString(idxPhoto) ?: ""
                )
            }
        }
    }

    override suspend fun updateContacts() {
        _contacts.emit(ImmutableListAdapter(fetchAllContacts()))
    }
}
