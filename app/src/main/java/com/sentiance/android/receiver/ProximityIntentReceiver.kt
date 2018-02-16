package com.sentiance.android.receiver

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.location.LocationManager
import com.sentiance.android.R
import com.sentiance.android.ui.map.MapsActivity

/**
 * Proximity broadcast receiver which notify user enter or exit the area surrounding the location.
 */
class ProximityIntentReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val key = LocationManager.KEY_PROXIMITY_ENTERING
        val entering = intent!!.getBooleanExtra(key, false)
        if (entering) {
            // entered surrounding the location
            notify(context, context!!.getString(R.string.app_name), context.getString(R.string.user_entered))
        } else {
            // exited surrounding the location
            notify(context, context!!.getString(R.string.app_name), context.getString(R.string.user_exited))
        }
    }

    /**
     * Send the notification when user enter or exit within defined radius and latitude/longitude
     */
    private fun notify(context: Context?, title: String, message: String) {
        val intent = Intent(context, MapsActivity::class.java)
        val pIntent = PendingIntent.getActivity(context, System.currentTimeMillis().toInt(), intent, 0)

        // Build notification
        val notification = Notification.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pIntent).build()

        val notificationManager = context!!.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(0, notification)
    }

}