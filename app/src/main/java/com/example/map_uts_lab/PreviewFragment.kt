package com.example.map_uts_lab

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.auth.api.signin.internal.Storage
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID


class PreviewFragment : Fragment() {

    companion object {
        private const val FILE_PATH = "file_path"

        fun newInstance(filePath: String): PreviewFragment{
            val fragment = PreviewFragment()
            val args = Bundle()
            args.putString(FILE_PATH, filePath)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var db:FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_preview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        val previewImage = view.findViewById<ImageView>(R.id.image_preview)
        val dateTimeView = view.findViewById<TextView>(R.id.date_time)
        val entryTypeView = view.findViewById<TextView>(R.id.entry_type_text)
        val nextButton = view.findViewById<Button>(R.id.next_button)
        val backButton = view.findViewById<Button>(R.id.back_button)

        val filePath = arguments?.getString(FILE_PATH)
        var date: Date? = null

        if(filePath != null){
            val imageFile = File(filePath)
            if (imageFile.exists()){
                val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                previewImage.setImageBitmap(bitmap)

                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                date = Date(imageFile.lastModified())
                dateTimeView.text = dateFormat.format(date)
            }
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateString = dateFormat.format(Date())

        db.collection("entries")
            .whereEqualTo("email", auth.currentUser?.email)
            .whereEqualTo("date", dateString)
            .get()
            .addOnSuccessListener { documents ->
                var entryType = "IN"
                var canAddEntry = true

                if(!documents.isEmpty){
                    val entries = documents.map { it.toObject(Entry::class.java) }
                    val hasInEntry = entries.any { it.entryType =="IN"}
                    val hasOutEntry = entries.any {it.entryType == "OUT"}

                    entryType = when {
                        hasInEntry && hasOutEntry -> {
                            canAddEntry = false
                            "Kamu sudah absen hari ini"
                        }
                        hasInEntry -> "OUT"
                        else -> "IN"
                    }
                }
                entryTypeView.text = entryType
                nextButton.isEnabled = canAddEntry
            }.addOnFailureListener { e ->
                Toast.makeText(activity, "Gagal Memeriksa Absen: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        nextButton.setOnClickListener{
            if (filePath != null && date != null){
                uploadImageAndSaveEntry(filePath, date){entryType ->
                    entryTypeView.text = entryType
                }
            }
        }
        backButton.setOnClickListener{
            parentFragmentManager.popBackStack()
        }
    }

    private fun uploadImageAndSaveEntry(filePath: String, date: Date, callback: (String) -> Unit){
        val user = auth.currentUser
        if(user != null){
            val email = user.email
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val dateString = dateFormat.format(date)
            val timeString = timeFormat.format(date)

            db.collection("entries")
                .whereEqualTo("email", email)
                .orderBy("date", Query.Direction.DESCENDING)
                .orderBy("time", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    var entryType = "IN"
                    var canAddEntry = true

                    if (!documents.isEmpty){
                        val lastEntry = documents.documents[0].toObject(Entry::class.java)
                        if (lastEntry != null){
                            val lastEntryDate = lastEntry.date
                            val lastEntryType = lastEntry.entryType

                            if(lastEntryDate == dateString){
                                if(lastEntryType == "IN"){
                                    entryType = "OUT"
                                }else if (lastEntryType =="OUT"){
                                    canAddEntry == false
                                    Toast.makeText(activity, "Kamu sudah absen hari ini", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                    if (canAddEntry){
                        val storageRef = storage.reference.child("images/${UUID.randomUUID()}.jpg")
                        val file = Uri.fromFile(File(filePath))
                        storageRef.putFile(file)
                            .addOnSuccessListener {
                                storageRef.downloadUrl.addOnSuccessListener { uri ->
                                    val entry = hashMapOf(
                                        "email" to email,
                                        "date" to dateString,
                                        "time" to timeString,
                                        "image" to uri.toString(),
                                        "entryType" to entryType
                                    )

                                    db.collection("entries").add(entry)
                                        .addOnSuccessListener {
                                            Toast.makeText(activity, "Absen saved", Toast.LENGTH_SHORT).show()
                                            callback(entryType)
                                            parentFragmentManager.beginTransaction()
                                                .replace(R.id.main_container, HistoryFragment())
                                                .addToBackStack(null)
                                                .commit()
                                        }.addOnFailureListener { e->
                                            Toast.makeText(activity, "Error Absen: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }.addOnFailureListener{ e ->
                                Toast.makeText(activity, "Gagal Mengupload Foto: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }.addOnFailureListener { e->
                    Toast.makeText(activity, "Gagal Memeriksa Absen: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }


}