package com.example.myapplication.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.adapter.LyricsAdapter
import com.example.myapplication.addsong.AddSong
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.model.LyricsModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Home : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: LyricsAdapter
    private var check = false
    lateinit var data: MutableList<LyricsModel>
    val TAG = "bro"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        Log.i(TAG, "onCreateView: ")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i(TAG, "onViewCreated: ")
        binding.floatBtn.setOnClickListener {
            if (check) {
                binding.floatBtn.setImageResource(R.drawable.ic_baseline_add_24)
                binding.tvAddsong.visibility = View.GONE
                check = false
            } else {
                binding.floatBtn.setImageResource(R.drawable.ic_baseline_clear_24)
                binding.tvAddsong.alpha = 0f
                binding.tvAddsong.animate().setDuration(500).alpha(1f)
                binding.tvAddsong.visibility = View.VISIBLE
                check = true
            }
        }

        binding.tvAddsong.setOnClickListener {
            startActivity(Intent(context, AddSong::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        Log.i(TAG, "onStart: ")
        data = mutableListOf()
        adapter = LyricsAdapter(data)
        adapter.mContext = requireContext()
        binding.recyclerChart.layoutManager = LinearLayoutManager(context)
        binding.recyclerChart.adapter = adapter
        init()
    }

    private fun init() {
        Log.i(TAG, "init: ")
        val song = FirebaseDatabase.getInstance().reference.child("ListSong").orderByChild("count")
            .limitToLast(10)
        song.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                data.clear()
                for (dataSnapshot: DataSnapshot in snapshot.children) {
                    val value = dataSnapshot.getValue(LyricsModel::class.java)
                    if (value != null) {
                        data.add(value)
                    }
                }
                data.reverse()
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}