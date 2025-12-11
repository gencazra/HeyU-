package com.azrag.heyu.data

object YeditepeData {

    val faculties = listOf(
        "Diş Hekimliği Fakültesi",
        "Eczacılık Fakültesi",
        "Eğitim Fakültesi",
        "Fen-Edebiyat Fakültesi",
        "Güzel Sanatlar Fakültesi",
        "Hukuk Fakültesi",
        "İktisadi ve İdari Bilimler Fakültesi",
        "İletişim Fakültesi",
        "Mimarlık Fakültesi",
        "Mühendislik Fakültesi",
        "Sağlık Bilimleri Fakültesi",
        "Tıp Fakültesi",
        "Ticari Bilimler Fakültesi"
    )

    val majorsByFaculty = mapOf(
        "Diş Hekimliği Fakültesi" to listOf("Diş Hekimliği"),
        "Eczacılık Fakültesi" to listOf("Eczacılık"),
        "Eğitim Fakültesi" to listOf("İngilizce Öğretmenliği", "Okul Öncesi Öğretmenliği", "Özel Eğitim Öğretmenliği", "Rehberlik ve Psikolojik Danışmanlık"),
        "Fen-Edebiyat Fakültesi" to listOf("Antropoloji", "Felsefe", "Fizik", "İngiliz Dili ve Edebiyatı", "Matematik", "Psikoloji", "Sosyoloji", "Tarih", "Türk Dili ve Edebiyatı", "Çeviribilim"),
        "Güzel Sanatlar Fakültesi" to listOf("Endüstriyel Tasarım", "Grafik Tasarımı", "Moda ve Tekstil Tasarımı", "Plastik Sanatlar ve Resim", "Sanat ve Kültür Yönetimi", "Tiyatro"),
        "Hukuk Fakültesi" to listOf("Hukuk"),
        "İktisadi ve İdari Bilimler Fakültesi" to listOf("Ekonomi", "İşletme", "Kamu Yönetimi", "Siyaset Bilimi ve Uluslararası İlişkiler", "Uluslararası İşletme Yönetimi (Almanca)"),
        "İletişim Fakültesi" to listOf("Gazetecilik", "Halkla İlişkiler ve Tanıtım", "Radyo, Televizyon ve Sinema", "Reklam Tasarımı ve İletişimi", "Görsel İletişim Tasarımı"),
        "Mimarlık Fakültesi" to listOf("İç Mimarlık", "Kentsel Tasarım ve Peyzaj Mimarlığı", "Mimarlık"),
        "Mühendislik Fakültesi" to listOf("Bilgisayar Mühendisliği", "Biyomedikal Mühendisliği", "Elektrik-Elektronik Mühendisliği", "Endüstri Mühendisliği", "Genetik ve Biyomühendislik", "Gıda Mühendisliği", "İnşaat Mühendisliği", "Kimya Mühendisliği", "Makine Mühendisliği", "Malzeme Bilimi ve Nanoteknoloji Mühendisliği", "Yazılım Mühendisliği"),
        "Sağlık Bilimleri Fakültesi" to listOf("Beslenme ve Diyetetik", "Fizyoterapi ve Rehabilitasyon", "Hemşirelik"),
        "Tıp Fakültesi" to listOf("Tıp"),
        "Ticari Bilimler Fakültesi" to listOf("Elektronik Ticaret ve Yönetimi", "Lojistik Yönetimi", "Turizm İşletmeciliği", "Uluslararası Finans", "Uluslararası Ticaret ve İşletmecilik", "Yönetim Bilişim Sistemleri")
    )

    val classLevels = listOf(
        "Hazırlık", "1", "2", "3", "4", "Irregular", "Graduate"
    )
}
