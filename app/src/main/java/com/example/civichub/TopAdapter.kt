package com.example.civichub

import android.content.Context
import android.content.res.Resources
import android.location.Geocoder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.util.*

class TopAdapter (private val dataSet: JSONArray) :
    RecyclerView.Adapter<TopAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val positionTextView: TextView
        val mailTextView: TextView
        val pointsTextView: TextView

        init {
            // Define click listener for the ViewHolder's View.
            positionTextView = view.findViewById(R.id.positionTextView)
            mailTextView = view.findViewById(R.id.mailTextView)
            pointsTextView = view.findViewById(R.id.pointsTextView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopAdapter.ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.top_cell, parent, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: TopAdapter.ViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element

        holder.positionTextView.text = (position+1).toString()
        holder.mailTextView.text = dataSet.getJSONObject(position).getString("mail")
        holder.pointsTextView.text = dataSet.getJSONObject(position).getInt("points").toString()
    }

    override fun getItemCount(): Int {
        return dataSet.length()
    }
}