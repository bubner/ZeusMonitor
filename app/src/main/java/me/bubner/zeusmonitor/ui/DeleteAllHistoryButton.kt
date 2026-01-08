package me.bubner.zeusmonitor.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun DeleteAllHistoryButton(onClick: () -> Unit = {}) {
    val confirmDialogOpen = remember { mutableStateOf(false) }

    if (confirmDialogOpen.value) {
        AlertDialog(
            icon = { Icon(imageVector = Icons.Default.Warning, contentDescription = "Warning") },
            title = { Text("Delete all?") },
            text = { Text("This will delete all previous Zeus Monitor captures. Are you sure?") },
            onDismissRequest = { confirmDialogOpen.value = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDialogOpen.value = false
                        onClick()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { confirmDialogOpen.value = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    )
                ) {
                    Text("Dismiss")
                }
            }
        )
    }

    FloatingActionButton(
        onClick = { confirmDialogOpen.value = true },
        containerColor = MaterialTheme.colorScheme.error,
        contentColor = MaterialTheme.colorScheme.onError
    ) {
        Icon(
            imageVector = Icons.Default.DeleteForever,
            contentDescription = "Delete All"
        )
    }
}
