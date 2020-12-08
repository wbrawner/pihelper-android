package com.wbrawner.pihelper


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.wbrawner.pihelper.databinding.FragmentAddPiHoleBinding
import org.koin.android.ext.android.inject

class AddPiHoleFragment : Fragment() {

    private val viewModel: AddPiHelperViewModel by inject()
    private var _binding: FragmentAddPiHoleBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPiHoleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val navController = findNavController()
        binding.scanNetworkButton.setOnClickListener {
            navController.navigate(
                R.id.action_addPiHoleFragment_to_scanNetworkFragment,
                null,
                null,
                FragmentNavigatorExtras(binding.piHelperLogo to "piHelperLogo")
            )
        }
        binding.ipAddress.setOnEditorActionListener { _, _, _ ->
            binding.connectButton.performClick()
        }
        binding.connectButton.setSuspendingOnClickListener(lifecycleScope) {
            showProgress(true)
            if (viewModel.connectToIpAddress(binding.ipAddress.text.toString())) {
                navController.navigate(
                    R.id.action_addPiHoleFragment_to_retrieveApiKeyFragment,
                    null,
                    null,
                    FragmentNavigatorExtras(binding.piHelperLogo to "piHelperLogo")
                )
            } else {
                AlertDialog.Builder(view.context)
                    .setTitle(R.string.connection_failed_title)
                    .setMessage(R.string.connection_failed)
                    .setPositiveButton(android.R.string.ok) { _, _ -> }
                    .show()
            }
            showProgress(false)
        }
    }

    private fun showProgress(show: Boolean) {
        if (show) {
            binding.piHelperLogo.startAnimation(
                RotateAnimation(
                    0f,
                    360f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f
                ).apply {
                    duration =
                        resources.getInteger(android.R.integer.config_longAnimTime).toLong() * 2
                    repeatMode = Animation.RESTART
                    repeatCount = Animation.INFINITE
                    interpolator = LinearInterpolator()
                    fillAfter = true
                }
            )
        } else {
            binding.piHelperLogo.clearAnimation()
        }
        binding.connectionForm.visibility = if (show) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
