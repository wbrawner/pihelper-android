package com.wbrawner.pihelper


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionInflater
import kotlinx.android.synthetic.main.fragment_retrieve_api_key.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class RetrieveApiKeyFragment : Fragment(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Main
    private val viewModel: AddPiHelperViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
        viewModel.authenticated.observe(this, Observer {
            findNavController().navigate(R.id.action_retrieveApiKeyFragment_to_mainFragment)
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_retrieve_api_key, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        connectWithPasswordButton.setOnClickListener {
            launch {
                try {
                    viewModel.authenticateWithPassword(password.text.toString())
                } catch (ignored: Exception) {
                    Log.e("Pi-Helper", "Failed to authenticate with password", ignored)
                    password.error = "Failed to authenticate with given password. Please verify " +
                            "you've entered it correctly and try again."
                }
            }
        }

        connectWithApiKeyButton.setOnClickListener {
            launch {
                try {
                    viewModel.authenticateWithApiKey(apiKey.text.toString())
                } catch (ignored: Exception) {
                    apiKey.error = "Failed to authenticate with given API key. Please verify " +
                            "you've entered it correctly and try again."
                }
            }
        }
    }

    override fun onDestroyView() {
        coroutineContext[Job]?.cancel()
        super.onDestroyView()
    }
}
