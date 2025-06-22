package com.gochang.agriculture

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.viewpager2.widget.ViewPager2
import com.gochang.agriculture.adapter.ProjectAdapter
import com.gochang.agriculture.databinding.ActivityHomeBinding
import com.gochang.agriculture.model.Project

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var notificationManager: NotificationManager
    private lateinit var projectAdapter: ProjectAdapter
    private lateinit var projects: List<Project>
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannels()
        setupProjects()
        setupTestButtons()
        setupPageIndicator()
    }
    
    private fun setupProjects() {
        projects = getSampleProjects()
        projectAdapter = ProjectAdapter(this, projects)
        binding.viewPagerProjects.adapter = projectAdapter
        
        // 초기 페이지 인디케이터 설정
        updatePageIndicator(0)
    }
    
    private fun setupPageIndicator() {
        binding.viewPagerProjects.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updatePageIndicator(position)
            }
        })
    }
    
    private fun updatePageIndicator(position: Int) {
        val currentProject = projects[position]
        val categoryEmoji = when(currentProject.category) {
            "agriculture" -> "🌾"
            "forestry" -> "🌲"
            "livestock" -> "🐄"
            "fishery" -> "🐟"
            else -> "📋"
        }
        
        binding.tvPageIndicator.text = "$categoryEmoji ${position + 1} / ${projects.size}"
    }
    
    private fun setupTestButtons() {
        // 테스트 알림 버튼
        binding.btnTestNotification.setOnClickListener {
            sendTestNotification()
        }
        
        binding.btnTestProjectNotification.setOnClickListener {
            sendTestProjectNotification()
        }
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 일반 알림 채널
            val generalChannel = NotificationChannel(
                "general_notifications",
                "일반 알림",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "일반적인 앱 알림"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
            }
            
            // 사업 알림 채널  
            val projectChannel = NotificationChannel(
                "project_notifications",
                "사업 신청 알림",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "농업보조사업 신청 기간 알림"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500, 250, 500)
            }
            
            // 긴급 알림 채널
            val urgentChannel = NotificationChannel(
                "urgent_notifications", 
                "긴급 알림",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "마감 임박 등 긴급 알림"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 100, 50, 100, 50, 100, 50, 500)
            }
            
            notificationManager.createNotificationChannels(listOf(generalChannel, projectChannel, urgentChannel))
        }
    }
    
    private fun sendTestNotification() {
        val notification = NotificationCompat.Builder(this, "general_notifications")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("🌾 고창 농업 알림 테스트")
            .setContentText("알림이 정상적으로 작동합니다!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("고창군 농업보조사업 알림 시스템이 정상적으로 작동하고 있습니다. 이제 놓치지 마세요!"))
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
            
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        Toast.makeText(this, "테스트 알림이 발송되었습니다! 📱", Toast.LENGTH_SHORT).show()
    }
    
    private fun sendTestProjectNotification() {
        val currentProject = projects[binding.viewPagerProjects.currentItem]
        
        val notification = NotificationCompat.Builder(this, "project_notifications")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("🔔 신청 기간입니다!")
            .setContentText("${currentProject.name} 사업 신청이 시작되었습니다")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("${currentProject.name} 사업 신청이 시작되었습니다.\n신청기간: ${currentProject.applicationPeriod}\n지원내용: ${currentProject.support1} ${currentProject.support2}\n📍 ${currentProject.location}"))
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 250, 500, 250, 500))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
            
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        Toast.makeText(this, "${currentProject.name} 사업 알림이 발송되었습니다! 🔔", Toast.LENGTH_SHORT).show()
    }
    
    private fun getSampleProjects(): List<Project> {
        return listOf(
            // 농업
            Project(
                id = "agr001",
                category = "agriculture",
                name = "중소농기계",
                applicationPeriod = "2025.03.01~03.31",
                support1 = "농기계구입",
                support2 = "최대200만원",
                target = "농업인",
                location = "농업정책과",
                etc = "선착순"
            ),
            Project(
                id = "agr002",
                category = "agriculture",
                name = "비닐하우스",
                applicationPeriod = "2025.04.01~04.30",
                support1 = "시설비지원",
                support2 = "최대500만원",
                target = "농업인",
                location = "농업정책과",
                etc = "심사후선정"
            ),
            Project(
                id = "agr003",
                category = "agriculture",
                name = "공익직불제",
                applicationPeriod = "2025.05.01~05.31",
                support1 = "직불금지급",
                support2 = "ha당120만원",
                target = "농업인",
                location = "농업정책과",
                etc = "기준면적확인"
            ),
            
            // 임업
            Project(
                id = "for001",
                category = "forestry",
                name = "임업직불제",
                applicationPeriod = "2025.06.01~06.30",
                support1 = "직불금지급",
                support2 = "ha당50만원",
                target = "임업인",
                location = "산림녹지과",
                etc = "산지확인필요"
            ),
            Project(
                id = "for002",
                category = "forestry",
                name = "산불감시원",
                applicationPeriod = "2025.02.01~02.28",
                support1 = "일자리제공",
                support2 = "일 8만원",
                target = "주민",
                location = "산림녹지과",
                etc = "면접심사"
            ),
            
            // 축산업
            Project(
                id = "liv001",
                category = "livestock",
                name = "축산악취개선",
                applicationPeriod = "2025.07.01~07.31",
                support1 = "시설개선비",
                support2 = "최대300만원",
                target = "축산농가",
                location = "축산과",
                etc = "현장점검후"
            ),
            Project(
                id = "liv002",
                category = "livestock",
                name = "축산환경개선",
                applicationPeriod = "2025.08.01~08.31",
                support1 = "환경개선비",
                support2 = "최대1000만원",
                target = "축산농가",
                location = "축산과",
                etc = "계획서제출"
            ),
            
            // 수산업
            Project(
                id = "fish001",
                category = "fishery",
                name = "양식장소독제",
                applicationPeriod = "2025.09.01~09.30",
                support1 = "소독제지원",
                support2 = "100% 지원",
                target = "양식업자",
                location = "수산과",
                etc = "신청서제출"
            ),
            Project(
                id = "fish002",
                category = "fishery",
                name = "수산물택배",
                applicationPeriod = "2025.10.01~10.31",
                support1 = "택배비지원",
                support2 = "50% 지원",
                target = "수산업자",
                location = "수산과",
                etc = "영수증제출"
            )
        )
    }
}