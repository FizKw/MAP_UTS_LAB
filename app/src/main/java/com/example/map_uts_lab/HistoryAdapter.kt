package com.example.map_uts_lab

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class HistoryAdapter(private val entries: List<Entry>): RecyclerView.Adapter<HistoryAdapter.ViewHolder>(){
    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val emailView: TextView = view.findViewById(R.id.email_view)
        val dateView: TextView = view.findViewById(R.id.date_view)
        val timeView: TextView = view.findViewById(R.id.time_view)
        val entryView: TextView = view.findViewById(R.id.entry_type_text)
        val imageView: ImageView = view.findViewById(R.id.image_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_absen, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = entries[position]
        holder.emailView.text = entry.email
        holder.dateView.text = entry.date
        holder.timeView.text = entry.time
        holder.entryView.text = entry.entryType
        Glide.with(holder.itemView.context).load(entry.image).into(holder.imageView)
    }

    override fun getItemCount() = entries.size

}
