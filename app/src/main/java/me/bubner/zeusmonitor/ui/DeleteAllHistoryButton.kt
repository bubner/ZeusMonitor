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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview

@Suppress("AssignedValueIsNeverRead")
@Preview
@Composable
fun DeleteAllHistoryButton(onClick: () -> Unit = {}) {
    var confirmDialogOpen by remember { mutableStateOf(false) }

    if (confirmDialogOpen) {
        AlertDialog(
            icon = { Icon(imageVector = Icons.Default.Warning, contentDescription = "Warning") },
            title = { Text("Delete all?") },
            text = { Text("This will delete all previous Zeus Monitor captures. Are you sure?") },
            onDismissRequest = { confirmDialogOpen = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDialogOpen = false
                        onClick()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    )
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { confirmDialogOpen = false },
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
        onClick = { confirmDialogOpen = true },
        containerColor = MaterialTheme.colorScheme.error,
        contentColor = MaterialTheme.colorScheme.onError
    ) {
        Icon(
            imageVector = Icons.Default.DeleteForever,
            contentDescription = "Delete All"
        )
    }
}
