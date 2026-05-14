package com.example.gokulahealth

import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.gokulahealth.data.Cattle
import java.io.File

class CattleAdapter(
    private var cattleList: List<Cattle>,
    private val onDeleteClicked: (Cattle) -> Unit
) : RecyclerView.Adapter<CattleAdapter.CattleViewHolder>() {

    class CattleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPhoto: ImageView = view.findViewById(R.id.ivRowPhoto)
        val tvTag: TextView = view.findViewById(R.id.tvRowTag)
        val tvName: TextView = view.findViewById(R.id.tvRowName)
        val tvBreed: TextView = view.findViewById(R.id.tvRowBreed)
        val tvDob: TextView = view.findViewById(R.id.tvRowDob)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteCattle)
        val btnHistory: Button = view.findViewById(R.id.btnViewHistory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CattleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cattle, parent, false)
        return CattleViewHolder(view)
    }

    override fun onBindViewHolder(holder: CattleViewHolder, position: Int) {
        val cow = cattleList[position]
        val context = holder.itemView.context // Required to access string resources

        // --- 1. LOCALIZED TEXT BINDING ---
        // These use the %1$s placeholders we defined in strings.xml
        holder.tvTag.text = context.getString(R.string.tag_id_format, cow.earTagId)
        holder.tvBreed.text = context.getString(R.string.breed_format, cow.breed)
        holder.tvDob.text = context.getString(R.string.born_format, cow.dateOfBirth)
        holder.tvName.text = cow.name // Names don't usually need translation

        // --- 2. IMAGE & FULL SCREEN LOGIC ---
        if (cow.imagePath != null) {
            val imgFile = File(cow.imagePath!!)
            if (imgFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                holder.ivPhoto.setImageBitmap(bitmap)

                holder.ivPhoto.setOnClickListener {
                    val intent = Intent(context, FullScreenImageActivity::class.java)
                    intent.putExtra("IMAGE_PATH", cow.imagePath)
                    context.startActivity(intent)
                }
            }
        } else {
            holder.ivPhoto.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        // --- 3. HEALTH RECORD CLICK LOGIC ---
        holder.btnHistory.setOnClickListener {
            val intent = Intent(context, VaccinationHistoryActivity::class.java)
            intent.putExtra("CATTLE_ID", cow.id)
            intent.putExtra("CATTLE_NAME", cow.name)
            context.startActivity(intent)
        }

        // --- 4. DELETE CLICK LOGIC ---
        holder.btnDelete.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Delete Record") // You can also localize this in strings.xml later
                .setMessage("Are you sure you want to remove ${cow.name}?")
                .setPositiveButton("Delete") { _, _ ->
                    onDeleteClicked(cow)
                }
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
        }
    }

    override fun getItemCount() = cattleList.size

    fun updateList(newList: List<Cattle>) {
        this.cattleList = newList
        notifyDataSetChanged()
    }
}