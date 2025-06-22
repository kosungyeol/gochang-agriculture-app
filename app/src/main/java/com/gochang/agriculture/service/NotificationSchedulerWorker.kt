package com.gochang.agriculture.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.gochang.agriculture.HomeActivity
import com.gochang.agriculture.R
import com.gochang.agriculture.model.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class NotificationSchedulerWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val WORK_NAME = "agriculture_notification_worker"
        private const val CHANNEL_ID = "scheduled_notifications"
        
        /**
         * 정기적인 알림 체크 작업을 스케줄링
         */
        fun schedulePeriodicNotifications(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<NotificationSchedulerWorker>(
                15, TimeUnit.MINUTES // 15분마다 체크
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .setRequiresBatteryNotLow(false)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
        }
        
        /**
         * 특정 사업에 대한 일회성 알림 스케줄링
         */
        fun scheduleProjectNotification(context: Context, project: Project, delayMinutes: Long = 0) {
            val data = Data.Builder()
                .putString("project_id", project.id)
                .putString("project_name", project.name)
                .putString("project_period", project.applicationPeriod)
                .putString("project_content", project.getFormattedNotificationText())
                .build()

            val workRequest = OneTimeWorkRequestBuilder<ProjectNotificationWorker>()
                .setInputData(data)
                .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            checkAndSendNotifications()
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private suspend fun checkAndSendNotifications() {
        val today = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(Date())
        
        // 저장된 프로젝트 목록을 확인하고 알림 전송
        val projects = getSavedProjects()
        
        projects.forEach { project ->
            if (shouldSendNotification(project, today)) {
                sendProjectNotification(project)
                markNotificationSent(project.id)
            }
        }
    }
    
    private fun shouldSendNotification(project: Project, today: String): Boolean {
        val sharedPreferences = context.getSharedPreferences("agriculture_app", Context.MODE_PRIVATE)
        val lastNotificationDate = sharedPreferences.getString("last_notification_${project.id}", "")
        
        return when {
            !project.isActive -> false
            project.notificationDate == null -> false
            project.notificationDate == today && lastNotificationDate != today -> true
            isApplicationStartDate(project, today) && lastNotificationDate != today -> true
            isDeadlineApproaching(project, today) && !sharedPreferences.getBoolean("deadline_sent_${project.id}", false) -> true
            else -> false
        }
    }
    
    private fun isApplicationStartDate(project: Project, today: String): Boolean {
        val applicationPeriod = project.applicationPeriod
        val startDate = applicationPeriod.split("~")[0].trim()
        return startDate == today
    }
    
    private fun isDeadlineApproaching(project: Project, today: String): Boolean {
        try {
            val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA)
            val todayDate = dateFormat.parse(today) ?: return false
            
            val applicationPeriod = project.applicationPeriod
            val endDate = applicationPeriod.split("~")[1].trim()
            val deadline = dateFormat.parse(endDate) ?: return false
            
            val diffInDays = (deadline.time - todayDate.time) / (1000 * 60 * 60 * 24)
            return diffInDays <= 3 && diffInDays >= 0 // 3일 이내
        } catch (e: Exception) {
            return false
        }
    }
    
    private fun sendProjectNotification(project: Project) {
        createNotificationChannel()
        
        val intent = Intent(context, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("project_id", project.id)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            project.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("🔔 ${project.getCategoryEmoji()} ${project.name}")
            .setContentText("신청 기간이 시작되었습니다!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(project.getFormattedNotificationText()))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 250, 500, 250, 500))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(project.id.hashCode(), notification)
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "스케줄된 사업 알림",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "사업 신청 기간 자동 알림"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500, 250, 500)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun markNotificationSent(projectId: String) {
        val sharedPreferences = context.getSharedPreferences("agriculture_app", Context.MODE_PRIVATE)
        val today = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(Date())
        
        sharedPreferences.edit()
            .putString("last_notification_$projectId", today)
            .apply()
    }
    
    private fun getSavedProjects(): List<Project> {
        // 실제로는 SharedPreferences나 데이터베이스에서 읽어옵니다
        return getSampleProjectData()
    }
    
    private fun getSampleProjectData(): List<Project> {
        return listOf(
            Project(
                id = "agr001",
                category = "agriculture",
                name = "중소농기계 지원",
                applicationPeriod = "2025.03.01~03.31",
                support1 = "농기계구입",
                support2 = "최대200만원",
                target = "농업인",
                location = "농업정책과",
                etc = "선착순",
                notificationDate = "2025.02.25",
                isActive = true
            )
        )
    }
}

/**
 * 개별 프로젝트 알림을 위한 워커
 */
class ProjectNotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val projectId = inputData.getString("project_id") ?: return@withContext Result.failure()
            val projectName = inputData.getString("project_name") ?: return@withContext Result.failure()
            val projectPeriod = inputData.getString("project_period") ?: return@withContext Result.failure()
            val projectContent = inputData.getString("project_content") ?: return@withContext Result.failure()
            
            sendNotification(projectId, projectName, projectPeriod, projectContent)
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
    
    private fun sendNotification(projectId: String, projectName: String, projectPeriod: String, projectContent: String) {
        createNotificationChannel()
        
        val intent = Intent(context, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("project_id", projectId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            projectId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, "scheduled_notifications")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("🔔 $projectName")
            .setContentText("신청 기간: $projectPeriod")
            .setStyle(NotificationCompat.BigTextStyle().bigText(projectContent))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 250, 500, 250, 500))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(projectId.hashCode(), notification)
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "scheduled_notifications",
                "스케줄된 사업 알림",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "예정된 사업 신청 알림"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500, 250, 500)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}