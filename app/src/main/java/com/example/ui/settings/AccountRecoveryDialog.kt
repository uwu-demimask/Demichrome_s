package com.example.ui.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.RetroButton
import com.example.ui.components.RetroOutlineButton
import com.example.ui.focus.FocusViewModel
import com.example.ui.theme.RetroTheme

/**
 * Account Recovery Dialog.
 * Allows users to recover account access and reset credentials persistently.
 */
@Composable
fun AccountRecoveryDialog(
    viewModel: FocusViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val palette = RetroTheme.palette

    var recoveryEmail by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var recoveryError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 440.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, palette.surfaceBorder, RoundedCornerShape(16.dp))
                .testTag("account_recovery_dialog"),
            color = palette.surface
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockReset,
                            contentDescription = null,
                            tint = palette.accent,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "RECOVER ACCOUNT",
                            color = palette.textPrimary,
                            fontFamily = palette.fontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = palette.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Enter your registered Demichrome account email and a new password to restore persistent cloud identity.",
                    color = palette.textSecondary,
                    fontFamily = palette.fontFamily,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = recoveryEmail,
                    onValueChange = { recoveryEmail = it },
                    label = { Text("Account Email", fontFamily = palette.fontFamily, fontSize = 11.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("recovery_email_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = palette.textPrimary,
                        unfocusedTextColor = palette.textPrimary,
                        focusedBorderColor = palette.surfaceBorder,
                        unfocusedBorderColor = palette.surfaceBorder.copy(alpha = 0.5f),
                        focusedLabelColor = palette.textPrimary,
                        unfocusedLabelColor = palette.textSecondary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password (min 6 chars)", fontFamily = palette.fontFamily, fontSize = 11.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("recovery_new_password_input"),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = palette.textPrimary,
                        unfocusedTextColor = palette.textPrimary,
                        focusedBorderColor = palette.surfaceBorder,
                        unfocusedBorderColor = palette.surfaceBorder.copy(alpha = 0.5f),
                        focusedLabelColor = palette.textPrimary,
                        unfocusedLabelColor = palette.textSecondary
                    )
                )

                recoveryError?.let { err ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = err,
                        color = Color(0xFFEF5350),
                        fontFamily = palette.fontFamily,
                        fontSize = 11.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RetroOutlineButton(
                        text = "CANCEL",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )

                    RetroButton(
                        text = if (isLoading) "RESETTING..." else "RESTORE ACCESS",
                        onClick = {
                            if (recoveryEmail.isBlank()) {
                                recoveryError = "Please enter your account email."
                                return@RetroButton
                            }
                            if (newPassword.length < 6) {
                                recoveryError = "Password must be at least 6 characters."
                                return@RetroButton
                            }

                            isLoading = true
                            recoveryError = null
                            viewModel.recoverAccount(recoveryEmail, newPassword) { res ->
                                isLoading = false
                                if (res.isSuccess) {
                                    Toast.makeText(context, "Account recovered & authenticated!", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                } else {
                                    recoveryError = res.exceptionOrNull()?.message ?: "Recovery failed. Account not found."
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("recovery_submit_btn")
                    )
                }
            }
        }
    }
}
