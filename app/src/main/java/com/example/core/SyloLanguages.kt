package com.example.core

data class SyloLanguage(
    val code: String,
    val englishName: String,
    val nativeName: String,
    val regionTag: String
)

object SyloLanguages {
    val all: List<SyloLanguage> = listOf(
        SyloLanguage("en", "English", "English", "Global / South Asia"),
        SyloLanguage("hi", "Hindi", "हिन्दी", "India"),
        SyloLanguage("mr", "Marathi", "मराठी", "Maharashtra"),
        SyloLanguage("bn", "Bengali", "বাংলা", "West Bengal & Bangladesh"),
        SyloLanguage("ta", "Tamil", "தமிழ்", "Tamil Nadu & Sri Lanka"),
        SyloLanguage("te", "Telugu", "తెలుగు", "Andhra & Telangana"),
        SyloLanguage("kn", "Kannada", "ಕನ್ನಡ", "Karnataka"),
        SyloLanguage("ml", "Malayalam", "മലയാളം", "Kerala"),
        SyloLanguage("gu", "Gujarati", "ગુજરાતી", "Gujarat"),
        SyloLanguage("pa", "Punjabi", "ਪੰਜਾਬੀ", "Punjab"),
        SyloLanguage("ur", "Urdu", "اردو", "Pakistan & India"),
        SyloLanguage("or", "Odia", "ଓଡ଼ିଆ", "Odisha"),
        SyloLanguage("as", "Assamese", "অসমীয়া", "Assam"),
        SyloLanguage("ne", "Nepali", "नेपाली", "Nepal & Sikkim"),
        SyloLanguage("si", "Sinhala", "සිංහල", "Sri Lanka")
    )

    fun byCode(code: String): SyloLanguage =
        all.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: all.first()
}

data class SyloLocalizedLabels(
    val navHome: String,
    val navExplore: String,
    val navCreate: String,
    val navVideos: String,
    val navProfile: String,
    val tagline: String,
    val liveNow: String,
    val goLive: String,
    val recommended: String,
    val following: String,
    val trending: String,
    val translate: String,
    val original: String,
    val news: String,
    val memes: String,
    val celebs: String,
    val messages: String,
    val alerts: String
)

object SyloStrings {
    private val english = SyloLocalizedLabels(
        navHome = "Home",
        navExplore = "Explore",
        navCreate = "Create",
        navVideos = "Videos",
        navProfile = "Profile",
        tagline = "Your Language. Your World.",
        liveNow = "LIVE",
        goLive = "Go Live",
        recommended = "For You",
        following = "Following",
        trending = "Trending",
        translate = "Translate",
        original = "Original",
        news = "News",
        memes = "Memes",
        celebs = "Celebs",
        messages = "Chat",
        alerts = "Alerts"
    )

