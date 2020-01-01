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
import android.widget.EditText
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getColor
import androidx.core.text.set
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.wbrawner.piholeclient.Status
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class MainFragment : Fragment(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Main
    private val viewModel: PiHelperViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        launch {
            if (arguments?.getBoolean(ACTION_ENABLE) == true) {
                viewModel.enablePiHole()
            } else if (arguments?.getBoolean(ACTION_DISABLE) == true) {
                viewModel.disablePiHole(arguments?.getLong(EXTRA_DURATION))
            }
            viewModel.monitorSummary()
        }
        viewModel.summary.observe(this, Observer { summary ->
            showProgress(false)
            val (statusColor, statusText) = if (
                summary.status == Status.DISABLED
            ) {
                enableButton?.visibility = View.VISIBLE
                disableButtons?.visibility = View.GONE
                Pair(R.color.colorDisabled, R.string.status_disabled)
            } else {
                enableButton?.visibility = View.GONE
                disableButtons?.visibility = View.VISIBLE
                Pair(R.color.colorEnabled, R.string.status_enabled)
            }
            status?.let {
                val status = getString(statusText)
                val statusLabel = getString(R.string.label_status, status)
                val start = statusLabel.indexOf(status)
                val end = start + status.length
                val statusSpan = SpannableString(statusLabel)
                statusSpan[start, end] = StyleSpan(Typeface.BOLD)
                statusSpan[start, end] = ForegroundColorSpan(getColor(it.context, statusColor))
                it.text = statusSpan
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_main, container, false)

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)
        showProgress(true)
        enableButton?.setOnClickListener {
            launch {
                showProgress(true)
                try {
                    viewModel.enablePiHole()
                } catch (ignored: Exception) {
                    Log.e("Pi-Helper", "Failed to enable Pi-Hole", ignored)
                }
            }
        }
        disable10SecondsButton?.setOnClickListener {
            launch {
                showProgress(true)
                try {
                    viewModel.disablePiHole(10)
                } catch (ignored: Exception) {
                    Log.e("Pi-Helper", "Failed to disable Pi-Hole", ignored)
                }
            }
        }
        disable30SecondsButton?.setOnClickListener {
            launch {
                showProgress(true)
                try {
                    viewModel.disablePiHole(30)
                } catch (ignored: Exception) {
                    Log.e("Pi-Helper", "Failed to disable Pi-Hole", ignored)
                }
            }
        }
        disable5MinutesButton?.setOnClickListener {
            launch {
                showProgress(true)
                try {
                    viewModel.disablePiHole(300)
                } catch (ignored: Exception) {
                    Log.e("Pi-Helper", "Failed to disable Pi-Hole", ignored)
                }
            }
        }
        disableCustomTimeButton?.setOnClickListener {
            val dialogView = LayoutInflater.from(it.context)
                .inflate(R.layout.dialog_disable_custom_time, null, false)
            AlertDialog.Builder(it.context)
                .setTitle(R.string.action_disable_custom)
                .setNegativeButton(android.R.string.cancel) { _, _ -> }
                .setPositiveButton(R.string.action_disable, null)
                .setView(dialogView)
                .create()
                .apply {
                    setOnShowListener {
                        getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                            launch {
                                try {
                                    val rawTime = dialogView.findViewById<EditText>(R.id.time)
                                        .text
                                        .toString()
                                        .toLong()
                                    val checkedId =
                                        dialogView.findViewById<RadioGroup>(R.id.timeUnit)
                                            .checkedRadioButtonId
                                    val computedTime = if (checkedId == R.id.seconds) rawTime
                                    else rawTime * 60
                                    viewModel.disablePiHole(computedTime)
                                    dismiss()
                                } catch (e: Exception) {
                                    dialogView.findViewById<EditText>(R.id.time)
                                        .error = "Failed to disable Pi-hole"
                                }
                            }
                        }
                    }
                }
                .show()
        }
        disablePermanentlyButton?.setOnClickListener {
            launch {
                showProgress(true)
                try {
                    viewModel.disablePiHole()
                } catch (ignored: Exception) {
                    Log.e("Pi-Helper", "Failed to disable Pi-Hole", ignored)
                }
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
        progressBar?.visibility = if (show) {
            progressBar?.startAnimation(RotateAnimation(
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
            progressBar?.clearAnimation()
            View.GONE
        }
        statusContent?.visibility = if (show) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    override fun onDestroyView() {
        coroutineContext[Job]?.cancel()
        super.onDestroyView()
    }

    companion object {
        const val ACTION_DISABLE = "com.wbrawner.pihelper.MainFragment.ACTION_DISABLE"
        const val ACTION_ENABLE = "com.wbrawner.pihelper.MainFragment.ACTION_ENABLE"
        const val EXTRA_DURATION = "com.wbrawner.pihelper.MainFragment.EXTRA_DURATION"
    }
}
