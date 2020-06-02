package activities

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import com.example.projectmanagement.R
import firebase.FirestoreClass
import kotlinx.android.synthetic.main.activity_splash.*


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        //MAKING THE ACTIVITY FULL SCREEN
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val typeface: Typeface = Typeface.createFromAsset(assets, "carbon bl.ttf")
        tv_app_name.typeface = typeface

        //JUMP FROM THE SPASH PAGE TO THE INTRO PAGE in 2500ms
        Handler().postDelayed({
            var currentUserID = FirestoreClass().getCurrentUserID()


            if(currentUserID.isNotEmpty()){
                startActivity(Intent(this, MainActivity::class.java))
            }else{
                startActivity(Intent(this, IntroActivity::class.java))
            }
            //USER SHOULD NOT BE ABLE TO COME BACK TO THIS ACTIVITY
            finish()
        },2500)
    }
}
