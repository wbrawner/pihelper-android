package com.wbrawner.pihelper


import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.transition.Transition
import androidx.transition.TransitionInflater
import com.wbrawner.pihelper.databinding.FragmentScanNetworkBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.Inet4Address

@AndroidEntryPoint
class ScanNetworkFragment : Fragment() {

    private val viewModel: AddPiHelperViewModel by activityViewModels()
    private var _binding: FragmentScanNetworkBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
            .addListener(object : Transition.TransitionListener {
                override fun onTransitionEnd(transition: Transition) {
                    animatePiHelperLogo()
                }

                override fun onTransitionResume(transition: Transition) {
                }

                override fun onTransitionPause(transition: Transition) {
                }

                override fun onTransitionCancel(transition: Transition) {
                }

                override fun onTransitionStart(transition: Transition) {
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanNetworkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.scanningIp.observe(viewLifecycleOwner, Observer {
            binding.ipAddress.text = it
        })
        viewModel.piHoleIpAddress.observe(viewLifecycleOwner, Observer { ipAddress ->
            if (ipAddress == null) {
                AlertDialog.Builder(view.context)
                    .setTitle(R.string.scan_failed_title)
                    .setMessage(R.string.scan_failed)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        findNavController().navigateUp()
                    }
                    .show()
                return@Observer
            }
            binding.piHelperLogo.animation?.let {
                it.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationRepeat(animation: Animation?) {
                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        navigateToApiKeyScreen()
                    }

                    override fun onAnimationStart(animation: Animation?) {
                    }
                })
                it.repeatCount = 0
            } ?: navigateToApiKeyScreen()
        })
        lifecycleScope.launch(Dispatchers.IO) {
            if (BuildConfig.DEBUG && Build.MODEL == "Android SDK built for x86") {
                // For emulators, just begin scanning the host machine directly
                viewModel.beginScanning("10.0.2.2")
                return@launch
            }
            (view.context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager)?.let { connectivityManager ->
                connectivityManager.allNetworks
                    .filter {
                        connectivityManager.getNetworkCapabilities(it)
                            ?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                            ?: false
                    }
                    .forEach { network ->
                        connectivityManager.getLinkProperties(network)
                            ?.linkAddresses
                            ?.filter { !it.address.isLoopbackAddress && it.address is Inet4Address }
                            ?.forEach { address ->
                                Log.d(
                                    "Pi-helper",
                                    "Found link address: ${address.address.hostName}"
                                )
                                viewModel.beginScanning(address.address.hostAddress)
                            }
                    }
            }
        }
        lifecycleScope.launch {
            delay(500)
            if (binding.piHelperLogo.animation == null) {
                animatePiHelperLogo()
            }
        }
    }

    private fun navigateToApiKeyScreen() {
        val extras = FragmentNavigatorExtras(
            binding.piHelperLogo to "piHelperLogo"
        )

        findNavController().navigate(
            R.id.action_scanNetworkFragment_to_retrieveApiKeyFragment,
            null,
            null,
            extras
        )
    }

    override fun onDestroyView() {
        binding.piHelperLogo.clearAnimation()
        _binding = null
        super.onDestroyView()
    }

    private fun animatePiHelperLogo() {
        binding.piHelperLogo.startAnimation(
            RotateAnimation(
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
            }
        )
    }
}
