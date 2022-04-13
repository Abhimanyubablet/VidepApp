package io.agora.agorauikit_android

import android.content.Context
import io.agora.rtm.*
import java.util.logging.Level
import java.util.logging.Logger

@ExperimentalUnsignedTypes
class AgoraRtmController(
    context: Context,
    private val hostView: AgoraVideoViewer
) {
    private var generatedRtmId: String? = null
    var isLoggedIn: Boolean = false
    private var isInRtmChannel: Boolean = false

    companion object {}

    fun initAgoraRtm(context: Context) {
        try {
            this.hostView.agRtmClient =
                RtmClient.createInstance(
                    context,
                    hostView.connectionData.appId,
                    this.hostView.agoraRtmClientHandler
                )
        } catch (e: Exception) {
            Logger.getLogger("AgoraUIKit")
                .log(Level.SEVERE, "Failed to initialize Agora RTM SDK. Error: $e")
        }
    }

    fun loginToRtm() {
        if (this.hostView.connectionData.rtmId.isNullOrEmpty()) {
            generateRtmId()
        }
        if (!isLoggedIn && hostView.isAgRrtmClientInitialized()) {
            Logger.getLogger("AgoraUIKit")
                .log(Level.SEVERE, "Trying to do RTM login")
            this.hostView.agRtmClient.login(
                this.hostView.connectionData.rtmToken,
                this.hostView.connectionData.rtmId,
                object : ResultCallback<Void?> {
                    override fun onSuccess(responseInfo: Void?) {
                        isLoggedIn = true
                        Logger.getLogger("AgoraUIKit")
                            .log(Level.INFO, "RTM user logged in successfully")
                        if (!isInRtmChannel) {
                            createRtmChannel()
                        }
                    }

                    override fun onFailure(errorInfo: ErrorInfo) {
                        Logger.getLogger("AgoraUIKit")
                            .log(Level.SEVERE, "RTM user login failed. Error: $errorInfo")
                    }
                })
        } else {
            Logger.getLogger("AgoraUIKit")
                .log(Level.INFO, "RTM user already logged in")
        }
    }

    fun createRtmChannel() {
        try {
            this.hostView.connectionData.rtmChannelName =
                this.hostView.connectionData.rtmChannelName?.let { this.hostView.connectionData.rtmChannelName }
                    ?: let { this.hostView.connectionData.channel }

            this.hostView.agRtmChannel =
                this.hostView.agRtmClient.createChannel(
                    this.hostView.connectionData.rtmChannelName,
                    this.hostView.agoraRtmChannelHandler
                )
        } catch (e: RuntimeException) {
            Logger.getLogger("AgoraUIKit").log(Level.SEVERE, "Failed to create RTM channel. Error: $e")
        }

        if (hostView.isAgRtmChannelInitialized()) {
            joinRtmChannel()
        }
    }

    private fun joinRtmChannel() {
        this.hostView.agRtmChannel.join(object : ResultCallback<Void> {
            override fun onSuccess(responseInfo: Void?) {
                isInRtmChannel = true
                Logger.getLogger("AgoraUIKit").log(Level.SEVERE, "RTM Channel Joined Successfully")
                if (isInRtmChannel) {
                    sendUserData(toChannel = true, hostView = hostView)
                }
            }

            override fun onFailure(errorInfo: ErrorInfo) {
                isInRtmChannel = false
                Logger.getLogger("AgoraUIKit")
                    .log(Level.SEVERE, "Failed to join RTM Channel. Error: $errorInfo")
            }
        })
    }

    fun generateRtmId() {
        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

        generatedRtmId = (1..10)
            .map { i -> kotlin.random.Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("");

        Logger.getLogger("AgoraUIKit").log(Level.SEVERE, "Generated RTM ID: $generatedRtmId")

        this.hostView.connectionData.rtmId = generatedRtmId
    }

}