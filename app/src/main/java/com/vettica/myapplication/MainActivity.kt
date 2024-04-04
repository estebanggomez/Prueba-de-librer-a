package com.vettica.myapplication

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.annotation.RequiresApi
import co.maasapp.lib_api_maas.nfc_api
import java.util.*

@RequiresApi(Build.VERSION_CODES.S)
class MainActivity : AppCompatActivity() {
    private lateinit var nfcAdapter: NfcAdapter
    private val pendingIntent: PendingIntent by lazy {
        PendingIntent.getActivity(
            this, 0,
            Intent(this, this::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE
        )
    }
    private var isoDep: IsoDep? = null
    private val NFC = nfc_api()

    private lateinit var tvId: TextView
    private lateinit var tvTipo: TextView
    private lateinit var tvSaldo: TextView
    private lateinit var tvLastTx: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvId = findViewById(R.id.TvId)
        tvTipo = findViewById(R.id.TvTipo)
        tvSaldo = findViewById(R.id.TvSaldo)
        tvLastTx = findViewById(R.id.TvLastTx)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Log.e("maasPrint", "El dispositivo no soporta NFC")
            finish()
        }
    }

    override fun onResume() {
        super.onResume()

        val techDiscovered = IntentFilter().apply {
            addAction(NfcAdapter.ACTION_TECH_DISCOVERED)
            addCategory(Intent.CATEGORY_DEFAULT)
        }

        val techList = arrayOf(
            arrayOf(android.nfc.tech.IsoDep::class.java.name)
        )

        nfcAdapter.enableForegroundDispatch(this, pendingIntent, arrayOf(techDiscovered), techList)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_TECH_DISCOVERED == intent.action) {
            isoDep = IsoDep.get(intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG) as Tag)
            val cardId = NFC.responceCardId(isoDep!!)
            val saldo = NFC.responceSaldo(isoDep!!)
            val todos = NFC.responceLastTransaction(isoDep!!)
            Log.e("ID", cardId[0])
            Log.e("Tipo", cardId[1])
            Log.e("saldo", saldo)
            Log.e("LastTransactions", Arrays.toString(todos))

            tvId.text = cardId[0]
            tvTipo.text = cardId[1]
            tvSaldo.text = saldo
            tvLastTx.text = Arrays.toString(todos)
        }
    }
}