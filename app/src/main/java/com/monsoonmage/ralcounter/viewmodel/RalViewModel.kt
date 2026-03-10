package com.monsoonmage.ralcounter.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.monsoonmage.ralcounter.engine.CoinFlipEngine
import com.monsoonmage.ralcounter.model.FlipResult
import com.monsoonmage.ralcounter.model.ManaPool
import com.monsoonmage.ralcounter.model.RalState

class RalViewModel : ViewModel() {

    // --- Ral state ---
    private val _ralState = MutableLiveData(RalState.MONSOON_MAGE)
    val ralState: LiveData<RalState> = _ralState

    // --- Storm count: ALL spells cast this turn (for storm mechanic) ---
    private val _stormCount = MutableLiveData(0)
    val stormCount: LiveData<Int> = _stormCount

    // --- Instant/Sorcery count: only for Ral's loyalty counters ---
    private val _instSorcCount = MutableLiveData(0)
    val instSorcCount: LiveData<Int> = _instSorcCount

    // --- Loyalty (planeswalker side) ---
    private val _loyaltyCounters = MutableLiveData(2)
    val loyaltyCounters: LiveData<Int> = _loyaltyCounters


    // --- Last flip result ---
    private val _lastFlipResult = MutableLiveData<FlipResult?>(null)
    val lastFlipResult: LiveData<FlipResult?> = _lastFlipResult

    // --- Transform available flag ---
    private val _canTransform = MutableLiveData(false)
    val canTransform: LiveData<Boolean> = _canTransform

    // --- Full flip history ---
    private val _flipHistory = MutableLiveData<List<FlipResult>>(emptyList())
    val flipHistory: LiveData<List<FlipResult>> = _flipHistory

    // --- Life total ---
    private val _lifeTotal = MutableLiveData(40)
    val lifeTotal: LiveData<Int> = _lifeTotal

    // --- Commander tax (times cast from command zone) ---
    private val _timesCast = MutableLiveData(0)
    val timesCast: LiveData<Int> = _timesCast

    // --- Floating mana pool ---
    private val _manaPool = MutableLiveData(ManaPool())
    val manaPool: LiveData<ManaPool> = _manaPool

    private var turnNumber = 1

    // -------------------------------------------------------------------------
    // Game Initialization
    // -------------------------------------------------------------------------
    
    fun startGame(startingLife: Int) {
        _lifeTotal.value = startingLife
        _ralState.value = RalState.MONSOON_MAGE
        _stormCount.value = 0
        _instSorcCount.value = 0

        _canTransform.value = false
        _lastFlipResult.value = null
        _loyaltyCounters.value = 2
        _flipHistory.value = emptyList()
        _manaPool.value = ManaPool()
        turnNumber = 1
    }

    // -------------------------------------------------------------------------
    // Spell casting: instant or sorcery (triggers coin flip in creature mode)
    // Increments BOTH storm count AND instant/sorcery count
    // -------------------------------------------------------------------------

    fun flipCoin(): FlipResult {
        val currentInstSorc = _instSorcCount.value ?: 0

        val isWin = CoinFlipEngine.flip()
        val result = FlipResult(isWin, currentInstSorc, turnNumber)
        _lastFlipResult.value = result

        // Add to history (newest first)
        val updated = (_flipHistory.value?.toMutableList() ?: mutableListOf())
            .also { it.add(0, result) }
        _flipHistory.value = updated

        if (isWin) {
            _canTransform.value = true
        } else {
            _canTransform.value = false
            _lifeTotal.value = (_lifeTotal.value ?: 40) - 1
        }
        return result
    }

    /** Cast a non-instant/sorcery spell: only increments storm count (no flip, no loyalty) */
    fun castOtherSpell() {
        _stormCount.value = (_stormCount.value ?: 0) + 1
    }

    /** Manual adjustment of instant/sorcery counter (also adjusts storm counter) */
    fun manualAdjustInstSorc(delta: Int) {
        val newInstSorc = maxOf(0, (_instSorcCount.value ?: 0) + delta)
        _instSorcCount.value = newInstSorc
        if (delta > 0) _stormCount.value = (_stormCount.value ?: 0) + delta
        else _stormCount.value = maxOf(0, (_stormCount.value ?: 0) + delta)
    }

    /** Manual adjustment of storm count only */
    fun manualAdjustStorm(delta: Int) {
        _stormCount.value = maxOf(0, (_stormCount.value ?: 0) + delta)
    }

    // -------------------------------------------------------------------------
    // Transform Ral → Leyline Prodigy
    // -------------------------------------------------------------------------

    fun transformRal() {
        val spells = _instSorcCount.value ?: 0
        _loyaltyCounters.value = 2 + spells   // enters with 2 + inst/sorc spells this turn
        _ralState.value = RalState.LEYLINE_PRODIGY
        _canTransform.value = false
    }

    /** Flip back to creature side (↩) - does NOT reset counters */
    fun flipBack() {
        _ralState.value = RalState.MONSOON_MAGE
        _lastFlipResult.value = null
        _canTransform.value = false
    }

    // -------------------------------------------------------------------------
    // Planeswalker abilities
    // -------------------------------------------------------------------------

    fun usePlusOne() {
        _loyaltyCounters.value = (_loyaltyCounters.value ?: 0) + 1
    }

    fun useMinusTwo() {
        _loyaltyCounters.value = (_loyaltyCounters.value ?: 0) - 2
    }

    fun useMinusEight() {
        _loyaltyCounters.value = (_loyaltyCounters.value ?: 0) - 8
    }

    fun adjustLoyalty(delta: Int) {
        _loyaltyCounters.value = maxOf(0, (_loyaltyCounters.value ?: 0) + delta)
    }

    // (Removed castCommander as per user request to remove commander cost UI)

    // -------------------------------------------------------------------------
    // Turn management
    // -------------------------------------------------------------------------

    fun newTurn() {
        turnNumber++
        _stormCount.value = 0
        _instSorcCount.value = 0

        _canTransform.value = false
        _lastFlipResult.value = null
    }

    // -------------------------------------------------------------------------
    // Life total
    // -------------------------------------------------------------------------

    fun adjustLife(delta: Int) {
        _lifeTotal.value = (_lifeTotal.value ?: 40) + delta
    }

    // -------------------------------------------------------------------------
    // Mana pool
    // -------------------------------------------------------------------------

    fun addMana(blue: Int = 0, red: Int = 0, generic: Int = 0) {
        val c = _manaPool.value ?: ManaPool()
        _manaPool.value = c.copy(
            blue = c.blue + blue,
            red = c.red + red,
            generic = c.generic + generic
        )
    }

    fun spendMana(blue: Int = 0, red: Int = 0, generic: Int = 0) {
        val c = _manaPool.value ?: ManaPool()
        _manaPool.value = c.copy(
            blue = maxOf(0, c.blue - blue),
            red = maxOf(0, c.red - red),
            generic = maxOf(0, c.generic - generic)
        )
    }

    fun clearMana() { _manaPool.value = ManaPool() }

    fun clearFlipHistory() {
        _flipHistory.value = emptyList()
        _lastFlipResult.value = null
    }
}
