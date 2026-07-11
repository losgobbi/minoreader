package org.dev.minoreader.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.dev.minoreader.data.ArticleDetail
import org.dev.minoreader.reader.ContentBlock
import org.dev.minoreader.util.formatDate

private sealed interface ReaderUi {
    data object Loading : ReaderUi
    data class Ready(val blocks: List<ContentBlock>) : ReaderUi
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    articleId: Long,
    vm: MainViewModel,
    onOpenOriginal: (String) -> Unit,
    onBack: () -> Unit,
) {
    val detail by produceState<ArticleDetail?>(initialValue = null, articleId) {
        value = vm.loadDetail(articleId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Article") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        val d = detail
        if (d == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            DetailContent(
                detail = d,
                vm = vm,
                modifier = Modifier.padding(padding),
                onOpenOriginal = { d.link?.let(onOpenOriginal) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailContent(
    detail: ArticleDetail,
    vm: MainViewModel,
    modifier: Modifier,
    onOpenOriginal: () -> Unit,
) {
    var isRead by remember(detail) { mutableStateOf(detail.isRead) }
    var isFavorite by remember(detail) { mutableStateOf(detail.isFavorite) }

    val reader by produceState<ReaderUi>(ReaderUi.Loading, detail.id) {
        value = ReaderUi.Ready(vm.loadArticleBlocks(detail))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(detail.title, style = MaterialTheme.typography.headlineSmall)

        val subtitle = buildString {
            append(detail.feedTitle)
            detail.author?.let { append(" · ").append(it) }
            formatDate(detail.publishedAt)?.let { append(" · ").append(it) }
        }
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilledTonalButton(onClick = { isRead = !isRead; vm.markRead(detail.id, isRead) }) {
                Text(if (isRead) "Read ✓" else "Mark as read")
            }
            OutlinedButton(onClick = { isFavorite = !isFavorite; vm.setFavorite(detail.id, isFavorite) }) {
                Icon(if (isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder, contentDescription = null)
                Text(if (isFavorite) " Favorited" else " Favorite")
            }
            if (detail.link != null) {
                Button(onClick = onOpenOriginal) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
                    Text(" Open original")
                }
            }
        }

        // AI summary (to be implemented): button + summary↔article toggle.
        OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.padding(top = 8.dp)) {
            Text("Summarize with AI (coming soon)")
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        when (val state = reader) {
            ReaderUi.Loading -> Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp))
                Text("  Loading article…", style = MaterialTheme.typography.bodyMedium)
            }

            is ReaderUi.Ready -> if (state.blocks.isEmpty()) {
                Text(
                    "Couldn't load the content here. Use \"Open original\".",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                ArticleContentView(state.blocks)
            }
        }
    }
}
