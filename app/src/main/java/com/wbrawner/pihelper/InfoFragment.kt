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
import com.wbrawner.pihelper.databinding.FragmentInfoBinding
import org.koin.android.ext.android.inject

class InfoFragment : Fragment() {
    private val viewModel: AddPiHelperViewModel by inject()
    private var _binding: FragmentInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as? AppCompatActivity)?.supportActionBar?.setTitle(R.string.action_settings)
        val html = getString(R.string.content_info)
        @Suppress("DEPRECATION")
        binding.infoContent.text = if (Build.VERSION.SDK_INT < 24)
            Html.fromHtml(html)
        else
            Html.fromHtml(html, 0)
        binding.infoContent.movementMethod = LinkMovementMethod.getInstance()
        binding.forgetPiHoleButton.setOnClickListener {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
