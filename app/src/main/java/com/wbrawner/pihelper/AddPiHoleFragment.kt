package com.wbrawner.pihelper


import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_add_pi_hole.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class AddPiHoleFragment : Fragment(), CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.Main
    private val viewModel: AddPiHelperViewModel by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_add_pi_hole, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val navController = findNavController()
        scanNetworkButton.setOnClickListener {
            navController.navigate(
                R.id.action_addPiHoleFragment_to_scanNetworkFragment,
                null,
                null,
                FragmentNavigatorExtras(piHelperLogo to "piHelperLogo")
            )
        }
        connectButton.setOnClickListener {
            launch {
                val progressDialog = AlertDialog.Builder(it.context)
                    .setTitle(R.string.connecting_to_pihole)
                    .setNegativeButton(R.string.action_cancel) { _, _ ->
                        cancel()
                    }
                    .setView(FrameLayout(it.context).apply {
                        addView(ProgressBar(it.context).apply {
                            layoutParams = FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.WRAP_CONTENT,
                                FrameLayout.LayoutParams.WRAP_CONTENT,
                                Gravity.CENTER
                            )
                        })
                    })
                    .show()
                if (viewModel.connectToIpAddress(ipAddress.text.toString())) {
                    navController.navigate(
                        R.id.action_addPiHoleFragment_to_retrieveApiKeyFragment,
                        null,
                        null,
                        FragmentNavigatorExtras(piHelperLogo to "piHelperLogo")
                    )
                } else {
                    AlertDialog.Builder(view.context)
                        .setTitle(R.string.connection_failed_title)
                        .setMessage(R.string.connection_failed)
                        .setPositiveButton(android.R.string.ok) { _, _ -> }
                        .show()
                }
                progressDialog.dismiss()
            }
        }
    }

    override fun onDestroyView() {
        cancel()
        super.onDestroyView()
    }
}
