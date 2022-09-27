package com.preonboarding.sensordashboard.presentation.measurement

import android.content.ContentValues.TAG
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.preonboarding.sensordashboard.R
import com.preonboarding.sensordashboard.databinding.FragmentMeasurementBinding
import com.preonboarding.sensordashboard.domain.model.AccInfo
import com.preonboarding.sensordashboard.domain.model.GyroInfo
import com.preonboarding.sensordashboard.domain.model.MeasureTarget
import com.preonboarding.sensordashboard.presentation.common.base.BaseFragment
import com.preonboarding.sensordashboard.presentation.common.util.NavigationUtil.navigateUp
import timber.log.Timber

class MeasurementFragment : BaseFragment<FragmentMeasurementBinding>(R.layout.fragment_measurement), SensorEventListener {
    private val viewModel: MeasurementViewModel by viewModels()

    private val sensorManager: SensorManager by lazy {
        requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private val accSensor: Sensor? by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    private val gyroSensor: Sensor? by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        bindingViewModel()
    }

    private fun bindingViewModel() {
        binding.viewModel = viewModel
        binding.measureTarget = viewModel.curMeasureTarget.value

        lifecycleScope.launchWhenCreated {
            viewModel.curMeasureTarget.collect {
                binding.measureTarget = it
            }
        }
    }

    private fun initViews() {
        binding.tbMeasurement.setNavigationOnClickListener {
            navigateUp()
        }

        binding.btnMeasureStart.setOnClickListener {
            startMeasurement()
        }

        binding.btnMeasurePause.setOnClickListener {
            stopMeasurement()
        }

        binding.tvMeasureAcc.setOnClickListener {
            changeMeasureTarget()
        }

        binding.tvMeasureGyro.setOnClickListener {
            changeMeasureTarget()
        }
    }

    private fun startMeasurement() {
        Timber.tag(TAG).e("START")

        when(viewModel.curMeasureTarget.value) {
                MeasureTarget.ACC -> {
                    sensorManager.registerListener(
                        this,
                        accSensor,
                        SensorManager.SENSOR_DELAY_NORMAL
                    )

                }

                MeasureTarget.GYRO -> {
                    sensorManager.registerListener(
                        this,
                        gyroSensor,
                        SensorManager.SENSOR_DELAY_NORMAL
                    )
                }
        }
    }

    private fun stopMeasurement() {
        Timber.tag(TAG).e("STOP")
        sensorManager.unregisterListener(this)
    }

    fun changeMeasureTarget() {

        with(viewModel) {
            when(curMeasureTarget.value) {
                MeasureTarget.ACC -> {
                    viewModel.setMeasureTarget(MeasureTarget.GYRO)
                }
                MeasureTarget.GYRO -> {
                    viewModel.setMeasureTarget(MeasureTarget.ACC)

                }
            }
            Timber.tag(TAG).e("현재 측정 타겟 : ${curMeasureTarget.value}")
        }
        stopMeasurement()

    }

    override fun onSensorChanged(sensorEvent: SensorEvent?) {

        when(sensorEvent?.sensor?.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val accInfo = AccInfo(
                    x = sensorEvent.values[0].toInt(),
                    y = sensorEvent.values[1].toInt(),
                    z = sensorEvent.values[2].toInt(),
                )
                Timber.tag(TAG).d("acc : $accInfo")
            }

            Sensor.TYPE_GYROSCOPE -> {
                val gyroInfo = GyroInfo(
                    x = (sensorEvent.values[0] * THOUS).toInt(),
                    y = (sensorEvent.values[1] * THOUS).toInt(),
                    z = (sensorEvent.values[2] * THOUS).toInt(),
                )
                Timber.tag(TAG).d("gyro : $gyroInfo")
            }
        }

    }


    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    companion object {
        private const val TAG = "MeasurementFragment"
        private const val THOUS = 1000
    }
}