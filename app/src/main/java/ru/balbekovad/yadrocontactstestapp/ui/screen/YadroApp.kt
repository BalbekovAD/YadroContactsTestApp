package ru.balbekovad.yadrocontactstestapp.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.StateFlow
import ru.balbekovad.yadrocontactstestapp.Contact
import ru.balbekovad.yadrocontactstestapp.R
import ru.balbekovad.yadrocontactstestapp.ui.ContactCard

@Composable
fun YadroApp(modifier: Modifier = Modifier, viewModel: YadroAppViewModel = hiltViewModel()) {
    val contacts by viewModel.contacts.collectAsState()
    val errorMessage = viewModel.errorMessage
    YadroApp(
        contacts,
        errorMessage,
        viewModel::onRefresh,
        viewModel::onCall,
        viewModel::onDeleteDuplicates,
        modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun YadroApp(
    contacts: ImmutableList<Contact>,
    errorMessage: StateFlow<String>,
    onRefresh: () -> Unit,
    onCall: (Contact) -> Unit,
    onDeleteDuplicates: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val permissionState = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.CALL_PHONE,
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.WRITE_CONTACTS,
        ),
        onPermissionsResult = {
            if (it[android.Manifest.permission.READ_CONTACTS] == true) {
                onRefresh()
            }
        }
    )
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(null) {
        onRefresh()
    }
    LaunchedEffect(errorMessage) {
        errorMessage.collect {
            if (it.isNotBlank())
                snackbarHostState.showSnackbar(it)
        }
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(innerPadding)
        ) {
            if (permissionState.permissions.any { !it.status.isGranted }) {
                Text(stringResource(R.string.permissions_request))
                FilledTonalButton(
                    onClick = { permissionState.launchMultiplePermissionRequest() },
                    content = { Text(stringResource(R.string.grant_permissions)) }
                )
            }
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(contacts, key = { it.id }) {
                    ContactCard(
                        contact = it,
                        onClick = {
                            if (permissionState.permissions[0].status.isGranted) onCall(it)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem()
                    )
                }
            }
            FilledTonalButton(
                onClick = onDeleteDuplicates,
                content = { Text(stringResource(R.string.delete_duplicate_contacts)) }
            )
            FilledTonalButton(
                onClick = onRefresh,
                content = { Text(stringResource(R.string.refresh)) }
            )

        }
    }
}