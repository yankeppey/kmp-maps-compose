package eu.buney.sample

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    MaterialTheme {
        var currentScreen by remember { mutableStateOf<DemoScreen?>(null) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(currentScreen?.title ?: "Maps Compose Multiplatform")
                    },
                    navigationIcon = {
                        if (currentScreen != null) {
                            IconButton(onClick = { currentScreen = null }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            val contentModifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)

            when (currentScreen) {
                null -> {
                    Column(
                        modifier = contentModifier,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        DemoList(
                            onDemoClick = { currentScreen = it }
                        )
                    }
                }
                DemoScreen.BasicMap -> MapScreen(modifier = contentModifier)
            }
        }
    }
}
