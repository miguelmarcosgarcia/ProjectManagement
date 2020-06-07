package activities

import adapters.CardMemberListItemsAdapter
import android.app.Activity
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.GridLayout
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.projectmanagement.R
import dialogs.LabelColorListDialog
import dialogs.MembersListDialog
import firebase.FirestoreClass
import kotlinx.android.synthetic.main.activity_card_details.*
import kotlinx.android.synthetic.main.activity_members.*
import models.*
import utils.Constants

class CardDetailsActivity : BaseActivity() {
    private lateinit var mBoardDetails: Board
    private var mTaskListPosition = -1
    private var mCardPosition = -1
    // A global variable for selected label color
    private var mSelectedColor = ""
    private lateinit var mMembersDetailList: ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_details)
        getIntentData()
        setUpActionBar()
        et_name_card_details.setText(mBoardDetails
            .taskList[mTaskListPosition]
            .cards[mCardPosition].name)

        //SET FOCUS AT THE END OF THE TEXT (WHEN CLICKING)
        et_name_card_details.setSelection(et_name_card_details.text.toString().length)

        mSelectedColor = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].labelColor
        if(mSelectedColor.isNotEmpty()){
            setColor()
        }

        btn_update_card_details.setOnClickListener{
            if(et_name_card_details.toString().isNotEmpty()){
                upDateCardDetails()
            }else{
                Toast.makeText(this, "Please enter a card Name", Toast.LENGTH_SHORT).show()
            }
        }

        tv_select_label_color.setOnClickListener{
            labelColorsListDialog()
        }

        tv_select_members.setOnClickListener{
            membersListDialog()
        }

        setUpSelectedMembersList()
    }

    private fun setUpActionBar(){
        setSupportActionBar(toolbar_card_details_activity)
        val actionBar = supportActionBar
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = mBoardDetails
                .taskList[mTaskListPosition]
                .cards[mCardPosition].name

        }
        toolbar_card_details_activity.setNavigationOnClickListener{
            onBackPressed()
        }
    }

    fun addUpdateTaskListSuccess(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_delete_card -> {
                deleteCard()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getIntentData(){
        if(intent.hasExtra((Constants.BOARD_DETAIL))){
            mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)
        }
        if(intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)){
            mTaskListPosition = intent.getIntExtra(
                Constants.TASK_LIST_ITEM_POSITION, -1)
        }
        if(intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)){
            mCardPosition = intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION, -1)
        }
        if(intent.hasExtra(Constants.BOARD_MEMBERS_LIST)){
            mMembersDetailList = intent.getParcelableArrayListExtra(Constants.BOARD_MEMBERS_LIST)!!
        }
    }
    private fun membersListDialog(){
        var cardAssignedMembersList = mBoardDetails
            .taskList[mTaskListPosition]
            .cards[mCardPosition].assignedTo

        if(cardAssignedMembersList.size>0){
             for (i in mMembersDetailList.indices){
                 for(j in cardAssignedMembersList){
                     if(mMembersDetailList[i].id ==j){
                         mMembersDetailList[i].selected = true
                     }
                 }
             }
        }else{
            for (i in mMembersDetailList.indices){
                mMembersDetailList[i].selected = false
            }
        }
        val listDialog = object : MembersListDialog(
            this,
            mMembersDetailList,
            resources.getString(R.string.str_select_member)
        ){
            override fun onItemSelected(user: User, action: String) {
                if(action == Constants.SELECT){
                    if(!mBoardDetails.taskList[mTaskListPosition]
                            .cards[mCardPosition].assignedTo.contains(user.id)){
                        mBoardDetails.taskList[mTaskListPosition]
                            .cards[mCardPosition].assignedTo.add(user.id)
                    }
                }
                else{
                    mBoardDetails.taskList[mTaskListPosition]
                        .cards[mCardPosition].assignedTo.remove(user.id)

                    for(i in mMembersDetailList.indices){
                        if(mMembersDetailList[i].id == user.id){
                            mMembersDetailList[i].selected = false
                        }
                    }
                }
                setUpSelectedMembersList()
            }
        }
        listDialog.show()

    }

    private fun upDateCardDetails(){
        val card = Card(
            et_name_card_details.text.toString(),
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].createdBy,
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo,
            mSelectedColor
        )
        val taskList: ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size-1)

        mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition] = card

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this, mBoardDetails)

    }
    private fun deleteCard(){
        val cardsList: ArrayList<Card> = mBoardDetails.taskList[mTaskListPosition].cards

        cardsList.removeAt(mCardPosition)

        val taskList: ArrayList<Task> = mBoardDetails.taskList
        //TO GET RID OF THE ADD CARD BUTTON
        taskList.removeAt(taskList.size-1)

        taskList[mTaskListPosition].cards = cardsList

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    /**
     * A function to add some static label colors in the list.
     */
    private fun colorsList(): ArrayList<String> {

        val colorsList: ArrayList<String> = ArrayList()
        colorsList.add("#43C86F")
        colorsList.add("#0C90F1")
        colorsList.add("#F72400")
        colorsList.add("#7A8089")
        colorsList.add("#D57C1D")
        colorsList.add("#770000")
        colorsList.add("#0022F8")

        return colorsList
    }
    private fun setColor(){
        tv_select_label_color.text=""
        tv_select_label_color.setBackgroundColor(Color.parseColor(mSelectedColor))
    }

    private fun labelColorsListDialog(){
        val colorsList: ArrayList<String> = colorsList()

        val listDialog = object : LabelColorListDialog(
            this@CardDetailsActivity,
            colorsList,
            resources.getString(R.string.str_select_label_color),
        mSelectedColor)
        {
            override fun onItemSelected(color: String) {
                mSelectedColor = color
                setColor()
            }
        }
        listDialog.show()
    }

    private fun setUpSelectedMembersList(){
        val cardAssignedMembersList =
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo

        val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

        for (i in mMembersDetailList.indices){
            for(j in cardAssignedMembersList){
                if(mMembersDetailList[i].id ==j){
                    val selectedMember = SelectedMembers(
                        mMembersDetailList[i].id,
                        mMembersDetailList[i].image
                    )
                    selectedMembersList.add(selectedMember)
                }
            }
        }
        if(selectedMembersList.size>0){
            selectedMembersList.add(SelectedMembers("", ""))
            tv_select_members.visibility = View.GONE
            rv_selected_members_list.visibility = View.VISIBLE

            rv_selected_members_list.layoutManager = GridLayoutManager(
                this, 6
            )
            val adapter = CardMemberListItemsAdapter(this, selectedMembersList)

            rv_selected_members_list.adapter = adapter
            adapter.setOnClickListener(
                object : CardMemberListItemsAdapter.OnClickListener{
                    override fun OnClick(){
                        membersListDialog()
                    }
                }
            )
        }else{
            tv_select_members.visibility = View.VISIBLE
            rv_selected_members_list.visibility = View.GONE
        }
    }
}