    fun forLang(code: String): SyloLocalizedLabels = when (code) {
        "hi" -> SyloLocalizedLabels(
            navHome = "होम", navExplore = "एक्सप्लोर", navCreate = "बनाएं", navVideos = "वीडियो",
            navProfile = "प्रोफ़ाइल", tagline = "आपकी भाषा. आपकी दुनिया.", liveNow = "लाइव",
            goLive = "लाइव जाएं", recommended = "आपके लिए", following = "फ़ॉलोइंग", trending = "ट्रेंडिंग",
            translate = "अनुवाद करें", original = "मूल भाषा", news = "समाचार", memes = "मीम्स",
            celebs = "सिनेमा", messages = "चैट", alerts = "अलर्ट"
        )
        "mr" -> SyloLocalizedLabels(
            navHome = "होम", navExplore = "शोध", navCreate = "तयार करा", navVideos = "व्हिडिओ",
            navProfile = "प्रोफाइल", tagline = "तुमची भाषा. तुमचं जग.", liveNow = "लाईव्ह",
            goLive = "लाईव्ह या", recommended = "तुमच्यासाठी", following = "फॉलोइंग", trending = "ट्रेंडिंग",
            translate = "भाषांतर", original = "मूळ मजकूर", news = "बातम्या", memes = "मीम्स",
            celebs = "सिनेमा", messages = "संदेश", alerts = "सूचना"
        )
        "bn" -> SyloLocalizedLabels(
            navHome = "হোম", navExplore = "অন্বেষণ", navCreate = "তৈরি করুন", navVideos = "ভিডিও",
            navProfile = "প্রোফাইল", tagline = "আপনার ভাষা. আপনার বিশ্ব.", liveNow = "লাইভ",
            goLive = "লাইভ শুরু", recommended = "আপনার জন্য", following = "ফলোয়িং", trending = "ট্রেন্ডিং",
            translate = "অনুবাদ", original = "মূল ভাষা", news = "সংবাদ", memes = "মিমস",
            celebs = "তারকা", messages = "চ্যাট", alerts = "অ্যালার্ট"
        )
        "ta" -> SyloLocalizedLabels(
            navHome = "முகப்பு", navExplore = "ஆராய்", navCreate = "உருவாக்கு", navVideos = "வீடியோ",
            navProfile = "சுயவிவரம்", tagline = "உங்கள் மொழி. உங்கள் உலகம்.", liveNow = "நேரலை",
            goLive = "நேரலை செல்", recommended = "உங்களுக்காக", following = "பின்தொடர்பவை", trending = "டிரெண்டிங்",
            translate = "மொழிபெயர்", original = "மூலம்", news = "செய்திகள்", memes = "மீம்ஸ்",
            celebs = "சினிமா", messages = "அரட்டை", alerts = "அறிவிப்பு"
        )
        "te" -> SyloLocalizedLabels(
            navHome = "హోమ్", navExplore = "అన్వేషించు", navCreate = "సృష్టించు", navVideos = "వీడియోలు",
            navProfile = "ప్రొఫైల్", tagline = "మీ భాష. మీ ప్రపంచం.", liveNow = "లైవ్",
            goLive = "లైవ్ వెళ్ళండి", recommended = "మీ కోసం", following = "ఫాలోయింగ్", trending = "ట్రెండింగ్",
            translate = "అనువాదం", original = "అసలు", news = "వార్తలు", memes = "మీమ్స్",
            celebs = "సినిమా", messages = "చాట్", alerts = "అలర్ట్స్"
        )
        "gu" -> SyloLocalizedLabels(
            navHome = "હોમ", navExplore = "શોધો", navCreate = "બનાવો", navVideos = "વીડિયો",
            navProfile = "પ્રોફાઇલ", tagline = "તમારી ભાષા. તમારી દુનિયા.", liveNow = "લાઇવ",
            goLive = "લાઇવ જાઓ", recommended = "તમારા માટે", following = "ફોલોઇંગ", trending = "ટ્રેન્ડિંગ",
            translate = "અનુવાદ", original = "મૂળ", news = "સમાચાર", memes = "મીમ્સ",
            celebs = "સિનેમા", messages = "ચેટ", alerts = "અલર્ટ"
        )
        "pa" -> SyloLocalizedLabels(
            navHome = "ਹੋਮ", navExplore = "ਖੋਜੋ", navCreate = "ਬਣਾਓ", navVideos = "ਵੀਡੀਓ",
            navProfile = "ਪ੍ਰੋਫਾਈਲ", tagline = "ਤੁਹਾਡੀ ਭਾਸ਼ਾ. ਤੁਹਾਡੀ ਦੁਨੀਆ.", liveNow = "ਲਾਈਵ",
            goLive = "ਲਾਈਵ ਜਾਓ", recommended = "ਤੁਹਾਡੇ ਲਈ", following = "ਫਾਲੋਇੰਗ", trending = "ਟ੍ਰੈਂਡਿੰਗ",
            translate = "ਅਨੁਵਾਦ", original = "ਮੂਲ", news = "ਖ਼ਬਰਾਂ", memes = "ਮੀਮਜ਼",
            celebs = "ਸਿਨੇਮਾ", messages = "ਚੈਟ", alerts = "ਅਲਰਟ"
        )
        "ur" -> SyloLocalizedLabels(
            navHome = "ہوم", navExplore = "دریافت", navCreate = "بنائیں", navVideos = "ویڈیوز",
            navProfile = "پروفائل", tagline = "آپ کی زبان۔ آپ کی دنیا۔", liveNow = "لائیو",
            goLive = "لائیو جائیں", recommended = "آپ کے لیے", following = "فالوونگ", trending = "ٹرینڈنگ",
            translate = "ترجمہ", original = "اصل", news = "خبریں", memes = "میمز",
            celebs = "ستارے", messages = "پیغامات", alerts = "اطلاعات"
        )
        else -> english
    }
}

object SyloCatalog {
    val interests = listOf(
        "Entertainment", "Bollywood", "Marathi Cinema", "South Cinema",
        "News", "Sports", "Comedy", "Memes", "Technology", "Lifestyle", "Music", "Travel"
    )
    val newsCategories = listOf(
        "All", "Breaking", "Bollywood", "Marathi Cinema", "South Cinema", "TV & Celeb", "Sports", "Tech", "National", "Regional"
    )
    val memeCategories = listOf("All", "Chai & Monsoon", "Cricket", "Desi Office", "Cinema", "Tech", "Regional")
    val videoFilters = listOf("Original", "Saffron Glow", "Violet Night", "Cinema 35mm", "Monsoon Blue")
    val videoSpeeds = listOf("0.5x", "1x", "1.5x", "2x")
    val videoDurations = listOf(15L, 30L, 60L, 180L)
    val licensedMusicTracks = listOf(
        "Mumbai Monsoon Lo-Fi (Licensed)",
        "Dhol & Synth Groove (Licensed)",
        "Sitar Chillwave Beat (Licensed)",
        "Chennai Sunset Funk (Licensed)",
        "Kathmandu Acoustic Breeze (Licensed)",
        "Original Creator Audio"
    )
    val liveGifts = listOf(
        Triple("Cutting Chai", 10L, "☕"),
        Triple("Marigold Garland", 50L, "🌼"),
        Triple("Dhol Beat", 150L, "🥁"),
        Triple("Royal Crown", 500L, "👑")
    )
}
