package com.example.data

object PredictionEngine {

    // Comprehensive Arabic vocabulary covering daily communication, greetings, common verbs, questions & adverbs
    private val commonArabicWords = listOf(
        "السلام", "عليكم", "ورحمة", "الله", "وبركاته",
        "صباح", "الخير", "النور", "الورد", "السعادة",
        "مساء", "كيف", "حالك", "صحتك", "أمورك", "أخبارك",
        "أنا", "بخير", "الحمد", "لله", "تمام", "شكراً", "شكرا",
        "جزيلاً", "جزيلا", "عفواً", "عفوا", "أهلاً", "وسهلاً",
        "مرحبا", "إن", "شاء", "ما", "تبارك", "جزاك", "خيراً",
        "بارك", "فيك", "لك", "يعطيك", "العافية", "الصحة",
        "تسلم", "يا", "غالي", "حبيبي", "صديقي", "أخي", "أختي",
        "في", "على", "من", "إلى", "عن", "مع", "هذا", "هذه",
        "هؤلاء", "ذلك", "تلك", "الذي", "التي", "الذين", "نعم", "لا",
        "أجل", "كلا", "أريد", "يمكن", "ممكن", "جميل", "رائع",
        "ممتاز", "حسناً", "حسنا", "طيب", "أكيد", "بالتأكيد", "طبعاً",
        "أراك", "قريباً", "لاحقاً", "غداً", "اليوم", "الآن", "وداعاً",
        "معك", "معي", "عندي", "عندك", "أين", "متى", "لماذا", "كم",
        "كل", "عام", "وأنتم", "بخير", "رمضان", "كريم", "عيد", "مبارك",
        "سعيد", "سنة", "جديدة", "مبروك", "ألف", "بالتوفيق", "إنشاء",
        "كيبورد", "برو", "تطبيق", "هاتف", "رسالة", "صوت", "فيديو",
        "صورة", "ملف", "رابط", "كلمة", "نص", "ترجمة", "نسخ", "لصق",
        "كتابة", "قراءة", "عمل", "شغل", "دوام", "جامعة", "مدرسة",
        "بيت", "منزل", "طريق", "سيارة", "سفر", "مطار", "فندق",
        "أحبك", "مشتاق", "وحشتني", "يسعد", "قلبك", "الله", "يحفظك",
        "يسلمك", "يوفقك", "يرعاك", "آمين", "يارب", "استغفر", "سبحان"
    )

    private val commonEnglishWords = listOf(
        "hello", "hi", "hey", "how", "are", "you", "good", "morning", "afternoon", "evening", "night",
        "thank", "thanks", "welcome", "please", "okay", "alright", "sure", "fine", "great", "nice",
        "awesome", "perfect", "friend", "happy", "love", "today", "tomorrow", "tonight", "work", "time",
        "where", "there", "here", "about", "could", "would", "should", "will", "can", "need", "want",
        "keyboard", "turbo", "pro", "message", "chat", "send", "receive", "call", "later", "soon",
        "brother", "sister", "family", "best", "wishes", "congratulations", "blessings", "peace"
    )

