package com.pdt.plume

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ProgressBar

import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.ArrayList

import android.R.attr.editable

class UserSearchActivity : AppCompatActivity() {

    // General Variables
    internal var LOG_TAG = UserSearchActivity::class.java.simpleName

    // Firebase Variables
    lateinit internal var mDatabase: DatabaseReference
    lateinit internal var mFirebaseAuth: FirebaseAuth
    internal var mFirebaseUser: FirebaseUser? = null
    lateinit internal var mUserId: String

    // UI Elements
    lateinit internal var listView: ListView
    lateinit internal var spinner: ProgressBar
    lateinit internal var searchBar: EditText
    lateinit internal var textWatcher: TextWatcher

    // UI Data
    lateinit internal var adapter: PeerAdapter

    // Arrays
    internal var searchResults = ArrayList<Peer>()
    internal var searchResultIDs = ArrayList<String>()
    internal var userNames = ArrayList<String>()
    internal var userList = ArrayList<Peer>()
    internal var userIDs = ArrayList<String>()
    internal var peerIDs = ArrayList<String>()

    // Theme Variables
    internal var mPrimaryColor: Int = 0
    internal var mDarkColor: Int = 0
    internal var mSecondaryColor: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_search)

        // Get references to the views
        val toolbar = findViewById(R.id.toolbar) as LinearLayout
        val homeButton = findViewById(R.id.home) as ImageView
        searchBar = findViewById(R.id.search_bar) as EditText
        val clearButton = findViewById(R.id.clear) as ImageView
        spinner = findViewById(R.id.progressBar) as ProgressBar
        spinner.visibility = View.GONE

        listView = findViewById(R.id.listView) as ListView
        adapter = PeerAdapter(this, R.layout.list_item_search_result, searchResults)
        listView.adapter = adapter

        // Set the listeners of the views
        homeButton.setOnClickListener { onBackPressed() }
        listView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            val activity: Class<*>
            if (resources.getBoolean(R.bool.isTablet))
                activity = AddPeerActivityTablet::class.java
            else
                activity = AddPeerActivity::class.java
            val intent = Intent(this@UserSearchActivity, activity)
            intent.putExtra("id", searchResultIDs[i])
            startActivity(intent)
        }

        // This listener sets the behavior of the clear button's visibility
        textWatcher = object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (charSequence.toString().length == 0) {
                    clearButton.visibility = View.GONE
                    searchResults.clear()
                    searchResultIDs.clear()
                    adapter.notifyDataSetChanged()
                } else {
                    clearButton.visibility = View.VISIBLE
                    ASyncUserSearch(charSequence.toString())
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        }
        searchBar.addTextChangedListener(textWatcher)

        clearButton.setOnClickListener { searchBar.setText("") }

        // Initialise Firebase
        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseUser = mFirebaseAuth.currentUser

        if (mFirebaseUser != null) {
            mDatabase = FirebaseDatabase.getInstance().reference
            mUserId = mFirebaseUser!!.uid

            // Store the ids of all the current peers
            FirebaseDatabase.getInstance().reference.child("users")
                    .child(mUserId).child("peers").addListenerForSingleValueEvent(object: ValueEventListener {

                override fun onDataChange(p0: DataSnapshot?) {
                    if (p0 != null)
                    for (peerSnapshot in p0.children)
                        peerIDs.add(peerSnapshot.key)

                    // Store the nicknames of all users
                    FirebaseDatabase.getInstance().reference.child("users")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    for (userSnapshot in dataSnapshot.children) {
                                        if (peerIDs.contains(userSnapshot.key)) continue
                                        val name = userSnapshot.child("nickname").getValue(String::class.java)
                                        var iconUri: String? = userSnapshot.child("icon").getValue(String::class.java)
                                        if (name != null && iconUri != null) {
                                            iconUri = iconUri.replace("icon", userSnapshot.key)
                                            if (!iconUri.contains("android.resource://")) {
                                                val iconUriSplit = iconUri.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                                                var file = File(filesDir, iconUriSplit[iconUriSplit.size - 1])
                                                if (file.exists()) {
                                                    // ADD THE LIST ITEM HERE
                                                    userIDs.add(userSnapshot.key)
                                                    userList.add(Peer(iconUri, name))
                                                    userNames.add(name)
                                                } else {
                                                    // File doesn't exist: Download from storage
                                                    val storage = FirebaseStorage.getInstance()
                                                    val storageRef = storage.reference
                                                    val iconRef = storageRef.child(userSnapshot.key).child("icon")

                                                    file = File(filesDir, userSnapshot.key + ".jpg")

                                                    val finalIconUri = iconUri
                                                    iconRef.getFile(file).addOnSuccessListener {
                                                        // ADD THE LIST ITEM HERE
                                                        userIDs.add(userSnapshot.key)
                                                        userList.add(Peer(finalIconUri, name))
                                                        userNames.add(name)
                                                    }
                                                }
                                            } else {
                                                // Drawable being used
                                                userIDs.add(userSnapshot.key)
                                                userList.add(Peer(iconUri, name))
                                                userNames.add(name)
                                            }
                                        }
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {}
                            })
                }

                override fun onCancelled(p0: DatabaseError?) {

                }

            })

        } else
            loadLogInView()

        // Initialise the theme variables
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        mPrimaryColor = preferences.getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), resources.getColor(R.color.colorPrimary))
        val hsv = FloatArray(3)
        val tempColor = mPrimaryColor
        Color.colorToHSV(tempColor, hsv)
        hsv[2] *= 0.8f
        mDarkColor = Color.HSVToColor(hsv)
        mSecondaryColor = preferences.getInt(getString(R.string.KEY_THEME_SECONDARY_COLOR), resources.getColor(R.color.colorAccent))
        val backgroundColor = preferences.getInt(getString(R.string.KEY_THEME_BACKGROUND_COLOUR), resources.getColor(R.color.backgroundColor))
        findViewById(R.id.activity_user_search).setBackgroundColor(backgroundColor)

        toolbar.setBackgroundColor(mPrimaryColor)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = mDarkColor
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            searchBar.backgroundTintList = ColorStateList.valueOf(mSecondaryColor)

    }

    private fun ASyncUserSearch(text: String) {
        searchResults.clear()
        searchResultIDs.clear()
        for (i in userNames.indices) {
            if (searchResultApproved(userNames[i], text, 0) && userIDs[i] != mUserId) {
                searchResults.add(userList[i])
                searchResultIDs.add(userIDs[i])
            }
        }
        for (i in userNames.indices) {
            if (searchResultApproved(userNames[i], text, 1) && !searchResultIDs.contains(userIDs[i])
                    && userIDs[i] != mUserId) {
                searchResults.add(userList[i])
                searchResultIDs.add(userIDs[i])
            }
        }
        for (i in userNames.indices) {
            if (searchResultApproved(userNames[i], text, 2) && !searchResultIDs.contains(userIDs[i])
                    && userIDs[i] != mUserId) {
                searchResults.add(userList[i])
                searchResultIDs.add(userIDs[i])
            }
        }
        for (i in userNames.indices) {
            if (searchResultApproved(userNames[i], text, 3) && !searchResultIDs.contains(userIDs[i])
                    && userIDs[i] != mUserId) {
                searchResults.add(userList[i])
                searchResultIDs.add(userIDs[i])
            }
        }
        adapter.notifyDataSetChanged()
    }

    internal fun searchResultApproved(nickname: String, text: String, l: Int): Boolean {
        val n = nickname.toLowerCase()
        val t = text.toLowerCase()
        if (n == t && l == 0) {
            Log.v(LOG_TAG, "returning 1")
            return true
        }
        if (n.startsWith(t) && l == 1) {
            Log.v(LOG_TAG, "returning 2, n = $n, t = $t")
            return true
        }

        val splitN = n.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val splitT = t.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (i in splitN.indices) {
            for (i1 in splitT.indices) {
                if (splitN[i] == splitT[i1] && l == 2) {
                    Log.v(LOG_TAG, "returning 3")
                    return true
                }
                if (splitN[i].startsWith(splitT[i1]) && l == 3) {
                    Log.v(LOG_TAG, "returning 4")
                    return true
                }
            }
        }

        return false
    }

    private fun loadLogInView() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        searchBar.removeTextChangedListener(textWatcher)
        startActivity(intent)
    }

}