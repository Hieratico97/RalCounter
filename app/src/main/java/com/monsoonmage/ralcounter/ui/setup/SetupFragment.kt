package com.monsoonmage.ralcounter.ui.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.monsoonmage.ralcounter.R
import com.monsoonmage.ralcounter.databinding.FragmentSetupBinding
import com.monsoonmage.ralcounter.viewmodel.RalViewModel

class SetupFragment : Fragment() {

    private var _binding: FragmentSetupBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RalViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCommanderMode.setOnClickListener {
            startGame(40)
        }

        binding.btnNormalMode.setOnClickListener {
            startGame(20)
        }

        binding.btnLangEn.setOnClickListener {
            androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
                androidx.core.os.LocaleListCompat.forLanguageTags("en")
            )
        }

        binding.btnLangEs.setOnClickListener {
            androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
                androidx.core.os.LocaleListCompat.forLanguageTags("es")
            )
        }
    }

    private fun startGame(startingLife: Int) {
        viewModel.startGame(startingLife)
        findNavController().navigate(R.id.action_setup_to_combo)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
