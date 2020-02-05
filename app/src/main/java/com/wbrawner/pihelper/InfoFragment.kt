package com.wbrawner.pihelper


import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.wbrawner.pihelper.MainActivity.Companion.ACTION_FORGET_PIHOLE
import kotlinx.android.synthetic.main.fragment_info.*
import kotlinx.android.synthetic.main.fragment_main.toolbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class InfoFragment : Fragment(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Main
    private val viewModel: AddPiHelperViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_info, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as? AppCompatActivity)?.supportActionBar?.setTitle(R.string.action_settings)
        val html = getString(R.string.content_info)
        @Suppress("DEPRECATION")
        infoContent?.text = if (Build.VERSION.SDK_INT < 24)
            Html.fromHtml(html)
        else
            Html.fromHtml(html, 0)
        infoContent.movementMethod = LinkMovementMethod.getInstance()
        forgetPiHoleButton?.setOnClickListener {
            AlertDialog.Builder(view.context)
                .setTitle(R.string.confirm_forget_pihole)
                .setMessage(R.string.warning_cannot_be_undone)
                .setNegativeButton(android.R.string.cancel) { _, _ -> }
                .setPositiveButton(R.string.action_forget_pihole) { _, _ ->
                    viewModel.forgetPihole()
                    val refreshIntent = Intent(
                        view.context.applicationContext,
                        MainActivity::class.java
                    ).apply {
                        action = ACTION_FORGET_PIHOLE
                        addFlags(
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    and Intent.FLAG_ACTIVITY_NEW_TASK
                        )
                    }
                    activity?.startActivity(refreshIntent)
                }
                .show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            findNavController().navigateUp()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
