package com.example.data

data class AiTone(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val icon: String,
    val description: String,
    val exampleSuffix: String
)

object AiToneEngine {

    val allTones = listOf(
        AiTone(
            id = "formal",
            nameAr = "رسمي واحترافي",
            nameEn = "Formal",
            icon = "👔",
            description = "صياغة مهذبة ومحترمة تناسب العمل والجهات الرسمية",
            exampleSuffix = "شاكرين ومقدرين حسن تعاونكم الكريم."
        ),
        AiTone(
            id = "friendly",
            nameAr = "ودي ولطيف",
            nameEn = "Friendly",
            icon = "😊",
            description = "أسلوب دافئ ومرحب يعبر عن الود والمحبة والأخوة",
            exampleSuffix = "يسعد قلبك ودمت بخير وسعادة دائماً 🌸"
        ),
        AiTone(
            id = "concise",
            nameAr = "مختصر ومباشر",
            nameEn = "Concise",
            icon = "⚡",
            description = "إيجاز ذكي يحذف الحشو ويوصل الرسالة بوضوح وسرعة",
            exampleSuffix = ""
        ),
        AiTone(
            id = "poetic",
            nameAr = "شاعري ومزخرف",
            nameEn = "Poetic",
            icon = "✨",
            description = "صياغة أدبية رقيقة بكلمات عربية منتقاة ولمسات جمالية",
            exampleSuffix = "طابت أوقاتكم بكل خير وجمال 🕊️"
        ),
        AiTone(
            id = "enthusiastic",
            nameAr = "مرح ومتحمس",
            nameEn = "Enthusiastic",
            icon = "🥳",
            description = "أسلوب تفاعلي مبهج مفعم بالحماس والطاقة الإيجابية",
            exampleSuffix = "حماس لا يوصف! استمر يا مبدع 🔥🎉"
        ),
        AiTone(
            id = "eloquent",
            nameAr = "فصيح وبليغ",
            nameEn = "Eloquent",
            icon = "📖",
            description = "لغة عربية فصحى عريقة وبلاغة أدبية فائقة الجودة",
            exampleSuffix = "ودمتم في حفظ الله ورعايته سالمين."
        ),
        AiTone(
            id = "apology",
            nameAr = "اعتذار مهذب",
            nameEn = "Apology",
            icon = "🙏",
            description = "صيغة اعتذار راقية وأنيقة تراعي المشاعر وتقدر الطرف الآخر",
            exampleSuffix = "أرجو تقبل فائق اعتذاري وتقديري العميق."
        ),
        AiTone(
            id = "persuasive",
            nameAr = "مقنع ومؤثر",
            nameEn = "Persuasive",
            icon = "🎯",
            description = "عرض الفكرة بأسلوب منطقي وجذاب يحفز على الموافقة السريعة",
            exampleSuffix = "نثق بأن هذا الاختيار هو الأنسب والأكثر فائدة."
        )
    )

