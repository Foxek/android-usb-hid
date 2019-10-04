package com.foxek.usb_custom_hid_demo

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.main_fragment.*


class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        ledState.setOnCheckedChangeListener { _, isChecked -> viewModel.setLedState(isChecked) }

        connectButton.setOnClickListener {
            viewModel.handleConnection()
        }

        viewModel.buttonState.observe(this, Observer {
            buttonState.isChecked = true
        })

        viewModel.usbOperationError.observe(this, Observer {
            if (it) {
                showMessage(getString(R.string.connection_state_failure))
                connectButton.text = getString(R.string.connect_hint)
                buttonState.isEnabled = false
                ledState.isEnabled = false
            } else {
                showMessage(getString(R.string.connection_state_success))
                connectButton.text = getString(R.string.disconnect_hint)
                buttonState.isEnabled = false
                ledState.isEnabled = false
            }
        })

    }

    private fun showMessage(message: String) {
        (activity as MainActivity).showMessage(message)
    }
}
