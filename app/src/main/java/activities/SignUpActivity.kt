package activities

import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import com.example.projectmanagement.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import firebase.FirestoreClass
import kotlinx.android.synthetic.main.activity_sign_up.*
import models.User

//EXTENDING BASEACTIVITY TO INHERIT THE METHODS IMPLEMENTED THERE
class SignUpActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        setupActionBar()

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

    //SETTING UP THE ACTION BAR FUNCTION
    private fun setupActionBar(){
        setSupportActionBar(toolbar_sign_up_activity)

        val actionBar = supportActionBar
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            //New vector asset with the style of an iOS back button
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }
        //Adding functionality to the back arrow
        toolbar_sign_up_activity.setNavigationOnClickListener{
            onBackPressed()
        }

        btn_sign_up.setOnClickListener{
            registerUser()
        }
    }

    private fun registerUser(){
        //CLEAN UP EMPTY SPACES
        val name: String = et_name.text.toString().trim{it<= ' '}
        val email: String = et_email.text.toString().trim{it<= ' '}
        val password: String = et_password.text.toString().trim{it<= ' '}

        if(validateForm(name, email, password)){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password).addOnCompleteListener{
                    task ->
                    if(task.isSuccessful){
                        val firebaseUser : FirebaseUser = task.result!!.user!!
                        val registeredEmail = firebaseUser.email!!
                        val user = User(firebaseUser.uid, name, registeredEmail)
                        FirestoreClass().registerUser(this, user)
                    }
                    else{
                        Toast.makeText(this,
                            task.exception!!.message, Toast.LENGTH_SHORT).show()
                    }
                }



        }

    }

    //IF THE FIELDS ARE EMPTY, RETURN SNACKBAR
    private  fun validateForm(name: String,email: String, password: String) : Boolean{
        return when{
            TextUtils.isEmpty(name) ->{
                showErrorSnackBar("Please enter a name")
                false
            }
            TextUtils.isEmpty(email) ->{
                showErrorSnackBar("Please enter a email address")
                false
            }
            TextUtils.isEmpty(password) ->{
                showErrorSnackBar("Please enter a password")
                false
            }
            else -> {
                true
            }
        }

    }
    fun userRegisteredSuccess(){
        Toast.makeText(this,
            "you have succesfully registered" +
                    "address", Toast.LENGTH_LONG).show()
        hideProgressDialog()
        FirebaseAuth.getInstance().signOut()
        finish()
    }
}
