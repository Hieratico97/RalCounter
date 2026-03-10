package com.monsoonmage.ralcounter.ui.fliplog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.monsoonmage.ralcounter.databinding.FragmentFlipLogBinding
import com.monsoonmage.ralcounter.viewmodel.RalViewModel

class FlipLogFragment : Fragment() {

    private var _binding: FragmentFlipLogBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RalViewModel by activityViewModels()
    private val adapter = FlipLogAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFlipLogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerFlips.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = this@FlipLogFragment.adapter
        }

        viewModel.flipHistory.observe(viewLifecycleOwner) { history ->
            adapter.submitList(history)
            val wins = history.count { it.isWin }
            val losses = history.size - wins
            binding.tvWinCount.text = wins.toString()
            binding.tvLossCount.text = losses.toString()
            binding.tvTotalFlips.text = history.size.toString()
            binding.tvWinRate.text = if (history.isEmpty()) "—"
            else "${(wins * 100 / history.size)}%"
            binding.tvEmptyState.visibility = if (history.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerFlips.visibility = if (history.isEmpty()) View.GONE else View.VISIBLE
        }

        binding.btnClearHistory.setOnClickListener { viewModel.clearFlipHistory() }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
