package ie.wit.medicineapp.firebase

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import ie.wit.medicineapp.models.GroupModel
import ie.wit.medicineapp.models.GroupStore
import timber.log.Timber

object FirebaseDBManager : GroupStore {

    var database: DatabaseReference = FirebaseDatabase.getInstance().reference

    override fun findAllGroups(userid: String, groupsList: MutableLiveData<List<GroupModel>>) {
        TODO("Not yet implemented")
    }

    override fun findGroupById(
        userid: String,
        groupId: String,
        group: MutableLiveData<GroupModel>
    ) {
        TODO("Not yet implemented")
    }

    override fun createGroup(firebaseUser: MutableLiveData<FirebaseUser>, group: GroupModel) {
        Timber.i("Firebase DB Reference : $database")
        val uid = firebaseUser.value!!.uid
        val key = database.child("groups").push().key
        if (key == null) {
            Timber.i("Firebase Error : Key Empty")
            return
        }
        group.uid = key
        val groupValues = group.toMap()
        val childAdd = HashMap<String, Any>()
        childAdd["/user-groups/$uid/$key"] = groupValues
        database.updateChildren(childAdd)
    }

    override fun deleteGroup(userid: String, groupId: String) {
        TODO("Not yet implemented")
    }

    override fun updateGroup(userid: String, groupId: String, group: GroupModel) {
        TODO("Not yet implemented")
    }
}