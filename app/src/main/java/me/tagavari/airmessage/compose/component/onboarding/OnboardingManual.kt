package me.tagavari.airmessage.compose.component.onboarding

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.tagavari.airmessage.R
import me.tagavari.airmessage.compose.component.AlertCard
import me.tagavari.airmessage.compose.ui.theme.AirMessageAndroidTheme
import me.tagavari.airmessage.constants.ExternalLinkConstants
import me.tagavari.airmessage.enums.ConnectionErrorCode
import me.tagavari.airmessage.helper.ErrorDetailsAction
import me.tagavari.airmessage.helper.ErrorDetailsHelper
import me.tagavari.airmessage.helper.IntentHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingManual(
	modifier: Modifier = Modifier,
	state: OnboardingManualState,
	onConnect: () -> Unit,
	onReset: () -> Unit,
	onFinish: () -> Unit,
) {
	var inputAddress by remember { mutableStateOf("") }
	var inputFallbackAddress by remember { mutableStateOf("") }
	var inputPassword by remember { mutableStateOf("") }
	
	Column(
		modifier = modifier
			.verticalScroll(rememberScrollState())
			.padding(24.dp),
		verticalArrangement = Arrangement.spacedBy(20.dp)
	) {
		//Server address
		Column(modifier = Modifier.fillMaxWidth()) {
			TextField(
				modifier = Modifier.fillMaxWidth(),
				value = inputAddress,
				onValueChange = { inputAddress = it },
				singleLine = true,
				enabled = state == OnboardingManualState.Idle,
				label = {
					Text(stringResource(R.string.message_setup_connect_address))
				},
				keyboardOptions = KeyboardOptions(
					keyboardType = KeyboardType.Uri
				)
			)
			
			InputSupportingTextConnection(
				modifier = Modifier.align(Alignment.End),
				show = state is OnboardingManualState.Connected,
				connected = state is OnboardingManualState.Connected && !state.fallback
			)
		}
		
		//Fallback address
		Column(modifier = Modifier.fillMaxWidth()) {
			TextField(
				modifier = Modifier.fillMaxWidth(),
				value = inputFallbackAddress,
				onValueChange = { inputFallbackAddress = it },
				singleLine = true,
				enabled = state == OnboardingManualState.Idle,
				label = {
					Text(stringResource(R.string.message_setup_connect_fallbackaddress))
				},
				keyboardOptions = KeyboardOptions(
					keyboardType = KeyboardType.Uri
				)
			)
			
			InputSupportingTextConnection(
				modifier = Modifier.align(Alignment.End),
				show = state is OnboardingManualState.Connected && state.fallback,
				connected = true
			)
		}
		
		//Password
		var passwordHidden by remember { mutableStateOf(true) }
		TextField(
			modifier = Modifier.fillMaxWidth(),
			value = inputPassword,
			onValueChange = { inputPassword = it },
			singleLine = true,
			enabled = state == OnboardingManualState.Idle,
			label = {
				Text(stringResource(R.string.message_setup_connect_password))
			},
			visualTransformation = if(passwordHidden) PasswordVisualTransformation() else VisualTransformation.None,
			keyboardOptions = KeyboardOptions(
				keyboardType = KeyboardType.Password
			),
			trailingIcon = {
				IconButton(onClick = { passwordHidden = !passwordHidden }) {
					Icon(
						imageVector = if(passwordHidden) Icons.Filled.Visibility
						else Icons.Filled.VisibilityOff,
						contentDescription = stringResource(
							if(passwordHidden) R.string.action_showpassword
							else R.string.action_hidepassword
						)
					)
				}
			}
		)
		
		when(state) {
			is OnboardingManualState.Idle, is OnboardingManualState.Error -> {
				if(state is OnboardingManualState.Error) {
					val errorDetails = remember(state.errorCode) {
						ErrorDetailsHelper.getErrorDetails(state.errorCode, true)
					}
					
					val context = LocalContext.current
					fun recoverError() {
						val button = errorDetails.button ?: return
						when(button.action) {
							ErrorDetailsAction.UPDATE_APP -> {
								Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}"))
									.let { context.startActivity(it) }
							}
							ErrorDetailsAction.UPDATE_SERVER -> {
								IntentHelper.launchUri(context, ExternalLinkConstants.serverUpdateAddress)
							}
							else -> {}
						}
					}
					
					AlertCard(
						icon = {
							Icon(
								imageVector = Icons.Outlined.CloudOff,
								contentDescription = null
							)
						},
						message = {
							Text(stringResource(errorDetails.label))
						},
						button = errorDetails.button?.let { button -> {
							TextButton(onClick = { recoverError() }) {
								Text(stringResource(button.label))
							}
						} }
					)
				}
				
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.End
				) {
					Button(onClick = onConnect) {
						Text(stringResource(R.string.action_checkconnection))
					}
				}
			}
			is OnboardingManualState.Connecting -> {
				Column {
					Text(
						text = stringResource(R.string.progress_connectionverification),
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
					
					Spacer(modifier = Modifier.height(8.dp))
					
					LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
				}
			}
			is OnboardingManualState.Connected -> {
				Card(modifier = Modifier.fillMaxWidth()) {
					Row(modifier = Modifier.padding(16.dp)) {
						Icon(
							imageVector = Icons.Outlined.CheckCircleOutline,
							contentDescription = null
						)
						
						Spacer(modifier = Modifier.width(8.dp))
						
						Text(
							modifier = modifier.padding(top = 1.dp),
							text = state.deviceName?.let { name -> stringResource(R.string.message_connection_connectedcomputer, name) }
								?: stringResource(R.string.message_connection_connected)
						)
					}
				}
				
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.End
				) {
					TextButton(onClick = onReset) {
						Text(stringResource(R.string.action_back))
					}
					
					Spacer(modifier = Modifier.width(20.dp))
					
					Button(onClick = onFinish) {
						Text(stringResource(R.string.action_done))
					}
				}
			}
		}
	}
}

