package ie.wit.medicineapp.firebase

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import ie.wit.medicineapp.R
import ie.wit.medicineapp.models.*
import ie.wit.medicineapp.ui.utils.NotificationService
import timber.log.Timber

object FirebaseDBManager : MedicineAppStore {

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
            }.addOnFailureListener {
                Timber.e("firebase Error getting data $it")
            }
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
        val childDelete: MutableMap<String, Any?> = HashMap()
        childDelete["/user-groups/$userid/$groupId"] = null
        childDelete["/user-medication/$userid/$groupId"] = null
        database.updateChildren(childDelete)
    }

    override fun updateGroup(userid: String, groupId: String, group: GroupModel) {
        val groupValues = group.toMap()
        val childUpdate: MutableMap<String, Any?> = HashMap()
        childUpdate["user-groups/$userid/$groupId"] = groupValues
        database.updateChildren(childUpdate)
    }

    override fun findGroupMedication(
        userid: String,
        groupId: String,
        medicineList: MutableLiveData<List<MedicineModel>>
    ) {
        database.child("user-medication").child(userid).child(groupId)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localList = ArrayList<MedicineModel>()
                    val children = snapshot.children
                    children.forEach {
                        val medicine = it.getValue(MedicineModel::class.java)
                        localList.add(medicine!!)
                    }
                    database.child("user-medication").child(userid).child(groupId)
                        .removeEventListener(this)

                    medicineList.value = localList
                }
            })
    }

    override fun deleteAllGroups(userid: String) {
        val childDelete: MutableMap<String, Any?> = HashMap()
        childDelete["/user-groups/$userid"] = null
        childDelete["/user-medication/$userid"] = null
        database.updateChildren(childDelete)
    }

    override fun findMedicineById(
        userid: String,
        groupId: String,
        medicineId: String,
        medicine: MutableLiveData<MedicineModel>
    ) {
        database.child("user-medication").child(userid)
            .child(groupId).child(medicineId).get().addOnSuccessListener {
                medicine.value = it.getValue(MedicineModel::class.java)
                Timber.i("firebase Got value ${it.value}")
            }.addOnFailureListener {
                Timber.e("firebase Error getting data $it")
            }
    }

    override fun createMedicine(
        firebaseUser: MutableLiveData<FirebaseUser>,
        medicine: MedicineModel,
        groupId: String
    ) {
        Timber.i("Firebase DB Reference : $database")
        val uid = firebaseUser.value!!.uid
        val key = database.child("medication").push().key
        if (key == null) {
            Timber.i("Firebase Error : Key Empty")
            return
        }
        medicine.uid = key
        val medicineValues = medicine.toMap()
        val childAdd = HashMap<String, Any>()
        childAdd["/user-medication/$uid/$groupId/$key"] = medicineValues
        database.updateChildren(childAdd)
    }

    override fun updateMedicine(
        userid: String,
        groupId: String,
        medicineId: String,
        medicine: MedicineModel
    ) {
        val medicineValues = medicine.toMap()
        val childUpdate: MutableMap<String, Any?> = HashMap()
        childUpdate["user-medication/$userid/$groupId/$medicineId"] = medicineValues
        database.updateChildren(childUpdate)
    }

    override fun deleteMedicine(userid: String, groupId: String, medicineId: String) {
        val childDelete: MutableMap<String, Any?> = HashMap()
        childDelete["/user-medication/$userid/$groupId/$medicineId"] = null
        database.updateChildren(childDelete)
    }

    override fun deleteAllMedicine(userid: String, groupId: String) {
        val childDelete: MutableMap<String, Any?> = HashMap()
        childDelete["/user-medication/$userid/$groupId"] = null
        database.updateChildren(childDelete)
    }

    override fun createReminder(
        firebaseUser: MutableLiveData<FirebaseUser>,
        reminder: ReminderModel
    ) {
        Timber.i("Firebase DB Reference : $database")
        val uid = firebaseUser.value!!.uid
        val key = database.child("reminders").push().key
        if (key == null) {
            Timber.i("Firebase Error : Key Empty")
            return
        }
        reminder.uid = key
        val reminderValues = reminder.toMap()
        val childAdd = HashMap<String, Any>()
        childAdd["/user-reminders/$uid/$key"] = reminderValues
        database.updateChildren(childAdd)
    }

    override fun findReminders(userid: String, reminderList: MutableLiveData<List<ReminderModel>>) {
        database.child("user-reminders").child(userid)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localList = ArrayList<ReminderModel>()
                    val children = snapshot.children
                    children.forEach {
                        val reminder = it.getValue(ReminderModel::class.java)
                        localList.add(reminder!!)
                    }
                    database.child("user-reminders").child(userid)
                        .removeEventListener(this)

                    reminderList.value = localList
                }
            })
    }

    override fun deleteReminder(userid: String, reminderId: String) {
        val childDelete: MutableMap<String, Any?> = HashMap()
        childDelete["/user-reminders/$userid/$reminderId"] = null
        database.updateChildren(childDelete)
    }

    override fun findReminderById(
        userid: String,
        reminderId: String,
        reminder: MutableLiveData<ReminderModel>
    ) {
        database.child("user-reminders").child(userid)
            .child(reminderId).get().addOnSuccessListener {
                reminder.value = it.getValue(ReminderModel::class.java)
                Timber.i("firebase Got value ${it.value}")
            }.addOnFailureListener {
                Timber.e("firebase Error getting data $it")
            }
    }

    override fun updateReminder(userid: String, reminderId: String, reminder: ReminderModel) {
        val reminderValues = reminder.toMap()
        val childUpdate: MutableMap<String, Any?> = HashMap()
        childUpdate["user-reminders/$userid/$reminderId"] = reminderValues
        database.updateChildren(childUpdate)
    }

    override fun onceOffReminderTriggered(userid: String, reminderId: String) {
        database.child("user-reminders")
            .child(userid).child(reminderId).child("active").setValue(false)
    }

    override fun confirmMedTaken(
        userid: String,
        groupId: String,
        medicineId: String,
        context: Context
    ) {
        val path = database.child("user-medication").child(userid).child(groupId).child(medicineId)
        database.child("user-medication").child(userid).child(groupId).child(medicineId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val quantity = snapshot.child("quantity").value.toString()
                    val reminderLimit = snapshot.child("reminderLimit").value.toString()
                    var newQuantity = quantity.toInt() - 1
                    val name = snapshot.child("name").value.toString()
                    if (quantity.toInt() != 0) {
                        path.child("quantity").setValue(newQuantity)
                    } else
                        newQuantity = 0
                    if (newQuantity <= reminderLimit.toInt()) {
                        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                        val phoneNum = sharedPreferences.getString("pharmacy", "")
                        val callIntent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNum, null))
                        val callPendingIntent = PendingIntent.getActivity(context, 5,
                            callIntent,
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                        val notification = NotificationCompat.Builder(
                            context,
                            NotificationService.channelID
                        )
                            .setContentTitle("Limit Reached")
                            .setContentText(
                                "Limit for $name has been reached. " +
                                        "Remaining: $newQuantity"
                            )
                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                            .setChannelId("highChannelID")
                            .addAction(R.drawable.ic_launcher_foreground,"Call Pharmacy",
                                callPendingIntent)
                            .setAutoCancel(true)
                            .build()
                        val manager =
                            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        manager.notify(NotificationService.notificationID, notification)
                    }
                }
            })

    }

    override fun createConfirmation(userid: String, confirmation: ConfirmationModel) {
        Timber.i("Firebase DB Reference : $database")
        val key = database.child("confirmations").push().key
        if (key == null) {
            Timber.i("Firebase Error : Key Empty")
            return
        }
        confirmation.uid = key
        val confirmationValues = confirmation.toMap()
        val childAdd = HashMap<String, Any>()
        childAdd["/user-confirmations/$userid/$key"] = confirmationValues
        database.updateChildren(childAdd)
    }

    override fun findHistory(
        userid: String, historyList: MutableLiveData<List<ConfirmationModel>>, day: Int, month: Int,
        year: Int
    ) {
        database.child("user-confirmations").child(userid)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localList = ArrayList<ConfirmationModel>()
                    val children = snapshot.children
                    children.forEach {
                        val confirmation = it.getValue(ConfirmationModel::class.java)
                        if (confirmation!!.day == day && confirmation.month == month && confirmation.year == year) {
                            localList.add(confirmation)
                        }
                        //localList.add(confirmation!!)

                    }
                    database.child("user-confirmations").child(userid)
                        .removeEventListener(this)

                    historyList.value = localList
                }
            })
    }

    override fun deleteConfirmationHistory(userid: String, confirmationId: String) {
        val childDelete: MutableMap<String, Any?> = HashMap()
        childDelete["/user-confirmations/$userid/$confirmationId"] = null
        database.updateChildren(childDelete)
    }
}
