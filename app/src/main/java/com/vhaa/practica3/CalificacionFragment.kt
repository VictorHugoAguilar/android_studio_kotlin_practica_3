package com.vhaa.practica3

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.snackbar.Snackbar
import com.vhaa.practica3.databinding.FragmentCalificacionBinding
import helpers.FileHelper
import java.text.DecimalFormat

/**
 * Clase de Calificación Fragment
 *
 * @author Victor Hugo Aguilar Aguilar
 */
class CalificacionFragment : Fragment() {
    private lateinit var binding: FragmentCalificacionBinding
    private var note1: Float = 0.0f
    private var note2: Float = 0.0f
    private var note3: Float = 0.0f
    private var day: Int = 1
    private var month: Int = 1
    private var year: Int = 1970

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCalificacionBinding.inflate(inflater,container, false )

        binding.btnInDateNote.setOnClickListener {
            launchFillDataActivity()
        }

        binding.btnCalcular.setOnClickListener {
            calculateResults()
        }

        binding.btnSaveHistory.setOnClickListener {
             alertDialogSavedHistoric(getString(R.string.calification_fragment_alert), getString(R.string.calification_fragment_alert_message))
        }

        return binding.root
    }

    companion object {
        const val STUDENT_NAME = "studentName"
        const val STUDENT_DATA = "studentData"
        const val NOTE_ONE = "note_1"
        const val NOTE_TWO = "note_2"
        const val NOTE_TREE = "note_3"
        const val DATE_DAY = "date_day"
        const val DATE_MONTH = "date_month"
        const val DATE_YEAR = "date_year"
    }

    /**
     * Lanzador de la pantalla para obteción de los datos
     */
    private fun launchFillDataActivity() {
        // Create object type intent
        val fillData = Intent(context, NameDateActivity::class.java).apply {
            putExtra(STUDENT_NAME, binding.inputNombre.text.toString())
        }
        // launch activity
        getDataDateAndNotes.launch(fillData)
    }

    /**
     * Calcula la nota y la descripción
     */
    private fun calculateResults() {
        val df = DecimalFormat("#.00")
        val noteFinal = evaluate()
        binding.lblResult.text = df.format(noteFinal)
        binding.lblDescripResult.text = obtainDescriptionNote(noteFinal)
        binding.btnSaveHistory.isEnabled = true
        binding.btnCalcular.isEnabled = false
        binding.inputNombre.clearFocus()
    }

    /**
     * Preparamos los datos y lo guardamos en un fichero
     * formato: 10;9;2020;David González del Arco;6.67;Bien;Presencial
     */
    private fun prepareDataAndSaveFile() {
        val name = binding.inputNombre.text.toString()
        val average = binding.lblResult.text.toString()
        val description = binding.lblDescripResult.text.toString()
        var mode = getString(R.string.MODE_PRESENCIAL)
        if (binding.rbnSemipresencial.isChecked) {
            mode = getString(R.string.MODE_SEMIPRESENCIAL)
        }
        val data = "$day;$month;$year;$name;$average;$description;$mode"
        val resultSaved = FileHelper.saveInFile(requireContext(), data)
        if ("OK" == resultSaved) {
            Toast.makeText(context, getString(R.string.RESULT_CORRECT), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, resultSaved, Toast.LENGTH_LONG).show()
        }
        clearData()
    }

    /**
     * Crear un registro de actividad result para obtener los datos desde la pantalla de
     * obtención de fecha y notas, si los resultado son OK, rellenamos las variables locales
     * si el resultado es KO limpiamos los datos de la pantalla
     *
     * @return nada, lanza la pantalla de obtención de datos.
     */
    private val getDataDateAndNotes =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                note1 = it.data?.getFloatExtra(NOTE_ONE, 0.0F)!!
                note2 = it.data?.getFloatExtra(NOTE_TWO, 0.0F)!!
                note3 = it.data?.getFloatExtra(NOTE_TREE, 0.0F)!!
                fillNotes()

                day = it.data?.getIntExtra(DATE_DAY, 1)!!
                month = it.data?.getIntExtra(DATE_MONTH, 1)!!
                year = it.data?.getIntExtra(DATE_YEAR, 1970)!!
                fillDate()

                binding.btnCalcular.isEnabled = true
                binding.inputNombre.clearFocus()
            }
            if (it.resultCode == Activity.RESULT_CANCELED) {
                clearData()
            }
        }

    /**
     * Rellenamos los datos de la fecha
     */
    private fun fillDate() {
        binding.lblDateIn.text = "$day/$month/$year"
    }

    /**
     * Rellenamos los datos de la notas
     */
    private fun fillNotes() {
        binding.lblNoteIn.text = "$note1 $note2 $note3"
    }

    /**
     * Limpiamos los valores para dejarlos por defecto
     */
    private fun clearData() {
        binding.inputNombre.text.clear()
        binding.btnCalcular.isEnabled = false
        binding.btnSaveHistory.isEnabled = false
        binding.lblDateIn.text = getString(R.string.lbl_empty_interrogation)
        binding.lblNoteIn.text = getString(R.string.lbl_empty_interrogation)
        binding.lblResult.text = ""
        binding.lblDescripResult.text = getString(R.string.lbl_main_calification)
    }

    /**
     * Obtener una descripción de la nota obtenida
     *
     * @return String con la descripcion del resultado de la nota final
     */
    private fun obtainDescriptionNote(noteFinal: Float): String {
        var description = ""
        if (noteFinal < 5) {
            description = getString(R.string.note_description_insuficient)
        }
        if (noteFinal >= 5 && noteFinal < 6) {
            description = getString(R.string.note_description_suficient)
        }
        if (noteFinal >= 6 && noteFinal < 9.5) {
            description = getString(R.string.note_description_good)
        }
        if (noteFinal >= 9.5) {
            description = getString(R.string.note_description_excelent)
        }
        return description
    }

    /**
     * Evalua las notas según los criterios
     *
     * Si el alumno pertenece a semipresencial su nota final será la nota media de las tres
     * evaluaciones.
     *
     * Si el alumno pertenece a presencial su nota final será la nota media, siempre y cuando
     * haya aprobado las tres evaluaciones. Si en alguna evaluación no ha  superado el 5 su nota
     * final será la menor de las tres evaluaciones:
     *
     * @return Nota final : Float
     */
    private fun evaluate(): Float {
        var finalNote = 0F

        if (binding.rbnSemipresencial.isChecked) {
            finalNote = calculateMedia(note1, note2, note3)
        } else if (binding.rbnPresencial.isChecked) {
            if (!checkAllApprovedNotes(note1, note2, note3)
            ) {
                finalNote = obtainMinimalNotes(note1, note2, note3)
            } else {
                finalNote = calculateMedia(note1, note2, note3)
            }
        }
        return finalNote
    }

    /**
     * Obtener la nota minima
     *
     * @param nota1 : Float
     * @param nota2: Float
     * @param nota3: Float
     * @return notaMinima: Float
     */
    private fun obtainMinimalNotes(note1: Float, note2: Float, note3: Float): Float {
        if (note1 <= note2 && note1 <= note3) {
            return note1
        }
        if (note2 < note1 && note2 <= note3) {
            return note2
        }
        return note3
    }

    /**
     * Comprueba que todas las notas estan aprovadas
     *
     * @param nota1 : Float
     * @param nota2: Float
     * @param nota3: Float
     * @return true si estan aprovadas todasﬁ: Boolean
     */
    private fun checkAllApprovedNotes(note1: Float, note2: Float, note3: Float): Boolean {
        println("checkAllApprovedNotes¡" + (note1 >= 5 && note2 >= 5 && note3 >= 5))
        return note1 >= 5 && note2 >= 5 && note3 >= 5
    }

    /**
     * Calcula la media de las notas pasadas por parámetro
     *
     * @param nota1 : Float
     * @param nota2: Float
     * @param nota3: Float
     * @return media de las notas : Float
     */
    private fun calculateMedia(note1: Float, note2: Float, note3: Float): Float {
        return (note1 + note2 + note3) / 3
    }

    /**
     * Alerta que nos pregunta si queremos o no guardar
     *
     * @param title :String titulo de la alerta
     * @param message :String mensaje de la alerta
     */
    private fun alertDialogSavedHistoric(title: String, message: String) {
        val builder = AlertDialog.Builder(context)
        // Se crea el AlertDialog.
        builder.apply {
            // Se asigna un título.
            setTitle(title)
            // Se asgina el cuerpo del mensaje.
            setMessage(message)
            // Se define el comportamiento de los botones.
            setPositiveButton(
                android.R.string.ok,
                DialogInterface.OnClickListener(function = actionButton)
            )
            setNegativeButton(android.R.string.cancel) { _, _ ->
                launchSnackBar(getString(R.string.calification_fragment_msn_cancel_saved), false )
            }
        }
        // Se muestra el AlertDialog.
        builder.show()
    }

    /**
     * Acción cuando se acepta guardar en el histórico
     */
    private val actionButton = { _: DialogInterface, _: Int ->
        prepareDataAndSaveFile()
        launchSnackBar(getString(R.string.calification_fragment_msn_saved_ok), false)
    }

    /**
     * Lanzador de snack-bar con la posibilidad de rechazarlo
     *
     * @param message :String
     * @param cancel :Boolean
     */
    private fun launchSnackBar(message: String, cancel: Boolean) {
        val customSnackBar: Snackbar = Snackbar.make(binding.clId, message,
            Snackbar.LENGTH_LONG)
        if(cancel){
            customSnackBar.setAction(getString(R.string.calification_fragment_msn_reject_action)) {
            }
        }
        customSnackBar.show()
    }


}