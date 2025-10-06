package com.example.lista3era

import android.os.Bundle
import android.text.InputFilter
import android.text.method.DigitsKeyListener
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var spinnerOpciones: Spinner
    private lateinit var spinnerUnidad: Spinner
    private lateinit var spinnerConductores: Spinner
    private lateinit var KmSalida: EditText
    private lateinit var KmLlegada: EditText
    private lateinit var editObac: EditText
    private lateinit var editDireccion: EditText
    private lateinit var editBomberos: EditText
    private lateinit var editObservaciones: EditText
    private lateinit var btnEnviar: Button
    private lateinit var btnNuevaLista: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // ================== REFERENCIAS ==================
        spinnerOpciones = findViewById(R.id.spinner2)
        spinnerUnidad = findViewById(R.id.spinner3)
        spinnerConductores = findViewById(R.id.spinner4)
        KmSalida = findViewById(R.id.editTextText3)
        KmLlegada = findViewById(R.id.editTextText4)
        editObac = findViewById(R.id.editTextText)
        editDireccion = findViewById(R.id.editTextText2)
        editBomberos = findViewById(R.id.editTextText6)
        editObservaciones = findViewById(R.id.editTextText5)
        btnEnviar = findViewById(R.id.button)

        // ================= SPINNER EMERGENCIAS =================
        val opciones = arrayOf(
            "Seleccione El Tipo De Emergencia",
            "1° Alarma de Incendio", "2° Alarma de Incendio", "3° Alarma de Incendio",
            "10-0-1", "10-0-2", "10-0-3", "10-0-4",
            "10-1-1", "10-1-2",
            "10-2-1", "10-2-2",
            "10-3-1", "10-3-2", "10-3-3", "10-3-5",
            "10-4-1", "10-4-2", "10-4-3",
            "10-5-1", "10-5-2", "10-5-3", "10-5-4",
            "10-6-1", "10-6-2",
            "10-7", "10-8",
            "Acuartelamiento grado 1°", "Acuartelamiento grado 2°", "Acuartelamiento grado 3°"
        )
        spinnerOpciones.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opciones)

        // ================= SPINNER UNIDAD =================
        val unidades = arrayOf("Seleccione unidad", "H-3", "BX-3", "B-3")
        spinnerUnidad.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, unidades)

        // ================= SPINNER CONDUCTORES =================
        val conductores = arrayOf(
            "Seleccione conductor",
            "309 (Eusebio Madariaga)",
            "305 (Juan Carlos Toro)",
            "C-6 (Jimmy Salinas)",
            "C-7 (Jorge Nilo)",
            "337 (Antonio Hinojosa)"
        )
        spinnerConductores.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, conductores)

        // ================= CAMPOS KM =================
        KmSalida.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        KmLlegada.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        KmSalida.keyListener = DigitsKeyListener.getInstance("0123456789")
        KmLlegada.keyListener = DigitsKeyListener.getInstance("0123456789")

        val digitsOnlyFilter = InputFilter { source, start, end, _, _, _ ->
            for (i in start until end) {
                if (!Character.isDigit(source[i])) {
                    Toast.makeText(this, "Solo se aceptan números", Toast.LENGTH_SHORT).show()
                    return@InputFilter ""
                }
            }
            null
        }
        KmSalida.filters = arrayOf(digitsOnlyFilter)
        KmLlegada.filters = arrayOf(digitsOnlyFilter)

        // ================= BOTÓN ENVIAR =================
        btnEnviar.setOnClickListener {
            val obac = editObac.text.toString()
            val emergencia = spinnerOpciones.selectedItem.toString()
            val unidad = spinnerUnidad.selectedItem.toString()
            val direccion = editDireccion.text.toString()
            val conductor = spinnerConductores.selectedItem.toString()
            val kmSalida = KmSalida.text.toString()
            val kmLlegada = KmLlegada.text.toString()
            val bomberos = editBomberos.text.toString()
            val observaciones = editObservaciones.text.toString()

            if (obac.isEmpty() || direccion.isEmpty() || bomberos.isEmpty() ||
                kmSalida.isEmpty() || kmLlegada.isEmpty() ||
                emergencia.contains("Seleccione") || unidad.contains("Seleccione") || conductor.contains("Seleccione")) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            } else {
                enviarDatos(
                    obac, emergencia, unidad, direccion,
                    conductor, kmSalida, kmLlegada, bomberos, observaciones
                )
            }
        }
        // ================= btn nueva lista =================
        btnNuevaLista = findViewById(R.id.button2)

        btnNuevaLista.setOnClickListener {
            // Limpiar todos los EditText
            editObac.text.clear()
            editDireccion.text.clear()
            editBomberos.text.clear()
            editObservaciones.text.clear()
            KmSalida.text.clear()
            KmLlegada.text.clear()

            // Resetear los spinners a la primera opción
            spinnerOpciones.setSelection(0)
            spinnerUnidad.setSelection(0)
            spinnerConductores.setSelection(0)

            Toast.makeText(this, "Lista lista para un nuevo registro", Toast.LENGTH_SHORT).show()
        }


        // ================= AJUSTE INSETS =================
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // ================= FUNCIÓN PARA ENVIAR DATOS =================
    private fun enviarDatos(
        obac: String, emergencia: String, unidad: String, direccion: String,
        conductor: String, kmSalida: String, kmLlegada: String,
        bomberos: String, observaciones: String
    ) {
        val url = "http://192.168.1.115/lista3era_app/insertar.php"

        val queue = Volley.newRequestQueue(this)
        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                Toast.makeText(this, "Servidor: $response", Toast.LENGTH_LONG).show()
            },
            { error ->
                Toast.makeText(this, "Error al enviar: ${error.message}", Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["obac"] = obac
                params["emergencia"] = emergencia
                params["unidad"] = unidad
                params["direccion"] = direccion
                params["conductor"] = conductor
                params["km_salida"] = kmSalida
                params["km_llegada"] = kmLlegada
                params["bomberos"] = bomberos
                params["observaciones"] = observaciones
                val locale = Locale.getDefault().language  // Devuelve "es", "en", etc.
                params["idioma"] = locale
                return params
            }
        }
        queue.add(stringRequest)
    }
}
