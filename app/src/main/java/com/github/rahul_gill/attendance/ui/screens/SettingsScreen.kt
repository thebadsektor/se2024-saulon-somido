package com.github.rahul_gill.attendance.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.github.rahul_gill.attendance.R
import com.github.rahul_gill.attendance.db.DatabaseHelper
import com.github.rahul_gill.attendance.prefs.PreferenceManager
import com.github.rahul_gill.attendance.prefs.UnsetClassesBehavior
import com.github.rahul_gill.attendance.ui.comps.*
import com.github.rahul_gill.attendance.util.Constants
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onGoBack: () -> Unit,
    dbHelper: DatabaseHelper
) {
    val context = LocalContext.current
    var isChangePasswordDialogShowing by remember { mutableStateOf(false) }

    val followSystemColor = PreferenceManager.followSystemColors.asState()
    val seedColor = PreferenceManager.colorSchemeSeed.asState()
    val theme = PreferenceManager.themeConfig.asState()
    val unsetClassBehaviour = PreferenceManager.unsetClassesBehavior.asState()
    val darkThemeType = PreferenceManager.darkThemeType.asState()
    val dateFormatOption = PreferenceManager.defaultDateFormatPref.asState()
    val timeFormatOption = PreferenceManager.defaultTimeFormatPref.asState()
    val defaultHomeTabOption = PreferenceManager.defaultHomeTabPref.asState()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(text = stringResource(id = R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onGoBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.go_back_screen)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
    ) { innerPadding ->
        val screenHeight = LocalConfiguration.current.screenHeightDp
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .heightIn(min = screenHeight.dp)
        ) {
            // Theme and Display Settings
            PreferenceGroupHeader(title = stringResource(id = R.string.look_and_feel))

            ListPreference(
                title = stringResource(id = R.string.app_theme),
                items = ThemeConfig.entries.toList(),
                leadingIcon = {
                    Icon(painter = painterResource(id = R.drawable.baseline_palette_24), contentDescription = null)
                },
                selectedItemIndex = ThemeConfig.entries.indexOf(theme.value),
                onItemSelection = { PreferenceManager.themeConfig.setValue(ThemeConfig.entries[it]) },
                itemToDescription = { themeIndex ->
                    stringResource(
                        id = when (ThemeConfig.entries[themeIndex]) {
                            ThemeConfig.FollowSystem -> R.string.follow_system
                            ThemeConfig.Light -> R.string.light
                            ThemeConfig.Dark -> R.string.dark
                        }
                    )
                }
            )

            SwitchPreference(
                title = stringResource(R.string.pure_black_background),
                isChecked = darkThemeType.value == DarkThemeType.Black,
                leadingIcon = {
                    Icon(painter = painterResource(id = R.drawable.baseline_contrast_24), contentDescription = null)
                },
                onCheckedChange = { checked ->
                    PreferenceManager.darkThemeType.setValue(if (checked) DarkThemeType.Black else DarkThemeType.Dark)
                }
            )

            SwitchPreference(
                title = stringResource(R.string.follow_system_colors),
                isChecked = followSystemColor.value,
                leadingIcon = {
                    Icon(painter = painterResource(id = R.drawable.baseline_format_color_fill_24), contentDescription = null)
                },
                onCheckedChange = { PreferenceManager.followSystemColors.setValue(it) }
            )

            AnimatedVisibility(visible = !followSystemColor.value) {
                GenericPreference(
                    title = stringResource(R.string.custom_color_scheme_seed),
                    summary = stringResource(id = R.string.custom_color_scheme_seed_summary),
                    onClick = { /* Color picker logic */ },
                    leadingIcon = {
                        Icon(painterResource(id = R.drawable.baseline_colorize_24), contentDescription = null)
                    },
                    trailingContent = {
                        Surface(
                            modifier = Modifier
                                .background(seedColor.value, shape = CircleShape)
                                .size(24.dp),
                            color = seedColor.value,
                            shape = CircleShape,
                            content = {}
                        )
                    }
                )
            }

            // Behavior Settings
            PreferenceGroupHeader(title = stringResource(id = R.string.behaviour))

            val unsetClassesBehaviorValues = UnsetClassesBehavior.entries.toTypedArray().toList()
            ListPreference(
                title = stringResource(id = R.string.unset_classes_behaviour),
                items = unsetClassesBehaviorValues,
                leadingIcon = { Icon(painterResource(id = R.drawable.baseline_check_24), contentDescription = null) },
                selectedItemIndex = unsetClassesBehaviorValues.indexOf(unsetClassBehaviour.value),
                onItemSelection = { PreferenceManager.unsetClassesBehavior.setValue(unsetClassesBehaviorValues[it]) },
                itemToDescription = { index ->
                    stringResource(
                        id = when (unsetClassesBehaviorValues[index]) {
                            UnsetClassesBehavior.ConsiderPresent -> R.string.consider_as_presents
                            UnsetClassesBehavior.ConsiderAbsent -> R.string.consider_as_absents
                            UnsetClassesBehavior.None -> R.string.do_nothing
                        }
                    )
                }
            )

            // Date and Time Formatting
            PreferenceGroupHeader(title = stringResource(id = R.string.date_time_formatting))

            val timeFormatOptions = stringArrayResource(id = R.array.time_format_choices).toList()
            ListPreference(
                title = stringResource(id = R.string.time_format),
                items = timeFormatOptions,
                leadingIcon = { Icon(painter = painterResource(id = R.drawable.baseline_access_time_24), contentDescription = null) },
                selectedItemIndex = timeFormatOptions.indexOf(timeFormatOption.value),
                onItemSelection = { selected -> PreferenceManager.defaultTimeFormatPref.setValue(timeFormatOptions[selected]) },
                itemToDescription = { tabOptionIndex ->
                    DateTimeFormatter.ofPattern(timeFormatOptions[tabOptionIndex]).format(LocalTime.now())
                }
            )

            val dateFormatOptions = stringArrayResource(id = R.array.date_format_choices).toList()
            ListPreference(
                title = stringResource(id = R.string.date_format),
                items = dateFormatOptions,
                leadingIcon = { Icon(painter = painterResource(id = R.drawable.baseline_calendar_today_24), contentDescription = null) },
                selectedItemIndex = dateFormatOptions.indexOf(dateFormatOption.value),
                onItemSelection = { selected -> PreferenceManager.defaultDateFormatPref.setValue(dateFormatOptions[selected]) },
                itemToDescription = { tabOptionIndex ->
                    DateTimeFormatter.ofPattern(dateFormatOptions[tabOptionIndex]).format(LocalDate.now())
                }
            )

            // Security Settings - Change Password Option
            PreferenceGroupHeader(title = stringResource(id = R.string.security))

            GenericPreference(
                title = stringResource(R.string.change_password),
                onClick = { isChangePasswordDialogShowing = true },
                leadingIcon = { Icon(painterResource(id = R.drawable.baseline_lock_24), contentDescription = null) }
            )

            if (isChangePasswordDialogShowing) {
                ChangePasswordDialog(
                    dbHelper = dbHelper,
                    onDismissRequest = { isChangePasswordDialogShowing = false }
                )
            }

            // About and Other Information
            PreferenceGroupHeader(title = stringResource(id = R.string.about))

            val context = LocalContext.current

            GenericPreference(
                title = stringResource(R.string.privacy_policy),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Constants.PRIVACY_POLICY_LINK))
                    context.startActivity(intent)
                },
                leadingIcon = { Icon(painterResource(id = R.drawable.baseline_privacy_tip_24), contentDescription = null) }
            )

            GenericPreference(
                title = stringResource(R.string.source_code),
                summary = Constants.GITHUB_APP_LINK,
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Constants.GITHUB_APP_LINK))
                    context.startActivity(intent)
                },
                leadingIcon = { Icon(painterResource(id = R.drawable.github), contentDescription = null) }
            )

            GenericPreference(
                title = stringResource(R.string.author_info),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Constants.GITHUB_USER_LINK))
                    context.startActivity(intent)
                },
                leadingIcon = { Icon(painterResource(id = R.drawable.baseline_person_24), contentDescription = null) }
            )
        }
    }
}

@Composable
fun ChangePasswordDialog(
    dbHelper: DatabaseHelper,
    onDismissRequest: () -> Unit
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Change Password") },
        text = {
            Column {
                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    label = { Text("Current Password") },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = showPassword,
                        onCheckedChange = { showPassword = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Show Password")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // Validate old password, new password, and confirm password
                    if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                        Toast.makeText(context, "All fields must be filled", Toast.LENGTH_SHORT).show()
                    } else if (!dbHelper.checkPassword(oldPassword)) {
                        Toast.makeText(context, "Incorrect current password", Toast.LENGTH_SHORT).show()
                    } else if (newPassword != confirmPassword) {
                        Toast.makeText(context, "New password and confirmation do not match", Toast.LENGTH_SHORT).show()
                    } else {
                        // Update password in the database
                        dbHelper.setPassword(newPassword)
                        Toast.makeText(context, "Password changed successfully", Toast.LENGTH_SHORT).show()
                        onDismissRequest()
                    }
                }
            ) {
                Text("Change Password")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}