package ru.limeek.floodfillapp.presentation

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import ru.limeek.floodfillapp.R
import ru.limeek.floodfillapp.databinding.DialogSizeBinding
import ru.limeek.floodfillapp.util.SingleLiveEvent

class SizeDialog: DialogFragment() {

    lateinit var binding : DialogSizeBinding
    var bitmapSize =  SingleLiveEvent<Pair<Int, Int>>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogSizeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(arguments != null) {
            binding.etHeight.setText(arguments!!.getInt("height", 0).toString())
            binding.etWidth.setText(arguments!!.getInt("width", 0).toString())
        }

        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnOk.setOnClickListener {
            val width  =  binding.etWidth.text.toString().toInt()
            val height = binding.etHeight.text.toString().toInt()
            bitmapSize.value = Pair(width, height)
            dismiss()
        }

        binding.etWidth.addTextChangedListener(errorTextWatcher)
        binding.etHeight.addTextChangedListener(errorTextWatcher)
    }

    private val errorTextWatcher = object: TextWatcher{
        override fun afterTextChanged(s: Editable?) {
            when {
                s.toString().toIntOrNull()?: 0 > 256 -> {
                    binding.tvError.visibility = View.VISIBLE
                    binding.tvError.text = getString(R.string.error_max_256)
                    binding.btnOk.isEnabled = false
                }
                s.toString().toIntOrNull()?: 0 <= 0 -> {
                    binding.tvError.visibility = View.VISIBLE
                    binding.tvError.text = getString(R.string.error_less_or_equal_zero)
                    binding.btnOk.isEnabled = false
                }
                else -> {
                    binding.tvError.visibility = View.GONE
                    binding.btnOk.isEnabled = true
                }
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }
}