sealed class OnboardingManualState {
	object Idle : OnboardingManualState()
	object Connecting : OnboardingManualState()
	data class Error(@ConnectionErrorCode val errorCode: Int) : OnboardingManualState()
	data class Connected(val deviceName: String?, val fallback: Boolean) : OnboardingManualState()
}

@Preview(name = "Initial state", widthDp = 384)
@Composable
private fun PreviewOnboardingWelcomeInitial() {
	AirMessageAndroidTheme {
		Surface(
			color = MaterialTheme.colorScheme.background
		) {
			OnboardingManual(
				state = OnboardingManualState.Idle,
				onConnect = {},
				onReset = {},
				onFinish = {}
			)
		}
	}
}

@Preview(name = "Connecting", widthDp = 384)
@Composable
private fun PreviewOnboardingWelcomeConnecting() {
	AirMessageAndroidTheme {
		Surface(
			color = MaterialTheme.colorScheme.background
		) {
			OnboardingManual(
				state = OnboardingManualState.Connecting,
				onConnect = {},
				onReset = {},
				onFinish = {}
			)
		}
	}
}

@Preview(name = "Connected", widthDp = 384)
@Composable
private fun PreviewOnboardingWelcomeConnected() {
	AirMessageAndroidTheme {
		Surface(
			color = MaterialTheme.colorScheme.background
		) {
			OnboardingManual(
				state = OnboardingManualState.Connected(deviceName = "My Computer", fallback = true),
				onConnect = {},
				onReset = {},
				onFinish = {}
			)
		}
	}
}

@Preview(name = "Error", widthDp = 384)
@Composable
private fun PreviewOnboardingWelcomeError() {
	AirMessageAndroidTheme {
		Surface(
			color = MaterialTheme.colorScheme.background
		) {
			OnboardingManual(
				state = OnboardingManualState.Error(errorCode = ConnectionErrorCode.unauthorized),
				onConnect = {},
				onReset = {},
				onFinish = {}
			)
		}
	}
}
