package activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.telecom.Call
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.projectmanagement.R
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import firebase.FirestoreClass
import kotlinx.android.synthetic.main.activity_my_profile.*
import kotlinx.android.synthetic.main.app_bar_main.*
import models.User
import utils.Constants
import java.io.IOException

class MyProfileActivity : BaseActivity() {

    private var mSelectedImageFileUri: Uri? = null
    private var mProfileImageURL: String=""
    private lateinit var mUserDetails: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)
        setUpActionBar()

        FirestoreClass().loadUserData(this)

        iv_profile_user_image.setOnClickListener{
            if(ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED){
                Constants.showImageChooser(this)

            }else{
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }
        btn_update.setOnClickListener{
            if(mSelectedImageFileUri!=null){
                uploadUsrImage()
            }else{
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()

            }
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
        ) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if(requestCode== Constants.READ_STORAGE_PERMISSION_CODE){
                if(grantResults.isNotEmpty() && grantResults[0] ==PackageManager.PERMISSION_GRANTED){
                    Constants.showImageChooser(this)
                }
            }
            else{
                Toast.makeText(this,
                    "Opps, you just denied the permissions for storage. You can allow them from the settings",
                    Toast.LENGTH_LONG).show()
            }
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if(resultCode == Activity.RESULT_OK
                && requestCode == Constants.PICK_IMAGE_REQUEST_CODE
                && data!!.data !=null){
                mSelectedImageFileUri = data.data

                try{
                    Glide
                        .with(this@MyProfileActivity)
                        .load(mSelectedImageFileUri)
                        .centerCrop()
                        .placeholder(R.drawable.ic_user_place_holder)
                        .into(iv_profile_user_image)
                }
                catch (e: IOException){
                    e.printStackTrace()
                }
            }
    }

    private fun setUpActionBar(){
        setSupportActionBar(toolbar_my_profile_activity)
        val actionBar = supportActionBar
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = resources.getString(R.string.my_profile_title)
        }
        toolbar_my_profile_activity.setNavigationOnClickListener{
            onBackPressed()
        }
    }
    fun setUserDataInUI(user: User){

        mUserDetails = user

        Glide
            .with(this@MyProfileActivity)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(iv_profile_user_image)

        et_name.setText(user.name)
        et_email.setText(user.email)
        if(user.mobile !=0L){
            et_mobile.setText(user.mobile.toString())
        }
    }
    private fun updateUserProfileData(){
        var userHashMap = HashMap<String, Any>()

        //CHECK IF IT IS NOT NULL OR THE SAME THAN STORED IN ORDER TO UPDATE THE VALUE STORED IN THE
        //HASHMAP
        if(mProfileImageURL.isNotEmpty() && mProfileImageURL != mUserDetails.image){
            userHashMap[Constants.IMAGE] = mProfileImageURL

        }
        //IF THE USER DIDN'T EVEN SELECT THE FIELD IT MAKES NO SENSE TO CALL THE DB
        if(et_name.text.toString()!= mUserDetails.name){
            userHashMap[Constants.NAME] = et_name.text.toString()

        }
        if(et_mobile.text.toString()!=mUserDetails.mobile.toString()){
            userHashMap[Constants.MOBILE] = et_mobile.text.toString().toLong()

        }

        FirestoreClass().updateUserProfileData(this, userHashMap)





    }

    private fun uploadUsrImage(){
        showProgressDialog(resources.getString(R.string.please_wait))

        if(mSelectedImageFileUri!=null){
            //UPLOAD IMAGE AND GIVING IT AN UNIQUE NAME
            val sRef : StorageReference = FirebaseStorage.getInstance()
                .reference.child("USER_IMAGE" + System.currentTimeMillis()+"."+
                Constants.getFileExtension(this, mSelectedImageFileUri))
            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener { 
                taskSnapshot ->
                //MAKE A STRING OUT OF WHERE THE FILE IS STORED
                Log.i(
                    "Firebase Image URL",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                   //THIS IS THE ACTUAL LINK
                    uri ->
                    Log.i("Downloadable ImageURL", uri.toString())
                    mProfileImageURL = uri.toString()

                    hideProgressDialog()
                    updateUserProfileData()

                }
            }.addOnFailureListener{
                exception ->
                Toast.makeText(this@MyProfileActivity,
                exception.message,
                Toast.LENGTH_LONG)
                    .show()

                hideProgressDialog()
            }
        }
    }

    fun profileUpdateSuccess(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }
}