package com.wbrawner.pihelper

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.wbrawner.pihelper.MainFragment.Companion.ACTION_DISABLE
import com.wbrawner.pihelper.MainFragment.Companion.ACTION_ENABLE
import com.wbrawner.pihelper.MainFragment.Companion.EXTRA_DURATION
import com.wbrawner.pihelper.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val addPiHoleViewModel: AddPiHelperViewModel by viewModels()
    private val navController: NavController by lazy {
        findNavController(R.id.content_main)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setBackgroundDrawable(
            ColorDrawable(
                ContextCompat.getColor(
                    this,
                    R.color.colorSurface
                )
            )
        )
        val args = when (intent.action) {
            ACTION_ENABLE -> {
                if (addPiHoleViewModel.apiKey == null) {
                    Toast.makeText(this, R.string.configure_pihelper, Toast.LENGTH_SHORT).show()
                    null
                } else {
                    Bundle().apply { putBoolean(ACTION_ENABLE, true) }
                }
            }
            ACTION_DISABLE -> {
                if (addPiHoleViewModel.apiKey == null) {
                    Toast.makeText(this, R.string.configure_pihelper, Toast.LENGTH_SHORT).show()
                    null
                } else {
                    Bundle().apply {
                        putBoolean(ACTION_DISABLE, true)
                        putLong(EXTRA_DURATION, intent.getIntExtra(EXTRA_DURATION, 10).toLong())
                    }
                }
            }
            ACTION_FORGET_PIHOLE -> {
                if (intent.component?.packageName == packageName) {
                    while (navController.popBackStack()) {
                        // Do nothing, just pop all the items off the back stack
                    }
                    // Just return an empty bundle so that the navigation branch below will load
                    // the correct screen
                    Bundle()
                } else {
                    null
                }
            }
            else -> null
        }
        when {
            navController.currentDestination?.id != R.id.placeholder && args == null -> {
                return
            }
            addPiHoleViewModel.baseUrl.isNullOrBlank() -> {
                navController.navigate(R.id.addPiHoleFragment, args)
            }
            addPiHoleViewModel.apiKey.isNullOrBlank() -> {
                navController.navigate(R.id.addPiHoleFragment)
                navController.navigate(R.id.retrieveApiKeyFragment, args)
            }
            else -> {
                navController.navigate(R.id.mainFragment, args)
            }
        }
    }

    override fun onBackPressed() {
        if (!navController.navigateUp()) {
            finish()
        }
        if (navController.currentDestination?.id == R.id.placeholder) {
            finish()
        }
    }

    companion object {
        const val ACTION_FORGET_PIHOLE = "com.wbrawner.pihelper.ACTION_FORGET_PIHOLE"
    }
}
