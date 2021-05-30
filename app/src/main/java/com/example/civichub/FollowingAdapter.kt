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
import org.json.JSONObject
import java.lang.Exception
import java.util.*

public class FollowingAdapter (private val dataSet: Array<JSONObject>, private val context: Context) :
    RecyclerView.Adapter<FollowingAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val followingTitleText: TextView
        val followingDescriptionText: TextView
        val followingAddressText: TextView

        init {
            // Define click listener for the ViewHolder's View.
            followingTitleText = view.findViewById(R.id.followingTitleText)
            followingDescriptionText = view.findViewById(R.id.followingDescriptionText)
            followingAddressText = view.findViewById(R.id.followingAddressText)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowingAdapter.ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.following_cell, parent, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: FollowingAdapter.ViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element

        Log.d("holder", dataSet[position].getJSONObject("issue").getString("title"))
        Log.d("holder nyez", dataSet[position].getJSONObject("issue").getString("description"))
        holder.followingTitleText.text = dataSet[position].getJSONObject("issue").getString("title")
        holder.followingDescriptionText.text = dataSet[position].getJSONObject("issue").getString("description")
        val geocoder: Geocoder = Geocoder(context, Locale.getDefault())
        try{
            val addresses = geocoder.getFromLocation(dataSet[position].getJSONObject("issue")
                .getDouble("latitude"), dataSet[position].getJSONObject("issue")
                .getDouble("longitude"),5)
            holder.followingAddressText.text = addresses.get(0).getAddressLine(0)

        }catch (e: Exception){
            holder.followingAddressText.text = Resources.getSystem().getString(R.string.error_fetching_address)
        }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }
}