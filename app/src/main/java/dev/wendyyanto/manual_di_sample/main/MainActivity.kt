package dev.wendyyanto.manual_di_sample.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import dev.wendyyanto.dependency_lib.annotation.Inject
import dev.wendyyanto.dependency_lib.di.Injectors
import dev.wendyyanto.manual_di_sample.R
import dev.wendyyanto.manual_di_sample.detail.DetailActivity
import dev.wendyyanto.manual_di_sample.main.module.MainModule
import dev.wendyyanto.manual_di_sample.main.presenter.MainPresenter
import dev.wendyyanto.manual_di_sample.main.utils.StringUtils

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var mainPresenter: MainPresenter

    @Inject
    lateinit var stringUtils: StringUtils

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Injectors.inject(MainModule::class, this)
        setContentView(R.layout.activity_main)
        Toast.makeText(this, stringUtils.test(), Toast.LENGTH_SHORT).show()
        findViewById<TextView>(R.id.tv_test).text = mainPresenter.getId()
        findViewById<TextView>(R.id.tv_test).setOnClickListener {
            goToDetail()
        }
    }

    private fun goToDetail() {
        startActivity(Intent(this, DetailActivity::class.java))
    }
}