    // Contextual Next-Word Prediction Map for natural phrase flow
    private val arabicNextWordPredictions = mapOf(
        "السلام" to listOf("عليكم", "ورحمة", "لكم"),
        "عليكم" to listOf("ورحمة", "السلام", "جميعاً"),
        "ورحمة" to listOf("الله", "وبركاته", "تعالى"),
        "وبركاته" to listOf("تحياتي", "أهلاً", "وبعد"),
        "صباح" to listOf("الخير", "النور", "الورد"),
        "مساء" to listOf("الخير", "النور", "السعادة"),
        "الخير" to listOf("والبركة", "يا", "جميعاً"),
        "كيف" to listOf("حالك", "صحتك", "أمورك"),
        "حالك" to listOf("اليوم", "يا", "بخير"),
        "الحمد" to listOf("لله", "حمداً", "دائماً"),
        "شكراً" to listOf("جزيلاً", "لك", "أخي"),
        "شكرا" to listOf("جزيلا", "لك", "يا"),
        "إن" to listOf("شاء", "الله", "كنت"),
        "شاء" to listOf("الله", "تعالى", "الرحمن"),
        "ما" to listOf("شاء", "هو", "أخبارك"),
        "جزاك" to listOf("الله", "خيراً", "كل"),
        "بارك" to listOf("الله", "فيك", "لك"),
        "يعطيك" to listOf("العافية", "الصحة", "خير"),
        "أهلاً" to listOf("وسهلاً", "بك", "فيك"),
        "أهلا" to listOf("وسهلا", "بك", "فيك"),
        "كل" to listOf("عام", "يوم", "مرة"),
        "عام" to listOf("وأنتم", "بخير", "سعيد"),
        "وأنتم" to listOf("بخير", "بصحة", "وسعادة"),
        "ألف" to listOf("مبروك", "شكر", "عافية"),
        "الله" to listOf("يسعدك", "يحفظك", "يبارك"),
        "أنا" to listOf("بخير", "في", "أريد"),
        "من" to listOf("فضلك", "أجل", "هنا"),
        "على" to listOf("خير", "كل", "هذا"),
        "في" to listOf("البيت", "العمل", "الطريق", "أمان الله"),
        "مع" to listOf("السلامة", "أطيب", "تحياتي", "الأسف"),
        "أراك" to listOf("قريباً", "لاحقاً", "غداً", "على خير"),
        "يسلمو" to listOf("يا غالي", "تسلم", "حبيبي", "كلك ذوق"),
        "تسلم" to listOf("حبيبي", "يا غالي", "إيدك", "من كل شر"),
        "أنا" to listOf("بخير", "في الطريق", "أريد", "معك"),
        "من" to listOf("فضلك", "عيوني", "بعد إذنك", "أجل"),
        "على" to listOf("خير", "راسي", "بركة الله", "فكرة"),
        "الله" to listOf("يحفظك", "يسعدك", "يبارك فيك", "يعطيك العافية")
    )

    private val englishNextWordPredictions = mapOf(
        "how" to listOf("are", "is", "about"),
        "are" to listOf("you", "they", "we"),
        "you" to listOf("doing", "welcome", "good"),
        "good" to listOf("morning", "evening", "night"),
        "thank" to listOf("you", "so", "very"),
        "thanks" to listOf("a lot", "so much", "brother"),
        "nice" to listOf("to meet", "day", "one"),
        "see" to listOf("you", "it", "soon"),
        "have" to listOf("a good", "a great", "fun"),
        "where" to listOf("are", "is", "can"),
        "what" to listOf("are", "is", "do"),
        "i" to listOf("am", "will", "would", "think")
    )

    private val portugueseNextWordPredictions = mapOf(
        "bom" to listOf("dia", "trabalho", "fim de semana"),
        "boa" to listOf("tarde", "noite", "sorte", "viagem"),
        "como" to listOf("está", "vai", "você", "fazer"),
        "muito" to listOf("obrigado", "bem", "bom", "legal"),
        "obrigado" to listOf("amigo", "de coração", "pela ajuda"),
        "tudo" to listOf("bem", "certo", "ótimo", "tranquilo"),
        "por" to listOf("favor", "que", "isso", "aqui"),
        "estou" to listOf("bem", "aqui", "chegando", "pronto"),
        "você" to listOf("pode", "está", "vai", "quer"),
        "onde" to listOf("está", "fica", "você"),
        "um" to listOf("abraço", "beijo", "bom dia"),
        "olá" to listOf("tudo bem", "como vai", "amigo")
    )

    private val commonKurdishWords = listOf(
        "سڵاو", "چۆنی", "باشم", "سوپاس", "بەیانی", "باش", "ئێوارە", "شەو", "خوات", "لەگەڵ",
        "بەخێربێیت", "زۆر", "ئەمڕۆ", "سبەینێ", "ئێستا", "بەڵێ", "نەخێر", "تکایە", "ببورە", "هاوڕێ"
    )

    private val commonFrenchWords = listOf(
        "bonjour", "bonsoir", "salut", "merci", "beaucoup", "comment", "ça", "va", "bien", "très",
        "oui", "non", "s'il", "plaît", "pardon", "demain", "aujourd'hui", "ami", "travail", "maison"
    )

    private val commonSpanishWords = listOf(
        "hola", "buenos", "días", "buenas", "tardes", "noches", "gracias", "muchas", "cómo", "estás",
        "bien", "perfecto", "por", "favor", "sí", "claro", "amigo", "mañana", "hoy", "trabajo"
    )

