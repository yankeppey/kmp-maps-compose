package eu.buney.sample

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Represents a screen/demo that can be navigated to.
 */
enum class DemoScreen(val title: String, val description: String) {
    BasicMap(
        title = "Basic Map",
        description = "Map with markers, overlays, and camera controls",
    ),
}

/**
 * A group of related demo screens, displayed as an expandable section.
 */
class DemoGroup(
    val title: String,
    val demos: List<DemoScreen>,
)

/**
 * All demo groups displayed on the main screen.
 */
val allDemoGroups = listOf(
    DemoGroup(
        title = "Maps",
        demos = listOf(
            DemoScreen.BasicMap,
        ),
    ),
)

/**
 * Displays a collapsible list of demo groups. Only one group is expanded at a time,
 * creating an accordion-style UI.
 *
 * Inspired by the DemoList in android-maps-compose.
 */
@Composable
fun DemoList(
    onDemoClick: (DemoScreen) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expandedGroup by remember { mutableStateOf<DemoGroup?>(null) }

    LazyColumn(modifier = modifier) {
        items(allDemoGroups) { group ->
            val isExpanded = expandedGroup == group
            Column {
                GroupHeaderItem(group, isExpanded) {
                    expandedGroup = if (isExpanded) null else group
                }

                AnimatedVisibility(visible = isExpanded) {
                    Column(
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                    ) {
                        group.demos.forEach { demo ->
                            DemoItem(demo) { onDemoClick(demo) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupHeaderItem(
    group: DemoGroup,
    isExpanded: Boolean,
    onGroupClicked: () -> Unit,
) {
    Card(
        colors = if (isExpanded) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onGroupClicked() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = group.title,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (isExpanded) {
                    Icons.Default.KeyboardArrowUp
                } else {
                    Icons.Default.KeyboardArrowDown
                },
                contentDescription = null
            )
        }
    }
}

@Composable
private fun DemoItem(
    demo: DemoScreen,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = demo.title, fontWeight = FontWeight.Bold)
            Text(text = demo.description)
        }
    }
}
