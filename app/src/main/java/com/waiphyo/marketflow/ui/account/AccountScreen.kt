@file:Suppress("ktlint:standard:function-naming")

package com.waiphyo.marketflow.ui.account

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.waiphyo.marketflow.ui.theme.PriceTrackerTheme

@Composable
fun AccountScreen(
    userName: String,
    userEmail: String,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    paddingValues: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .padding(bottom = 10.dp)
                            .size(76.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Profile image",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier
                                .padding(18.dp)
                                .size(40.dp),
                        )
                    }
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = userEmail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Appearance",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "Theme",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                ThemeOptionRow(
                    label = "Light",
                    selected = !isDarkTheme,
                    onClick = { onThemeChange(false) },
                )
                ThemeOptionRow(
                    label = "Dark",
                    selected = isDarkTheme,
                    onClick = { onThemeChange(true) },
                )
            }
        }
    }
}

@Composable
private fun ThemeOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Preview(showBackground = true, name = "Account Light")
@Composable
private fun AccountScreenPreview() {
    PriceTrackerTheme(darkTheme = false) {
        AccountScreen(
            userName = "MarketFlow User",
            userEmail = "user@marketflow.app",
            isDarkTheme = false,
            onThemeChange = {},
            paddingValues = PaddingValues(0.dp),
        )
    }
}

@Preview(
    showBackground = true,
    name = "Account Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun AccountScreenDarkPreview() {
    PriceTrackerTheme(darkTheme = true) {
        AccountScreen(
            userName = "MarketFlow User",
            userEmail = "user@marketflow.app",
            isDarkTheme = true,
            onThemeChange = {},
            paddingValues = PaddingValues(0.dp),
        )
    }
}
