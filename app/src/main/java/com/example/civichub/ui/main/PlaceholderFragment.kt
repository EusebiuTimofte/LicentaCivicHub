package com.example.civichub.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.civichub.ItemDecorationFollowing
import com.example.civichub.R
import com.example.civichub.TopAdapter
import com.example.civichub.databinding.FragmentTopsBinding

/**
 * A placeholder fragment containing a simple view.
 */
class PlaceholderFragment : Fragment() {

    private lateinit var pageViewModel: PageViewModel
    private var _binding: FragmentTopsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageViewModel = ViewModelProvider(this).get(PageViewModel::class.java).apply {
            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1, context!!)
            //in functie de arg_section_number, cer standard, audit sau aitoritate si apelez set users list
            // in on create view iau recycler view si adapterul si setez la adapter
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentTopsBinding.inflate(inflater, container, false)
        val root = binding.root

//        val textView: TextView = binding.sectionLabel
//        pageViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })
        val recyclerView: RecyclerView = binding.topRecyclerView
        val viewManager = LinearLayoutManager(this.context)
        pageViewModel.users.observe(viewLifecycleOwner, {
            recyclerView.apply {
                setHasFixedSize(true)
                layoutManager = viewManager
                adapter = TopAdapter(it)
                addItemDecoration(ItemDecorationFollowing(2))
            }
        })
        return root
    }

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(sectionNumber: Int): PlaceholderFragment {
            return PlaceholderFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}