package com.vhaa.practica3

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.vhaa.practica3.databinding.ActivityMainBinding

/**
 * Vista Principal de la Aplicación
 *
 * @author Victor Hugo Aguilar Aguilar
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Recogemos el componente ViewPager2
        val viewPager2 = binding.viewPager2
        // Se crea el adapter.
        val adapter = ViewPager2Adapter(supportFragmentManager, lifecycle)
        // Se añaden los fragments y los títulos de pestañas.
        adapter.addFragment(CalificacionFragment(), "Calificacion")
        adapter.addFragment(HistoricFragment(), "Históricos")
        // Se asocia el adpater al viewPager2
        viewPager2.adapter = adapter
        // Carga de las pestañas en el TabLayout
        TabLayoutMediator(binding.tabLayout, viewPager2) { tab, position ->
            tab.text = adapter.getPageTitle(position)
        }.attach()
    }
}