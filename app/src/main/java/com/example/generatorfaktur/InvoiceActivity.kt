package com.example.generatorfaktur

import android.content.DialogInterface
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import com.beardedhen.androidbootstrap.BootstrapEditText
import com.beardedhen.androidbootstrap.TypefaceProvider
import com.example.generatorfaktur.DBManager.BasicDBManager
import com.example.generatorfaktur.invBuilder.AbstractInvcBuilder
import com.example.generatorfaktur.invBuilder.InvcBuilder
import com.example.generatorfaktur.invoiceProperties.Entity
import com.example.generatorfaktur.invoiceProperties.InvoiceItem
import kotlinx.android.synthetic.main.content_invoice1.*
import kotlinx.android.synthetic.main.invoice_parametrs_dialog.*
import kotlinx.android.synthetic.main.item_dialog.*

class InvoiceActivity : AppCompatActivity() {


    var itemList = ArrayList<InvoiceItem>()
    private lateinit var itemArrayAdapter: ItemArrayAdapter
    lateinit var builder: AbstractInvcBuilder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_invoice)
        TypefaceProvider.registerDefaultIconSets()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        itemArrayAdapter = ItemArrayAdapter(this, itemList)

        itemArrayAdapter = ItemArrayAdapter(this, itemList)
        itemListView.adapter = itemArrayAdapter

        builder = InvcBuilder(applicationContext)



    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.invoice_create_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_create -> {
            val buyer = Entity(
                buyerNameText.text.toString(),
                buyerAdressText.text.toString(),
                buyerPostalText.text.toString(),
                buyerPhoneText.text.toString(),
                buyerNIPText.text.toString())

            //TODO sprzedawca
            val seller = Entity("Tomek", "Stodola", "64-600", "432432423", "")

            val reicipient = Entity(
                recipientNameText.text.toString(),
                recipientAdressText.text.toString(),
                recipientPostalText.text.toString(),
                recipientPhoneText.text.toString(),
                recipientNIPText.text.toString())

            builder.setBuyer(buyer).setDealer(seller).setReicipient(reicipient)

            //TODO currentdate i ID
            builder.setProperties("23-04-1004", "siema")
            builder.setPaymentProperty("a", "22-33-4444", "c", "d")


            PropertyDialog()

            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    fun buyerOnClick(view: View) {
        choosePersonDialog("buyer")
    }

    fun recipientOnClick(view: View) {
        choosePersonDialog("recipient")
    }



    //Dialog wyświetlający listę posiadanych w bazie klientów
    //Pozwala przejść do dialogu dodającego nowego klienta
    fun choosePersonDialog(who: String) {
        val builder = AlertDialog.Builder(this)
        if(who == "buyer") {
            builder.setTitle("Wybierz nabywcę")
        } else {
            builder.setTitle("Wybierz odbiorcę")
        }

        val dataBase = BasicDBManager(this)
        val entityList = ArrayList<Entity>()
        val entityArrayAdapter = EntityArrayAdapter(this, entityList)


        AsyncTask.execute {
            entityList.clear()
            entityList.addAll(dataBase.getAllEntity())
            runOnUiThread {
                entityArrayAdapter.notifyDataSetChanged()
            }
        }

        builder
            .setSingleChoiceItems(entityArrayAdapter, -1, object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                val result = ArrayList<String>()
                result.add(entityList[which].name)
                result.add(entityList[which].nip)
                result.add(entityList[which].address)
                result.add(entityList[which].postal)
                result.add(entityList[which].phoneNumber)
                setTexts(result, who)
                dialog?.dismiss()
            }
        })
            .setCancelable(true)
            .setPositiveButton("NOWY") { _, _ ->
                doDialog(who)
            }
        val alertDialog = builder.create()
        alertDialog.show()

    }

    //Dialog wybierający metodę płatności i kończący generowanie faktury
    fun PropertyDialog () {
        val li = LayoutInflater.from(this)
        val dialog = li.inflate(R.layout.invoice_parametrs_dialog, null)
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setView(dialog)
        alertDialogBuilder
            .setCancelable(true)
            .setPositiveButton("Zatwierdź") { _, _ ->
                //TODO date + ID + sprzedawca
                when (dialog.findViewById<RadioGroup>(R.id.paymentGroup).checkedRadioButtonId) {
                    R.id.paymentCashButton -> {
                        builder.setPaymentProperty("Gotówka", "22-33-4444", "a", "d")
                    }
                    else -> {
                        builder.setPaymentProperty("Przelew", "22-33-4444", "a", "d")
                    }
                }
                builder.setProperties("23-04-1004", "siema")
                val result = builder.generate()

                val myIntent = Intent(this, PreviewActivity::class.java)
                myIntent.putExtra("HTML", result)
                myIntent.putExtra("ID", builder.invoice.invoiceId)
                startActivity(myIntent)
            }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }


    //Uruchamia dialog do wypełnienia danych klienta/sprzedającego
    fun doDialog(who: String){
        val li = LayoutInflater.from(this)
        val dialog = li.inflate(R.layout.fab_dialog, null)
        val result = ArrayList<String>()
        val alertDialogBuilder = AlertDialog.Builder(this)

        alertDialogBuilder.setView(dialog)

        alertDialogBuilder
            .setCancelable(true)
            .setPositiveButton("DODAJ") { _, _ ->
                result.add(dialog.findViewById<BootstrapEditText>(R.id.entityName).text.toString())
                result.add(dialog.findViewById<BootstrapEditText>(R.id.entityNIP).text.toString())
                result.add(dialog.findViewById<BootstrapEditText>(R.id.entityAddress).text.toString())
                result.add(dialog.findViewById<BootstrapEditText>(R.id.entityPostal).text.toString())
                result.add(dialog.findViewById<BootstrapEditText>(R.id.entityPhone).text.toString())

                setTexts(result, who)

            }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    //Ustawia TextViews w zależności od wprowadzonych danych po zamknięciu dialogu.
    //Można wykorzystać, przesyłając w Intencie z Activity klientów, dla wygenerowania faktury konkretnemu klientowi
    //Można wypełniać dane sprzedającego automatycznie, wczytując liste stringów z danymi dla Dealera, np. z shared preferences
    fun setTexts(data: ArrayList<String>, who: String) {
        when (who) {
            "recipient" -> {
                recipientNameText.text = data[0]
                recipientNIPText.text = data[1]
                recipientAdressText.text = data[2]
                recipientPostalText.text = data[3]
                recipientPhoneText.text = data[4]
            }
            "buyer" -> {
                buyerNameText.text = data[0]
                buyerNIPText.text = data[1]
                buyerAdressText.text = data[2]
                buyerPostalText.text = data[3]
                buyerPhoneText.text =data[4]
            }
        }
    }




    //Odpowiada za FAB na liście itemów
    fun itemFABOnClick(view: View) {
        val li = LayoutInflater.from(this)
        val dialog = li.inflate(R.layout.item_dialog, null)

        val alertDialogBuilder = AlertDialog.Builder(this)

        alertDialogBuilder.setView(dialog)

        alertDialogBuilder
            .setCancelable(true)
            .setPositiveButton("DODAJ") {  _, _ ->

                //TODO add validator

                itemList.add( builder.addInvoiceItem(
                    dialog.findViewById<EditText>(R.id.itemName).text.toString(),
                    dialog.findViewById<EditText>(R.id.itemPrice).text.toString().toDouble(),
                    dialog.findViewById<EditText>(R.id.itemQuantity).text.toString().toDouble(),
                    dialog.findViewById<EditText>(R.id.itemVAT).text.toString().toDouble()/100))
                itemArrayAdapter.notifyDataSetChanged()

                Snackbar.make(view, "Dodano przedmiot.", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show()

            }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()

    }



}