package com.gochang.agriculture.fragment

import android.content.Context
import android.os.Bundle
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.gochang.agriculture.MainActivity
import com.gochang.agriculture.R
import com.gochang.agriculture.databinding.FragmentInterestSelectionBinding

class InterestSelectionFragment : Fragment() {
    private var _binding: FragmentInterestSelectionBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var vibrator: Vibrator
    private val selectedInterests = mutableSetOf<String>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInterestSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        
        setupButtons()
    }
    
    private fun setupButtons() {
        // 관심분야 선택 버튼들
        binding.btnAgriculture.setOnClickListener {
            vibrator.vibrate(100)
            toggleInterest("agriculture", binding.btnAgriculture)
        }
        
        binding.btnForestry.setOnClickListener {
            vibrator.vibrate(100)
            toggleInterest("forestry", binding.btnForestry)
        }
        
        binding.btnLivestock.setOnClickListener {
            vibrator.vibrate(100)
            toggleInterest("livestock", binding.btnLivestock)
        }
        
        binding.btnFishery.setOnClickListener {
            vibrator.vibrate(100)
            toggleInterest("fishery", binding.btnFishery)
        }
        
        // 다음 버튼
        binding.btnNext.setOnClickListener {
            vibrator.vibrate(100)
            if (validateSelection()) {
                saveInterests()
                completeOnboarding()
            }
        }
    }
    
    private fun toggleInterest(interest: String, button: View) {
        if (selectedInterests.contains(interest)) {
            selectedInterests.remove(interest)
            button.isSelected = false
        } else {
            selectedInterests.add(interest)
            button.isSelected = true
        }
    }
    
    private fun validateSelection(): Boolean {
        if (selectedInterests.isEmpty()) {
            showToast(getString(R.string.error_no_interest_selected))
            return false
        }
        return true
    }
    
    private fun saveInterests() {
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        
        sharedPreferences.edit().apply {
            putStringSet("user_interests", selectedInterests)
            apply()
        }
    }
    
    private fun completeOnboarding() {
        (activity as? MainActivity)?.completeOnboarding()
    }
    
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}