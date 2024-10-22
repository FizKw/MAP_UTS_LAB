package com.example.map_uts_lab

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.replace
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class HomePageFragment : Fragment() {

    private lateinit var dateView: TextView
    private lateinit var timeView: TextView
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dateView = view.findViewById(R.id.date_view)
        timeView = view.findViewById(R.id.time_view)

        updateDateTime()

        val absenButton = view.findViewById<Button>(R.id.button_absen)
        absenButton.setOnClickListener{
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_container, CameraFragment())
                .addToBackStack(null)
                .commit()
        }

    }

    private fun updateDateTime(){
        val dateFormat = SimpleDateFormat("EEEE, dd/MM/yyyy", Locale("id", "ID"))
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        val currentDate = Date()
        dateView.text = dateFormat.format(currentDate)
        timeView.text = timeFormat.format(currentDate)

        handler.postDelayed({updateDateTime()}, 1000)


    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
    }


}