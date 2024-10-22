package com.example.map_uts_lab

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class ProfileFragment : Fragment() {

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
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val emailView = view.findViewById<TextView>(R.id.email_view)
        val nameView = view.findViewById<TextView>(R.id.name_view)
        val nimView = view.findViewById<TextView>(R.id.nim_view)
        val updateProfileButton = view.findViewById<Button>(R.id.update_profile_button)
        val logoutButton = view.findViewById<Button>(R.id.logout_button)

        val user = auth.currentUser
        if (user != null) {
            emailView.text = user.email

            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val name = document.getString("name")
                        val nim = document.getString("nim")

                        if (name.isNullOrEmpty() || nim.isNullOrEmpty()) {
                            nameView.visibility = View.GONE
                            nimView.visibility = View.GONE
                        } else {
                            nameView.text = name
                            nimView.text = nim
                        }
                    }
                }
        }

        updateProfileButton.setOnClickListener{
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_container, UpdateProfileFragment())
                .commit()
        }

        logoutButton.setOnClickListener{
            auth.signOut()
            requireActivity().startActivity(Intent(requireContext(), AuthActivity::class.java))
            requireActivity().finish()
        }


    }


}