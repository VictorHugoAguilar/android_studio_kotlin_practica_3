package com.vhaa.practica3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.vhaa.practica3.databinding.FragmentHistoricBinding
import helpers.FileHelper

/**
 * Fragmento para los historicos
 *
 * @author Victor Hugo Aguilar Aguilar
 */
class HistoricFragment : Fragment() {
    private lateinit var adapter: HistoricAdapter
    private lateinit var binding: FragmentHistoricBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHistoricBinding.inflate(inflater, container, false)
        setUpRecyclerView()
        return binding.root
    }

    /**
     * Utilización del onResumen para actualizar el historico cuando este cambien
     */
    override fun onResume() {
        super.onResume()
        setUpRecyclerView()
    }

    /**
     * Configurar el recycler view con los datos que recuopera al leer los ficheros
     */
    private fun setUpRecyclerView() {
        val readFile = context?.let { FileHelper.readFile(it) }
        // Si hay datos o no ha surgido ningún error
        if (readFile != null) {
            if (readFile.result) {
                // Esta opción a TRUE significa que el RV tendrá
                // hijos del mismo tamaño, optimiza su creación.
                binding.rvHistorical.setHasFixedSize(true)
                // Se indica el contexto para RV en forma de lista.
                //binding.rvHistorical.layoutManager = LinearLayoutManager(this)
                binding.rvHistorical.layoutManager = GridLayoutManager(context, 1)
                // Se genera el adapter.
                adapter = context?.let { HistoricAdapter(readFile.notes, it) }!!
                // Se asigna el adapter al RV.
                binding.rvHistorical.adapter = adapter
            } else {
                var messageError: String = getString(R.string.msg_not_found_data_file)
                    if (readFile.message.isNotBlank()) {
                        messageError = readFile.message
                }
                Toast.makeText(context, messageError, Toast.LENGTH_SHORT).show()
            }
        }
    }
}