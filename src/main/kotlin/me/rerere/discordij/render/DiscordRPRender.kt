package me.rerere.discordij.render

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import de.jcm.discordgamesdk.Core
import de.jcm.discordgamesdk.CreateParams
import de.jcm.discordgamesdk.activity.Activity
import kotlinx.coroutines.*
import me.rerere.discordij.APPLICATION_ID
import java.util.*

@Service
class DiscordRPRender : Disposable {
    private val scope = CoroutineScope(
        Dispatchers.Default + SupervisorJob()
    )

    private val activityManager by lazy {
        val core = Core(
            CreateParams().apply {
                clientID = APPLICATION_ID
                flags = CreateParams.getDefaultFlags()
            }
        )
        scope.launch(Dispatchers.IO) {
            delay(1000L)
            core.runCallbacks()
        }
        core.activityManager()
    }

    fun updateActivity(activity: ActivityWrapper) {
        val activityNative = Activity()
        activityNative.state = activity.state
        activityNative.details = activity.details
        activity.startTimestamp?.let {
            activityNative.timestamps().start = Date(it).toInstant()
        }
        activity.endTimestamp?.let {
            activityNative.timestamps().end = Date(it).toInstant()
        }
        activityNative.assets().largeImage = activity.largeImageKey
        activityNative.assets().largeText = activity.largeImageText
        activityNative.assets().smallImage = activity.smallImageKey
        activityNative.assets().smallText = activity.smallImageText
        scope.launch(Dispatchers.IO) {
            activityManager.updateActivity(activityNative)
        }
    }

    fun clearActivity() {
        activityManager.clearActivity()
    }

    override fun dispose() {
        clearActivity()
        scope.cancel()
    }
}