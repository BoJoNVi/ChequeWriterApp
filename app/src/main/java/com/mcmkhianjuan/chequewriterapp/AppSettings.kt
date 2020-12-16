package com.mcmkhianjuan.chequewriterapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AppSettings : AppCompatActivity() {

    val firebaseStoreDatabaseInit = Firebase.firestore
    private lateinit var authFireStore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_settings)

        val browseImageButtonTapped: Button = findViewById(R.id.browseImageButton)
        browseImageButtonTapped.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    // Permission Denied
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                    // Show PopUp to Request Runtime permission
                    requestPermissions(permissions, PERMISSION_CODE)
                } else {
                    // Permission already granted
                    pickImageFromGallery()

                }
            } else {
                // System OS is LESS THAN Marshmallow / Android L and below
                pickImageFromGallery()
            }
        }

    }

    private fun pickImageFromGallery() {
        // Intent to Pick Image
        val imagePickIntent = Intent(Intent.ACTION_PICK)
        imagePickIntent.type = "image/*"
        startActivityForResult(imagePickIntent, IMAGE_PICK_CODE)

    }

    companion object {
        // Image Pick Code
        private const val IMAGE_PICK_CODE = 1000

        // Permission Code
        private const val PERMISSION_CODE = 1001
    }

    // Handle permission result

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission from Popup Granted
                    pickImageFromGallery()
                } else {
                    // Permission from Popup denied
                    Toast.makeText(
                        this,
                        "Permission of Storage Access is denied",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val businessLogoSelect: ImageView = findViewById(R.id.businessLogoSelectedImage)
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            businessLogoSelect.setImageURI(data?.data)
        }
    }


}