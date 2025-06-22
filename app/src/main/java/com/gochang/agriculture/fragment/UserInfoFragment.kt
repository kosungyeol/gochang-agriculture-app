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
import com.gochang.agriculture.databinding.FragmentUserInfoBinding

class UserInfoFragment : Fragment() {
    private var _binding: FragmentUserInfoBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var vibrator: Vibrator
    private var selectedGender: String = ""
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserInfoBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        
        setupButtons()
    }
    
    private fun setupButtons() {
        // 성별 선택 버튼
        binding.btnMale.setOnClickListener {
            vibrator.vibrate(100) // 0.1초 진동
            selectGender("male")
        }
        
        binding.btnFemale.setOnClickListener {
            vibrator.vibrate(100) // 0.1초 진동
            selectGender("female")
        }
        
        // 다음 버튼
        binding.btnNext.setOnClickListener {
            vibrator.vibrate(100) // 0.1초 진동
            if (validateInput()) {
                saveUserInfo()
                (activity as? MainActivity)?.let { mainActivity ->
                    // ViewPager의 다음 페이지로 이동
                    val viewPager = mainActivity.binding.viewPager
                    if (viewPager.currentItem < 1) {
                        viewPager.currentItem = viewPager.currentItem + 1
                    }
                }
            }
        }
    }
    
    private fun selectGender(gender: String) {
        selectedGender = gender
        
        // 버튼 선택 상태 업데이트
        binding.btnMale.isSelected = (gender == "male")
        binding.btnFemale.isSelected = (gender == "female")
    }
    
    private fun validateInput(): Boolean {
        val name = binding.etName.text.toString().trim()
        val birthDate = binding.etBirthDate.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        
        when {
            name.isEmpty() -> {
                showToast(getString(R.string.error_empty_name))
                return false
            }
            birthDate.isEmpty() -> {
                showToast(getString(R.string.error_empty_birth_date))
                return false
            }
            phone.isEmpty() -> {
                showToast(getString(R.string.error_empty_phone))
                return false
            }
            selectedGender.isEmpty() -> {
                showToast(getString(R.string.error_no_gender_selected))
                return false
            }
        }
        return true
    }
    
    private fun saveUserInfo() {
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        
        sharedPreferences.edit().apply {
            putString("user_name", binding.etName.text.toString().trim())
            putString("user_birth_date", binding.etBirthDate.text.toString().trim())
            putString("user_phone", binding.etPhone.text.toString().trim())
            putString("user_gender", selectedGender)
            apply()
        }
    }
    
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}