    fun transformTone(input: String, toneId: String): String {
        val trimmed = input.trim()
        if (trimmed.isBlank()) {
            return when (toneId) {
                "formal" -> "السلام عليكم ورحمة الله، أود التواصل معكم بخصوص هذا الأمر، شاكرين ومقدرين حسن تعاونكم."
                "friendly" -> "أهلاً يا غالي! أتمنى تكون بأفضل حال وصحة، يسعد قلبك 🌸"
                "concise" -> "مرحباً، يرجى التكرم بالاطلاع والتأكيد."
                "poetic" -> "طابت أوقاتكم بكل ما هو جميل وأنيق 🕊️✨"
                "enthusiastic" -> "أهلاً بالبطل! يوم رائع ومليء بالإنجازات والنشاط 🔥🎉"
                "eloquent" -> "تحية طيبة مباركة تزجي إليكم أسمى آيات التقدير والثناء."
                "apology" -> "أعتذر بصدق عن أي تقصير أو إزعاج، وأرجو تقبل كامل أسفي وتقديري."
                "persuasive" -> "يسرني أن أعرض عليكم هذه الفكرة الرائعة التي تحقق أقصى درجات النجاح."
                else -> trimmed
            }
        }

        return when (toneId) {
            "formal" -> {
                var res = trimmed
                res = res.replace(Regex("^(مرحبا|هلا|هاي|سلام)"), "السلام عليكم ورحمة الله وبركاته، تحية طيبة وبعد:")
                res = res.replace(Regex("ابغي|ابي|اريد"), "أرجو التكرم بالموافقة على")
                res = res.replace(Regex("شكرا|مشكور"), "نشكر لكم حسن تعاونكم واهتمامكم")
                res = res.replace(Regex("معليش|اسف"), "نلتمس منكم العذر والسموحة")
                if (!res.endsWith(".")) res += "."
                res + " شاكرين لكم ومقدرين جهودكم."
            }
            "friendly" -> {
                var res = trimmed
                res = res.replace(Regex("^(مرحبا|سلام)"), "أهلاً وسهلاً يا غالي 👋")
                res = res.replace("شكرا", "تسلم وتعيش يا طيب، يسعد قلبك 🌸")
                if (!res.contains("🌸") && !res.contains("❤️") && !res.contains("😊")) {
                    res += " 😊🤍"
                }
                res
            }
            "concise" -> {
                // Remove filler words
                var res = trimmed
                val fillers = listOf("يعني", "بصراحة", "نوعاً ما", "في الحقيقة", "أود أن أقول", "كما تعلم", "بالمناسبة")
                for (f in fillers) {
                    res = res.replace(f, "")
                }
                res = res.replace(Regex("\\s+"), " ").trim()
                if (res.length > 80) {
                    val words = res.split(" ")
                    if (words.size > 8) {
                        words.take(8).joinToString(" ") + "..."
                    } else res
                } else res
            }
            "poetic" -> {
                "🕊️ " + trimmed.split(" ").joinToString(" ") { word ->
                    if (word.length > 3 && (word.endsWith("ه") || word.endsWith("ة"))) "$word 🌸" else word
                } + " ✨"
            }
            "enthusiastic" -> {
                var res = trimmed
                res = res.replace("!", "!! 🔥")
                if (!res.contains("🔥")) res += " 🔥💪"
                if (!res.contains("🎉")) res += " 🎉"
                res
            }
            "eloquent" -> {
                var res = trimmed
                res = res.replace("كيفك", "كيف أصبحت وما هي أخبارك؟")
                res = res.replace("تمام", "على خير ما يُرام والحمد لله")
                res = res.replace("ابي", "يروق لي ويطيب خاطري أن")
                res = res.replace("حلو", "بديع ورائق")
                res = res.replace("زين", "حسن ومتقن")
                "« " + res + " »"
            }
            "apology" -> {
                "أعتذر بصدق عن أي التباس أو تقصير، وأود توضيح التالي: " + trimmed + ". تقبلوا فائق تقديري واعتذاري."
            }
            "persuasive" -> {
                "من واقع التجربة والحرص على أفضل النتائج، أؤكد لكم: " + trimmed + ". وبذلك نضمن التفوق والنجاح المؤكد بإذن الله."
            }
            else -> trimmed
        }
    }

    // Rephrasing generator (3 alternatives)
    fun generateRephrasings(input: String): List<String> {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return listOf("يرجى كتابة نص لإعادة صياغته")

        return listOf(
            "بأسلوب احترافي: " + transformTone(trimmed, "formal"),
            "بأسلوب مبسط وودي: " + transformTone(trimmed, "friendly"),
            "بأسلوب موجز ومباشر: " + transformTone(trimmed, "concise")
        )
    }

    // Quick Diacritics (Tashkeel) generator
    fun addAutomaticTashkeel(arabicText: String): String {
        val words = arabicText.trim().split(" ")
        val diacriticsMap = mapOf(
            "الله" to "اللَّهِ",
            "بسم" to "بِسْمِ",
            "الرحمن" to "الرَّحْمَنِ",
            "الرحيم" to "الرَّحِيمِ",
            "الحمد" to "الْحَمْدُ",
            "شكرا" to "شُكْراً",
            "مرحبا" to "مَرْحَباً",
            "السلام" to "السَّلَامُ",
            "عليكم" to "عَلَيْكُمْ",
            "صباح" to "صَبَاحُ",
            "مساء" to "مَسَاءُ",
            "الخير" to "الْخَيْرِ",
            "كيف" to "كَيْفَ",
            "حالك" to "حَالُكَ",
            "نعم" to "نَعَمْ",
            "كيبورد" to "كِيبُورْد",
            "تطبيق" to "تَطْبِيق",
            "جميل" to "جَمِيلٌ",
            "رائع" to "رَائِعٌ"
        )
        return words.joinToString(" ") { word ->
            diacriticsMap[word] ?: word
        }
    }
}
