package com.preonboarding.sensordashboard.presentation.replay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.preonboarding.sensordashboard.domain.model.PlayType
import com.preonboarding.sensordashboard.domain.model.ViewType
import com.preonboarding.sensordashboard.domain.repository.MeasurementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReplayViewModel @Inject constructor (
): ViewModel() {

    var measureTime = 0

    private val _curViewType: MutableStateFlow<ViewType> =
        MutableStateFlow(ViewType.INITIAL)
    val curViewType: StateFlow<ViewType>
        get() = _curViewType

    private val _curPlayType: MutableStateFlow<PlayType> =
        MutableStateFlow(PlayType.Stop)
    val curPlayType: StateFlow<PlayType>
        get() = _curPlayType

    private val _timerCount: MutableStateFlow<Int> =
        MutableStateFlow(0)
    val timerCount : StateFlow<Int>
        get() = _timerCount

    private lateinit var timerJob : Job

    private fun startTimer() {
        if(::timerJob.isInitialized) {
            timerJob.cancel()
        }

        _timerCount.value = 0
        timerJob = viewModelScope.launch {
            while (timerCount.value < measureTime) {
                _timerCount.value = timerCount.value + 1
                delay(100L)
            }

            changeTimerStatus()
            _timerCount.value = measureTime
        }
    }

    private fun stopTimer() {
        if (::timerJob.isInitialized) {
            timerJob.cancel()
        }
    }

    fun changeTimerStatus() {
        when (curPlayType.value) {
            PlayType.Stop -> {
                _curPlayType.value = PlayType.Play
                startTimer()
            }
            PlayType.Play -> {
                _curPlayType.value = PlayType.Stop
                stopTimer()
            }
        }
    }

    fun setReplayViewType(viewType: ViewType) {
        _curViewType.value = viewType
    }

    fun applyTimeFormat(time: Double) {
        measureTime = (time * 10).toInt()
    }
}