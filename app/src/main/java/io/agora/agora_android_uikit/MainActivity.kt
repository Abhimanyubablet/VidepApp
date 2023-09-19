package io.agora.agora_android_uikit

import android.Manifest
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.Log.println
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import io.agora.agorauikit_android.AgoraButton
import io.agora.agorauikit_android.AgoraConnectionData
import io.agora.agorauikit_android.AgoraSettings
import io.agora.agorauikit_android.AgoraVideoViewer
import io.agora.agorauikit_android.requestPermission
import io.agora.rtc2.Constants

// Ask for Android device permissions at runtime.
private const val PERMISSION_REQ_ID = 22
private val REQUESTED_PERMISSIONS = arrayOf<String>(
    Manifest.permission.RECORD_AUDIO,
    Manifest.permission.CAMERA,
    Manifest.permission.WRITE_EXTERNAL_STORAGE
)

@ExperimentalUnsignedTypes
class MainActivity : AppCompatActivity() {
    var agView: AgoraVideoViewer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btn = findViewById<Button>(R.id.btn)
        btn.setOnClickListener {
            try {
                agView = AgoraVideoViewer(
                    this,
                    AgoraConnectionData("d9981cf4a63a44fc8c9e656940d4b2be"),
                    agoraSettings = settingsWithExtraButtons()
                )
            } catch (e: Exception) {
                Log.e("VideoUIKit App", "Could not initialize AgoraVideoViewer. Check your App ID is valid. ${e.message}")
                return@setOnClickListener
            }

            val set = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            addContentView(agView, set)

            if (AgoraVideoViewer.requestPermission(this)) {
                agView?.join("test", role = Constants.CLIENT_ROLE_BROADCASTER)
            } else {
                val joinButton = Button(this)
                joinButton.text = "Allow Camera and Microphone, then click here"
                joinButton.setOnClickListener {
                    if (AgoraVideoViewer.requestPermission(this)) {
                        (joinButton.parent as ViewGroup).removeView(joinButton)
                        agView?.join("test", role = Constants.CLIENT_ROLE_BROADCASTER)
                    }
                }
                joinButton.setBackgroundColor(Color.GREEN)
                joinButton.setTextColor(Color.BLACK)
                addContentView(
                    joinButton,
                    FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 300)
                )
            }
        }
    }

    private fun settingsWithExtraButtons(): AgoraSettings {
        val agoraSettings = AgoraSettings()

        val agBeautyButton = AgoraButton(this)
        agBeautyButton.clickAction = {
            it.isSelected = !it.isSelected
            agBeautyButton.setImageResource(
                if (it.isSelected) android.R.drawable.star_on else android.R.drawable.star_off
            )
            it.background.setTint(if (it.isSelected) Color.GREEN else Color.GRAY)
            agView?.agkit?.setBeautyEffectOptions(it.isSelected, agView?.beautyOptions)
        }
        agBeautyButton.setImageResource(android.R.drawable.star_off)

        agoraSettings.extraButtons = mutableListOf(agBeautyButton)

        return agoraSettings
    }
}