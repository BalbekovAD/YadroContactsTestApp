package ru.balbekovad.yadrocontactstestapp.ui.screen

import android.content.ComponentName
import android.content.Context
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.balbekovad.yadrocontactstestapp.Contact
import ru.balbekovad.yadrocontactstestapp.ContactCleanupService
import ru.balbekovad.yadrocontactstestapp.hasPermission
import ru.balbekovad.yadrocontactstestapp.ContactsRepository
import ru.balbekovad.yadrocontactstestapp.IContactCleanupService
import ru.balbekovad.yadrocontactstestapp.R
import javax.inject.Inject

@HiltViewModel
class YadroAppViewModel @Inject constructor(
    private val contactsRepository: ContactsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    val contacts: StateFlow<ImmutableList<Contact>> get() = contactsRepository.contacts

    private val _message: MutableStateFlow<String> = MutableStateFlow("")
    val errorMessage: StateFlow<String> get() = _message

    fun onRefresh() {
        viewModelScope.launch {
            if (!context.hasPermission(android.Manifest.permission.READ_CONTACTS)) {
                _message.emit(context.getString(R.string.need_read_contacts))
                return@launch
            }
            withContext(Dispatchers.IO) { contactsRepository.updateContacts() }
        }
    }

    fun onCall(contact: Contact) {
        viewModelScope.launch(Dispatchers.Main) {
            if (context.hasPermission(android.Manifest.permission.CALL_PHONE)) {
                try {
                    context.startActivity(
                        Intent(Intent.ACTION_CALL, contact.uri)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                } catch (e: Exception) {
                    _message.emit(context.getString(R.string.phone_call_internal_error))
                    Log.e(this::class.simpleName, "onCall: exception", e)
                }
            } else {
                _message.emit(context.getString(R.string.need_phone_call_permission))
            }
        }
    }

    fun onDeleteDuplicates() {
        viewModelScope.launch {
            if (!context.hasPermission(android.Manifest.permission.WRITE_CONTACTS)) {
                _message.emit("Please, grant permission to change your contacts")
                return@launch
            }

            try {
                context.bindService(
                    Intent(context, ContactCleanupService::class.java),
                    object : ServiceConnection {
                        override fun onServiceConnected(
                            name: ComponentName?,
                            service: IBinder?
                        ) {
                            viewModelScope.launch {
                                val deletionCount = IContactCleanupService.Stub.asInterface(service)
                                    ?.deleteDuplicateContacts()
                                when {
                                    deletionCount == null -> _message
                                        .emit(context.getString(R.string.service_connection_error))

                                    deletionCount == 0 -> _message
                                        .emit(context.getString(R.string.dup_contacts_not_found))

                                    deletionCount < 0 -> _message
                                        .emit(context.getString(R.string.deleting_duplicates_internal_error))

                                    else -> {
                                        _message.emit(
                                            context.getString(
                                                R.string.deduplicating_contacts_success,
                                                deletionCount
                                            )
                                        )
                                        contactsRepository.updateContacts()
                                    }
                                }
                            }
                        }

                        override fun onServiceDisconnected(name: ComponentName?) {
                        }
                    },
                    BIND_AUTO_CREATE
                )
            } catch (e: Exception) {
                Log.e(this::class.simpleName, "onDeleteDuplicates: exception", e)
            }
        }
    }

}