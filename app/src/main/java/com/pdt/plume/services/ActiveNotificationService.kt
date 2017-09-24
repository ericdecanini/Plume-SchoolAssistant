package com.pdt.plume.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.support.annotation.Nullable
import android.support.v4.app.NotificationCompat
import android.util.Log

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.pdt.plume.*
import com.pdt.plume.R
import com.pdt.plume.data.DbContract
import com.pdt.plume.data.DbHelper
import java.util.*

class ActiveNotificationService : Service() {

    val LOG_TAG = ActiveNotificationService::class.java.simpleName
    private val mBinder = LocalBinder()

    override fun onBind(intent: Intent): IBinder? = mBinder

    inner class LocalBinder : Binder() {
        internal val service: ActiveNotificationService
            get() = this@ActiveNotificationService
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
                    .addChildEventListener(object : ChildEventListener {
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

                        override fun onChildRemoved(p0: DataSnapshot?) {}
                    })

            // Send the notifications for the shared tasks
            ref.child("users").child(mUserId).child("tasks").addChildEventListener(object : ChildEventListener {
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
                                ref.child("users").child(sharer).addListenerForSingleValueEvent(object : ValueEventListener {
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

                override fun onChildRemoved(p0: DataSnapshot?) {}
            })

            // Send the notifications for due tasks
            ref.child("users").child(mUserId).child("tasks").addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(p0: DataSnapshot?, p1: String?) {
                    val key = p0!!.key
                    val duedate = p0.child("duedate").value as Long
                    val c = Calendar.getInstance()
                    c.timeInMillis = duedate
                    val tomorrow = Calendar.getInstance()
                    tomorrow.set(Calendar.DAY_OF_MONTH, tomorrow.get(Calendar.DAY_OF_MONTH + 1))

                    val c1 = Calendar.getInstance()
                    val year = c1.get(Calendar.YEAR)
                    val month = c1.get(Calendar.MONTH)
                    val day = c1.get(Calendar.DAY_OF_MONTH)

                    val preferences = PreferenceManager.getDefaultSharedPreferences(this@ActiveNotificationService)
                    if (preferences.getString("duetask$key", "") != "$year$month$day"
                            && preferences.getBoolean(getString(R.string.KEY_SETTINGS_TASK_NOTIFICATION), false)) {
                        if (c.get(Calendar.DAY_OF_MONTH) == tomorrow.get(Calendar.DAY_OF_MONTH)) {
                            sendDueTaskNotification(-1, p0.key, p0.child("title").value as String,
                                    p0.child("icon").value as String)
                            preferences.edit().putString("duetask$key", "$year$month$day").apply()
                        }


                    }
                }

                override fun onCancelled(p0: DatabaseError?) {
                }

                override fun onChildMoved(p0: DataSnapshot?, p1: String?) {
                }

                override fun onChildChanged(p0: DataSnapshot?, p1: String?) {
                }

                override fun onChildRemoved(p0: DataSnapshot?) {
                }

            })
        } else {
            // Send the notification for due tasks
            val dbHelper = DbHelper(this)
            val cursor = dbHelper.taskData
            for (i in 0 until cursor.count) {
                cursor.moveToPosition(i)
                val id = cursor.getInt(cursor.getColumnIndex(DbContract.TasksEntry._ID))
                val icon = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_ICON))
                val title = cursor.getString(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_TITLE))
                val duedate = cursor.getLong(cursor.getColumnIndex(DbContract.TasksEntry.COLUMN_DUEDATE))
                val c = Calendar.getInstance()
                c.timeInMillis = duedate
                val tomorrow = Calendar.getInstance()
                tomorrow.set(Calendar.DAY_OF_MONTH, tomorrow.get(Calendar.DAY_OF_MONTH + 1))

                val c1 = Calendar.getInstance()
                val year = c1.get(Calendar.YEAR)
                val month = c1.get(Calendar.MONTH)
                val day = c1.get(Calendar.DAY_OF_MONTH)

                val preferences = PreferenceManager.getDefaultSharedPreferences(this@ActiveNotificationService)
                if (preferences.getString("duetask$id", "") != "$year$month$day"
                        && preferences.getBoolean(getString(R.string.KEY_SETTINGS_TASK_NOTIFICATION), false)) {
                    if (c.get(Calendar.DAY_OF_MONTH) == tomorrow.get(Calendar.DAY_OF_MONTH)) {
                        sendDueTaskNotification(id, "", title, icon)
                        preferences.edit().putString("duetask$id", "$year$month$day").apply()
                    }
                }
            }
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
            resultIntent.putExtra("notransition", "notransition")
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

    fun sendDueTaskNotification(id: Int?, firebaseId: String?, name: String, icon: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            val iconBitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.parse(icon))
            val mBuilder = object : NotificationCompat.Builder(this) {}
                    .setContentTitle(getString(R.string.notification_task_due_text))
                    .setContentText(getString(R.string.notification_task_due_title, name))
                    .setSmallIcon(R.drawable.ic_assignment)
                    .setLargeIcon(iconBitmap)
                    .setPriority(android.support.v7.app.NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(Notification.DEFAULT_ALL)
            val resultIntent = Intent(this, TasksDetailActivity::class.java)
            resultIntent.putExtra("icon", icon)
            resultIntent.putExtra("notransition", "notransition")
            if (firebaseId != "")
                resultIntent.putExtra("id", firebaseId)
            else
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

    fun debounceDay(tag: String): Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this@ActiveNotificationService)
        val lastCheckDate = preferences.getLong("lastCheckDate$tag", 0)
        val c = Calendar.getInstance()
        c.timeInMillis = lastCheckDate
        val day = c.get(Calendar.DAY_OF_MONTH)
        val cc = Calendar.getInstance()
        cc.timeInMillis = System.currentTimeMillis()
        val cDay = cc.get(Calendar.DAY_OF_MONTH)

        preferences.edit().putLong("lastCheckDate$tag", System.currentTimeMillis()).apply()

        return cDay > day + 1
    }

}
