package ru.balbekovad.yadrocontactstestapp

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.provider.ContactsContract
import android.util.Log
import android.util.Log.e
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

data class VisibleContactData(
    val name: String,
    val phone: String,
    val imageURL: String
) {
    constructor(contact: Contact) : this(contact.name, contact.phone, contact.imageURL)
}

@AndroidEntryPoint
class ContactCleanupService : Service() {

    @Inject
    lateinit var contactsRepository: ContactsRepository

    override fun onBind(intent: Intent): IBinder = object : IContactCleanupService.Stub() {
        override fun deleteDuplicateContacts(): Int {
            try {
                val duplicatesIds = findDuplicates()

                for (id in duplicatesIds) {
                    contentResolver.delete(
                        ContactsContract.RawContacts.CONTENT_URI,
                        "${ContactsContract.RawContacts.CONTACT_ID} = ?",
                        arrayOf(id)
                    )
                }
                return duplicatesIds.size
            } catch (e: Exception) {
                e("CleanupService", "Error deleting duplicates", e)
                return -1
            }
        }
    }

    private fun findDuplicates(): List<String> {
        val set = hashSetOf<VisibleContactData>()
        return contactsRepository.fetchAllContacts().mapNotNull { contact ->
            if (!set.add(VisibleContactData(contact))) {
                Log.i("ContactCleanupService", "deleteDuplicateContacts: $contact is not unique")
                contact.id
            } else {
                Log.i("ContactCleanupService", "deleteDuplicateContacts: $contact is unique")
                null
            }
        }
    }
}
