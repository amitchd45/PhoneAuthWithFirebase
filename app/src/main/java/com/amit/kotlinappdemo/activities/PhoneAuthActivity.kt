package com.amit.kotlinappdemo.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amit.kotlinappdemo.databinding.ActivityPhoneAuthBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

class PhoneAuthActivity : AppCompatActivity() {

    // view binding
    private lateinit var binding: ActivityPhoneAuthBinding

    // force fully rend code
    private var forceResendingToken: PhoneAuthProvider.ForceResendingToken? = null
    private lateinit var mCallBack: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var mVerificationId: String? = null
    private lateinit var firebaseAuth: FirebaseAuth

    //progress dialog
    private lateinit var progressDialog: ProgressDialog
    private  val TAG = "PhoneAuthActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        callBack()
        onClick()

    }

    private fun onClick() {
        binding.btnContinue.setOnClickListener {
            val phone = binding.phoneEt.text.toString().trim()

            if (TextUtils.isEmpty(phone)) {
                Toast.makeText(
                    this@PhoneAuthActivity,
                    "Please enter phone number",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                startPhoneNumberVerification(phone)
            }
        }
        binding.resendCodeTv.setOnClickListener {
            val phone = binding.phoneEt.text.toString().trim()

            if (TextUtils.isEmpty(phone)) {
                Toast.makeText(
                    this@PhoneAuthActivity,
                    "Please enter phone number",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                resendVerificationCode(phone, this!!.forceResendingToken!!)
            }
        }
        binding.btnSubmit.setOnClickListener {
            val code = binding.codeEt.text.toString().trim()

            if (TextUtils.isEmpty(code)) {
                Toast.makeText(
                    this@PhoneAuthActivity,
                    "Please enter verification code...",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                mVerificationId?.let { it1 -> verifyPhoneNumberWithCode(it1, code) }
            }
        }
    }

    private fun callBack() {
        mCallBack = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(phoneAuthCrediential: PhoneAuthCredential) {

                Log.i(TAG, "onVerificationCompleted: == $phoneAuthCrediential")
                signInWithPhoneAuthCredential(phoneAuthCrediential)

            }

            override fun onVerificationFailed(e: FirebaseException) {

                progressDialog.dismiss()
                Toast.makeText(this@PhoneAuthActivity, "${e.message}", Toast.LENGTH_SHORT).show()
                Log.i(TAG, "onVerificationFailed: ==${e.message}")


            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {

                Log.i(TAG, "onCodeSent: $verificationId")
                mVerificationId = verificationId
                forceResendingToken = token
                progressDialog.dismiss()

                binding.phoneLl.visibility = View.GONE
                binding.codeLl.visibility = View.VISIBLE

                Toast.makeText(
                    this@PhoneAuthActivity,
                    "verification code sent...",
                    Toast.LENGTH_SHORT
                ).show()

                binding.codeSentDisTv.text = "Please type the verification code we sent to ${
                    binding.phoneEt.text.toString().trim()
                }"


            }

        }
    }

    private fun init() {
        binding.phoneLl.visibility = View.VISIBLE
        binding.codeLl.visibility = View.GONE

        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this);
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)
    }

    private fun startPhoneNumberVerification(phone: String) {
        progressDialog.setMessage("verifying phone number...")
        progressDialog.show()

        Log.i(TAG, "phone number == $phone")

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallBack)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private fun resendVerificationCode(
        phone: String,
        token: PhoneAuthProvider.ForceResendingToken
    ) {
        progressDialog.setMessage("verifying phone number...")
        progressDialog.show()

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallBack)
            .setForceResendingToken(token)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private fun verifyPhoneNumberWithCode(verificationId: String, code: String) {

        progressDialog.setMessage("verifying code...")
        progressDialog.show()

        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithPhoneAuthCredential(credential)

    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        progressDialog.setMessage("Logging In...")
        progressDialog.show()
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")

                    val user = task.result?.user

                    progressDialog.dismiss()
                    val phone = firebaseAuth.currentUser?.phoneNumber
                    Toast.makeText(this, "Logged as $phone", Toast.LENGTH_SHORT).show()

                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()

                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)

                    progressDialog.dismiss()
                    Toast.makeText(this, "${task.exception}", Toast.LENGTH_SHORT).show()

                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                }
            }
    }


}