package com.example.kotlinartbook

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinartbook.databinding.RecyclerRowBinding

class ArtAdapter (val arrayList: ArrayList<Art>): RecyclerView.Adapter<ArtAdapter.ArtHolder>() {

    class ArtHolder(val binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root) {


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ArtHolder(binding)
    }

    override fun onBindViewHolder(holder: ArtHolder, position: Int) {
      holder.binding.recyclerRow.setText(arrayList.get(position).name)
        holder.itemView.setOnClickListener() {
            var intent = Intent(holder.itemView.context,ArtActivity::class.java)
            intent.putExtra("info","old")// Bu aktiviteye tıklandığında eskiden kaydolmuş bir kayıt gösterileceği için değeri "old" olarak yazdık.
            intent.putExtra("id",arrayList.get(position).id)
            holder.itemView.context.startActivity(intent)

        }
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }
}