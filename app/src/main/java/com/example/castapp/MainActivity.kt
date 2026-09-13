package com.example.castapp

import android.os.Bundle
import android.view.Menu
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener

class MainActivity : AppCompatActivity() {

    private var castContext: CastContext? = null
    private var castSession: CastSession? = null
    private lateinit var tvStatus: TextView
    private lateinit var btnCast: Button

    private val sessionManagerListener = object : SessionManagerListener<CastSession> {
        override fun onSessionStarted(session: CastSession, sessionId: String) {
            castSession = session
            tvStatus.text = "Status: Connected to ${session.castDevice?.friendlyName}"
        }

        override fun onSessionEnded(session: CastSession, error: Int) {
            castSession = null
            tvStatus.text = "Status: Disconnected"
        }

        override fun onSessionResumed(session: CastSession, wasSuspended: Boolean) {
            castSession = session
            tvStatus.text = "Status: Connected to ${session.castDevice?.friendlyName}"
        }

        override fun onSessionStartFailed(session: CastSession, error: Int) {}
        override fun onSessionEnding(session: CastSession) {}
        override fun onSessionResuming(session: CastSession, sessionId: String) {}
        override fun onSessionResumeFailed(session: CastSession, error: Int) {}
        override fun onSessionSuspended(session: CastSession, reason: Int) {}
        override fun onSessionStarting(session: CastSession) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        btnCast = findViewById(R.id.btnCast)

        castContext = CastContext.getSharedInstance(this)

        btnCast.setOnClickListener {
            playMediaOnReceiver()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.main_menu, menu)
        CastButtonFactory.setUpMediaRouteButton(applicationContext, menu, R.id.media_route_menu_item)
        return true
    }

    override fun onResume() {
        super.onResume()
        castContext?.sessionManager?.addSessionManagerListener(
            sessionManagerListener,
            CastSession::class.java
        )
        castSession = castContext?.sessionManager?.currentCastSession
    }

    override fun onPause() {
        super.onPause()
        castContext?.sessionManager?.removeSessionManagerListener(
            sessionManagerListener,
            CastSession::class.java
        )
    }

    private fun playMediaOnReceiver() {
        val session = castSession
        if (session == null || !session.isConnected) {
            Toast.makeText(this, "Please connect to a Cast device first!", Toast.LENGTH_SHORT).show()
            return
        }

        val remoteMediaClient = session.remoteMediaClient ?: return

        val movieMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE).apply {
            putString(MediaMetadata.KEY_TITLE, "Big Buck Bunny Sample")
            putString(MediaMetadata.KEY_SUBTITLE, "Public Domain Stream")
        }

        val mediaInfo = MediaInfo.Builder("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            .setContentType("video/mp4")
            .setMetadata(movieMetadata)
            .build()

        val mediaLoadRequestData = MediaLoadRequestData.Builder()
            .setMediaInfo(mediaInfo)
            .setAutoplay(true)
            .build()

        remoteMediaClient.load(mediaLoadRequestData)
        Toast.makeText(this, "Sending stream to TV...", Toast.LENGTH_SHORT).show()
    }
}
