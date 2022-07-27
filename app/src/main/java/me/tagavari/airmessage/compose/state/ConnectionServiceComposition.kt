package me.tagavari.airmessage.compose.state

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.runtime.*
import me.tagavari.airmessage.connection.ConnectionManager
import me.tagavari.airmessage.data.SharedPreferencesManager
import me.tagavari.airmessage.enums.ProxyType
import me.tagavari.airmessage.helper.ConnectionServiceLaunchHelper
import me.tagavari.airmessage.service.ConnectionService
import me.tagavari.airmessage.service.ConnectionService.ConnectionBinder

val LocalConnectionManager = compositionLocalOf<ConnectionManager?> { null }

/**
 * Binds an activity to the connection services, and exposes the
 * connection manager via [LocalConnectionManager]
 */
@Composable
fun ConnectionServiceComposition(
	activity: Activity,
	body: @Composable () -> Unit
) {
	var connectionManager by remember {
		mutableStateOf<ConnectionManager?>(null)
	}
	
	DisposableEffect(activity) {
		var isServiceBound = false
		
		//Create the service connection
		val serviceConnection = object : ServiceConnection {
			override fun onServiceConnected(name: ComponentName, service: IBinder) {
				val binder = service as ConnectionBinder
				
				isServiceBound = true
				
				connectionManager = binder.connectionManager.also {
					it.connect()
				}
			}
			
			override fun onServiceDisconnected(name: ComponentName) {
				isServiceBound = false
				connectionManager = null
			}
		}
		
		//Make sure we have a configured connection
		if(SharedPreferencesManager.isConnectionConfigured(activity)) {
			//Start the persistent connection service if required
			if(SharedPreferencesManager.isConnectionConfigured(activity)
				&& SharedPreferencesManager.getProxyType(activity) == ProxyType.direct) {
				ConnectionServiceLaunchHelper.launchPersistent(activity)
			}
			
			//Bind to the connection service
			activity.bindService(
				Intent(activity, ConnectionService::class.java),
				serviceConnection,
				Context.BIND_AUTO_CREATE
			)
		}
		
		onDispose {
			//Unbind when we go out of scope
			if(isServiceBound) {
				activity.unbindService(serviceConnection)
			}
		}
	}
	
	CompositionLocalProvider(LocalConnectionManager provides connectionManager) {
		body()
	}
}
