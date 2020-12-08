package com.wbrawner.pihelper


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionInflater
import com.wbrawner.pihelper.databinding.FragmentRetrieveApiKeyBinding
import org.koin.android.ext.android.inject

class RetrieveApiKeyFragment : Fragment() {
    private val viewModel: AddPiHelperViewModel by inject()
    private var _binding: FragmentRetrieveApiKeyBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
        viewModel.authenticated.observe(this, Observer {
            if (!it) return@Observer
            findNavController().navigate(R.id.action_retrieveApiKeyFragment_to_mainFragment)
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRetrieveApiKeyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.password.setOnEditorActionListener { _, _, _ ->
            binding.connectWithPasswordButton.performClick()
        }
        binding.connectWithPasswordButton.setSuspendingOnClickListener(lifecycleScope) {
            showProgress(true)
            try {
                viewModel.authenticateWithPassword(binding.password.text.toString())
            } catch (ignored: Exception) {
                Log.e("Pi-helper", "Failed to authenticate with password", ignored)
                binding.password.error = "Failed to authenticate with given password. Please verify " +
                        "you've entered it correctly and try again."
                showProgress(false)
            }
        }
        binding.apiKey.setOnEditorActionListener { _, _, _ ->
            binding.connectWithApiKeyButton.performClick()
        }
        binding.connectWithApiKeyButton.setSuspendingOnClickListener(lifecycleScope) {
            showProgress(true)
            try {
                viewModel.authenticateWithApiKey(binding.apiKey.text.toString())
            } catch (ignored: Exception) {
                binding.apiKey.error = "Failed to authenticate with given API key. Please verify " +
                        "you've entered it correctly and try again."
                showProgress(false)
            }
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
        binding.authenticationForm.visibility = if (show) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
