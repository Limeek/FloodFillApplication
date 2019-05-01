package ru.limeek.floodfillapp.presentation

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import ru.limeek.floodfillapp.model.Algorithm
import ru.limeek.floodfillapp.R
import ru.limeek.floodfillapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var viewModel: MainActivityViewModel
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)

        if(viewModel.bitmapMatrix == null)
            viewModel.generateMatrix()

        initBitmaps()
        initSpinners()
        binding.sbSpeed.setOnSeekBarChangeListener(sbChangeListener)
        binding.btnGenerate.setOnClickListener { generateBitmaps() }

        if(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
            binding.btnSize!!.setOnClickListener{openSizeDialog()}
        else{
            binding.etHeight!!.setText(viewModel.bitmapSize.first.toString())
            binding.etWidth!!.setText(viewModel.bitmapSize.second.toString())
            binding.etHeight!!.addTextChangedListener(heightTextWatcher)
            binding.etWidth!!.addTextChangedListener(widthTextWatcher)
        }
    }



    private fun initBitmaps(){
        binding.cstmView.setBitmapSize(viewModel.bitmapSize)
        binding.cstmView2.setBitmapSize(viewModel.bitmapSize)
        binding.cstmView.setBitmapMatrix(viewModel.bitmapMatrix!!)
        binding.cstmView2.setBitmapMatrix(viewModel.bitmapMatrix!!)

        binding.cstmView.setOnTouchListener { _, event -> if(event.action == MotionEvent.ACTION_DOWN) runFillTouchEvent(event) else false}
        binding.cstmView2.setOnTouchListener { _, event -> if(event.action == MotionEvent.ACTION_DOWN) runFillTouchEvent(event) else false }
    }

    private fun initSpinners(){
        val adapter = ArrayAdapter<Algorithm>(this, R.layout.spinner_head, viewModel.spinnerEntries)
        adapter.setDropDownViewResource(R.layout.spinner_item)
        binding.spnrFirst.adapter = adapter
        binding.spnrSecond.adapter = adapter

        binding.spnrFirst.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                binding.cstmView.setAlgorithm(adapter.getItem(position)!!)
            }
        }

        binding.spnrSecond.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                binding.cstmView2.setAlgorithm(adapter.getItem(position)!!)
            }
        }

        binding.spnrSecond.setSelection(1)
    }

    private fun runFillTouchEvent(event: MotionEvent): Boolean{
        return if(!isFillsRunning()) {
            binding.cstmView.triggerFillEvent(event)
            binding.cstmView2.triggerFillEvent(event)
        }
        else{
            showWaitToast()
            return false
        }
    }

    private fun isFillsRunning(): Boolean{
        return binding.cstmView.isFloodFillRunning() || binding.cstmView2.isFloodFillRunning()
    }

    private fun openSizeDialog(){
        val sizeDialog = SizeDialog()

        sizeDialog.arguments = Bundle().apply {
            putInt("width", viewModel.bitmapSize.first)
            putInt("height", viewModel.bitmapSize.second)
        }

        val sizeObserver = Observer<Pair<Int, Int>>{
            viewModel.bitmapSize = it
            sizeDialog.bitmapSize.removeObservers(this)
            sizeDialog.dismiss()
        }

        sizeDialog.bitmapSize.observe(this, sizeObserver)
        sizeDialog.show(supportFragmentManager, "123")
    }

    private fun showWaitToast(){
        Toast.makeText(this, getString(R.string.toast_wait_fill), Toast.LENGTH_SHORT).show()
    }

    private fun generateBitmaps(){
        if(!isFillsRunning()) {
            viewModel.generateMatrix()
            initBitmaps()
            binding.cstmView.invalidate()
            binding.cstmView2.invalidate()
        }
        else {
            showWaitToast()
        }
    }

    private val widthTextWatcher = object: TextWatcher{
        override fun afterTextChanged(s: Editable?) {
            when{
                s.toString().toIntOrNull()?: 0 > 256 -> {
                    Toast.makeText(this@MainActivity, getString(R.string.error_max_256), Toast.LENGTH_SHORT).show()
                    binding.etWidth!!.setText(viewModel.bitmapSize.first.toString())
                }
                s.toString().toIntOrNull()?: 0 <= 0 -> {
                    Toast.makeText(this@MainActivity, getString(R.string.error_less_or_equal_zero), Toast.LENGTH_SHORT).show()
                    binding.etWidth!!.setText(viewModel.bitmapSize.first.toString())
                }
                else ->{
                    viewModel.bitmapSize = Pair(binding.etWidth!!.text.toString().toInt(), binding.etHeight!!.text.toString().toInt())
                }
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

    }

    private val heightTextWatcher = object: TextWatcher{
        override fun afterTextChanged(s: Editable?) {
            when{
                s.toString().toIntOrNull()?: 0 > 256 -> {
                    Toast.makeText(this@MainActivity, getString(R.string.error_max_256), Toast.LENGTH_SHORT).show()
                    binding.etHeight!!.setText(viewModel.bitmapSize.second.toString())
                }
                s.toString().toIntOrNull()?: 0 <= 0 -> {
                    Toast.makeText(this@MainActivity, getString(R.string.error_less_or_equal_zero), Toast.LENGTH_SHORT).show()
                    binding.etHeight!!.setText(viewModel.bitmapSize.second.toString())
                }
                else ->{
                    viewModel.bitmapSize = Pair(binding.etWidth!!.text.toString().toInt(), binding.etHeight!!.text.toString().toInt())
                }
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

    }

    private val sbChangeListener = object: SeekBar.OnSeekBarChangeListener{
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if(fromUser) setSpeedForCstmViews(progress)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}

        override fun onStopTrackingTouch(seekBar: SeekBar?) {}

    }

    fun setSpeedForCstmViews(progress: Int){
        var speed = 1500 * (1 - (progress.toFloat()/100))
        if (speed == 0f) speed = 10f
        binding.cstmView.setSpeed(speed.toLong())
        binding.cstmView2.setSpeed(speed.toLong())
    }

}