    private val commonPortugueseWords = listOf(
        "olá", "oi", "bom", "dia", "boa", "tarde", "noite", "tudo", "bem", "obrigado",
        "muito", "por", "favor", "sim", "não", "amigo", "hoje", "amanhã", "trabalho", "casa"
    )

    private val commonTurkishWords = listOf(
        "merhaba", "selam", "nasılsın", "iyiyim", "teşekkürler", "günaydın", "iyi", "akşamlar", "geceler",
        "evet", "hayır", "lütfen", "tamam", "harika", "arkadaş", "bugün", "yarın", "şimdi", "güzel"
    )

    private val commonPersianWords = listOf(
        "سلام", "خوبی", "ممنون", "سپاس", "صبح", "بخیر", "عصر", "شب", "خوش", "آمدید",
        "بله", "خیر", "لطفاً", "ببخشید", "امروز", "فردا", "الان", "دوست", "عالی", "خیلی"
    )

    private val commonRussianWords = listOf(
        "привет", "здравствуйте", "спасибо", "хорошо", "как", "дела", "доброе", "утро", "вечер",
        "пожалуйста", "да", "нет", "сегодня", "завтра", "друг", "отлично", "конечно", "дом", "работа"
    )

    private val kurdishNextWordPredictions = mapOf(
        "سڵاو" to listOf("چۆنی", "باشیت", "هاوڕێ"),
        "بەیانی" to listOf("باش", "خۆش", "پیرۆز"),
        "زۆر" to listOf("سوپاس", "باشە", "جوانە"),
        "خوات" to listOf("لەگەڵ", "هاوکار بێت")
    )

    private val frenchNextWordPredictions = mapOf(
        "bonjour" to listOf("comment", "à tous", "mon ami"),
        "merci" to listOf("beaucoup", "bien", "infiniment"),
        "comment" to listOf("ça va", "allez-vous", "vas-tu"),
        "s'il" to listOf("vous plaît", "te plaît")
    )

    private val spanishNextWordPredictions = mapOf(
        "buenos" to listOf("días", "amigos"),
        "buenas" to listOf("tardes", "noches"),
        "muchas" to listOf("gracias", "felicidades"),
        "cómo" to listOf("estás", "te va", "está"),
        "por" to listOf("favor", "supuesto")
    )

    private val turkishNextWordPredictions = mapOf(
        "iyi" to listOf("akşamlar", "geceler", "günler", "çalışmalar"),
        "çok" to listOf("teşekkürler", "güzel", "iyi"),
        "teşekkür" to listOf("ederim", "ediyorum")
    )

    private val persianNextWordPredictions = mapOf(
        "صبح" to listOf("بخیر", "شما", "زیبا"),
        "خیلی" to listOf("ممنون", "خوب", "عالی"),
        "حال" to listOf("شما", "تو", "چطوره")
    )

    private val russianNextWordPredictions = mapOf(
        "доброе" to listOf("утро", "время"),
        "как" to listOf("дела", "вы", "ты"),
        "большое" to listOf("спасибо", "счастье")
    )

    private fun getDictionaryForLang(isArabic: Boolean, langCode: String): List<String> {
        val code = langCode.lowercase()
        return when {
            code.startsWith("ku") -> commonKurdishWords
            code.startsWith("fr") -> commonFrenchWords
            code.startsWith("es") -> commonSpanishWords
            code.startsWith("pt") -> commonPortugueseWords
            code.startsWith("tr") -> commonTurkishWords
            code.startsWith("fa") -> commonPersianWords
            code.startsWith("ru") -> commonRussianWords
            isArabic || code == "ar" -> commonArabicWords
            else -> commonEnglishWords
        }
    }

    private fun getNextWordMapForLang(isArabic: Boolean, langCode: String): Map<String, List<String>> {
        val code = langCode.lowercase()
        return when {
            code.startsWith("ku") -> kurdishNextWordPredictions
            code.startsWith("fr") -> frenchNextWordPredictions
            code.startsWith("es") -> spanishNextWordPredictions
            code.startsWith("pt") -> portugueseNextWordPredictions
            code.startsWith("tr") -> turkishNextWordPredictions
            code.startsWith("fa") -> persianNextWordPredictions
            code.startsWith("ru") -> russianNextWordPredictions
            isArabic || code == "ar" -> arabicNextWordPredictions
            else -> englishNextWordPredictions
        }
    }

