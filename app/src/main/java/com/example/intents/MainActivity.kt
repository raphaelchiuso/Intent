package com.example.intents

import android.Manifest.permission.CALL_PHONE
import android.content.Intent
import android.content.Intent.ACTION_CALL
import android.content.Intent.ACTION_CHOOSER
import android.content.Intent.ACTION_DIAL
import android.content.Intent.ACTION_PICK
import android.content.Intent.ACTION_VIEW
import android.content.Intent.EXTRA_INTENT
import android.content.Intent.EXTRA_TITLE
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Environment.DIRECTORY_PICTURES
import android.telephony.ims.RegistrationManager
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.intents.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val amb: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var parl: ActivityResultLauncher<Intent>
    private lateinit var pcarl: ActivityResultLauncher<String>
    private lateinit var piarl: ActivityResultLauncher<Intent>

    companion object Constantes {
        const val PARAMETRO_EXTRA = "PARAMETRO_EXTRA"
        const val PARAMETRO_REQUEST_CODE = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(amb.root)

        parl = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let {

                }
            }
        }
        pcarl = registerForActivityResult(ActivityResultContracts.RequestPermission()){
            permissaoConcedida ->
            if (permissaoConcedida) {
                chamarOuDiscar(true)
            }
            else {
                Toast.makeText(this, "Permissão Necessária!", Toast.LENGTH_SHORT).show();
            }
        }
        piarl = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            resultado ->
            if (resultado.resultCode == RESULT_OK) {
                resultado.data?.data?.let {
                    startActivity(Intent(ACTION_VIEW, it))
                }
            }

        }

        setSupportActionBar(amb.toolbarTb)
        supportActionBar?.apply {
            title = getString(R.string.app_name)
            subtitle = this@MainActivity.javaClass.canonicalName
        }

//        JAVA
        amb.entrarParametroBt.setOnClickListener {
            val parametroIntent = Intent(this, ParametroActivity::class.java)
            parametroIntent.putExtra(PARAMETRO_EXTRA, amb.parametroTv.text.toString())
            startActivityForResult(parametroIntent, PARAMETRO_REQUEST_CODE)
        }

        Intent(this, ParametroActivity::class.java).apply {
            amb.parametroTv.text.toString().let{
                putExtra(PARAMETRO_EXTRA, it)
            }
            startActivityForResult(this, PARAMETRO_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PARAMETRO_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.getStringExtra(PARAMETRO_EXTRA)?.let {
                retorno ->
                amb.parametroTv.text = retorno
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.viewMi -> {
                // JAVA
                val url: Uri = Uri.parse(amb.parametroTv.text.toString())
                val navegadorIntent = Intent(ACTION_VIEW, url)
                startActivity(navegadorIntent)
                true
            }
            R.id.callMi -> {
                if (checkSelfPermission(CALL_PHONE) == PERMISSION_GRANTED) {
                    chamarOuDiscar(true)
                } else {
                    pcarl.launch(CALL_PHONE)
                }
                true
            }
            R.id.dialMi -> {
                // KOTLIN
                chamarOuDiscar(false)
                true
            }
            R.id.pickMi -> {
                val caminho = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES).path
                val pegarImagemIntent = Intent(ACTION_PICK);
                pegarImagemIntent.setDataAndType(Uri.parse(caminho), "image/*")
                piarl.launch(pegarImagemIntent)
                true
            }
            R.id.choosenMi -> {
                Uri.parse(amb.parametroTv.text.toString()).let {
                    Intent(ACTION_VIEW, it).let {
                        navegadorIntent ->
                        val escolherAppIntent = Intent(ACTION_CHOOSER)
                        escolherAppIntent.putExtra(EXTRA_TITLE, "Escolha seu navegador")
                        escolherAppIntent.putExtra(EXTRA_INTENT, navegadorIntent)
                        startActivity(escolherAppIntent)
                    }
                }
                true
            }
            else -> { false }
        }
    }

    private fun chamarOuDiscar(chamar: Boolean) {
        Uri.parse("tel: ${amb.parametroTv.text}").let {
            Intent(if (chamar) ACTION_CALL else ACTION_DIAL).apply {
                data = it
                startActivity(this)
            }
        }
    }
}