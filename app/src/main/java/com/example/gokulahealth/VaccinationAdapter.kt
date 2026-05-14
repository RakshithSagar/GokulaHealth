package com.example.gokulahealth

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gokulahealth.data.Vaccination
import java.text.SimpleDateFormat
import java.util.*

class VaccinationAdapter(private var list: List<Vaccination>) :
    RecyclerView.Adapter<VaccinationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvVaccineName)
        val date: TextView = view.findViewById(R.id.tvVaccineDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_vaccination, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val context = holder.itemView.context // Access context for string resources

        holder.name.text = item.vaccineName

        // --- 1. FORMAT THE DATE ---
        // Using Locale.getDefault() ensures the date format is appropriate for the region
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val formattedDate = sdf.format(Date(item.dueDate))

        // --- 2. LOCALIZED "DUE" LABEL ---
        // Uses the format string from strings.xml (e.g., "Due: %1$s" or "ದಿನಾಂಕ: %1$s")
        holder.date.text = context.getString(R.string.due_date_format, formattedDate)
    }

    override fun getItemCount() = list.size

    fun updateList(newList: List<Vaccination>) {
        list = newList
        notifyDataSetChanged()
    }
}