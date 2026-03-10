package com.monsoonmage.ralcounter.ui.combo

import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.monsoonmage.ralcounter.R
import com.monsoonmage.ralcounter.databinding.FragmentComboBinding
import com.monsoonmage.ralcounter.model.RalState
import com.monsoonmage.ralcounter.viewmodel.RalViewModel

class ComboFragment : Fragment() {

    private var _binding: FragmentComboBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RalViewModel by activityViewModels()
    private var isAnimating = false
    private var isResetting = false
    private var lastBackPressTime = 0L
    private val BACK_PRESS_INTERVAL = 2000L

    private var lastStormCount = -1
    private var lastInstSorcCount = -1
    private var lastLifeTotal = -1
    private var lastLoyalty = -1
    private var lastBlueCount = -1
    private var lastRedCount = -1
    private var lastGenericCount = -1
    private var lastTotalMana = -1
    private val counterAnimators = mutableMapOf<android.widget.TextView, AnimatorSet>()
    private val buttonAnimators = mutableMapOf<com.google.android.material.button.MaterialButton, AnimatorSet>()
    
    private val deltaValues = mutableMapOf<android.widget.TextView, Int>()
    private val deltaRunnables = mutableMapOf<android.widget.TextView, Runnable>()
    private val deltaViews = mutableMapOf<android.widget.TextView, android.widget.TextView>()
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentComboBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupClickListeners()
        setupBackPress()
    }

    private fun setupBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastBackPressTime < BACK_PRESS_INTERVAL) {
                    MaterialAlertDialogBuilder(requireContext(), R.style.Theme_RalCounter_Dialog)
                        .setTitle(R.string.new_game_confirm_title)
                        .setMessage(R.string.new_game_confirm_msg)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.confirm) { _, _ ->
                            findNavController().navigate(R.id.action_combo_to_setup)
                        }
                        .show()
                } else {
                    lastBackPressTime = currentTime
                    android.widget.Toast.makeText(requireContext(), R.string.back_to_reset, android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun setupObservers() {
        // Ral state: update UI sections (non-dramatically)
        viewModel.ralState.observe(viewLifecycleOwner) { updateStateUI(it) }

        // Storm count (all spells)
        viewModel.stormCount.observe(viewLifecycleOwner) {
            if (lastStormCount != -1 && !isResetting) {
                val diff = it - lastStormCount
                if (diff > 0) animateIncreaseScale(binding.tvStormCount, R.color.white)
                else if (diff < 0) animateDecreaseShake(binding.tvStormCount, R.color.white)
                if (diff != 0) showDelta(binding.tvStormCount, diff)
            }
            lastStormCount = it
            binding.tvStormCount.text = it.toString()
        }

        // Instant/Sorcery count (for Ral loyalty)
        viewModel.instSorcCount.observe(viewLifecycleOwner) {
            if (lastInstSorcCount != -1 && !isResetting) {
                val diff = it - lastInstSorcCount
                if (diff > 0) animateIncreaseScale(binding.tvInstSorcCount, R.color.white)
                else if (diff < 0) animateDecreaseShake(binding.tvInstSorcCount, R.color.white)
                if (diff != 0) showDelta(binding.tvInstSorcCount, diff)
            }
            lastInstSorcCount = it
            binding.tvInstSorcCount.text = it.toString()
        }



        // Loyalty counters
        viewModel.loyaltyCounters.observe(viewLifecycleOwner) {
            if (lastLoyalty != -1 && !isResetting) {
                val diff = it - lastLoyalty
                if (diff > 0) animateIncreaseScale(binding.tvLoyalty, R.color.white)
                else if (diff < 0) animateDecreaseShake(binding.tvLoyalty, R.color.white)
                if (diff != 0) showDelta(binding.tvLoyalty, diff)
            }
            lastLoyalty = it
            binding.tvLoyalty.text = it.toString()
        }

        // Transform available
        viewModel.canTransform.observe(viewLifecycleOwner) { can ->
            binding.layoutTransform.visibility = if (can) View.VISIBLE else View.GONE
        }

        // Flip result
        viewModel.lastFlipResult.observe(viewLifecycleOwner) { result ->
            if (result != null && viewModel.ralState.value == RalState.MONSOON_MAGE) {
                binding.cardFlipResult.visibility = View.VISIBLE
                if (result.isWin) {
                    binding.tvFlipResult.text = getString(R.string.flip_win)
                    binding.tvFlipResult.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.win_color)
                    )
                    binding.tvFlipSubtitle.text = getString(R.string.flip_win_sub)
                } else {
                    binding.tvFlipResult.text = getString(R.string.flip_lose)
                    binding.tvFlipResult.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.lose_color)
                    )
                    binding.tvFlipSubtitle.text = getString(R.string.flip_lose_sub)
                }
            } else {
                binding.cardFlipResult.visibility = View.GONE
            }
        }

        // Life total
        viewModel.lifeTotal.observe(viewLifecycleOwner) {
            if (lastLifeTotal != -1 && !isResetting) {
                val diff = it - lastLifeTotal
                if (diff > 0) animateIncreaseScale(binding.tvLifeTotal, R.color.white)
                else if (diff < 0) animateDecreaseShake(binding.tvLifeTotal, R.color.white)
                if (diff != 0) showDelta(binding.tvLifeTotal, diff)
            }
            lastLifeTotal = it
            binding.tvLifeTotal.text = it.toString()
        }

        // (Commander cost observer removed)

        // Mana pool
        viewModel.manaPool.observe(viewLifecycleOwner) { pool ->
            if (lastBlueCount != -1 && !isResetting) {
                val diff = pool.blue - lastBlueCount
                if (diff > 0) animateIncreaseScale(binding.tvBlueCount, R.color.white)
                else if (diff < 0) animateDecreaseShake(binding.tvBlueCount, R.color.white)
                if (diff != 0) showDelta(binding.tvBlueCount, diff)
            }
            lastBlueCount = pool.blue
            binding.tvBlueCount.text = pool.blue.toString()

            if (lastRedCount != -1 && !isResetting) {
                val diff = pool.red - lastRedCount
                if (diff > 0) animateIncreaseScale(binding.tvRedCount, R.color.white)
                else if (diff < 0) animateDecreaseShake(binding.tvRedCount, R.color.white)
                if (diff != 0) showDelta(binding.tvRedCount, diff)
            }
            lastRedCount = pool.red
            binding.tvRedCount.text = pool.red.toString()

            if (lastGenericCount != -1 && !isResetting) {
                val diff = pool.generic - lastGenericCount
                if (diff > 0) animateIncreaseScale(binding.tvGenericCount, R.color.white)
                else if (diff < 0) animateDecreaseShake(binding.tvGenericCount, R.color.white)
                if (diff != 0) showDelta(binding.tvGenericCount, diff)
            }
            lastGenericCount = pool.generic
            binding.tvGenericCount.text = pool.generic.toString()

            if (lastTotalMana != -1 && !isResetting) {
                val diff = pool.total - lastTotalMana
                if (diff > 0) animateIncreaseScale(binding.tvTotalMana, R.color.gold_bright)
                else if (diff < 0) animateDecreaseShake(binding.tvTotalMana, R.color.gold_bright)
                if (diff != 0) showDelta(binding.tvTotalMana, diff)
            }
            lastTotalMana = pool.total
            binding.tvTotalMana.text = pool.total.toString()
        }
    }

    private fun setupClickListeners() {
        // ── Coin flip (instant/sorcery)
        binding.btnCastSpell.setOnClickListener {
            if (!isAnimating && viewModel.ralState.value == RalState.MONSOON_MAGE) {
                performCoinFlip()
            }
        }

        // ── Storm counter manual adjustments (all spells)
        binding.btnStormMinus.setOnClickListener { viewModel.manualAdjustStorm(-1) }
        binding.btnStormPlus.setOnClickListener { 
            viewModel.manualAdjustStorm(1) 
            animateButtonGreen(binding.btnStormPlus)
        }

        // ── Instant/Sorcery counter manual adjustments (for Ral loyalty)
        binding.btnInstSorcMinus.setOnClickListener { viewModel.manualAdjustInstSorc(-1) }
        binding.btnInstSorcPlus.setOnClickListener { 
            viewModel.manualAdjustInstSorc(1) 
            animateButtonGreen(binding.btnInstSorcPlus)
        }

        // ── Transform Ral
        binding.btnTransform.setOnClickListener { animateTransform() }

        // ── Flip back to creature (↩)
        binding.btnFlipBack.setOnClickListener { viewModel.flipBack() }

        // ── Planeswalker abilities
        binding.btnPlusOne.setOnClickListener { viewModel.usePlusOne() }
        binding.btnMinusTwo.setOnClickListener { viewModel.useMinusTwo() }
        binding.btnMinusEight.setOnClickListener { viewModel.useMinusEight() }

        // ── Direct Loyalty adjustments
        binding.btnLoyaltyMinus.setOnClickListener { viewModel.adjustLoyalty(-1) }
        binding.btnLoyaltyPlus.setOnClickListener { 
            viewModel.adjustLoyalty(1) 
            animateButtonGreen(binding.btnLoyaltyPlus)
        }

        // ── Life total
        binding.btnLifeMinus.setOnClickListener { viewModel.adjustLife(-1) }
        binding.btnLifePlus.setOnClickListener { 
            viewModel.adjustLife(1) 
            animateButtonGreen(binding.btnLifePlus)
        }

        // ── Mana pool
        binding.btnBluePlus.setOnClickListener { 
            viewModel.addMana(blue = 1) 
            animateButtonGreen(binding.btnBluePlus)
        }
        binding.btnBlueMinus.setOnClickListener { viewModel.spendMana(blue = 1) }
        
        binding.btnRedPlus.setOnClickListener { 
            viewModel.addMana(red = 1) 
            animateButtonGreen(binding.btnRedPlus)
        }
        binding.btnRedMinus.setOnClickListener { viewModel.spendMana(red = 1) }
        
        binding.btnGenericPlus.setOnClickListener { 
            viewModel.addMana(generic = 1) 
            animateButtonGreen(binding.btnGenericPlus)
        }
        binding.btnGenericMinus.setOnClickListener { viewModel.spendMana(generic = 1) }

        // ── Turn actions
        binding.btnNewTurn.setOnClickListener {
            isResetting = true
            viewModel.newTurn()
            viewModel.clearMana()
            
            // clear transient delta views forcefully
            deltaViews.values.forEach { 
                it.animate().cancel() 
                binding.rootFrame.removeView(it)
            }
            deltaViews.clear()
            deltaValues.clear()
            
            isResetting = false

            binding.btnNewTurn.animate().alpha(0.3f).setDuration(80)
                .withEndAction { binding.btnNewTurn.animate().alpha(1f).setDuration(300).start() }
                .start()
        }

        // ── New Game
        binding.btnNewGame.setOnClickListener {
            findNavController().navigate(R.id.action_combo_to_setup)
        }
    }

    // ──────────────────────────────────────────────
    // Coin flip animation
    // ──────────────────────────────────────────────

    private fun performCoinFlip() {
        isAnimating = true
        binding.btnCastSpell.isEnabled = false

        val rotateOut = ObjectAnimator.ofFloat(binding.btnCastSpell, "rotationY", 0f, 90f).apply {
            duration = 180
            interpolator = AccelerateInterpolator()
        }
        val rotateIn = ObjectAnimator.ofFloat(binding.btnCastSpell, "rotationY", -90f, 0f).apply {
            duration = 180
            interpolator = DecelerateInterpolator()
        }

        rotateOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                val result = viewModel.flipCoin()
                binding.btnCastSpell.text = if (result.isWin) getString(R.string.flip_win) else getString(R.string.flip_lose)
            }
        })

        AnimatorSet().apply {
            playSequentially(rotateOut, rotateIn)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    binding.btnCastSpell.isEnabled = true
                    isAnimating = false
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (_binding != null) binding.btnCastSpell.text = getString(R.string.cast_spell)
                    }, 2000)
                }
            })
            start()
        }
    }

    // ──────────────────────────────────────────────
    // Transform animation (horizontal flip of state card)
    // ──────────────────────────────────────────────

    private fun animateTransform() {
        // Now that the top banner is removed, just perform the transition directly
        viewModel.transformRal()
    }

    // ──────────────────────────────────────────────
    // Counter Value Animations
    // ──────────────────────────────────────────────

    private fun animateIncreaseScale(textView: android.widget.TextView, defaultColorRes: Int) {
        counterAnimators[textView]?.cancel()

        textView.scaleX = 1f
        textView.scaleY = 1f
        textView.setShadowLayer(0f, 0f, 0f, android.graphics.Color.TRANSPARENT)

        val set = AnimatorSet()
        counterAnimators[textView] = set

        val colorGreen = ContextCompat.getColor(requireContext(), R.color.win_color)
        val glowAnim = ValueAnimator.ofFloat(0f, 25f, 0f)
        glowAnim.addUpdateListener { animator ->
            val radius = animator.animatedValue as Float
            textView.setShadowLayer(radius, 0f, 0f, colorGreen)
        }

        val scaleX = ObjectAnimator.ofFloat(textView, "scaleX", 1f, 1.4f, 1f)
        val scaleY = ObjectAnimator.ofFloat(textView, "scaleY", 1f, 1.4f, 1f)

        set.playTogether(glowAnim, scaleX, scaleY)
        set.duration = 400
        set.interpolator = OvershootInterpolator()

        set.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                if (counterAnimators[textView] == set) {
                    counterAnimators.remove(textView)
                    textView.scaleX = 1f
                    textView.scaleY = 1f
                    textView.setShadowLayer(0f, 0f, 0f, android.graphics.Color.TRANSPARENT)
                }
            }
        })
        set.start()
    }

    private fun animateDecreaseShake(textView: android.widget.TextView, defaultColorRes: Int) {
        counterAnimators[textView]?.cancel()

        textView.translationX = 0f
        textView.setShadowLayer(0f, 0f, 0f, android.graphics.Color.TRANSPARENT)

        val set = AnimatorSet()
        counterAnimators[textView] = set

        val colorRed = ContextCompat.getColor(requireContext(), R.color.lose_color)
        val glowAnim = ValueAnimator.ofFloat(0f, 25f, 0f)
        glowAnim.addUpdateListener { animator ->
            val radius = animator.animatedValue as Float
            textView.setShadowLayer(radius, 0f, 0f, colorRed)
        }

        val shake = ObjectAnimator.ofFloat(textView, "translationX", 0f, 10f, -10f, 10f, -10f, 5f, -5f, 0f)

        set.playTogether(glowAnim, shake)
        set.duration = 400

        set.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                if (counterAnimators[textView] == set) {
                    counterAnimators.remove(textView)
                    textView.translationX = 0f
                    textView.setShadowLayer(0f, 0f, 0f, android.graphics.Color.TRANSPARENT)
                }
            }
        })
        set.start()
    }

    // ──────────────────────────────────────────────
    // Floating Delta Animations (+X / -Y)
    // ──────────────────────────────────────────────

    private fun showDelta(textView: android.widget.TextView, delta: Int) {
        if (delta == 0) return
        
        val currentDelta = (deltaValues[textView] ?: 0) + delta
        deltaValues[textView] = currentDelta

        var deltaTv = deltaViews[textView]
        if (deltaTv == null) {
            deltaTv = android.widget.TextView(requireContext()).apply {
                textSize = 28f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                layoutParams = android.widget.FrameLayout.LayoutParams(
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                )
                elevation = 10f
                setShadowLayer(8f, 0f, 4f, android.graphics.Color.BLACK)
            }
            binding.rootFrame.addView(deltaTv)
            deltaViews[textView] = deltaTv
        }

        // Setup color and text
        val isPositive = currentDelta >= 0
        deltaTv.text = if (isPositive) "+$currentDelta" else currentDelta.toString()
        val colorRes = if (isPositive) R.color.win_color else R.color.lose_color
        deltaTv.setTextColor(ContextCompat.getColor(requireContext(), colorRes))

        // Position it relative to the target textView
        val location = IntArray(2)
        textView.getLocationInWindow(location)
        val rootLocation = IntArray(2)
        binding.rootFrame.getLocationInWindow(rootLocation)
        
        val relativeX = location[0] - rootLocation[0]
        val relativeY = location[1] - rootLocation[1]

        deltaTv.x = relativeX + textView.width.toFloat() + 15f
        deltaTv.y = relativeY - 20f

        // Reset animation state
        deltaTv.alpha = 1f
        deltaTv.animate().cancel()
        
        // Pop animation
        deltaTv.scaleX = 0.5f
        deltaTv.scaleY = 0.5f
        deltaTv.animate()
            .scaleX(1.3f).scaleY(1.3f)
            .setDuration(150)
            .setInterpolator(OvershootInterpolator())
            .withEndAction {
                deltaTv.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
            }
            .start()

        // Clear previous runnable
        deltaRunnables[textView]?.let { handler.removeCallbacks(it) }
        
        // Hide after 1.5 seconds of inactivity
        val runnable = Runnable {
            deltaTv.animate()
                .alpha(0f)
                .translationYBy(-40f)
                .setDuration(300)
                .withEndAction {
                    if (_binding == null) return@withEndAction
                    binding.rootFrame.removeView(deltaTv)
                    deltaViews.remove(textView)
                    deltaValues.remove(textView)
                    deltaTv.translationY = 0f 
                }
                .start()
        }
        deltaRunnables[textView] = runnable
        handler.postDelayed(runnable, 1500)
    }

    // ──────────────────────────────────────────────
    // State UI update (non-dramatic: just show/hide sections and tint)
    // ──────────────────────────────────────────────

    private fun updateStateUI(state: RalState) {
        val isCreature = state == RalState.MONSOON_MAGE

        // Show/hide main sections
        binding.sectionCreature.visibility = if (isCreature) View.VISIBLE else View.GONE
        binding.sectionPlaneswalker.visibility = if (isCreature) View.GONE else View.VISIBLE

        // Hide flip result when switching away from creature
        if (!isCreature) binding.cardFlipResult.visibility = View.GONE

        // Subtle background color transition
        val fromColor = if (isCreature)
            ContextCompat.getColor(requireContext(), R.color.bg_planeswalker)
        else
            ContextCompat.getColor(requireContext(), R.color.bg_creature)
        val toColor = if (isCreature)
            ContextCompat.getColor(requireContext(), R.color.bg_creature)
        else
            ContextCompat.getColor(requireContext(), R.color.bg_planeswalker)

        ValueAnimator.ofArgb(fromColor, toColor).apply {
            duration = 350
            addUpdateListener { binding.root.setBackgroundColor(it.animatedValue as Int) }
            start()
        }
    }

    // ──────────────────────────────────────────────
    // Button Animations
    // ──────────────────────────────────────────────
    private fun animateButtonGreen(button: com.google.android.material.button.MaterialButton) {
        buttonAnimators[button]?.cancel()
        
        val defaultColorList = button.backgroundTintList
        val defaultColor = defaultColorList?.defaultColor ?: ContextCompat.getColor(requireContext(), R.color.izzet_blue)
        val colorGreen = ContextCompat.getColor(requireContext(), R.color.win_color)
        
        val set = AnimatorSet()
        buttonAnimators[button] = set
        
        val colorAnim = ValueAnimator.ofFloat(0f, 1f, 0f)
        colorAnim.addUpdateListener { animator ->
            val ratio = animator.animatedValue as Float
            val current = androidx.core.graphics.ColorUtils.blendARGB(defaultColor, colorGreen, ratio)
            button.backgroundTintList = android.content.res.ColorStateList.valueOf(current)
        }
        
        set.play(colorAnim)
        set.duration = 250
        set.interpolator = DecelerateInterpolator()
        
        set.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                if (buttonAnimators[button] == set) {
                    buttonAnimators.remove(button)
                    button.backgroundTintList = defaultColorList
                }
            }
        })
        set.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        
        deltaRunnables.values.forEach { handler.removeCallbacks(it) }
        deltaRunnables.clear()
        deltaViews.clear()
        deltaValues.clear()
        
        _binding = null
    }
}
