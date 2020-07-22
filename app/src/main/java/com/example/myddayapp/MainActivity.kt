package com.example.myddayapp

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.toast
import org.jetbrains.anko.yesButton


class MainActivity : AppCompatActivity() {
    private val OPEN_GALLERY = 1
    private val REQUEST_READ_EXTERNAL_STORAGE = 1000
    private var uri : String? =""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Glide.with(this).load(File("/storage/emulated/0/Pictures/KakaoTalk/1595329958618.jpg").toString()).into(imageView)

        loadData()
        openButton.setOnClickListener {
            askPermission()

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            R.id.action_showListview -> {
                val intent1 = Intent(this, ListActivity::class.java)
                startActivity(intent1)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openGallery() {
        val intent: Intent = Intent(Intent.ACTION_PICK)
        intent.setType("image/*")

        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        //갤러리든 다른 사진 어플이든 갤러리 어플 선택할 수 있도록 하는 createChooser 사용
        startActivityForResult(Intent.createChooser(intent, "Get Album"), OPEN_GALLERY)

    }

    private fun askPermission() {
        //권한을 먼저 물어보는 것
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                alert("사진 정보를 얻으려면 외부 저장소 권한이 필수로 필요합니다", "권한이 필요한 이유") {
                    yesButton {
                        //권한 요청
                        ActivityCompat.requestPermissions(
                            this@MainActivity,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            REQUEST_READ_EXTERNAL_STORAGE
                        )
                    }
                    noButton { }
                }.show()
            } else {
                //권한 요청
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_READ_EXTERNAL_STORAGE
                )
            }
        } else {
            //권한이 이미 허용된 것
            openGallery()
        }
    }
    private fun getRealPathFromURI(uri: Uri): String? {
        var columnIndex = 0
        var proj = arrayOf(MediaStore.Images.Media.DATA)
        var cursor = contentResolver.query(uri, proj, null, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            }
        }
        return cursor?.getString(columnIndex)
    }
    private fun saveData(uri : String?){
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = pref.edit()

        editor.putString("SaveUri",uri).apply()
    }
    private fun loadData()  {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val saveuri = pref.getString("SaveUri","0")

        if(saveuri != "0") {
            val savepath = saveuri?.let { getUriFromPath(it) }
            try {
                Glide.with(this).load(savepath).into(imageView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Log.d("REALPATH22", "${savepath}")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == OPEN_GALLERY) {
                var currentImageUrl: Uri? = data?.data

                uri = currentImageUrl?.let { getRealPathFromURI(it) }
                Log.d("REALPATH","${uri}")
                saveData(uri)
                try {
                    Glide.with(this).load(currentImageUrl).into(imageView)
                    //Glide.with(this).load(uri).into(imageView)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            Log.d("ActivityResult", "wrong")
        }
    }

    fun getUriFromPath(filePath: String): Uri? {
        val cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null, "_data = '$filePath'", null, null
        )
        cursor!!.moveToNext()
        val id = cursor.getInt(cursor.getColumnIndex("_id"))
        return ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toLong())
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_READ_EXTERNAL_STORAGE -> {
                if ((grantResults.isNotEmpty()
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    askPermission()
                } else {
                    toast("권한 거부 됨")
                }
            }
        }
    }
}