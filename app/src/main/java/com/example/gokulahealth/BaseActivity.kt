package com.example.gokulahealth

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        // This force-applies the saved language before the screen loads
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }
}