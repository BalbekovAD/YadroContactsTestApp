package ru.balbekovad.yadrocontactstestapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import ru.balbekovad.yadrocontactstestapp.R
import ru.balbekovad.yadrocontactstestapp.Contact
import ru.balbekovad.yadrocontactstestapp.ui.theme.YadroContactsTestAppTheme

@Preview
@Composable
private fun ContactCardPreview() = YadroContactsTestAppTheme {
    ContactCard(
        Contact(
            "aboba",
            "Alexander Balbekov",
            "+79276890347",
            "https://cdn.jsdelivr.net/gh/alohe/memojis/png/vibrent_5.png",
        ),
        {}
    )
}

@Composable
fun ContactCard(contact: Contact, onClick: () -> Unit, modifier: Modifier = Modifier) = Card(
    modifier.clickable(onClick = onClick),
) {
    Row(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = contact.imageURL,
            contentDescription = stringResource(R.string.avatar_content_description),
            modifier = Modifier.size(40.dp),
            error = painterResource(R.drawable.fallback_avatar),
            contentScale = ContentScale.FillBounds
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(contact.name)
            Text(contact.phone)
        }
    }
}