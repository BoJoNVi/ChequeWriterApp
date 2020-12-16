package com.mcmkhianjuan.chequewriterapp


import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.lang.Exception

class SignupActivity : AppCompatActivity() {

    val firebaseStoreDatabaseInit = Firebase.firestore
    private lateinit var auth: FirebaseAuth


    // check internet connection to let server know that account will be registered
    private fun isInternetAvailable(context: Context): Boolean {
        var result = false
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            result = when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.run {
                connectivityManager.activeNetworkInfo?.run {
                    result = when (type) {
                        ConnectivityManager.TYPE_WIFI -> true
                        ConnectivityManager.TYPE_MOBILE -> true
                        ConnectivityManager.TYPE_ETHERNET -> true
                        else -> false
                    }
                }
            }
        }
        return result
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()

        val loginActivityTapped: Button = findViewById(R.id.loginActivityButton)
        val showHideButtonSignUpTapped: Button = findViewById(R.id.showHideSignupButton)
        val passwordFieldSignUpText: EditText = findViewById(R.id.passwordFieldSignUp)
        val passwordFieldConfirmSignUpText: EditText = findViewById(R.id.passwordFieldConfirmSignUp)
        val signUpAccountButtonTapped: Button = findViewById(R.id.signUpAccountButtonTrigger)


        showHideButtonSignUpTapped.setOnClickListener {
            if (showHideButtonSignUpTapped.text.toString() == "SHOW") {
                passwordFieldSignUpText.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
                passwordFieldConfirmSignUpText.transformationMethod =
                    PasswordTransformationMethod.getInstance()
                showHideButtonSignUpTapped.text = "HIDE"
            } else {
                passwordFieldSignUpText.transformationMethod =
                    PasswordTransformationMethod.getInstance()
                passwordFieldConfirmSignUpText.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
                showHideButtonSignUpTapped.text = "SHOW"
            }
        }

        signUpAccountButtonTapped.setOnClickListener {

            val emailText: EditText = findViewById(R.id.emailAddressSignUpField)
            val emailInput = emailText.text.toString()
            val passwordInput = passwordFieldSignUpText.text.toString()
            val confirmPasswordInput = passwordFieldConfirmSignUpText.text.toString()
            val fullNameText: EditText = findViewById(R.id.fullNameTextField)
            val fullNameInput = fullNameText.text.toString()


            if (isInternetAvailable(this)) {
                if (passwordInput != confirmPasswordInput) {
                    passwordFieldSignUpText.error = "Password do not match in your Confirm Password"
                    passwordFieldConfirmSignUpText.error =
                        "Confirm Password do not match in your Password"
                } else if (emailInput.trim().isEmpty() || passwordInput.trim()
                        .isEmpty() || confirmPasswordInput.trim().isEmpty() || fullNameInput.trim()
                        .isEmpty()
                ) {
                    Toast.makeText(this, "Please input all fields", Toast.LENGTH_SHORT)
                        .show()
                } else if (emailInput.trim().isNotEmpty() || passwordInput.trim()
                        .isNotEmpty() || confirmPasswordInput.trim()
                        .isNotEmpty() || fullNameInput.trim().isNotEmpty()
                ) {
                    registerUser()
                } else if (!isValidEmail(emailInput)) {
                    emailText.error = "Please input a valid Email Address"
                    return@setOnClickListener
                } else {
                    Toast.makeText(this, "Please input all required fields", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                val dialogBuilder = AlertDialog.Builder(this)
                Intent(this, MainActivity::class.java)
                // set message of alert dialog
                dialogBuilder.setMessage(
                    "Make sure that WI-FI or Mobile Data is turned on, then try again.\n" +
                            "You cannot Sign Up Account without Internet Connection."
                )
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

        loginActivityTapped.setOnClickListener {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }
    }

    private fun registerUser() {

        // Find ID in the xml file
        val emailText: EditText = findViewById(R.id.emailAddressSignUpField)
        val passwordFieldSignUpText: EditText = findViewById(R.id.passwordFieldSignUp)
        val fullNameText: EditText = findViewById(R.id.fullNameTextField)
        // Convert to String
        val emailInput = emailText.text.toString()
        val passwordInput = passwordFieldSignUpText.text.toString()
        val fullNameInput = fullNameText.text.toString()

//        val fullNameTextInputToString = fullNameTextInput.text.toString()

        auth.createUserWithEmailAndPassword(emailInput.trim(), passwordInput.trim())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    saveFirestoreDatabase(emailInput, fullNameInput)
                } else if (!task.isSuccessful) {
                    try {
                        throw task.exception!!
                    } catch (e: FirebaseAuthUserCollisionException) {
                        Toast.makeText(
                            this,
                            "The account [ $emailInput ] have already existed.",
                            Toast.LENGTH_SHORT
                        ).show()
                        emailText.requestFocus()
                    } catch (e: FirebaseAuthWeakPasswordException) {
                        Toast.makeText(
                            this,
                            "Weak Password. Input at-least 6 characters.",
                            Toast.LENGTH_SHORT
                        ).show()
                        passwordFieldSignUpText.requestFocus()
                    } catch (e: Exception) {
                        Log.e(this.toString(), e.message.toString())
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Account is unable to register. Please try again. \n" + task.exception,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    fun saveFirestoreDatabase(
        emailInput: String,
        fullNameInput: String,
    ) {
        // FireStore | Store Data in FireStore Database
        // Access a Cloud Firestore instance from your Activity

//        refUsers = FirebaseDatabase.getInstance().reference.child("users").child(firebaseUser!!.uid)
        val firebaseStoreDatabaseGetInstance = FirebaseFirestore.getInstance()

        val user: MutableMap<String, Any> = HashMap()
        user["emailInput"] = emailInput
        user["fullNameInput"] = fullNameInput
        firebaseStoreDatabaseGetInstance.collection("users").add(user).addOnSuccessListener {
            Toast.makeText(
                this@SignupActivity,
                "The account is created successfully",
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }.addOnFailureListener {
            Toast.makeText(
                this@SignupActivity,
                "Failed to Sign Up. Please try again.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    override fun onStart() {
        super.onStart()

        val user = auth.currentUser

        if (user != null) {
            val userRedirect = Intent(this, DashboardActivity::class.java)
            startActivity(userRedirect)
        } else {
            Log.e("User Status ", "User Null")
        }
    }

    private fun isValidEmail(emailInput: String): Boolean {
        return !TextUtils.isEmpty(emailInput) && Patterns.EMAIL_ADDRESS.matcher(emailInput)
            .matches()
    }


}

