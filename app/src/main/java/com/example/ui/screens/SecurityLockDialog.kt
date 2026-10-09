package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.security.SecurityLockType
import com.example.ui.theme.ExpenseCoral
import com.example.ui.theme.PrimaryEmerald

@Composable
fun ConfigureSecurityLockDialog(
    currentLockType: SecurityLockType,
    onDismiss: () -> Unit,
    onSavePin: (currentCred: String?, newPin: String) -> Boolean,
    onSavePassword: (currentCred: String?, newPassword: String) -> Boolean,
    onRemoveLock: (currentCred: String) -> Boolean
) {
    var selectedTab by remember {
        mutableStateOf(if (currentLockType == SecurityLockType.PASSWORD) 1 else 0)
    }

    val hasExistingLock = currentLockType != SecurityLockType.NONE

    var currentCredential by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }

    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var currentPasswordVisible by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp)
                .testTag("configure_security_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = PrimaryEmerald,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (hasExistingLock) "Update App Lock" else "Set App Lock",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tab Selector: PIN vs Password
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = PrimaryEmerald,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                            errorMessage = null
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Pin, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("4-Digit PIN", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            errorMessage = null
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Password", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Error Banner
                if (errorMessage != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ExpenseCoral.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = ExpenseCoral,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = ExpenseCoral,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // If user already has a lock, require current credentials
                if (hasExistingLock) {
                    OutlinedTextField(
                        value = currentCredential,
                        onValueChange = {
                            currentCredential = it
                            errorMessage = null
                        },
                        label = {
                            Text(
                                if (currentLockType == SecurityLockType.PIN) "Current 4-Digit PIN"
                                else "Current Password"
                            )
                        },
                        singleLine = true,
                        visualTransformation = if (currentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (currentLockType == SecurityLockType.PIN) KeyboardType.NumberPassword else KeyboardType.Password
                        ),
                        trailingIcon = {
                            IconButton(onClick = { currentPasswordVisible = !currentPasswordVisible }) {
                                Icon(
                                    imageVector = if (currentPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryEmerald,
                            cursorColor = PrimaryEmerald
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("current_security_credential_input")
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // PIN Tab Form
                if (selectedTab == 0) {
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = {
                            if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                newPin = it
                                errorMessage = null
                            }
                        },
                        label = { Text("New 4-Digit PIN") },
                        placeholder = { Text("e.g. 1234") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryEmerald,
                            cursorColor = PrimaryEmerald
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_pin_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = {
                            if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                confirmPin = it
                                errorMessage = null
                            }
                        },
                        label = { Text("Confirm 4-Digit PIN") },
                        placeholder = { Text("Re-enter 4 digits") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryEmerald,
                            cursorColor = PrimaryEmerald
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("confirm_pin_input")
                    )
                } else {
                    // Password Tab Form
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = {
                            newPassword = it
                            errorMessage = null
                        },
                        label = { Text("New Password") },
                        placeholder = { Text("At least 4 characters") },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryEmerald,
                            cursorColor = PrimaryEmerald
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_password_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            errorMessage = null
                        },
                        label = { Text("Confirm Password") },
                        placeholder = { Text("Re-enter password") },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryEmerald,
                            cursorColor = PrimaryEmerald
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("confirm_password_input")
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions: Save or Remove Lock
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (hasExistingLock) {
                        OutlinedButton(
                            onClick = {
                                if (currentCredential.isBlank()) {
                                    errorMessage = "Please enter your current PIN/password to remove lock"
                                    return@OutlinedButton
                                }
                                val ok = onRemoveLock(currentCredential)
                                if (!ok) {
                                    errorMessage = "Incorrect current PIN or password"
                                } else {
                                    onDismiss()
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseCoral),
                            border = BorderStroke(1.dp, ExpenseCoral.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("remove_lock_button")
                        ) {
                            Text("Remove", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Button(
                        onClick = {
                            if (hasExistingLock && currentCredential.isBlank()) {
                                errorMessage = "Please enter current credentials first"
                                return@Button
                            }

                            if (selectedTab == 0) {
                                // PIN
                                if (newPin.length != 4) {
                                    errorMessage = "PIN must be exactly 4 digits"
                                    return@Button
                                }
                                if (newPin != confirmPin) {
                                    errorMessage = "PINs do not match"
                                    return@Button
                                }
                                val ok = onSavePin(
                                    if (hasExistingLock) currentCredential else null,
                                    newPin
                                )
                                if (!ok) {
                                    errorMessage = "Failed to update PIN. Check current credentials."
                                } else {
                                    onDismiss()
                                }
                            } else {
                                // Password
                                if (newPassword.length < 4) {
                                    errorMessage = "Password must be at least 4 characters"
                                    return@Button
                                }
                                if (newPassword != confirmPassword) {
                                    errorMessage = "Passwords do not match"
                                    return@Button
                                }
                                val ok = onSavePassword(
                                    if (hasExistingLock) currentCredential else null,
                                    newPassword
                                )
                                if (!ok) {
                                    errorMessage = "Failed to update password. Check current credentials."
                                } else {
                                    onDismiss()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("save_security_lock_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Lock", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
