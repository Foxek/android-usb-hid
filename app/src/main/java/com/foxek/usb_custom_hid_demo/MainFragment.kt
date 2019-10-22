package com.foxek.usb_custom_hid_demo

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.foxek.usb_custom_hid_demo.type.Error
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

        ledState.setOnCheckedChangeListener { _, isChecked -> viewModel.changeLedButtonPressed(isChecked) }

        connectButton.setOnClickListener {
            viewModel.connectButtonPressed()
        }

        viewModel.buttonState.observe(this, Observer {
            buttonState.isChecked = true
        })

        viewModel.usbOperationError.observe(this, Observer {
            when (it) {
                is Error.NoDeviceFoundError -> showMessage(getString(R.string.error_no_device))
                is Error.UsbConnectionError -> showMessage(getString(R.string.error_no_connection))
                is Error.ClaimInterfaceError -> showMessage(getString(R.string.error_claim_interface))
                is Error.ReadReportError -> showMessage(getString(R.string.error_read_report))
                /* handle other error */
            }
            connectButton.text = getString(R.string.connect_hint)
            buttonState.isEnabled = false
            ledState.isEnabled = false
        })

        viewModel.usbOperationSuccess.observe(this, Observer {
            showMessage(getString(R.string.connection_state_success))
            connectButton.text = getString(R.string.disconnect_hint)
            buttonState.isEnabled = false
            ledState.isEnabled = false
        })

    }


//    private showError(Error)
    private fun showMessage(message: String) {
        (activity as MainActivity).showMessage(message)
    }
}
