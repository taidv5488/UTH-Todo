package com.example.bt4_oop

import com.google.firebase.database.FirebaseDatabase

object RealtimeDatabaseManager {

    private val database = FirebaseDatabase.getInstance(
        "https://tai06-ecfdc-default-rtdb.asia-southeast1.firebasedatabase.app/"
    )

    private val usersRef = database.getReference("users")

    fun getUserRef(uid: String) = usersRef.child(uid)
}