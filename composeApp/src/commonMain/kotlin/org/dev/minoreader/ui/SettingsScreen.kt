package org.dev.minoreader.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(vm: MainViewModel) {
    val feeds by vm.feeds.collectAsState()
    var url by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text("Add feed", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("RSS/Atom URL") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
        OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Category (e.g. Tech, News)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
        Button(
            onClick = {
                vm.addFeed(url, category)
                url = ""
                category = ""
            },
            enabled = url.isNotBlank(),
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Text("Add")
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        Text("Feeds (${feeds.size})", style = MaterialTheme.typography.titleMedium)
        if (feeds.isEmpty()) {
            Text(
                "No feeds yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        feeds.forEach { feed ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(feed.title, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "${feed.category} · ${feed.url}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = { vm.deleteFeed(feed.id) }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Remove feed")
                }
            }
            HorizontalDivider()
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        Text("AI (summaries)", style = MaterialTheme.typography.titleMedium)
        Text(
            "Provider (Claude/OpenAI) and API key settings coming soon.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
