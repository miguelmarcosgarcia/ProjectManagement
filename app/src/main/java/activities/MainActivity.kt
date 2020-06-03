package activities

import adapters.BoardItemsAdapter
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.sax.StartElementListener
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.projectmanagement.R
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import firebase.FirestoreClass
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.*
import models.Board
import models.User
import utils.Constants

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object{
        const val MY_PROFILE_REQUEST_CODE: Int = 11
        const val CREATE_BOARD_REQUEST_CODE: Int = 12
    }
    private lateinit var mUserName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupActionBar()

        //WHENEVER IS CLICKED, DO WHAT IT IS SPECIFIED IN HERE
        nav_view.setNavigationItemSelectedListener(this)

        FirestoreClass().loadUserData(this, true)

        //SEND USER TO CREATE BOARD ACTIVITY WHEN CLICKING ON PLUS ICON
        fab_create_board.setOnClickListener{
            val intent = Intent(this,
            CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME, mUserName)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }
    }
    //CREATING THE BOARD IN THE UI
    fun populateBoardsListToUI(boardList: ArrayList<Board>){
        hideProgressDialog()

        if(boardList.size>0){
            rv_boards_list.visibility = View.VISIBLE
            tv_no_boards_available.visibility = View.GONE

            rv_boards_list.layoutManager = LinearLayoutManager(this)
            rv_boards_list.setHasFixedSize(true)

            val adapter = BoardItemsAdapter(this, boardList)
            rv_boards_list.adapter = adapter

            adapter.setOnClickListener(object : BoardItemsAdapter.OnClickListener {
                override fun onClick(position: Int, model: Board) {
                    startActivity(Intent(this@MainActivity, TaskListActivity::class.java))
                }
            })

        }else{
            rv_boards_list.visibility = View.GONE
            tv_no_boards_available.visibility = View.VISIBLE
        }
    }

    //CREATING THE ACTION BAR
    private fun setupActionBar(){
        setSupportActionBar(toolbar_main_activity)
        toolbar_main_activity.setNavigationIcon(R.drawable.ic_action_navigation_menu)
        toolbar_main_activity.setNavigationOnClickListener{
            toggleDrawer()
        }
    }
    private fun toggleDrawer(){
        if(drawer_layout.isDrawerOpen(GravityCompat.START)){
            drawer_layout.closeDrawer(GravityCompat.START)
        }else{
            drawer_layout.openDrawer(GravityCompat.START)
        }
    }
    override fun onBackPressed(){
        if(drawer_layout.isDrawerOpen(GravityCompat.START)){
            drawer_layout.closeDrawer(GravityCompat.START)
        }else{
            //DEFINED IN BASEACTIVITY (SO THE CLASS SHOULD INHERIT FROM BASEACTIVITY)
            doubleBackToExit()
        }
    }
    fun updateNavigationUserDetails(user: User, readBoardList: Boolean){

        mUserName = user.name

        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(nav_user_image);

        tv_username.text = user.name
        if(readBoardList){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardList(this)

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK
            && requestCode == MY_PROFILE_REQUEST_CODE){
            FirestoreClass().loadUserData(this)
        }
        //TO REFRESH THE ACTIVITY VIEW ON CREATED BOARD
        else if(resultCode == Activity.RESULT_OK
            && requestCode == CREATE_BOARD_REQUEST_CODE){
            FirestoreClass().getBoardList(this)

        }
        else{
            Log.e("Cancelled", "Cancelled")
        }
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_my_profile -> {
                startActivityForResult(Intent(this,
                    MyProfileActivity::class.java),
                    MY_PROFILE_REQUEST_CODE)
            }
            R.id.nav_sign_out ->{
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)

        //TO DELETE THE ERROR (need to return sth)
        return true
    }
}
