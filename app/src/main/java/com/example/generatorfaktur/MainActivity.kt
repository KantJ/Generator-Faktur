package com.example.generatorfaktur

import android.Manifest
import android.arch.persistence.room.Room
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.View
import com.beardedhen.androidbootstrap.TypefaceProvider
import com.example.generatorfaktur.DBManager.AppDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        TypefaceProvider.registerDefaultIconSets()
        supportActionBar!!.hide()

        //APLICATION ASK FOR PERMISSION TO WORK WITH FILES IN EXTERNAS
        //TODO rozwiazanie w razie gdy uzytkownik sie nie zgodzi
        val permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            val REQUEST_EXTERNAL_STORAGE = 1
            val PERMISSIONS_STORAGE =
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(
                this,
                PERMISSIONS_STORAGE,
                REQUEST_EXTERNAL_STORAGE
            )
        }
    }

    fun customerOnClick(view: View) {
        val myIntent = Intent(this, EntityActivity::class.java)
        startActivityForResult(myIntent, 997)
    }

    fun invoiceOnClick(view: View) {
        val myIntent = Intent(this, InvoiceActivity::class.java)
        startActivityForResult(myIntent, 998)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            997 -> {

            }
            998 -> {

            }
        }
    }

}
