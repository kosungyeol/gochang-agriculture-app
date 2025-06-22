package com.gochang.agriculture

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.gochang.agriculture.adapter.ProjectAdapter
import com.gochang.agriculture.databinding.ActivityHomeBinding
import com.gochang.agriculture.model.Project

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupProjects()
    }
    
    private fun setupProjects() {
        val sampleProjects = getSampleProjects()
        val adapter = ProjectAdapter(this, sampleProjects)
        binding.viewPagerProjects.adapter = adapter
        
        // 페이지 변경 시 진동 피드백
        binding.viewPagerProjects.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // 진동 피드백은 프래그먼트에서 처리
            }
        })
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