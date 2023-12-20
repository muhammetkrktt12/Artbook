package com.example.kotlinartbook

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Binder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.ContentInfo
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.core.graphics.createBitmap
import androidx.core.view.ContentInfoCompat
import com.example.kotlinartbook.databinding.ActivityArtBinding
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.nio.file.attribute.AclFileAttributeView

class ArtActivity : AppCompatActivity() {
    lateinit var binding: ActivityArtBinding
    lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    lateinit var permissionLauncher: ActivityResultLauncher<String>;
    var selectedBitmap: Bitmap? = null
    lateinit var dataBase : SQLiteDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityArtBinding.inflate(layoutInflater)
        val view = binding.root
        super.onCreate(savedInstanceState)
        setContentView(view)
        registerLauncher()

        dataBase = this.openOrCreateDatabase("Arts", MODE_PRIVATE,null)

        val intent = intent
        val info = intent.getStringExtra("info")


        if (info.equals("new")){
            binding.artName.setText("")
            binding.artisName.setText("")
            binding.year.setText("")
            binding.saveButton.visibility = View.VISIBLE

        }else {

            //Eğer yeni bir şey kaydetmeyeceksek önceden kaydolanları burada göstereceğiz.
            binding.saveButton.visibility = View.INVISIBLE
            val selectedId = intent.getIntExtra("id",0)
            val cursor = dataBase.rawQuery("SELECT * FROM arts WHERE id = ?", arrayOf(selectedId.toString()))
            val artNameIx = cursor.getColumnIndex("artname")
            val artArtistIx = cursor.getColumnIndex("artisname")
            val yearIx = cursor.getColumnIndex("year")
            val imageIx = cursor.getColumnIndex("image")

            while (cursor.moveToNext()) {

                binding.artName.setText(cursor.getString(artNameIx))
                binding.artisName.setText(cursor.getString(artArtistIx))
                binding.year.setText(cursor.getString(yearIx))
                val byteArray = cursor.getBlob(imageIx)
                val bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
                binding.imageView.setImageBitmap(bitmap)

            }
            cursor.close()

        }


    }


    fun selectedImage(view:View) {

        if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            //requestPermission
            //rational
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)) {

                Snackbar.make(view,"Permission Needed For Gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give permission",View.OnClickListener {

                    //request Permission
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

                }).show();
            }
            else {

                //request permission
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }

        }
        else {
            //permission Granted
            val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intentToGallery)
            //Intent
        }
    }

    fun save(view: View) {

        val artName = binding.artName.text.toString()
        val artisName = binding.artisName.text.toString()
        val year = binding.year.text.toString()

        if(selectedBitmap != null) {

        val smallBitmap = makeSmallerBitmap(selectedBitmap!!,380)

            //Bir görseli veriye, yani 0 ve 1'lere çevirme
            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            var byteArray = outputStream.toByteArray()

            try {

               // val dataBase = this.openOrCreateDatabase("Arts", MODE_PRIVATE,null)
                dataBase.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY, artname VARCHAR,artisname VARCHAR,year VARCHAR,image BLOB)")
                val sqlString = "INSERT INTO arts (artname,artisname,year,image) VALUES (?,?,?,?)"
                val statement = dataBase.compileStatement(sqlString)// Bu fonksiyon ile gelen verileri üstteki soru işaretlerine teker teker bağladık.
                statement.bindString(1,artName)
                statement.bindString(2,artisName)
                statement.bindString(3,year)
                statement.bindBlob(4,byteArray)
                statement.execute()//uygulamak anlamına gelir.
            }catch (e:Exception){
                e.printStackTrace()
            }

            val intent = Intent(this@ArtActivity,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // Bundan önceki bütün aktiviteler kapanır.
            startActivity(intent)
        }

    }

    private fun registerLauncher() {

     activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

         if(result.resultCode == RESULT_OK) {

             val intentFromResult = result.data
             if (intentFromResult != null) {

                 val imageData = intentFromResult.data

                 if (imageData !=null) {

                     try {

                         if (Build.VERSION.SDK_INT >= 28) {
                             val source = ImageDecoder.createSource(this@ArtActivity.contentResolver,imageData)//content resolver ile veri istenir ve cursor ile veriler okunur.
                             selectedBitmap = ImageDecoder.decodeBitmap(source)
                             binding.imageView.setImageBitmap(selectedBitmap)
                         }
                         else {

                             selectedBitmap = MediaStore.Images.Media.getBitmap(contentResolver,imageData)
                             binding.imageView.setImageBitmap(selectedBitmap)
                         }

                     }catch (e:Exception) {

                         e.printStackTrace()
                     }

                 }

             }
         }
     }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {result ->

            if (result == true) {

                //Intent

                var intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
            else {
                //permissionDenied

                Toast.makeText(this,"Permission Needed",Toast.LENGTH_SHORT).show();
            }

        }
    }

    private fun makeSmallerBitmap(image:Bitmap,maximumSize:Int) : Bitmap {

       var width = image.width//300
       var height = image.height
        val bitmapRatio = width.toDouble()/height.toDouble()
        if(bitmapRatio>1) {

            width = maximumSize//420
            val scaleHeight = width / bitmapRatio
            height = scaleHeight.toInt()
        }
        else {

            height = maximumSize
            val scaleWidth = height*bitmapRatio
            width = scaleWidth.toInt()
        }

        return Bitmap.createScaledBitmap(image,width,height,true)
    }
}