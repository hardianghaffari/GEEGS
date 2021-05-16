package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.companion.companion
import com.example.myapplication.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Login : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private lateinit var sharedPreferences: SharedPreferences
    private var moveFromVerifiedEmailToLogin = false
    private val TAG = "check"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)

        val emailSaved = sharedPreferences.getString(companion.CHECK_EMAIL, null)
        val passwordSaved = sharedPreferences.getString(companion.CHECK_PASSWORD, null)
        val rememberMe = sharedPreferences.getBoolean(companion.REMEMBER_ME, false)
        moveFromVerifiedEmailToLogin = intent.getBooleanExtra(companion.MOVE_FROM_VERIFIED_EMAIL_TO_LOGIN, false)

        Log.i(TAG, "onCreate email: $emailSaved")
        Log.i(TAG, "onCreate pass: $passwordSaved")
        Log.i(TAG, "onCreate: $rememberMe")
        if (rememberMe && emailSaved != null && passwordSaved != null) {
            binding.email.setText(emailSaved)
            binding.password.setText(passwordSaved)
        }

        binding.rememberMe.isChecked = rememberMe

        binding.btnSignup.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
        }

        binding.btnLogin.setOnClickListener {

            binding.progressBar.visibility = View.VISIBLE

            if (binding.email.text.toString().trim().isEmpty()) {
                binding.email.error = "Please enter email"
                binding.email.requestFocus()
                binding.progressBar.visibility = View.INVISIBLE
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(binding.email.text.toString().trim()).matches()) {
                binding.email.error = "Please enter valid email"
                binding.email.requestFocus()
                binding.progressBar.visibility = View.INVISIBLE
                return@setOnClickListener
            }

            if (binding.password.text.toString().trim().isEmpty()) {
                binding.password.error = "Please enter password"
                binding.password.requestFocus()
                binding.progressBar.visibility = View.INVISIBLE
                return@setOnClickListener
            }

            login()

        }

    }

    fun login() {
        auth.signInWithEmailAndPassword(
            binding.email.text.toString().trim(),
            binding.password.text.toString().trim()
        )
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    //get user
                    user = auth.currentUser
                    if (user.isEmailVerified) {
                        val reference = FirebaseDatabase.getInstance().reference.child("Users")
                            .child(auth.currentUser.uid)
                        reference.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                binding.progressBar.visibility = View.INVISIBLE
                                val user = auth.currentUser
                                updateUI(user)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Failed to read value
                                binding.progressBar.visibility = View.INVISIBLE
                            }
                        })
                    } else {
                        Toast.makeText(this, "Verified your account first!", Toast.LENGTH_SHORT)
                            .show()
                        val move = Intent(this, VerifiedEmail::class.java)
                        move.putExtra(companion.MOVE_FROM_LOGIN_TO_VERIFIED_EMAIL, true)
                        startActivity(move)

                    }


                } else {
                    // If sign in fails, display a message to the user.
                    updateUI(null)
                    Toast.makeText(this, "Incorrect email or password", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.INVISIBLE
                }
            }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null && !moveFromVerifiedEmailToLogin) {
            updateUI(auth.currentUser)
        }
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG, "onPause: ${binding.rememberMe.isChecked}")
        val editor = sharedPreferences.edit()
        if (binding.rememberMe.isChecked) {
            editor.putString(companion.CHECK_EMAIL, binding.email.text.toString())
            editor.putString(companion.CHECK_PASSWORD, binding.password.text.toString())
            editor.putBoolean(companion.REMEMBER_ME, true)
            editor.apply()
        } else editor.clear().commit()
    }
}