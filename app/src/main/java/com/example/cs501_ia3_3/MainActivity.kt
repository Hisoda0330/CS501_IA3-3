package com.example.cs501_ia3_3
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                ContactsScreen()
            }
        }
    }
}

data class Contact(val name: String, val phone: String)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContactsScreen() {
    val contacts = remember { generateContacts(100) } // >= 50 contacts
    val grouped = remember(contacts) {
        contacts
            .sortedBy { it.name.lowercase(Locale.getDefault()) }
            .groupBy { it.name.first().uppercaseChar() }
            .toSortedMap()
    }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Show FAB only when scrolled past item index 10
    val showFab by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 10 }
    }

    Scaffold(
        floatingActionButton = {
            AnimatedVisibility(
                visible = showFab,
                enter = fadeIn(animationSpec = tween(200)),
                exit = fadeOut(animationSpec = tween(200))
            ) {
                FloatingActionButton(
                    onClick = {
                        // Use animateScrollToItem()
                        scope.launch { listState.animateScrollToItem(0) }
                    }
                ) {
                    Text("Top")
                }
            }
        }
    ) { inner ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            grouped.forEach { (letter, itemsForLetter) ->
                stickyHeader {
                    Header(letter)
                }
                items(itemsForLetter, key = { it.name }) { contact ->
                    ContactRow(contact)
                    Divider()
                }
            }
        }
    }
}

@Composable
private fun Header(letter: Char) {
    Surface(
        tonalElevation = 4.dp,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp))
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = letter.toString(),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun ContactRow(contact: Contact) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Simple circle avatar with initials
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small)
        ) {
            Text(
                text = initials(contact.name),
                modifier = Modifier.align(Alignment.Center),
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = contact.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = contact.phone,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun initials(name: String): String {
    val parts = name.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
    val first = parts.getOrNull(0)?.firstOrNull()?.uppercaseChar() ?: '•'
    val second = parts.getOrNull(1)?.firstOrNull()?.uppercaseChar()
    return if (second != null) "$first$second" else "$first"
}

private fun generateContacts(n: Int): List<Contact> {
    // Sample pools to synthesize many names
    val firstNames = listOf(
        "Alice","Aaron","Ava","Abby","Ben","Brian","Bella","Bruce","Carla","Cindy",
        "Chris","Derek","Diana","Ethan","Ella","Edward","Fiona","Frank","Grace","Gavin",
        "Hanna","Henry","Ivy","Isaac","Jack","Julia","James","Kara","Kevin","Lara",
        "Liam","Maya","Mason","Nina","Noah","Olivia","Oscar","Paula","Peter","Quinn",
        "Rita","Ryan","Sara","Sean","Tina","Tom","Uma","Uri","Vera","Victor",
        "Will","Wendy","Xena","Xavier","Yara","Yusuf","Zara","Zane"
    )
    val lastNames = listOf(
        "Anderson","Baker","Clark","Davis","Edwards","Foster","Garcia","Hughes","Iverson","Johnson",
        "King","Lopez","Miller","Nguyen","Owens","Parker","Quinn","Roberts","Smith","Taylor",
        "Upton","Vega","Williams","Xu","Young","Zimmerman"
    )
    val phones = sequence {
        var base = 1000
        while (true) {
            yield(String.format("(213) 555-%04d", base))
            base++
        }
    }.iterator()

    val out = mutableListOf(Contact("Zed Alpha", "(213) 555-0000")) // ensure 'Z' group exists
    // Combine pools to get at least n unique contacts
    loop@ for (f in firstNames) {
        for (l in lastNames) {
            out += Contact("$f $l", phones.next())
            if (out.size >= n) break@loop
        }
    }
    return out
}