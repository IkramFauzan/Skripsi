package com.example.testingfirebaserealtime.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.testingfirebaserealtime.MainActivity
import com.example.testingfirebaserealtime.User
import com.example.testingfirebaserealtime.databinding.ActivitySignUpBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

@Suppress("DEPRECATION")
class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var googleSignInClient: GoogleSignInClient

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = FirebaseDatabase.getInstance()

        binding.buttonRegister.setOnClickListener {
            val username = binding.userNameRegister.text.toString()
            val email = binding.emailRegister.text.toString()
            val password = binding.passwordRegister.text.toString()

            if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                registerFirebase(username, email, password)
            } else {
                Toast.makeText(this, "Field must not be empty", Toast.LENGTH_SHORT).show()
            }
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1014940034594-j4lhli1vefrlnisjnooansc7fis3s7t0.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.signInButton.setOnClickListener {
            signInWithGoogle()
        }

        binding.keHalamanRegister.setOnClickListener {
            val intent = Intent(this, SignActivity::class.java)
            startActivity(intent)
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        resultLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    if (user != null) {
                        // Save user data to Firebase Realtime Database
                        saveUserDataToDatabase(
                            user.uid, user.displayName ?: "", user.email ?: "",
                            user.photoUrl?.toString() ?: ""
                        )
                        Toast.makeText(this, "Google sign in success", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun registerFirebase(username: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid ?: ""
                val userRef = db.getReference("users").child(userId)
                val userData = User(username, email, password)

                userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // User data already exists
                            Toast.makeText(this@SignUpActivity, "User data already exists", Toast.LENGTH_SHORT).show()
                        } else {
                            // User data doesn't exist, proceed with registration
                            userRef.setValue(userData)
                                .addOnSuccessListener {
                                    Toast.makeText(this@SignUpActivity, "Register success", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                                    startActivity(intent)
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(this@SignUpActivity, "Failed to register: ${exception.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle database error
                        Toast.makeText(this@SignUpActivity, "Database error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .addOnFailureListener { exception ->
                // Handle failure to create user account
                Toast.makeText(this, "Failed to create user account: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun saveUserDataToDatabase(userId: String, username: String, email: String, photo: String) {
        val userData = hashMapOf(
            "username" to username,
            "email" to email,
            "profileImageUrl" to photo
        )
        db.reference.child("users").child(userId).setValue(userData)
            .addOnSuccessListener {
                // User data saved successfully
            }
            .addOnFailureListener { e ->
                // Error saving user data
            }
    }
}