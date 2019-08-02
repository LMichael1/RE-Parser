package com.lmichael.dfa

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.beust.klaxon.Klaxon
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream




class MainActivity : AppCompatActivity() {

    var dfa: DFA? = DFA()
    lateinit var listView: ListView
    var history: ArrayList<String> = ArrayList()
    lateinit var adapter: ArrayAdapter<String>
    var filename: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

      //  button.isEnabled=false
        buttonMinimize.isEnabled=false

        listView = findViewById<View>(R.id.history) as ListView
        adapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_1,
            history
        )
        listView.setAdapter(adapter)

        floatingActionButton2.setOnClickListener {

            val intent = Intent()
                .setType("text/plain")
                .setAction(Intent.ACTION_GET_CONTENT)

            startActivityForResult(Intent.createChooser(intent, "Select a file"), 111)

        }
        findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.input_string).setOnEditorActionListener { v, actionId, event ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {

                    v?.hideKeyboard()
                    true
                }
                else -> false
            }
        }
        val permissionStatus = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)

        if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                0
            )
        }
    }
    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
    fun startDFA(view: View){
        view.hideKeyboard()
        val re = findViewById<TextView>(R.id.input_string2).text.toString()
        if (re!=""){
            try {
                val nfa = NFA()
                nfa.regexToNFA(re)
                dfa = nfa.Determine()
                dfa = dfa?.minimize()
                input_string.isEnabled = true
               // button.isEnabled=true
                buttonMinimize.isEnabled=false
                textView.text="Автомат создан"
                textView2.visibility=View.INVISIBLE
                history.clear()
                adapter.notifyDataSetChanged()
            }
            catch (e: Exception){
                input_string2.text?.clear()
                Toast.makeText(this, "Ошибка", Toast.LENGTH_SHORT).show()
                buttonMinimize.isEnabled = false
           //     input_string.isEnabled = false
                textView2.visibility = View.INVISIBLE
                history.clear()
                adapter.notifyDataSetChanged()
                return
            }
        }
        if (dfa == null || !dfa!!.checkDFA()){
            input_string2.text?.clear()
            Toast.makeText(this, "Ошибка", Toast.LENGTH_SHORT).show()
            buttonMinimize.isEnabled = false
           // input_string.isEnabled = false
            textView2.visibility = View.INVISIBLE
            history.clear()
            adapter.notifyDataSetChanged()
            return
        }
        if(dfa?.processString(input_string.text.toString())==true){
            textView2.text="ДА!"
            textView2.setTextColor(Color.parseColor("#3f51b5"))
            textView2.visibility=View.VISIBLE
        }
        else {
            textView2.text="НЕТ!"
            textView2.setTextColor(Color.parseColor("#7f0000"))
            textView2.visibility=View.VISIBLE
        }
        history.clear()
        //   history.add("ccccc")
        for (it: String in dfa?.history as ArrayList){
            history.add(it)
        }
        //  history=dfa?.history as ArrayList<String>
        adapter.notifyDataSetChanged()
    }
    fun startMinimization(view: View){
        val dfa1: DFA? = dfa?.minimize()
        if (dfa1?.checkDFA() as Boolean == false){
            val myToast = Toast.makeText(this, "Ошибка!", Toast.LENGTH_SHORT)
            myToast.show()
            return
        }

        val count: Int = dfa?.states?.count() as Int - dfa1.states.count()
        if(count==0){
            val myToast = Toast.makeText(this, "Минимизация не дала результата.", Toast.LENGTH_SHORT)
            buttonMinimize.isEnabled=false
            myToast.show()
            return
        }
        val myToast = Toast.makeText(this, "Минимизированно. Количество состояний уменьшено на " + count + ".", Toast.LENGTH_SHORT)
        myToast.show()

        dfa=dfa1

        val result = Klaxon().toJsonString(dfa)


       // val filename = "minimized.txt"
        // create a File object for the parent directory
        val dfaDirectory = File("/sdcard/documents/DFAs")
        // have the object build the directory structure, if needed.
        dfaDirectory.mkdirs()
        // create a File object for the output file
        val outputFile = File(dfaDirectory, filename+"_minimized.txt")
        // now attach the OutputStream to the file object, instead of a String representation
        try {
            val fos = FileOutputStream(outputFile)
            fos.write(result.toByteArray())
            fos.flush()
            fos.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        buttonMinimize.isEnabled=false
    }
/*    fun toastMe(view: View) {
        // val myToast = Toast.makeText(this, message, duration);
        val myToast = Toast.makeText(this, input_string.imeActionLabel, Toast.LENGTH_SHORT)
        myToast.show()
     //   getPublicAlbumStorageDir()
    } */



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 111 && resultCode == RESULT_OK) {
            val selectedFile = data?.data //The uri with the location of the file

            val file = File(selectedFile?.path)


            filename = file.nameWithoutExtension

            var istr = getContentResolver().openInputStream(selectedFile)
            dfa = null
            var nfa: NFA? = null
            var flag = false
            try {
                dfa = Klaxon().parse<DFA>(istr.bufferedReader().use { it.readText() })
            }
            catch (e: Exception){
                flag=true
            }
            if (dfa==null) flag = true
            else{
                if (dfa?.checkDFA()==false) flag=true
            }
            if (flag) {
                try {
                    istr = getContentResolver().openInputStream(selectedFile)
                    nfa = Klaxon().parse<NFA>(istr.bufferedReader().use { it.readText() })
                    flag = false
                } catch (e: Exception) {
                    flag = true
                }
            }
            if (flag) {
          //      button.isEnabled = false
                buttonMinimize.isEnabled = false
         //       input_string.isEnabled = false
                textView.text = "Автомат не загружен"
                textView2.visibility = View.INVISIBLE
                history.clear()
                adapter.notifyDataSetChanged()
                val myToast = makeText(this, "Ошибка чтения файла", Toast.LENGTH_SHORT)
                myToast.show()
            }
            if (nfa!=null){
                try{
                    dfa = nfa.Determine()
                }
                catch (e: Exception)
                {
                    val myToast = makeText(this, "Ошибка детерминизации", Toast.LENGTH_SHORT)
                    myToast.show()
             //       button.isEnabled=false
                    buttonMinimize.isEnabled=false
                    input_string.isEnabled=false
                    return
                }
                if (dfa == null)
                {
                    val myToast = makeText(this, "Ошибка детерминизации", Toast.LENGTH_SHORT)
                    myToast.show()
              //      button.isEnabled=false
                    buttonMinimize.isEnabled=false
                    input_string.isEnabled=false
                    return
                }
                val myToast = makeText(this, "NFA загружен и детерминизирован", Toast.LENGTH_SHORT)

                val result = Klaxon().toJsonString(dfa)
                val dfaDirectory = File("/sdcard/documents/DFAs")
                // have the object build the directory structure, if needed.
                dfaDirectory.mkdirs()
                // create a File object for the output file
                val outputFile = File(dfaDirectory, filename+"_determined.txt")
                // now attach the OutputStream to the file object, instead of a String representation
                try {
                    val fos = FileOutputStream(outputFile)
                    fos.write(result.toByteArray())
                    fos.flush()
                    fos.close()
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
                myToast.show()
            }
            if (dfa?.checkDFA()==true) {
                input_string.isEnabled = true
             //   button.isEnabled=true
                buttonMinimize.isEnabled=true
                textView.text="Автомат загружен"
                textView2.visibility=View.INVISIBLE
                history.clear()
                adapter.notifyDataSetChanged()
                val myToast = makeText(this, "Автомат загружен", Toast.LENGTH_SHORT)
                myToast.show()
            }
            else{
                button.isEnabled=false
                buttonMinimize.isEnabled=false
        //        input_string.isEnabled=false
                textView.text="Автомат не загружен"
                textView2.visibility=View.INVISIBLE
                val myToast = makeText(this, "Ошибка чтения", Toast.LENGTH_SHORT)
                myToast.show()
            }
            input_string.text?.clear()
            input_string2.text?.clear()
        }
    }

}

