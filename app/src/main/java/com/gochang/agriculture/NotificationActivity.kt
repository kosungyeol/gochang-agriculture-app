package com.gochang.agriculture

import android.content.Context
import android.os.Bundle
import android.os.Vibrator
import androidx.appcompat.app.AppCompatActivity
import com.gochang.agriculture.databinding.ActivityNotificationBinding

class NotificationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotificationBinding
    private lateinit var vibrator: Vibrator
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        
        setupNotificationData()
        setupButtons()
    }
    
    private fun setupNotificationData() {
        val projectName = intent.getStringExtra("project_name") ?: "보조사업"
        val applicationPeriod = intent.getStringExtra("application_period") ?: ""
        val location = intent.getStringExtra("location") ?: "관련 부서"
        
        binding.apply {
            tvNotificationProjectName.text = projectName
            tvNotificationPeriod.text = applicationPeriod
            tvNotificationLocation.text = location
        }
    }
    
    private fun setupButtons() {
        // 내일 오전 10시 알림
        binding.btnTomorrowAlarm.setOnClickListener {
            vibrator.vibrate(100)
            // TODO: 내일 오전 10시 알람 설정
            finish()
        }
        
        // 내년 동일사업 다시알림
        binding.btnNextYearAlarm.setOnClickListener {
            vibrator.vibrate(100)
            // TODO: 내년 알림 설정
            finish()
        }
        
        // 관심없음
        binding.btnNotInterested.setOnClickListener {
            vibrator.vibrate(100)
            // TODO: 해당 사업 알림 비활성화
            finish()
        }
    }
}