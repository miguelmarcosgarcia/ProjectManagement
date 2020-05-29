package firebase

import activities.SignInActivity
import activities.SignUpActivity
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import models.User
import utils.Constants

class FirestoreClass {

    //Instance of FireStore
    private  val mFirestore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity, userInfo: User){
        mFirestore.collection(Constants.USERS)
            .document(getCurrentUserId()).set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }.addOnFailureListener{
                e -> Log.e(activity.javaClass.simpleName, "Error writing document", e)
            }

    }
    fun signInUser(activity: SignInActivity, userInfo: User){
        mFirestore.collection((Constants.USERS))
            .document(getCurrentUserId())
            .get()
            .addOnSuccessListener {document ->
                //CREATE A USER OBJECT IN ORDER TO HAVE ALL THE INFO ASSOCIATED WITH THE LOGGED IN USER
                val loggedInUser = document.toObject(User::class.java)
                if(loggedInUser!=null)

                    activity.signInSuccess(loggedInUser)
            }.addOnFailureListener{
                e -> Log.e(activity.javaClass.simpleName, "Error", e)
            }
    }

    fun getCurrentUserId(): String{

        var currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID = ""
        if (currentUser != null){
            currentUserID = currentUser.uid
        }
        return currentUserID
    }

}