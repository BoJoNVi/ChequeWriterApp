package com.mcmkhianjuan.chequewriterapp


import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

class LoginActivity : AppCompatActivity() {
    private var backPressedTime: Long = 0
    private var backToast: Toast? = null
    private lateinit var auth: FirebaseAuth

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


    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val user = auth.currentUser

        if (user != null) {
            val accessDashboardAlready = Intent(this, DashboardActivity::class.java)
            startActivity(accessDashboardAlready)
        }
    }

    private fun signInAccount() {
        val emailFieldInputSignIn: EditText = findViewById(R.id.emailField)
        val passwordFieldInputSignIn: EditText = findViewById(R.id.passwordField)
        val emailInput = emailFieldInputSignIn.text.toString()
        val passwordInput = passwordFieldInputSignIn.text.toString()
        auth.signInWithEmailAndPassword(emailInput.trim(), passwordInput.trim())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val accessDashboard = Intent(this, DashboardActivity::class.java)
                    startActivity(accessDashboard)
                } else if (!task.isSuccessful) {
                    try {
                        throw task.exception!!
                    } catch (e: FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this, "Invalid Account", Toast.LENGTH_SHORT).show()
                        emailFieldInputSignIn.requestFocus()
                    } catch (e: Exception) {
                        Log.e(this.toString(), e.message.toString())
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Authentication Error. Please try again.\n" + task.exception,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()

        val emailField: EditText = findViewById(R.id.emailField)
        val passwordField: EditText = findViewById(R.id.passwordField)
        val loginButtonTapped: Button = findViewById(R.id.loginButton)
        val showHideButtonTapped: Button = findViewById(R.id.showHideButtonLogin)
        val signUpButtonTapped: Button = findViewById(R.id.signUpActivityButton)
//        val privacyPolicyButtonTapped: Button = findViewById(R.id.privacyPolicyButton)

        showHideButtonTapped.setOnClickListener {
            if (showHideButtonTapped.text.toString() == "SHOW") {
                passwordField.transformationMethod = HideReturnsTransformationMethod.getInstance()
                showHideButtonTapped.text = "HIDE"
            } else {
                passwordField.transformationMethod = PasswordTransformationMethod.getInstance()
                showHideButtonTapped.text = "SHOW"
            }
        }

        loginButtonTapped.setOnClickListener {
            val emailInputString = emailField.text.toString()
            val passwordInputString = passwordField.text.toString()

            if (isInternetAvailable(this)) {
                if (emailInputString.trim().isEmpty()) {
                    emailField.error = "Please input Email Address"
                } else if (passwordInputString.trim().isEmpty()) {
                    passwordField.error = "Please input your Password"
                } else if (emailInputString.trim().isNotEmpty() || passwordInputString.trim()
                        .isNotEmpty()
                ) {
                    signInAccount()
                } else {
                    Toast.makeText(this, "Input required", Toast.LENGTH_SHORT).show()
                }
            } else {
                val dialogBuilder = AlertDialog.Builder(this)
                Intent(this, MainActivity::class.java)
                // set message of alert dialog
                dialogBuilder.setMessage(
                    "Make sure that WI-FI or Mobile Data is turned on, then try again.\n" +
                            "You cannot Log In Account without Internet Connection."
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

        signUpButtonTapped.setOnClickListener {
            val signUpIntent = Intent(this, SignupActivity::class.java)
            startActivity(signUpIntent)
        }

//        val closeAppTapped: Button = findViewById(R.id.exitAppButton)
//        closeAppTapped.setOnClickListener {
//            when (Build.VERSION.SDK_INT) {
//                in Int.MIN_VALUE..20 ->
//                    ActivityCompat.finishAffinity(this)
//                else -> finishAndRemoveTask()
//            }
//        }
    }


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

}