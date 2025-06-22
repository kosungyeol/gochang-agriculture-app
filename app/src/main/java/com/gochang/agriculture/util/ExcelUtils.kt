package com.gochang.agriculture.util

import android.content.Context
import android.net.Uri
import com.gochang.agriculture.model.Project
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class ExcelUtils {
    
    companion object {
        private val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA)
        
        /**
         * 엑셀 파일에서 사업 데이터를 읽어옵니다
         */
        fun readProjectsFromExcel(context: Context, uri: Uri): List<Project> {
            val projects = mutableListOf<Project>()
            
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                inputStream?.use { stream ->
                    val workbook = WorkbookFactory.create(stream)
                    val sheet = workbook.getSheetAt(0) // 첫 번째 시트
                    
                    // 헤더 행을 건너뛰고 데이터 행부터 읽기
                    for (i in 1..sheet.lastRowNum) {
                        val row = sheet.getRow(i) ?: continue
                        
                        try {
                            val project = Project(
                                id = getCellValue(row.getCell(0)),
                                category = getCellValue(row.getCell(1)),
                                name = getCellValue(row.getCell(2)),
                                applicationPeriod = getCellValue(row.getCell(3)),
                                support1 = getCellValue(row.getCell(4)),
                                support2 = getCellValue(row.getCell(5)),
                                target = getCellValue(row.getCell(6)),
                                location = getCellValue(row.getCell(7)),
                                etc = getCellValue(row.getCell(8)),
                                notificationDate = getCellValue(row.getCell(9)),
                                isActive = getCellValue(row.getCell(10)).equals("TRUE", ignoreCase = true),
                                phone = getCellValue(row.getCell(11)),
                                email = getCellValue(row.getCell(12)),
                                requirements = getCellValue(row.getCell(13))
                            )
                            
                            if (project.id.isNotEmpty() && project.name.isNotEmpty()) {
                                projects.add(project)
                            }
                        } catch (e: Exception) {
                            // 잘못된 행은 건너뛰기
                            continue
                        }
                    }
                    
                    workbook.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                throw Exception("엑셀 파일을 읽는 중 오류가 발생했습니다: ${e.message}")
            }
            
            return projects
        }
        
        /**
         * 셀 값을 문자열로 변환
         */
        private fun getCellValue(cell: org.apache.poi.ss.usermodel.Cell?): String {
            return when {
                cell == null -> ""
                cell.cellType == org.apache.poi.ss.usermodel.CellType.STRING -> cell.stringCellValue.trim()
                cell.cellType == org.apache.poi.ss.usermodel.CellType.NUMERIC -> {
                    if (DateUtil.isCellDateFormatted(cell)) {
                        dateFormat.format(cell.dateCellValue)
                    } else {
                        cell.numericCellValue.toInt().toString()
                    }
                }
                cell.cellType == org.apache.poi.ss.usermodel.CellType.BOOLEAN -> cell.booleanCellValue.toString()
                cell.cellType == org.apache.poi.ss.usermodel.CellType.FORMULA -> {
                    try {
                        cell.stringCellValue.trim()
                    } catch (e: Exception) {
                        cell.numericCellValue.toString()
                    }
                }
                else -> ""
            }
        }
        
        /**
         * 샘플 엑셀 파일을 앱 내부 저장소에 생성
         */
        fun createSampleExcelFile(context: Context): Boolean {
            return try {
                val sampleData = getSampleProjectData()
                val csvContent = buildString {
                    appendLine("id,category,name,applicationPeriod,support1,support2,target,location,etc,notificationDate,isActive,phone,email,requirements")
                    sampleData.forEach { project ->
                        appendLine("${project.id},${project.category},${project.name},${project.applicationPeriod},${project.support1},${project.support2},${project.target},${project.location},${project.etc},${project.notificationDate ?: ""},${project.isActive},${project.phone ?: ""},${project.email ?: ""},${project.requirements ?: ""}")
                    }
                }
                
                val file = context.getFileStreamPath("sample_projects.csv")
                file.writeText(csvContent)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
        
        /**
         * 샘플 프로젝트 데이터
         */
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
                    isActive = true,
                    phone = "063-560-2456",
                    email = "agri@gochang.go.kr",
                    requirements = "농업인증명서, 사업계획서"
                ),
                Project(
                    id = "agr002",
                    category = "agriculture",
                    name = "스마트팜 지원",
                    applicationPeriod = "2025.04.01~04.30",
                    support1 = "시설비지원",
                    support2 = "최대500만원",
                    target = "농업인",
                    location = "농업정책과",
                    etc = "심사후선정",
                    notificationDate = "2025.03.25",
                    isActive = true,
                    phone = "063-560-2456",
                    email = "smart@gochang.go.kr",
                    requirements = "농업인증명서, 시설설치계획서, 견적서"
                ),
                Project(
                    id = "for001",
                    category = "forestry",
                    name = "조림지원사업",
                    applicationPeriod = "2025.05.01~05.31",
                    support1 = "조림비지원",
                    support2 = "ha당150만원",
                    target = "임업인",
                    location = "산림녹지과",
                    etc = "산지확인필요",
                    notificationDate = "2025.04.25",
                    isActive = true,
                    phone = "063-560-2789",
                    email = "forest@gochang.go.kr",
                    requirements = "산지이용계획서, 조림계획서"
                )
            )
        }
    }
}