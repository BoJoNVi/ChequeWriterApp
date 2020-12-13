package com.mcmkhianjuan.chequewriterapp


import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextClock
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DashboardActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var backPressedTime: Long = 0
    private var backToast: Toast? = null

    override fun onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            backToast!!.cancel()
            super.onBackPressed()
            return
        } else {
            backToast =
                Toast.makeText(baseContext, "Press back again to exit", Toast.LENGTH_SHORT)
            backToast!!.show()
        }
        backPressedTime = System.currentTimeMillis()
    }

    // Check connection when user wants to log out so the server will notice
    private fun checkConnectivity() {
        val manager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = manager.activeNetworkInfo

        if (activeNetwork == null) {
            val dialogBuilder = AlertDialog.Builder(this)
            Intent(this, MainActivity::class.java)
            // set message of alert dialog
            dialogBuilder.setMessage("Make sure that WI-FI or mobile data is turned on, then try again")
                // if the dialog is cancelable
                .setCancelable(false)
                // positive button text and action
                .setPositiveButton("Retry") { _: DialogInterface, _: Int ->
                    recreate()
                }
                // negative button text and action
                .setNegativeButton("Cancel") { _: DialogInterface, _: Int ->
                    recreate()
                }

            // create dialog box
            val alert = dialogBuilder.create()
            // set title for alert dialog box
            alert.setTitle("No Internet Connection")
            alert.setIcon(R.mipmap.ic_launcher)
            // show alert dialog
            alert.show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        checkConnectivity()
        auth = FirebaseAuth.getInstance()
        
        val logOutInDashBoard: Button = findViewById(R.id.logOutDashboardButton)

        logOutInDashBoard.setOnClickListener {
            checkConnectivity()
            FirebaseAuth.getInstance().signOut()
            val goLoginPageIntent = Intent(this, LoginActivity::class.java)
            goLoginPageIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(goLoginPageIntent)
            finish()
        }

        val calendar: Calendar = Calendar.getInstance()
        val currentDate: String = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.time)
        val textViewDate = findViewById<TextView>(R.id.DateTextView)
        textViewDate.text = currentDate


    }
}