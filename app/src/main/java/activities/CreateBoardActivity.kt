package activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.projectmanagement.R
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import firebase.FirestoreClass
import kotlinx.android.synthetic.main.activity_create_board.*
import kotlinx.android.synthetic.main.activity_my_profile.*
import models.Board
import utils.Constants
import java.io.IOException

class CreateBoardActivity : BaseActivity() {

    // Add a global variable for URI of a selected image from phone storage.
    private var mSelectedImageFileUri: Uri? = null

    private lateinit var mUserName: String

    //if there's already a url for the image, we need to know it
    private var mBoardImageURL: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_board)

        setupActionBar()

        if(intent.hasExtra(Constants.NAME)){
            mUserName = intent.getStringExtra(Constants.NAME)
        }

        iv_board_image.setOnClickListener { view ->

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                Constants.showImageChooser(this@CreateBoardActivity)
            } else {
                /*Requests permissions to be granted to this application. These permissions
                 must be requested in your manifest, they should not be granted to your app,
                 and they should have protection level*/
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }
        btn_create.setOnClickListener{
            if(mSelectedImageFileUri!=null){
                //UPLOAD PROCESS SHOULD ONLY START AFTER CLICKING THE SUBMIT BUTTON
                uploadBoardImage()
            }else{
                showProgressDialog(resources.getString(R.string.please_wait))
                createBoard()
            }
        }
    }

    // Here the read storage permission result will be handled. And further execution will be done.)
    /**
     * This function will notify the user after tapping on allow or deny
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            //If permission is granted
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChooser(this@CreateBoardActivity)
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(
                    this,
                    "Oops, you just denied the permission for storage. You can also allow it from settings.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // Get the result of the image selection based on the constant code.)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK
            && requestCode == Constants.PICK_IMAGE_REQUEST_CODE
            && data!!.data != null
        ) {
            mSelectedImageFileUri = data.data

            try {
                // Load the board image in the ImageView.
                Glide
                    .with(this@CreateBoardActivity)
                    .load(Uri.parse(mSelectedImageFileUri.toString())) // URI of the image
                    .centerCrop() // Scale type of the image.
                    .placeholder(R.drawable.ic_user_place_holder) // A default place holder
                    .into(iv_board_image) // the view in which the image will be loaded.
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun boardCreatedSuccesfully(){
        hideProgressDialog()

        setResult(Activity.RESULT_OK)

        finish()
    }

    private fun createBoard(){
        //CREATING THE BOARD
        val assignedUsersArrayList: ArrayList<String> = ArrayList()
        assignedUsersArrayList.add(getCurrentUserId())

        //PREPARING THE BOARD INFO
        var board = Board(
            et_board_name.text.toString(),
            mBoardImageURL,
            mUserName,
            assignedUsersArrayList
        )
        //CREATING THE BOARD USING THE PREPARED INFO
        FirestoreClass().createBoard(this, board)
    }

    private fun uploadBoardImage(){
        showProgressDialog(resources.getString(R.string.please_wait))

        val sRef : StorageReference =
            FirebaseStorage.getInstance().reference.child(
                "BOARD_IMAGE" + System.currentTimeMillis()
                        +"."+ Constants.getFileExtension(this, mSelectedImageFileUri))
        sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                taskSnapshot ->
            //MAKE A STRING OUT OF WHERE THE FILE IS STORED
            Log.i(
                "Board Image URL",
                taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
            )
            taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                //THIS IS THE ACTUAL LINK
                    uri ->
                Log.i("Downloadable ImageURL", uri.toString())
                mBoardImageURL = uri.toString()

                hideProgressDialog()
                createBoard()

            }
        }.addOnFailureListener{
                exception ->
            Toast.makeText(this,
                exception.message,
                Toast.LENGTH_LONG)
                .show()

            hideProgressDialog()
        }
    }




    /**
     * A function to setup action bar
     */
    private fun setupActionBar() {

        setSupportActionBar(toolbar_create_board_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_create_board_activity.setNavigationOnClickListener { onBackPressed() }
    }

}