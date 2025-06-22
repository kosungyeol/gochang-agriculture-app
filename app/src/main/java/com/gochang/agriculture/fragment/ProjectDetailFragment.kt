package com.gochang.agriculture.fragment

import android.content.Context
import android.os.Bundle
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.gochang.agriculture.databinding.FragmentProjectDetailBinding
import com.gochang.agriculture.model.Project

class ProjectDetailFragment : Fragment() {
    private var _binding: FragmentProjectDetailBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var vibrator: Vibrator
    private lateinit var project: Project
    private var position: Int = 0
    private var totalCount: Int = 0
    
    companion object {
        private const val ARG_PROJECT_ID = "project_id"
        private const val ARG_PROJECT_NAME = "project_name"
        private const val ARG_PROJECT_CATEGORY = "project_category"
        private const val ARG_PROJECT_PERIOD = "project_period"
        private const val ARG_PROJECT_SUPPORT1 = "project_support1"
        private const val ARG_PROJECT_SUPPORT2 = "project_support2"
        private const val ARG_PROJECT_TARGET = "project_target"
        private const val ARG_PROJECT_LOCATION = "project_location"
        private const val ARG_PROJECT_ETC = "project_etc"
        private const val ARG_POSITION = "position"
        private const val ARG_TOTAL_COUNT = "total_count"
        
        fun newInstance(project: Project, position: Int, totalCount: Int): ProjectDetailFragment {
            val fragment = ProjectDetailFragment()
            val args = Bundle().apply {
                putString(ARG_PROJECT_ID, project.id)
                putString(ARG_PROJECT_NAME, project.name)
                putString(ARG_PROJECT_CATEGORY, project.category)
                putString(ARG_PROJECT_PERIOD, project.applicationPeriod)
                putString(ARG_PROJECT_SUPPORT1, project.support1)
                putString(ARG_PROJECT_SUPPORT2, project.support2)
                putString(ARG_PROJECT_TARGET, project.target)
                putString(ARG_PROJECT_LOCATION, project.location)
                putString(ARG_PROJECT_ETC, project.etc)
                putInt(ARG_POSITION, position)
                putInt(ARG_TOTAL_COUNT, totalCount)
            }
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProjectDetailBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        
        loadProjectData()
        setupUI()
        setupButtons()
    }
    
    private fun loadProjectData() {
        arguments?.let { args ->
            project = Project(
                id = args.getString(ARG_PROJECT_ID) ?: "",
                category = args.getString(ARG_PROJECT_CATEGORY) ?: "",
                name = args.getString(ARG_PROJECT_NAME) ?: "",
                applicationPeriod = args.getString(ARG_PROJECT_PERIOD) ?: "",
                support1 = args.getString(ARG_PROJECT_SUPPORT1) ?: "",
                support2 = args.getString(ARG_PROJECT_SUPPORT2) ?: "",
                target = args.getString(ARG_PROJECT_TARGET) ?: "",
                location = args.getString(ARG_PROJECT_LOCATION) ?: "",
                etc = args.getString(ARG_PROJECT_ETC) ?: ""
            )
            position = args.getInt(ARG_POSITION, 0)
            totalCount = args.getInt(ARG_TOTAL_COUNT, 0)
        }
    }
    
    private fun setupUI() {
        binding.apply {
            // 진행상황 표시
            tvProgress.text = "${project.getCategoryEmoji()} ${project.getCategoryName()} 사업 ${position + 1}/$totalCount"
            
            // 프로젝트 정보 표시
            tvProjectName.text = project.name
            tvApplicationPeriod.text = project.applicationPeriod
            tvSupport1.text = project.support1
            tvSupport2.text = project.support2
            tvTarget.text = project.target
            tvLocation.text = project.location
            tvEtc.text = project.etc
        }
    }
    
    private fun setupButtons() {
        binding.btnNotification.setOnClickListener {
            vibrator.vibrate(100) // 0.1초 진동
            toggleNotification()
        }
    }
    
    private fun toggleNotification() {
        project.isNotificationEnabled = !project.isNotificationEnabled
        
        val message = if (project.isNotificationEnabled) {
            "🔔 \"${project.name}\" 알림이 설정되었습니다"
        } else {
            "🔕 \"${project.name}\" 알림이 해제되었습니다"
        }
        
        // 버튼 상태 업데이트
        binding.btnNotification.text = if (project.isNotificationEnabled) {
            "🔕 알림해제"
        } else {
            "🔔 알림받기"
        }
        
        // SharedPreferences에 저장
        saveNotificationSetting()
        
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    
    private fun saveNotificationSetting() {
        val sharedPreferences = requireContext().getSharedPreferences("notifications", Context.MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putBoolean(project.id, project.isNotificationEnabled)
            apply()
        }
    }
    
    private fun loadNotificationSetting() {
        val sharedPreferences = requireContext().getSharedPreferences("notifications", Context.MODE_PRIVATE)
        project.isNotificationEnabled = sharedPreferences.getBoolean(project.id, false)
        
        binding.btnNotification.text = if (project.isNotificationEnabled) {
            "🔕 알림해제"
        } else {
            "🔔 알림받기"
        }
    }
    
    override fun onResume() {
        super.onResume()
        loadNotificationSetting()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}