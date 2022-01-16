package ie.wit.medicineapp.firebase

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import ie.wit.medicineapp.models.GroupModel
import ie.wit.medicineapp.models.GroupStore
import timber.log.Timber

object FirebaseDBManager : GroupStore {

    var database: DatabaseReference = FirebaseDatabase.getInstance().reference

    override fun findAllGroups(userid: String, groupsList: MutableLiveData<List<GroupModel>>) {
        database.child("user-groups").child(userid)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localList = ArrayList<GroupModel>()
                    val children = snapshot.children
                    children.forEach {
                        val group = it.getValue(GroupModel::class.java)
                        localList.add(group!!)
                    }
                    database.child("user-groups").child(userid)
                        .removeEventListener(this)

                    groupsList.value = localList
                }
            })
    }

    override fun findGroupById(
        userid: String,
        groupId: String,
        group: MutableLiveData<GroupModel>
    ) {
        database.child("user-groups").child(userid)
            .child(groupId).get().addOnSuccessListener {
                group.value = it.getValue(GroupModel::class.java)
                Timber.i("firebase Got value ${it.value}")
            }.addOnFailureListener{
                Timber.e("firebase Error getting data $it")
            }    }

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
        val childDelete : MutableMap<String, Any?> = HashMap()
        childDelete["/user-groups/$userid/$groupId"] = null
        database.updateChildren(childDelete)
    }

    override fun updateGroup(userid: String, groupId: String, group: GroupModel) {
        val groupValues = group.toMap()
        val childUpdate : MutableMap<String, Any?> = HashMap()
        childUpdate["user-groups/$userid/$groupId"] = groupValues
        database.updateChildren(childUpdate)
    }
}