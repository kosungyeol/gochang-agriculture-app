package com.gochang.agriculture

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // 메시지 데이터 처리
        remoteMessage.notification?.let {
            sendNotification(it.title ?: "", it.body ?: "")
        }

        // 데이터 페이로드가 있는 경우
        if (remoteMessage.data.isNotEmpty()) {
            val projectId = remoteMessage.data["project_id"]
            val projectName = remoteMessage.data["project_name"]
            val applicationPeriod = remoteMessage.data["application_period"]
            
            if (projectId != null && projectName != null) {
                sendProjectNotification(projectName, applicationPeriod ?: "")
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        
        // 새 토큰을 서버에 전송
        sendTokenToServer(token)
    }

    private fun sendNotification(title: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "agriculture_notifications"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
            .setVibrate(longArrayOf(0, 500, 250, 500)) // 긴 진동 (0.5초)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android 8.0 이상에서는 알림 채널 생성 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "농업보조사업 알림",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "농업보조사업 신청 기간 알림"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }

    private fun sendProjectNotification(projectName: String, applicationPeriod: String) {
        val intent = Intent(this, NotificationActivity::class.java).apply {
            putExtra("project_name", projectName)
            putExtra("application_period", applicationPeriod)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "project_notifications"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("🔔 신청 기간입니다!")
            .setContentText("$projectName 사업 신청이 시작되었습니다")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$projectName 사업 신청이 시작되었습니다.\n신청기간: $applicationPeriod"))
            .setAutoCancel(true)
            .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
            .setVibrate(longArrayOf(0, 500, 250, 500, 250, 500)) // 더 긴 진동
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "보조사업 신청 알림",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "보조사업 신청 기간 시작 알림"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500, 250, 500)
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    private fun sendTokenToServer(token: String) {
        // 실제 서버 구현 시 토큰을 서버에 전송
        // 현재는 로컬에 저장
        val sharedPreferences = getSharedPreferences("firebase", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("fcm_token", token).apply()
    }
}