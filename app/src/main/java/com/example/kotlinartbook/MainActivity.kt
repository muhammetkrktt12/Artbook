package com.example.kotlinartbook

import android.content.Intent
import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kotlinartbook.databinding.ActivityMainBinding
import kotlin.Exception

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var arrayList: ArrayList<Art>
    private lateinit var artAdapter: ArtAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        super.onCreate(savedInstanceState)
        setContentView(view)

        arrayList = ArrayList<Art>()
        artAdapter = ArtAdapter(arrayList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = artAdapter
        try {

            val database = this.openOrCreateDatabase("Arts", MODE_PRIVATE,null)
            val cursor = database.rawQuery("SELECT * FROM arts",null)
            val nameIx = cursor.getColumnIndex("artname")
            val id = cursor.getColumnIndex("id")

            while (cursor.moveToNext()) {

                val name = cursor.getString(nameIx)
                val id = cursor.getInt(id)
                val art = Art(name,id)
                arrayList.add(art)

            }
                artAdapter.notifyDataSetChanged()
            cursor.close()


        }catch (e:Exception) {
            e.printStackTrace()
        }


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val menuItem = menuInflater
        menuItem.inflate(R.menu.art_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
   if(item.itemId == R.id.art_menu_item) {

       val intent = Intent(this@MainActivity,ArtActivity::class.java);
       intent.putExtra("info","new")
       startActivity(intent)
   }
        return super.onOptionsItemSelected(item)
    }
}