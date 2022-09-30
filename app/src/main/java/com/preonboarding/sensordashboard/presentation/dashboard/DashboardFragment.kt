package com.preonboarding.sensordashboard.presentation.dashboard

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import com.preonboarding.sensordashboard.R
import com.preonboarding.sensordashboard.databinding.FragmentDashboardBinding
import com.preonboarding.sensordashboard.domain.model.MeasureResult
import com.preonboarding.sensordashboard.domain.model.ViewType
import com.preonboarding.sensordashboard.presentation.common.OptionDialog
import com.preonboarding.sensordashboard.presentation.common.base.BaseFragment
import com.preonboarding.sensordashboard.presentation.common.util.NavigationUtil.navigate
import com.preonboarding.sensordashboard.presentation.common.util.NavigationUtil.navigateWithArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class DashboardFragment : BaseFragment<FragmentDashboardBinding>(R.layout.fragment_dashboard) {

    private val viewModel: DashboardViewModel by viewModels()
    private val pagingAdapter: DashboardPagingAdapter by lazy {
        DashboardPagingAdapter(
            optionClicked = {
                showDialog(it)
            },
            itemClicked = {
                navigateWithArgs(
                    DashboardFragmentDirections.actionDashboardToReplay(
                        it,
                        ViewType.VIEW
                    )
                )
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindViews()
        viewModel.getAllMeasurement()
        initRecyclerView()
        observeMeasureData()
        observeLoadState()
    }

    private fun initRecyclerView() {
        binding.rvDashboard.apply {
            adapter = pagingAdapter

            val itemDecoration = DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
            addItemDecoration(itemDecoration)
        }
    }

    private fun bindViews() = with(binding) {
        btnMeasurement.setOnClickListener {
            navigate(R.id.action_dashboard_to_measurement)
        }
    }

    private fun observeMeasureData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.measureData.collectLatest { measureList ->
                    Timber.e("$measureList")
                    pagingAdapter.submitData(measureList)
                }
            }
        }
    }

    private fun observeLoadState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                pagingAdapter.loadStateFlow.collect {
                    Timber.e("${it.source.append}")
                    binding.lpiPaging.isVisible = it.source.append is LoadState.Loading
                }
            }
        }
    }

    private fun showDialog(measureResult: MeasureResult) {
        val dialog = OptionDialog(
            requireContext(),
            playClicked = {
                navigateWithArgs(
                    DashboardFragmentDirections.actionDashboardToReplay(
                        measureResult,
                        ViewType.PLAY
                    )
                )
            },
            deleteClicked = {
                Toast.makeText(requireContext(), "Delete", Toast.LENGTH_SHORT).show()
            })

        dialog.showDialog()
    }
}