    fun getDefaultSuggestionsForLang(isArabic: Boolean, langCode: String): List<PredictionResult> {
        val code = langCode.lowercase()
        return when {
            code.startsWith("ku") -> listOf(PredictionResult("سڵاو"), PredictionResult("سوپاس"), PredictionResult("باشم"))
            code.startsWith("fr") -> listOf(PredictionResult("Bonjour"), PredictionResult("Merci"), PredictionResult("Salut"))
            code.startsWith("es") -> listOf(PredictionResult("Hola"), PredictionResult("Gracias"), PredictionResult("Buenos"))
            code.startsWith("pt") -> listOf(PredictionResult("Olá"), PredictionResult("Tudo bem"), PredictionResult("Obrigado"))
            code.startsWith("tr") -> listOf(PredictionResult("Merhaba"), PredictionResult("Teşekkürler"), PredictionResult("İyi"))
            code.startsWith("fa") -> listOf(PredictionResult("سلام"), PredictionResult("ممنون"), PredictionResult("خوبی"))
            code.startsWith("ru") -> listOf(PredictionResult("Привет"), PredictionResult("Спасибо"), PredictionResult("Хорошо"))
            isArabic || code == "ar" -> listOf(PredictionResult("السلام"), PredictionResult("مرحبا"), PredictionResult("صباح"))
            else -> listOf(PredictionResult("Hello"), PredictionResult("How"), PredictionResult("Thanks"))
        }
    }

    private fun getFillersForLang(isArabic: Boolean, langCode: String): List<String> {
        val code = langCode.lowercase()
        return when {
            code.startsWith("ku") -> listOf("سوپاس", "باشە", "بەڵێ", "زۆر باشە")
            code.startsWith("fr") -> listOf("Merci", "D'accord", "Oui", "Super")
            code.startsWith("es") -> listOf("Gracias", "Claro", "Sí", "Genial")
            code.startsWith("pt") -> listOf("Obrigado", "Tudo bem", "Sim", "Ótimo")
            code.startsWith("tr") -> listOf("Teşekkürler", "Tamam", "Evet", "Harika")
            code.startsWith("fa") -> listOf("ممنون", "باشه", "بله", "عالی")
            code.startsWith("ru") -> listOf("Спасибо", "Хорошо", "Да", "Отлично")
            isArabic || code == "ar" -> listOf("شكراً", "تمام", "نعم", "ممتاز", "أكيد")
            else -> listOf("Okay", "Sure", "Great", "Yes", "Thanks")
        }
    }

    data class PredictionResult(
        val word: String,
        val isCorrection: Boolean = false,
        val isCustomCandidate: Boolean = false
    )

    fun levenshteinDistance(s1: String, s2: String): Int {
        val a = s1.lowercase()
        val b = s2.lowercase()
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length

        val costs = IntArray(b.length + 1) { it }
        for (i in 1..a.length) {
            costs[0] = i
            var nw = i - 1
            for (j in 1..b.length) {
                val cj = if (a[i - 1] == b[j - 1]) nw else 1 + minOf(costs[j], costs[j - 1], nw)
                nw = costs[j]
                costs[j] = cj
            }
        }
        return costs[b.length]
    }

