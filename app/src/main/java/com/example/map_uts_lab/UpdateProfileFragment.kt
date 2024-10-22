package com.example.map_uts_lab

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class UpdateProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_update_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nameInputField = view.findViewById<TextInputEditText>(R.id.name_input_field)
        val nimInputField = view.findViewById<TextInputEditText>(R.id.nim_input_field)
        val saveButton = view.findViewById<Button>(R.id.save_button)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val user = auth.currentUser
        if (user != null) {
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val name = document.getString("name")
                        val nim = document.getString("nim")

                        nameInputField.setText(name)
                        nimInputField.setText(nim)
                    }
                }.addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error fetching data: ${e.message}", Toast.LENGTH_SHORT).show()
                }

            saveButton.setOnClickListener{
                val name = nameInputField.text.toString()
                val nim = nimInputField.text.toString()

                if(name.isNotEmpty() && nim.isNotEmpty()){
                    val userData = hashMapOf(
                        "name" to name,
                        "nim" to nim
                    )

                    db.collection("users").document(user.uid).set(userData)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Profile Updated", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Error updating information: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.main_container, ProfileFragment())
                        .addToBackStack(null)
                        .commit()
                } else {
                    Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }


            }
        }



    }


}