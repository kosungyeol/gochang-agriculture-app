package com.gochang.agriculture

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.gochang.agriculture.adapter.ProjectAdapter
import com.gochang.agriculture.databinding.ActivityHomeBinding
import com.gochang.agriculture.model.Project
import com.gochang.agriculture.service.NotificationSchedulerWorker
import com.gochang.agriculture.util.ExcelUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var notificationManager: NotificationManager
    private lateinit var projectAdapter: ProjectAdapter
    private lateinit var projects: MutableList<Project>
    
    // 파일 선택기
    private val excelFileLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { loadProjectsFromExcel(it) }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannels()
        setupProjects()
        setupButtons()
        setupPageIndicator()
        
        // 백그라운드 알림 스케줄러 시작
        NotificationSchedulerWorker.schedulePeriodicNotifications(this)
    }
    
    private fun setupProjects() {
        projects = getSampleProjects().toMutableList()
        projectAdapter = ProjectAdapter(this, projects)
        binding.viewPagerProjects.adapter = projectAdapter
        
        // 초기 페이지 인디케이터 설정
        updatePageIndicator(0)
        updateStatus("기본 사업 데이터가 로드되었습니다")
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
        if (projects.isNotEmpty()) {
            val currentProject = projects[position]
            val categoryEmoji = currentProject.getCategoryEmoji()
            binding.tvPageIndicator.text = "$categoryEmoji ${position + 1} / ${projects.size}"
        }
    }
    
    private fun updateStatus(message: String) {
        binding.tvStatus.text = "📱 $message"
    }
    
    private fun setupButtons() {
        // 테스트 알림 버튼
        binding.btnTestNotification.setOnClickListener {
            sendTestNotification()
        }
        
        binding.btnTestProjectNotification.setOnClickListener {
            sendTestProjectNotification()
        }
        
        // 엑셀 파일 불러오기
        binding.btnLoadExcel.setOnClickListener {
            showExcelLoadDialog()
        }
        
        // 알림 설정
        binding.btnManageNotifications.setOnClickListener {
            showNotificationSettingsDialog()
        }
        
        // 샘플 파일 생성
        binding.btnCreateSample.setOnClickListener {
            createSampleFile()
        }
    }
    
    private fun showExcelLoadDialog() {
        AlertDialog.Builder(this)
            .setTitle("📊 엑셀 파일 불러오기")
            .setMessage("사업 데이터가 포함된 엑셀 파일을 선택하세요.\n\n지원 형식: .xlsx, .xls\n\n기존 데이터는 새로운 데이터로 교체됩니다.")
            .setPositiveButton("파일 선택") { _, _ ->
                excelFileLauncher.launch("*/*")
            }
            .setNegativeButton("취소", null)
            .show()
    }
    
    private fun loadProjectsFromExcel(uri: Uri) {
        updateStatus("엑셀 파일을 읽는 중...")
        
        lifecycleScope.launch {
            try {
                val newProjects = withContext(Dispatchers.IO) {
                    ExcelUtils.readProjectsFromExcel(this@HomeActivity, uri)
                }
                
                if (newProjects.isNotEmpty()) {
                    projects.clear()
                    projects.addAll(newProjects)
                    projectAdapter.notifyDataSetChanged()
                    updatePageIndicator(0)
                    binding.viewPagerProjects.currentItem = 0
                    
                    updateStatus("${newProjects.size}개 사업이 로드되었습니다!")
                    Toast.makeText(this@HomeActivity, "✅ ${newProjects.size}개 사업을 성공적으로 불러왔습니다!", Toast.LENGTH_LONG).show()
                    
                    // 새로운 프로젝트들에 대한 알림 스케줄링
                    scheduleNotificationsForProjects(newProjects)
                    
                } else {
                    updateStatus("엑셀 파일에서 사업 데이터를 찾을 수 없습니다")
                    Toast.makeText(this@HomeActivity, "❌ 유효한 사업 데이터가 없습니다", Toast.LENGTH_LONG).show()
                }
                
            } catch (e: Exception) {
                updateStatus("엑셀 파일 로드 실패")
                Toast.makeText(this@HomeActivity, "❌ 파일 로드 실패: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun scheduleNotificationsForProjects(projectList: List<Project>) {
        projectList.forEach { project ->
            if (project.isActive && !project.notificationDate.isNullOrEmpty()) {
                // 실제 날짜 계산 후 알림 스케줄링
                NotificationSchedulerWorker.scheduleProjectNotification(this, project, 0)
            }
        }
        Toast.makeText(this, "🔔 ${projectList.size}개 사업에 대한 알림이 설정되었습니다", Toast.LENGTH_SHORT).show()
    }
    
    private fun showNotificationSettingsDialog() {
        val options = arrayOf(
            "🔔 즉시 테스트 알림 보내기",
            "⏰ 1분 후 알림 보내기",
            "📅 현재 사업 알림 스케줄링",
            "🚫 모든 알림 취소"
        )
        
        AlertDialog.Builder(this)
            .setTitle("⚙️ 알림 설정")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> sendTestNotification()
                    1 -> scheduleDelayedNotification()
                    2 -> scheduleCurrentProjectNotification()
                    3 -> cancelAllNotifications()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }
    
    private fun scheduleDelayedNotification() {
        if (projects.isNotEmpty()) {
            val currentProject = projects[binding.viewPagerProjects.currentItem]
            NotificationSchedulerWorker.scheduleProjectNotification(this, currentProject, 1) // 1분 후
            Toast.makeText(this, "⏰ 1분 후에 ${currentProject.name} 알림이 전송됩니다", Toast.LENGTH_SHORT).show()
            updateStatus("1분 후 알림 예약됨: ${currentProject.name}")
        }
    }
    
    private fun scheduleCurrentProjectNotification() {
        if (projects.isNotEmpty()) {
            val currentProject = projects[binding.viewPagerProjects.currentItem]
            NotificationSchedulerWorker.scheduleProjectNotification(this, currentProject, 0)
            Toast.makeText(this, "📅 ${currentProject.name} 알림이 스케줄되었습니다", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun cancelAllNotifications() {
        notificationManager.cancelAll()
        Toast.makeText(this, "🚫 모든 알림이 취소되었습니다", Toast.LENGTH_SHORT).show()
        updateStatus("모든 알림이 취소되었습니다")
    }
    
    private fun createSampleFile() {
        lifecycleScope.launch {
            val success = withContext(Dispatchers.IO) {
                ExcelUtils.createSampleExcelFile(this@HomeActivity)
            }
            
            if (success) {
                Toast.makeText(this@HomeActivity, "✅ 샘플 파일이 생성되었습니다!\n내부 저장소의 sample_projects.csv 확인", Toast.LENGTH_LONG).show()
                updateStatus("샘플 CSV 파일이 생성되었습니다")
            } else {
                Toast.makeText(this@HomeActivity, "❌ 샘플 파일 생성에 실패했습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    "general_notifications",
                    "일반 알림",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "일반적인 앱 알림"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 300, 200, 300)
                },
                
                NotificationChannel(
                    "project_notifications",
                    "사업 신청 알림",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "농업보조사업 신청 기간 알림"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 500, 250, 500, 250, 500)
                },
                
                NotificationChannel(
                    "urgent_notifications", 
                    "긴급 알림",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "마감 임박 등 긴급 알림"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 100, 50, 100, 50, 100, 50, 500)
                }
            )
            
            notificationManager.createNotificationChannels(channels)
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
        updateStatus("테스트 알림 발송 완료")
    }
    
    private fun sendTestProjectNotification() {
        if (projects.isNotEmpty()) {
            val currentProject = projects[binding.viewPagerProjects.currentItem]
            
            val notification = NotificationCompat.Builder(this, "project_notifications")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("🔔 신청 기간입니다!")
                .setContentText("${currentProject.name} 사업 신청이 시작되었습니다")
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText(currentProject.getFormattedNotificationText()))
                .setAutoCancel(true)
                .setVibrate(longArrayOf(0, 500, 250, 500, 250, 500))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
                
            notificationManager.notify(System.currentTimeMillis().toInt(), notification)
            Toast.makeText(this, "${currentProject.name} 사업 알림이 발송되었습니다! 🔔", Toast.LENGTH_SHORT).show()
            updateStatus("${currentProject.name} 알림 발송 완료")
        }
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
                etc = "선착순",
                notificationDate = "2025.02.25",
                isActive = true,
                phone = "063-560-2456",
                email = "agri@gochang.go.kr"
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
                etc = "심사후선정",
                notificationDate = "2025.03.25",
                isActive = true,
                phone = "063-560-2456"
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
                etc = "기준면적확인",
                isActive = true
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
                etc = "산지확인필요",
                isActive = true
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
                etc = "면접심사",
                isActive = true
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
                etc = "현장점검후",
                isActive = true
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
                etc = "계획서제출",
                isActive = true
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
                etc = "신청서제출",
                isActive = true
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
                etc = "영수증제출",
                isActive = true
            )
        )
    }
}