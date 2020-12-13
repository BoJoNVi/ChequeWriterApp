package com.mcmkhianjuan.chequewriterapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    val REQUEST_CAMERA_PERMISSION = 101
    val REQUEST_READSTORAGE_PERMISSION = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var preferences = getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE)
        var firstTime = preferences.getString("FirstTimeInstall", "")

        if (firstTime.equals("Yes")) {
            //If application was opened for the firs time.
            moveToLoginPane()
        } else {
            var editor = preferences.edit()
            editor.putString("FirstTimeInstall", "Yes")
            editor.apply()
        }

        val proceedAnimatedButton: Animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val proceedTapped: Button = findViewById(R.id.proceedButton)
        proceedTapped.startAnimation(proceedAnimatedButton)
        proceedTapped.setOnClickListener {
            moveToLoginPane()
        }

        clickPermissionButtons()
    }

    fun moveToLoginPane() {
        // use an intent to travel from one activity to another.
        val intent = Intent(this, LoginActivity::class.java)
        this.finish()
        startActivity(intent)
    }

    private fun checkForPermission(permission: String, name: String, requestCode: Int) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            when {
                ContextCompat.checkSelfPermission(
                    applicationContext,
                    permission
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Toast.makeText(
                        applicationContext,
                        "$name permission granted.",
                        Toast.LENGTH_SHORT
                    ).show()

                }
                shouldShowRequestPermissionRationale(permission) -> showDialog(
                    permission,
                    name,
                    requestCode
                )
                // Check for permission, if its not been granted, its going to ask for the permission
                else -> ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)

            }

        }
        val proceedTapped: Button = findViewById(R.id.proceedButton)
        proceedTapped.isEnabled = true
    }

    private fun clickPermissionButtons() {
        val cameraButtonClicked: Button = findViewById(R.id.requestCameraPermissionButton)
        cameraButtonClicked.setOnClickListener {
            checkForPermission(Manifest.permission.CAMERA, "camera", REQUEST_CAMERA_PERMISSION)

        }

        val readStorageButtonClicked: Button = findViewById(R.id.accessReadPermissionButton)
        readStorageButtonClicked.setOnClickListener {
            checkForPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                "read/write external storage",
                REQUEST_READSTORAGE_PERMISSION
            )
        }
        val proceedAnimatedButton: Animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        cameraButtonClicked.startAnimation(proceedAnimatedButton)
        readStorageButtonClicked.startAnimation(proceedAnimatedButton)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        fun innerCheck(name: String) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(applicationContext, "$name permission refused", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(applicationContext, "$name permission granted", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> innerCheck("camera")
            REQUEST_READSTORAGE_PERMISSION -> innerCheck("read/write external storage")
        }

    }

    private fun showDialog(permission: String, name: String, requestCode: Int) {
        val builder = AlertDialog.Builder(this)

        builder.apply {
            setMessage("Permission to access your $name is required to use this Application")
            setTitle("Permission required")
            setPositiveButton("OK") { _, _ ->
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(permission),
                    requestCode
                )
            }
        }
        val dialog = builder.create()
        dialog.show()

    }
}
