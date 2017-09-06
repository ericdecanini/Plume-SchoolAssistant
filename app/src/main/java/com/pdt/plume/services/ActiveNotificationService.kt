package com.pdt.plume.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.support.v4.app.NotificationCompat
import android.util.Log

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.pdt.plume.*
import com.pdt.plume.R
import java.util.*

class ActiveNotificationService : Service() {

    val LOG_TAG = ActiveNotificationService::class.java.simpleName

    override fun onBind(intent: Intent): IBinder? {
        // TODO: Return the communication channel to the service.
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
        val mFirebaseUser = FirebaseAuth.getInstance().currentUser
        if (mFirebaseUser != null) {
            val mUserId = mFirebaseUser.uid
            val ref = FirebaseDatabase.getInstance().reference
            debounce("requests")
            debounce("sharedtasks")

            // Send the notification for the requests
            ref.child("users").child(mUserId).child("requests")
                    .addChildEventListener(object: ChildEventListener {
                        override fun onCancelled(p0: DatabaseError?) {}
                        override fun onChildMoved(p0: DataSnapshot?, p1: String?) {}
                        override fun onChildChanged(p0: DataSnapshot?, p1: String?) {}
                        override fun onChildAdded(p0: DataSnapshot?, p1: String?) {
                            if (debounce("requests")) {
                                if (p0 != null) {
                                    if (p0.childrenCount == 1.toLong())
                                        for (requestSnapshot in p0.children) {
                                            if (requestSnapshot.child("nickname").value != null && requestSnapshot.child("icon").value != null
                                                    && requestSnapshot.child("flavour").value != null)
                                                sendRequestNotificationSingle(p0.child("nickname").getValue(String::class.java),
                                                        requestSnapshot.child("icon").getValue(String::class.java),
                                                        requestSnapshot.child("flavour").getValue(String::class.java))
                                            else Log.w(LOG_TAG, "Nickname or Icon returned null")
                                        }
                                    else sendRequestNotificationMultiple(p0.childrenCount.toInt())
                                }
                            }
                        }
                        override fun onChildRemoved(p0: DataSnapshot?) {}})

            // Send the notifications for the shared tasks
            ref.child("users").child(mUserId).child("tasks").addChildEventListener(object: ChildEventListener {
                override fun onCancelled(p0: DatabaseError?) {}
                override fun onChildMoved(p0: DataSnapshot?, p1: String?) {}
                override fun onChildChanged(p0: DataSnapshot?, p1: String?) {}
                override fun onChildAdded(p0: DataSnapshot?, p1: String?) {
                    if (debounce("sharedtasks")) {
                        if (p0 != null) {
                            val sharerObject = p0.child("sharer").value
                            var sharer = ""
                            if (sharerObject != null)
                                sharer = sharerObject as String
                            if (sharer != "") {
                                val title = p0.child("title").value as String
                                val icon = p0.child("icon").value as String
                                val key = p0.key
                                ref.child("users").child(sharer).addListenerForSingleValueEvent(object: ValueEventListener {
                                    override fun onCancelled(p0: DatabaseError?) {}
                                    override fun onDataChange(p0: DataSnapshot?) {
                                        if (p0 != null) {
                                            val sharerName = p0.child("nickname").value as String
                                            sendSharedTaskNotification(sharerName, title, icon, key)
                                        }
                                    }
                                })
                            }
                        }
                    }
                }
                override fun onChildRemoved(p0: DataSnapshot?) {} })
        }
    }

    fun sendSharedTaskNotification(sharer: String, taskTitle: String, icon: String, id: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            val iconBitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.parse(icon))
            val mBuilder = object : NotificationCompat.Builder(this) {}
                    .setContentTitle(getString(R.string.notification_shared_task_title, taskTitle, sharer))
                    .setContentText(getString(R.string.notification_shared_task_text))
                    .setSmallIcon(R.drawable.ic_assignment)
                    .setLargeIcon(iconBitmap)
                    .setPriority(android.support.v7.app.NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(Notification.DEFAULT_ALL)
            val resultIntent = Intent(this, TasksDetailActivity::class.java)
            resultIntent.putExtra("sharer", sharer)
            resultIntent.putExtra("icon", icon)
            resultIntent.putExtra("id", id)
            val stackBuilder = TaskStackBuilder.create(this)
            stackBuilder.addParentStack(TasksDetailActivity::class.java)
            stackBuilder.addNextIntent(resultIntent)
            val resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            mBuilder.setContentIntent(resultPendingIntent)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(Utility.generateUniqueId(), mBuilder.build())
        }
    }

    fun sendRequestNotificationSingle(name: String, icon: String, flavour: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            val iconBitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.parse(icon))
            val mBuilder = object : NotificationCompat.Builder(this) {}
                    .setContentTitle(getString(R.string.request_notification_single_title, name))
                    .setContentText(getString(R.string.request_notification_single_text, name))
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setLargeIcon(iconBitmap)
                    .setPriority(android.support.v7.app.NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(Notification.DEFAULT_ALL)
            val resultIntent = Intent(this, AcceptPeerActivity::class.java)
            resultIntent.putExtra("name", name)
            resultIntent.putExtra("icon", icon)
            resultIntent.putExtra("flavour", flavour)
            val stackBuilder = TaskStackBuilder.create(this)
            stackBuilder.addParentStack(AcceptPeerActivity::class.java)
            stackBuilder.addNextIntent(resultIntent)
            val resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            mBuilder.setContentIntent(resultPendingIntent)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(Utility.generateUniqueId(), mBuilder.build())
        }
    }

    fun sendRequestNotificationMultiple(c: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            val iconBitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.parse("android.resource://com.pdt.plume/drawable/art_profile_default"))
            val mBuilder = object : NotificationCompat.Builder(this) {}
                    .setContentTitle(getString(R.string.request_notification_multiple_title, c.toString()))
                    .setContentText(getString(R.string.request_notification_multiple_text))
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setLargeIcon(iconBitmap)
                    .setPriority(android.support.v7.app.NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(Notification.DEFAULT_ALL)
            val resultIntent = Intent(this, RequestsActivity::class.java)
            val stackBuilder = TaskStackBuilder.create(this)
            stackBuilder.addParentStack(AcceptPeerActivity::class.java)
            stackBuilder.addNextIntent(resultIntent)
            val resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            mBuilder.setContentIntent(resultPendingIntent)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(Utility.generateUniqueId(), mBuilder.build())
        }
    }

    fun debounce(tag: String): Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this@ActiveNotificationService)
        val lastCheckDate = preferences.getLong("lastCheckDate$tag", 0)
        val c = Calendar.getInstance()
        c.timeInMillis = lastCheckDate
        val second = c.get(Calendar.SECOND)
        val cc = Calendar.getInstance()
        cc.timeInMillis = System.currentTimeMillis()
        val cSecond = cc.get(Calendar.SECOND)

        preferences.edit().putLong("lastCheckDate$tag", System.currentTimeMillis()).apply()

        return cSecond > second + 5
    }

}
