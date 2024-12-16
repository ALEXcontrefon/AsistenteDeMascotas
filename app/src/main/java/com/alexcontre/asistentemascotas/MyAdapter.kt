package com.alexcontre.asistentemascotas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyAdapter(private val itemList: List<Any>) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemNameTextView: TextView = itemView.findViewById(R.id.itemNameTextView)
        val itemDetailsTextView: TextView = itemView.findViewById(R.id.itemDetailsTextView)
        val detailsLayout: LinearLayout = itemView.findViewById(R.id.detailsLayout)
        val expandButton: Button = itemView.findViewById(R.id.expandButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = itemList[position]

        when (item) {
            is MainActivity.Pet -> {
                holder.itemNameTextView.text = item.name
                holder.itemDetailsTextView.text = """
                    Breed: ${item.breed}
                    Age: ${item.age}
                    Type: ${item.type}
                    Sex: ${item.sex}
                """.trimIndent()
            }
            is MainActivity.Reminder -> {
                val petName = item.petName ?: "Unknown Pet"
                holder.itemNameTextView.text = item.title
                holder.itemDetailsTextView.text = """
        Description: ${item.description}
        Date: ${item.date}
        Pet: $petName
    """.trimIndent()
            }
            else -> {
                holder.itemNameTextView.text = "Unknown Item"
                holder.itemDetailsTextView.text = "No details available"
            }
        }

        holder.expandButton.setOnClickListener {
            if (holder.detailsLayout.visibility == View.GONE) {
                holder.detailsLayout.visibility = View.VISIBLE
                holder.expandButton.text = "-"
            } else {
                holder.detailsLayout.visibility = View.GONE
                holder.expandButton.text = "+"
            }
        }
    }

    override fun getItemCount() = itemList.size
}