    fun getPredictionsWithCorrection(
        prefix: String,
        isArabic: Boolean,
        userWords: List<String> = emptyList(),
        prevWord: String? = null,
        userDict: UserDictionaryManager? = null,
        langCode: String = if (isArabic) "ar" else "en"
    ): List<PredictionResult> {
        val clean = prefix.trim()
        val dictionary = getDictionaryForLang(isArabic, langCode)
        val nextWordMap = getNextWordMapForLang(isArabic, langCode)
        val cleanPrev = prevWord?.trim()?.replace(Regex("[،؛؟?!.,:;]+$"), "")

        // 1. If prefix is empty: show context-aware next-word predictions (n-gram / user history)
        if (clean.isEmpty()) {
            if (!cleanPrev.isNullOrBlank()) {
                val learnedNext = userDict?.getLearnedNextWords(cleanPrev) ?: emptyList()
                val staticNext = nextWordMap[cleanPrev.lowercase()] ?: nextWordMap[cleanPrev] ?: emptyList()
                val candidates = (learnedNext + staticNext).distinct()
                if (candidates.isNotEmpty()) {
                    return candidates.take(3).map { PredictionResult(word = it) }
                }
            }
            return getDefaultSuggestionsForLang(isArabic, langCode)
        }

        val results = mutableListOf<PredictionResult>()

        // 2. Direct SpellCheckDictionary correction (Arabic & English)
        val correction = SpellCheckDictionary.getCorrection(clean, isArabic || langCode == "ar")
        if (correction != null && !correction.equals(clean, ignoreCase = true)) {
            results.add(PredictionResult(word = correction, isCorrection = true))
        }

        // 3. User learned words sorted by frequency + dictionary
        val learnedFreqWords = userDict?.getWordsByFrequency() ?: userWords
        val combined = (learnedFreqWords + dictionary).distinct()

        // 4. Context-aware n-gram priority: words that follow prevWord AND start with prefix
        if (!cleanPrev.isNullOrBlank()) {
            val learnedNext = userDict?.getLearnedNextWords(cleanPrev) ?: emptyList()
            val staticNext = nextWordMap[cleanPrev.lowercase()] ?: nextWordMap[cleanPrev] ?: emptyList()
            val contextMatches = (learnedNext + staticNext).filter {
                it.startsWith(clean, ignoreCase = true)
            }
            for (cm in contextMatches) {
                if (results.size < 3 && results.none { it.word.equals(cm, ignoreCase = true) }) {
                    results.add(PredictionResult(word = cm, isCorrection = false))
                }
            }
        }

        // 5. General Prefix matches (exact start)
        val prefixMatches = combined.filter {
            it.startsWith(clean, ignoreCase = true)
        }.sortedWith(compareBy({ it.length }, { it }))

        for (m in prefixMatches) {
            if (results.size < 3 && results.none { it.word.equals(m, ignoreCase = true) }) {
                results.add(PredictionResult(word = m, isCorrection = false))
            }
        }

        // 6. Approximate Spell Check using fast bounded search (Levenshtein distance <= 2)
        if (results.size < 3 && clean.length in 3..12) {
            val candidateSlice = combined.take(80)
            for (candidate in candidateSlice) {
                if (results.size >= 3) break
                if (kotlin.math.abs(candidate.length - clean.length) <= 1 &&
                    levenshteinDistance(candidate, clean) in 1..2 &&
                    results.none { it.word.equals(candidate, ignoreCase = true) }
                ) {
                    results.add(PredictionResult(word = candidate, isCorrection = true))
                }
            }
        }

        // 7. If user typed a custom new word, offer to save
        if (clean.length >= 3 && !combined.any { it.equals(clean, ignoreCase = true) } && correction == null && results.size < 3) {
            results.add(PredictionResult(word = clean, isCustomCandidate = true))
        }

        // 8. Natural fillers if still under 3
        val fallbackWords = getFillersForLang(isArabic, langCode)
        for (fb in fallbackWords) {
            if (results.size >= 3) break
            if (results.none { it.word.equals(fb, ignoreCase = true) }) {
                results.add(PredictionResult(word = fb))
            }
        }

        return results.take(3)
    }

    fun getNextWordPredictions(
        prevWord: String,
        isArabic: Boolean,
        userDict: UserDictionaryManager? = null,
        langCode: String = if (isArabic) "ar" else "en"
    ): List<PredictionResult> {
        val clean = prevWord.trim().replace(Regex("[،؛؟?!.,:;]+$"), "")
        if (clean.isEmpty()) {
            return getPredictionsWithCorrection("", isArabic, userDict = userDict, langCode = langCode)
        }
        val learned = userDict?.getLearnedNextWords(clean) ?: emptyList()
        val nextWordMap = getNextWordMapForLang(isArabic, langCode)
        val staticNext = nextWordMap[clean.lowercase()] ?: nextWordMap[clean] ?: emptyList()
        val predictions = (learned + staticNext).distinct()
        if (predictions.isNotEmpty()) {
            return predictions.take(3).map { PredictionResult(word = it) }
        }
        return getFillersForLang(isArabic, langCode).take(3).map { PredictionResult(it) }
    }

    fun getPredictions(prefix: String, isArabic: Boolean, userWords: List<String> = emptyList(), langCode: String = if (isArabic) "ar" else "en"): List<String> {
        return getPredictionsWithCorrection(prefix, isArabic, userWords, langCode = langCode).map { it.word }
    }
}
