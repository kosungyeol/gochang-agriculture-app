package com.gochang.agriculture

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Vibrator
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.gochang.agriculture.adapter.OnboardingAdapter
import com.gochang.agriculture.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var vibrator: Vibrator
    private lateinit var sharedPreferences: SharedPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 진동 서비스 초기화
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        
        // 첫 실행인지 확인
        val isFirstLaunch = sharedPreferences.getBoolean("first_launch", true)
        
        if (isFirstLaunch) {
            // 온보딩 화면 표시
            setupOnboarding()
        } else {
            // 메인 앱으로 이동
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }
    
    private fun setupOnboarding() {
        val adapter = OnboardingAdapter(this)
        binding.viewPager.adapter = adapter
        
        // 페이지 변경 시 진동 피드백
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // 짧은 진동 (0.1초)
                vibrator.vibrate(100)
            }
        })
    }
    
    // 온보딩 완료 후 호출
    fun completeOnboarding() {
        sharedPreferences.edit().putBoolean("first_launch", false).apply()
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}