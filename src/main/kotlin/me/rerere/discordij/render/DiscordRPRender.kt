package me.rerere.discordij.render

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import de.jcm.discordgamesdk.ActivityManager
import de.jcm.discordgamesdk.Core
import de.jcm.discordgamesdk.CreateParams
import de.jcm.discordgamesdk.activity.Activity
import kotlinx.coroutines.*
import me.rerere.discordij.APPLICATION_ID
import me.rerere.discordij.DiscordIJ
import java.util.*

@Service
class DiscordRPRender : Disposable {
    private lateinit var activityManager: ActivityManager
    private val scope = CoroutineScope(
        Dispatchers.Default + SupervisorJob()
    )

    // TODO: Maybe let use to re-init discord rp if they want
    fun init() = kotlin.runCatching {
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
        activityManager = core.activityManager()
    }.also {
        DiscordIJ.logger.info("DiscordRP init result: ${it.isSuccess}")
    }

    init {
        init()
    }

    fun updateActivity(activity: ActivityWrapper) = kotlin.runCatching {
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
            kotlin.runCatching {
                activityManager.updateActivity(activityNative)
            }
                .onFailure {
                    DiscordIJ.logger.warn("Failed to update activity: " + it.message)
                }
        }
    }

    fun clearActivity() = kotlin.runCatching {
        activityManager.clearActivity()
    }

    override fun dispose() {
        clearActivity()
        scope.cancel()
    }
}