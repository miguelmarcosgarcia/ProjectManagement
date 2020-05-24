package activities

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.projectmanagement.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.dialog_progress.*

open class BaseActivity : AppCompatActivity() {

    //SO IF THE USER PRESS BACK TWICE, THE APP SHOULD CLOSE
    private var doubleBackToExitPressedOnce = false

    //DISPLAY DIALOG WHILE USING
    private lateinit var mProgressDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
    }

    fun showProgressDialog(text:String){
        mProgressDialog = Dialog(this)

        //SET THE SCREEN CONTENT FROM A LAYOUT RESOURCE
        //RESOURCE WILL BE INFLATED, ADDING ALL TOP LEVEL VIEWS TO THE SCREEN
        mProgressDialog.setContentView(R.layout.dialog_progress)

        mProgressDialog.tv_progress_text.text = text

        //START THE DIALOG AND DISPLAY IT ON SCREEN
        mProgressDialog.show()

    }

    fun hideProgressDialog(){
        mProgressDialog.dismiss()
    }

    //USING FIREBASE TO GET THE CURRENT USER ID
    //TO SHOW A USER ONLY THE DATA RELEVANT TO HIS PROFILE
    fun getCurrentUserId():String{
        return FirebaseAuth.getInstance().currentUser!!.uid
    }

    fun doubleBackToExit(){
        if (doubleBackToExitPressedOnce){
            super.onBackPressed()
            return
        }
        //IF USER PRESS ONCE THE BACK BUTTON WE SHOW A MESSAGE TELLING THEM THAT THEY NEED TO PRESS
        //AGAIN TO LEAVE THE APP --> AVOID THEM ACCIDENTALLY LEAVING THE APP

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this,
        resources.getString(R.string.please_click_back_again_to_exit),
        Toast.LENGTH_SHORT).show()

        //AFTER 2000ms THE USER WILL NEED TO PRESS TWICE AGAIN TO LEAVE THE APP
        Handler().postDelayed({doubleBackToExitPressedOnce = false}, 2000)

    }

    //TO SHOW
    fun showErrorSnackBar(message: String){
        val snackBar = Snackbar.make(findViewById(android.R.id.content),
        message, Snackbar.LENGTH_LONG)

        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(ContextCompat.getColor(this, R.color.snackbar_error_color))

        snackBar.show()
    }


}
