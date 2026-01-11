package com.azrag.heyu.data

object YeditepeData {

    val faculties = listOf(
        "Faculty of Dentistry",
        "Faculty of Pharmacy",
        "Faculty of Education",
        "Faculty of Arts and Sciences",
        "Faculty of Fine Arts",
        "Faculty of Law",
        "Faculty of Economics and Administrative Sciences",
        "Faculty of Communication",
        "Faculty of Architecture",
        "Faculty of Engineering",
        "Faculty of Health Sciences",
        "Faculty of Medicine",
        "Faculty of Commercial Sciences"
    )

    val majorsByFaculty = mapOf(
        "Faculty of Dentistry" to listOf("Dentistry"),
        "Faculty of Pharmacy" to listOf("Pharmacy"),
        "Faculty of Education" to listOf("English Language Teaching", "Preschool Teacher", "Special Education Teacher", "Guidance and Psychological Counseling"),
        "Faculty of Arts and Sciences" to listOf("Anthropology", "Philosophy", "Physics", "English Language and Literature", "Mathematics", "Psychology", "Sociology", "History", "Turkish Language and Literature", "Translation and Interpreting Studies"),
        "Faculty of Fine Arts" to listOf("Industrial Design", "Graphic Design", "Fashion and Textile Design", "Plastic Arts and Painting", "Art and Culture Management", "Theater"),
        "Faculty of Law" to listOf("Law"),
        "Faculty of Economics and Administrative Sciences" to listOf("Economics", "Business Administration", "Public Administration", "Political Science and International Relations", "International Business Administration (German)"),
        "Faculty of Communication" to listOf("Journalism", "Public Relations and Advertising", "Radio, Television and Cinema", "Advertising Design and Communication", "Visual Communication Design"),
        "Faculty of Architecture" to listOf("Interior Architecture", "Urban Design and Landscape Architecture", "Architecture"),
        "Faculty of Engineering" to listOf("Computer Engineering", "Biomedical Engineering", "Electrical and Electronics Engineering", "Industrial Engineering", "Genetics and Bioengineering", "Food Engineering", "Civil Engineering", "Chemical Engineering", "Mechanical Engineering", "Materials Science and Nanotechnology Engineering", "Software Engineering"),
        "Faculty of Health Sciences" to listOf("Nutrition and Dietetics", "Physiotherapy and Rehabilitation", "Nursing"),
        "Faculty of Medicine" to listOf("Medicine"),
        "Faculty of Commercial Sciences" to listOf("E-Commerce and Management", "Logistics Management", "Tourism Management", "International Finance", "International Trade and Business", "Management Information Systems")
    )

    val classLevels = listOf(
        "Preparatory", "1", "2", "3", "4", "Irregular", "Graduate"
    )
}