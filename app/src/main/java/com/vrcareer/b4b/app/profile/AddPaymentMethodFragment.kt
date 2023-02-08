package com.vrcareer.b4b.app.profile

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.vrcareer.b4b.app.Constants.BANK_ACCOUNT_MODE
import com.vrcareer.b4b.app.Constants.PAYMENT_MODE_FIREBASE_PATH_ROOT
import com.vrcareer.b4b.app.Constants.UPI_MODE
import com.vrcareer.b4b.databinding.FragmentAddPaymentMethodBinding
import com.vrcareer.b4b.model.PaymentDetails


class AddPaymentMethodFragment : Fragment() {
    private var binding: FragmentAddPaymentMethodBinding? = null
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var existedPaymentDetail: PaymentDetails? = null

    init {
        db.reference.child("${PAYMENT_MODE_FIREBASE_PATH_ROOT}/${auth.currentUser?.uid}").get()
            .addOnSuccessListener {
                if (it.exists()) {
                    val payModeDetail = it.getValue(PaymentDetails::class.java)
                    existedPaymentDetail = payModeDetail
                    Log.d("Existed:", "$existedPaymentDetail")
                    binding?.let { b ->
                        when (existedPaymentDetail?.selected_mode) {
                            UPI_MODE -> {
                                b.mcUpi.isChecked = true
                                b.etUpiId.setText(existedPaymentDetail?.upiId)
                                b.etBankAccountNumber.setText(existedPaymentDetail?.bank_account_number)
                                b.etBankFullName.setText(existedPaymentDetail?.bank_account_name)
                                b.etBankIfsc.setText(existedPaymentDetail?.bank_ifsc)
                                b.mcBankAccount.isChecked = false
                            }
                            BANK_ACCOUNT_MODE -> {
                                b.mcBankAccount.isChecked = true
                                b.etUpiId.setText(existedPaymentDetail?.upiId)
                                b.etBankAccountNumber.setText(existedPaymentDetail?.bank_account_number)
                                b.etBankFullName.setText(existedPaymentDetail?.bank_account_name)
                                b.etBankIfsc.setText(existedPaymentDetail?.bank_ifsc)
                                b.mcUpi.isChecked = false
                            }
                            else -> {

                            }
                        }
                    }
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAddPaymentMethodBinding.inflate(inflater, container, false)

        /*  binding.mcUpi.setOnClickListener {
              binding.mcBankAccount.isChecked = false
              binding.mcUpi.isChecked = true
          }
          binding.mcBankAccount.setOnClickListener {
              binding.mcUpi.isChecked = false
              binding.mcBankAccount.isChecked = true
          }*/
        var bankOptions = listOf("Savings", "Current")
        val bankOptionsAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, bankOptions)
        (binding?.etAccountType?.editText as? AutoCompleteTextView)?.setText(bankOptions[0])
        (binding?.etAccountType?.editText as? AutoCompleteTextView)?.setAdapter(bankOptionsAdapter)

        binding?.topAppBarLocation?.setNavigationOnClickListener {
            val action =
                AddPaymentMethodFragmentDirections.actionAddPaymentMethodFragmentToProfileHomeFragment()
            findNavController().navigate(action)
        }
        binding?.btnAddUpi?.setOnClickListener {
            val upiId = binding?.etUpiId?.text.toString()
            val paymentDetails: PaymentDetails = existedPaymentDetail?.let {
                it.copy(
                    selected_mode = UPI_MODE,
                    upiId = upiId
                )
            } ?: PaymentDetails(
                id = auth.currentUser?.uid,
                selected_mode = UPI_MODE,
                upiId = upiId,
                activation_time = System.currentTimeMillis()
            )
            if (upiId.isNotEmpty()) {
                updatePaymentDetailsIdToFireBase(paymentDetails)
                binding?.etUpiId?.clearFocus()
                binding?.mcUpi?.isChecked = true
                binding?.mcBankAccount?.isChecked = false
            } else {
                binding?.etUpiId?.requestFocus()
                binding?.etUpiId?.error = "Field can't be empty"

            }

        }

        binding?.btnAddBankDetails?.setOnClickListener {
            val bankAccountHolderName = binding?.etBankFullName?.text.toString()
            val bankAccountNumber = binding?.etBankAccountNumber?.text.toString()
            val confirmAccountNumber = binding?.etBankAccountNumberConfirm?.text.toString()
            val bankType = binding?.etAccountType?.editText?.text.toString()
            val bankIFSC = binding?.etBankIfsc?.text.toString()


            if (bankAccountHolderName.isEmpty()) {
                binding?.etBankFullName?.requestFocus()
                binding?.etBankFullName?.error = "Field can't be empty"
            } else if (bankAccountNumber.isEmpty()) {
                binding?.etBankAccountNumber?.requestFocus()
                binding?.etBankAccountNumber?.error = "Field can't be empty"
            } else if(bankAccountNumber != confirmAccountNumber){
                binding?.etBankAccountNumberConfirm?.requestFocus()
                binding?.etBankAccountNumberConfirm?.error = "Account Number MisMatched"

            }else if (bankType.isEmpty()){
                binding?.etAccountType?.requestFocus()
                binding?.etAccountType?.error = "Please Select Account type"
            }
            else if (bankIFSC.isEmpty()) {
                binding?.etBankIfsc?.requestFocus()
                binding?.etBankIfsc?.error = "Field can't be empty"
            } else {
                val paymentDetails: PaymentDetails = existedPaymentDetail?.let {
                    it.copy(
                        selected_mode = BANK_ACCOUNT_MODE,
                        bank_account_name = bankAccountHolderName,
                        bank_account_number = bankAccountNumber,
                        bank_account_type = bankType,
                        bank_ifsc = bankIFSC,
                        activation_time = System.currentTimeMillis()
                    )
                } ?: PaymentDetails(
                    id = auth.currentUser?.uid,
                    selected_mode = BANK_ACCOUNT_MODE,
                    bank_account_name = bankAccountHolderName,
                    bank_account_number = bankAccountNumber,
                    bank_account_type = bankType,
                    bank_ifsc = bankIFSC,
                    activation_time = System.currentTimeMillis()
                )
                updatePaymentDetailsIdToFireBase(paymentDetails)
            }

            binding?.mcBankAccount?.isChecked = true
            binding?.mcUpi?.isChecked = false
        }
        return binding?.root
    }

    private fun updatePaymentDetailsIdToFireBase(paymentDetails: PaymentDetails) {
        auth.currentUser?.uid?.let {
            db.reference.child(PAYMENT_MODE_FIREBASE_PATH_ROOT).child(it).setValue(paymentDetails)
                .addOnSuccessListener {
                    Log.d("UPI", "$paymentDetails ")
                }.addOnFailureListener {
                    Log.d("UPI", "Failed")
                }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(
            true // default to enabled
        ) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            this,  // LifecycleOwner
            callback
        )
    }
}