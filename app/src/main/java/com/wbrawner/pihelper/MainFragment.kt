package com.wbrawner.pihelper


import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getColor
import androidx.core.text.set
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.wbrawner.pihelper.databinding.DialogDisableCustomTimeBinding
import com.wbrawner.pihelper.databinding.FragmentMainBinding
import com.wbrawner.piholeclient.Status
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainFragment : Fragment() {
    private val viewModel: PiHelperViewModel by activityViewModels()
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        lifecycleScope.launch {
            if (arguments?.getBoolean(ACTION_ENABLE) == true) {
                viewModel.enablePiHole()
            } else if (arguments?.getBoolean(ACTION_DISABLE) == true) {
                viewModel.disablePiHole(arguments?.getLong(EXTRA_DURATION))
            }
            viewModel.monitorSummary()
        }
        viewModel.status.observe(this, {
            showProgress(false)
            val (statusColor, statusText) = when (it) {
                Status.DISABLED -> {
                    binding.enableButton.visibility = View.VISIBLE
                    binding.disableButtons.visibility = View.GONE
                    Pair(R.color.colorDisabled, R.string.status_disabled)
                }
                Status.ENABLED -> {
                    binding.enableButton.visibility = View.GONE
                    binding.disableButtons.visibility = View.VISIBLE
                    Pair(R.color.colorEnabled, R.string.status_enabled)
                }
                else -> {
                    binding.enableButton.visibility = View.GONE
                    binding.disableButtons.visibility = View.GONE
                    Pair(R.color.colorUnknown, R.string.status_unknown)
                }
            }
            val status = getString(statusText)
            val statusLabel = getString(R.string.label_status, status)
            val start = statusLabel.indexOf(status)
            val end = start + status.length
            val statusSpan = SpannableString(statusLabel)
            statusSpan[start, end] = StyleSpan(Typeface.BOLD)
            statusSpan[start, end] =
                ForegroundColorSpan(getColor(binding.status.context, statusColor))
            binding.status.text = statusSpan
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        showProgress(true)
        binding.enableButton.setSuspendingOnClickListener(lifecycleScope) {
            showProgress(true)
            try {
                viewModel.enablePiHole()
            } catch (ignored: Exception) {
                Log.e("Pi-helper", "Failed to enable Pi-Hole", ignored)
            }
        }
        binding.disable10SecondsButton.setSuspendingOnClickListener(lifecycleScope) {
            showProgress(true)
            try {
                viewModel.disablePiHole(10)
            } catch (ignored: Exception) {
                Log.e("Pi-helper", "Failed to disable Pi-Hole", ignored)
            }
        }
        binding.disable30SecondsButton.setSuspendingOnClickListener(lifecycleScope) {
            showProgress(true)
            try {
                viewModel.disablePiHole(30)
            } catch (ignored: Exception) {
                Log.e("Pi-helper", "Failed to disable Pi-Hole", ignored)
            }
        }
        binding.disable5MinutesButton.setSuspendingOnClickListener(lifecycleScope) {
            showProgress(true)
            try {
                viewModel.disablePiHole(300)
            } catch (ignored: Exception) {
                Log.e("Pi-helper", "Failed to disable Pi-Hole", ignored)
            }
        }
        binding.disableCustomTimeButton.setOnClickListener {
            val dialogView = DialogDisableCustomTimeBinding.inflate(
                LayoutInflater.from(it.context),
                view as ViewGroup,
                false
            )
            AlertDialog.Builder(it.context)
                .setTitle(R.string.action_disable_custom)
                .setNegativeButton(android.R.string.cancel) { _, _ -> }
                .setPositiveButton(R.string.action_disable, null)
                .setView(dialogView.root)
                .create()
                .apply {
                    setOnShowListener {
                        getButton(AlertDialog.BUTTON_POSITIVE)
                            .setSuspendingOnClickListener(lifecycleScope) {
                                try {
                                    val rawTime = dialogView.time
                                        .text
                                        .toString()
                                        .toLong()
                                    val computedTime =
                                        when (dialogView.timeUnit.checkedRadioButtonId) {
                                            R.id.seconds -> rawTime
                                            R.id.minutes -> rawTime * 60
                                            else -> rawTime * 3600
                                        }
                                    viewModel.disablePiHole(computedTime)
                                    dismiss()
                                } catch (e: Exception) {
                                    dialogView.time.error = "Failed to disable Pi-hole"
                                }
                            }
                    }
                }
                .show()
        }
        binding.disablePermanentlyButton.setSuspendingOnClickListener(lifecycleScope) {
            showProgress(true)
            try {
                viewModel.disablePiHole()
            } catch (ignored: Exception) {
                Log.e("Pi-helper", "Failed to disable Pi-Hole", ignored)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.settings) {
            findNavController().navigate(R.id.action_mainFragment_to_settingsFragment)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showProgress(show: Boolean) {
        binding.progressBar.visibility = if (show) {
            binding.progressBar.startAnimation(RotateAnimation(
                0f,
                360f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
            ).apply {
                duration = resources.getInteger(android.R.integer.config_longAnimTime).toLong() * 2
                repeatMode = Animation.RESTART
                repeatCount = Animation.INFINITE
                interpolator = LinearInterpolator()
                fillAfter = true
            })
            View.VISIBLE
        } else {
            binding.progressBar.clearAnimation()
            View.GONE
        }
        binding.statusContent.visibility = if (show) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val ACTION_DISABLE = "com.wbrawner.pihelper.MainFragment.ACTION_DISABLE"
        const val ACTION_ENABLE = "com.wbrawner.pihelper.MainFragment.ACTION_ENABLE"
        const val EXTRA_DURATION = "com.wbrawner.pihelper.MainFragment.EXTRA_DURATION"
    }
}
