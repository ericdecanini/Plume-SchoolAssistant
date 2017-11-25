package com.pdt.plume

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_match_class.*

class MatchClassActivity : AppCompatActivity() {

    val LOG_TAG = MatchClassActivity::class.java.simpleName

    // Firebase Data
    lateinit var mFirebaseUser: FirebaseUser
    lateinit var mUserId: String

    lateinit var name: String
    lateinit var icon: String
    lateinit var flavour: String
    lateinit var requestingUserId: String

    var enabledA = -1
    var enabledB = -1

    val mMatchingList = ArrayList<MatchingClass>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_class)

        // Initialise Firebase
        mFirebaseUser = FirebaseAuth.getInstance().currentUser!!
        mUserId = mFirebaseUser.uid

        requestingUserId = intent.getStringExtra("id")
        name = intent.getStringExtra("name")
        icon = intent.getStringExtra("icon")
        flavour = if (intent.hasExtra("flavour"))
            intent.getStringExtra("flavour")
        else ""

        val classesRef = FirebaseDatabase.getInstance().reference
                .child("users").child(mUserId).child("requests")
                .child(requestingUserId).child("classes")

        // Initialise the theme variables
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val mPrimaryColor = preferences.getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), resources.getColor(R.color.colorPrimary))
        val hsv = FloatArray(3)
        val tempColor = mPrimaryColor
        Color.colorToHSV(tempColor, hsv)
        hsv[2] *= 0.8f // value component
        val mDarkColor = Color.HSVToColor(hsv)
        val backgroundColor = preferences.getInt(getString(R.string.KEY_THEME_BACKGROUND_COLOUR), resources.getColor(R.color.backgroundColor))

        val textColor = preferences.getInt(getString(R.string.KEY_THEME_TEXT_COLOUR), resources.getColor(R.color.gray_900))
        Color.colorToHSV(textColor, hsv)
        hsv[2] *= 0.8f
        val textColor800 = Color.HSVToColor(hsv)

        Color.colorToHSV(textColor, hsv)
        hsv[2] *= 0.6f
        val saturatedTextColor = Color.HSVToColor(hsv)

        supportActionBar!!.setBackgroundDrawable(ColorDrawable(mPrimaryColor))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = mDarkColor
        }

        master_layout.setBackgroundColor(backgroundColor)
        header.setTextColor(textColor800)

        // Inflate the class list first
        val mClassList = ArrayList<MatchingClass>()

        val mClassAdapter = MatchingClassAdapter(this@MatchClassActivity, R.layout.list_item_class_small, mClassList)
        val mMatchingAdapter = MatchingClassAdapter(this, R.layout.list_item_matching_class, mMatchingList)

        window.decorView.findViewById(android.R.id.content).isEnabled = false
        classList.adapter = mClassAdapter

        FirebaseDatabase.getInstance().reference
                .child("users").child(mUserId).child("classes")
                .addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot?) {
                        window.decorView.findViewById(android.R.id.content).isEnabled = true

                        for (x in p0!!.children)
                            mClassList.add(MatchingClass(
                                    x.child("icon").value as String, x.key, "", ""))
                        mClassAdapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(p0: DatabaseError?) {
                        AlertDialog.Builder(this@MatchClassActivity)
                                .setTitle(getString(R.string.error))
                                .setMessage(getString(R.string.check_internet))
                                .setPositiveButton(getString(R.string.ok), null)
                                .show()
                        startActivity(Intent(this@MatchClassActivity, MainActivity::class.java))
                    }
                })

        classList.setOnItemClickListener { adapterView, view, i, l ->
            for (i in 0.. mClassList.size - 1)
                mClassList[i].activated = 0
            mClassAdapter.notifyDataSetChanged()

            if (enabledB == i)
                enabledB = -1

            else {
                enabledB = i
                mClassList[i].activated = 1
                mClassAdapter.notifyDataSetChanged()
            }

            // Check for a match
            if (enabledA > -1) {
                mMatchingList[enabledA].title = mClassList[enabledB].title
                mMatchingList[enabledA].icon = mClassList[enabledB].icon

                classesRef.child(mMatchingList[enabledA].originalTitle).child("newtitle")
                        .setValue(mMatchingList[enabledA].title)
                classesRef.child(mMatchingList[enabledA].originalTitle).child("newicon")
                        .setValue(mMatchingList[enabledA].icon)


                for (i in 0.. mClassList.size - 1)
                    mClassList[i].activated = 0
                for (i in 0.. mMatchingList.size - 1)
                    mMatchingList[i].activated = 0

                mMatchingAdapter.notifyDataSetChanged()
                mClassAdapter.notifyDataSetChanged()

                if (enabledA < mMatchingList.size - 1 && mMatchingList[enabledA+1].title == "") {
                    enabledA++
                    mMatchingList[enabledA].activated = 1
                    mMatchingAdapter.notifyDataSetChanged()
                } else enabledA = -1

                enabledB = -1
            }
        }

        // Now inflate the unmatched classes
        mMatchingList.addAll(intent.getParcelableArrayListExtra("matchingList"))
        mismatchedList.adapter = mMatchingAdapter

        mismatchedList.setOnItemClickListener { adapterView, view, i, l ->
            for (i in 0.. mMatchingList.size - 1)
                mMatchingList[i].activated = 0
                mMatchingAdapter.notifyDataSetChanged()

            if (enabledA == i)
                enabledA = -1

            else {
                enabledA = i
                mMatchingList[i].activated = 1
                mMatchingAdapter.notifyDataSetChanged()
            }


            // Check for a match
            if (enabledB > -1) {
                mMatchingList[enabledA].title = mClassList[enabledB].title
                mMatchingList[enabledA].icon = mClassList[enabledB].icon

                classesRef.child(mMatchingList[enabledA].originalTitle).child("newtitle")
                        .setValue(mMatchingList[enabledA].title)
                classesRef.child(mMatchingList[enabledA].originalTitle).child("newicon")
                        .setValue(mMatchingList[enabledA].icon)

                for (i in 0.. mClassList.size - 1)
                    mClassList[i].activated = 0
                for (i in 0.. mMatchingList.size - 1)
                    mMatchingList[i].activated = 0

                mMatchingAdapter.notifyDataSetChanged()
                mClassAdapter.notifyDataSetChanged()

                // Go to the next item if applicable
                if (enabledA < mMatchingList.size - 1 && mMatchingList[enabledA+1].title == "") {
                    enabledA++
                    mMatchingList[enabledA].activated = 1
                    mMatchingAdapter.notifyDataSetChanged()
                } else enabledA = -1

                enabledB = -1
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_confirm, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item!!.itemId == android.R.id.home) {
            finish()
            return true
        }

        if (item!!.itemId == R.id.action_done) {
            val unmatchedClasses = ArrayList<String>()
            for (i in 0.. mMatchingList.size - 1) {
                if (mMatchingList[i].title == "")
                    unmatchedClasses.add(mMatchingList[i].title)
            }
            if (unmatchedClasses.isNotEmpty()) {
                AlertDialog.Builder(this)
                        .setTitle(getString(R.string.dialog_unmatched_title))
                        .setMessage(getString(R.string.dialog_unmatched_text))
                        .setNegativeButton(getString(R.string.cancel), null)
                        .setPositiveButton(getString(R.string.ok), DialogInterface.OnClickListener { dialogInterface, i ->
                            val intent = Intent(this, AcceptPeerActivity::class.java)
                            intent.putExtra("requestingUserId", requestingUserId)
                            intent.putExtra("title", name)
                            intent.putExtra("flavour", flavour)
                            intent.putExtra("icon", icon)
                            startActivity(intent)
                        }).show()
            } else {
                val intent = Intent(this, AcceptPeerActivity::class.java)
                intent.putExtra("requestingUserId", requestingUserId)
                intent.putExtra("title", name)
                intent.putExtra("flavour", flavour)
                intent.putExtra("icon", icon)
                startActivity(intent)
                return true
            }

        }

        return true
    }
}
