package com.gochang.agriculture.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            
            // 부팅 완료 후 알림 스케줄러 재시작
            NotificationSchedulerWorker.schedulePeriodicNotifications(context)
        }
    }
}