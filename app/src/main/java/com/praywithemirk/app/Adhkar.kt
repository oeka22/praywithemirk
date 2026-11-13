package com.praywithemirk.app

/** Ein Dhikr / Bittgebet mit Zielanzahl für den Zähler. */
class Dhikr(
    val arabic: String,
    val translit: String,
    val meaning: T,
    val count: Int,
    val source: String,
    val note: T? = null,
)

/**
 * Sunnah-Adhkar rund um das Gebet.
 * Texte nach den bekannten Sammlungen (Bukhari, Muslim, Abu Dawud, Tirmidhi, Nasa'i, Ibn Majah);
 * die Übersetzungen sind sinngemäß.
 */
object Adhkar {

    /** Beim und direkt nach dem Gebetsruf (für alle Gebete gleich). */
    val atAdhan = listOf(
        Dhikr(
            arabic = "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
            translit = "La hawla wa la quwwata illa billah",
            meaning = T(
                "Die Worte des Muezzins leise mitsprechen. Bei „Hayya ʿala s-salah“ und „Hayya ʿala l-falah“ sagt man stattdessen: Es gibt keine Macht und keine Kraft außer bei Allah.",
                "Müezzinin sözlerini sessizce tekrarlayın. „Hayye ale's-salâ“ ve „Hayye ale'l-felâh“ denildiğinde ise: Güç ve kuvvet ancak Allah'tandır, deyin.",
                "Quietly repeat the words of the muezzin. At “Hayya ʿala s-salah” and “Hayya ʿala l-falah” say instead: There is no power and no strength except with Allah."
            ),
            count = 1,
            source = "Muslim 385",
        ),
        Dhikr(
            arabic = "أَشْهَدُ أَنْ لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، وَأَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ، رَضِيتُ بِاللَّهِ رَبًّا، وَبِمُحَمَّدٍ رَسُولًا، وَبِالْإِسْلَامِ دِينًا",
            translit = "Ashhadu an la ilaha illallahu wahdahu la sharika lah, wa anna Muhammadan ʿabduhu wa rasuluh. Raditu billahi rabban, wa bi-Muhammadin rasulan, wa bil-islami dina",
            meaning = T(
                "Ich bezeuge, dass es keinen Gott gibt außer Allah, dem Einen ohne Teilhaber, und dass Muhammad Sein Diener und Gesandter ist. Ich bin zufrieden mit Allah als Herrn, mit Muhammad als Gesandtem und mit dem Islam als Religion.",
                "Şehadet ederim ki Allah'tan başka ilah yoktur, O tektir, ortağı yoktur; ve Muhammed O'nun kulu ve elçisidir. Rab olarak Allah'tan, elçi olarak Muhammed'den ve din olarak İslam'dan razıyım.",
                "I bear witness that there is no god but Allah alone, without partner, and that Muhammad is His servant and messenger. I am pleased with Allah as Lord, with Muhammad as messenger and with Islam as religion."
            ),
            count = 1,
            source = "Muslim 386",
        ),
        Dhikr(
            arabic = "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ",
            translit = "Allahumma salli ʿala Muhammadin wa ʿala ali Muhammad",
            meaning = T(
                "O Allah, segne Muhammad und die Familie Muhammads. (Nach dem Adhan Segenswünsche auf den Propheten ﷺ sprechen.)",
                "Allah'ım, Muhammed'e ve Muhammed'in ailesine salât eyle. (Ezandan sonra Peygamber ﷺ'e salavat getirilir.)",
                "O Allah, send blessings upon Muhammad and the family of Muhammad. (Send blessings on the Prophet ﷺ after the adhan.)"
            ),
            count = 1,
            source = "Muslim 384",
        ),
        Dhikr(
            arabic = "اللَّهُمَّ رَبَّ هَذِهِ الدَّعْوَةِ التَّامَّةِ، وَالصَّلَاةِ الْقَائِمَةِ، آتِ مُحَمَّدًا الْوَسِيلَةَ وَالْفَضِيلَةَ، وَابْعَثْهُ مَقَامًا مَحْمُودًا الَّذِي وَعَدْتَهُ",
            translit = "Allahumma rabba hadhihid-daʿwatit-tammah, was-salatil-qa'imah, ati Muhammadanil-wasilata wal-fadilah, wabʿath-hu maqaman mahmudanil-ladhi waʿadtah",
            meaning = T(
                "O Allah, Herr dieses vollkommenen Rufes und des bevorstehenden Gebets: Gib Muhammad die Wasila und den Vorzug und erwecke ihn an der gepriesenen Stellung, die Du ihm versprochen hast.",
                "Ey bu eksiksiz davetin ve kılınacak namazın Rabbi olan Allah'ım! Muhammed'e vesileyi ve fazileti ver, onu vaat ettiğin Makam-ı Mahmud'a ulaştır.",
                "O Allah, Lord of this perfect call and the prayer about to be established, grant Muhammad al-Wasilah and excellence, and raise him to the praised station You have promised him."
            ),
            count = 1,
            source = "Bukhari 614",
        ),
        Dhikr(
            arabic = "الدُّعَاءُ لَا يُرَدُّ بَيْنَ الْأَذَانِ وَالْإِقَامَةِ",
            translit = "Ad-duʿa'u la yuraddu baynal-adhani wal-iqamah",
            meaning = T(
                "Das Bittgebet zwischen Adhan und Iqama wird nicht zurückgewiesen – nutze diese Zeit für deine persönlichen Duʿas.",
                "Ezan ile kamet arasında yapılan dua geri çevrilmez – bu vakti kendi dualarınız için değerlendirin.",
                "A supplication between the adhan and the iqamah is not rejected – use this time for your personal du'as."
            ),
            count = 1,
            source = "Abu Dawud 521, Tirmidhi 212",
        ),
    )

    /** Hinweise zu den Sunnah-Gebeten je Gebet (Index wie in [P]). */
    fun sunnahFor(prayer: Int): T = when (prayer) {
        P.FAJR -> T(
            "2 Rakʿa Sunnah vor dem Pflichtgebet (muʾakkada). Der Prophet ﷺ sagte: „Die zwei Rakʿa vor Fajr sind besser als die Welt und alles, was darin ist.“ (Muslim 725)",
            "Farzdan önce 2 rekât sünnet (müekked). Peygamber ﷺ: „Sabah namazının iki rekât sünneti dünyadan ve içindekilerden daha hayırlıdır.“ (Müslim 725)",
            "2 rak'ahs sunnah before the obligatory prayer (mu'akkadah). The Prophet ﷺ said: “The two rak'ahs before Fajr are better than the world and all it contains.” (Muslim 725)"
        )
        P.DHUHR -> T(
            "4 Rakʿa Sunnah vor und 2 Rakʿa nach dem Pflichtgebet (muʾakkada). Freitags: das Freitagsgebet (Dschumuʿa) mit seiner Sunnah. Wer täglich 12 Rakʿa freiwillig betet, dem wird ein Haus im Paradies gebaut. (Muslim 728, Tirmidhi 415)",
            "Farzdan önce 4, sonra 2 rekât sünnet (müekked). Cuma günü: Cuma namazı ve sünnetleri. Her gün 12 rekât sünnet kılana cennette bir ev yapılır. (Müslim 728, Tirmizi 415)",
            "4 rak'ahs sunnah before and 2 after the obligatory prayer (mu'akkadah). On Fridays: Jumu'ah with its sunnah. Whoever prays 12 voluntary rak'ahs daily, a house is built for him in Paradise. (Muslim 728, Tirmidhi 415)"
        )
        P.ASR -> T(
            "4 Rakʿa Sunnah vor dem Pflichtgebet (nicht muʾakkada): „Möge Allah dem gnädig sein, der vor Asr vier Rakʿa betet.“ (Abu Dawud 1271, Tirmidhi 430)",
            "Farzdan önce 4 rekât sünnet (gayr-i müekked): „İkindiden önce dört rekât kılana Allah rahmet etsin.“ (Ebu Davud 1271, Tirmizi 430)",
            "4 rak'ahs sunnah before the obligatory prayer (not mu'akkadah): “May Allah have mercy on one who prays four before Asr.” (Abu Dawud 1271, Tirmidhi 430)"
        )
        P.MAGHRIB -> T(
            "2 Rakʿa Sunnah nach dem Pflichtgebet (muʾakkada).",
            "Farzdan sonra 2 rekât sünnet (müekked).",
            "2 rak'ahs sunnah after the obligatory prayer (mu'akkadah)."
        )
        else -> T(
            "2 Rakʿa Sunnah nach dem Pflichtgebet (muʾakkada), danach das Witr-Gebet. Nach hanafitischer Praxis zusätzlich 4 Rakʿa vor dem Pflichtgebet (nicht muʾakkada).",
            "Farzdan sonra 2 rekât sünnet (müekked), ardından vitir namazı. Hanefi mezhebinde ayrıca farzdan önce 4 rekât (gayr-i müekked) sünnet kılınır.",
            "2 rak'ahs sunnah after the obligatory prayer (mu'akkadah), then Witr. In Hanafi practice also 4 rak'ahs before the obligatory prayer (not mu'akkadah)."
        )
    }

    private val istighfar = Dhikr(
        "أَسْتَغْفِرُ اللَّهَ", "Astaghfirullah",
        T("Ich bitte Allah um Vergebung.", "Allah'tan bağışlanma dilerim.", "I seek Allah's forgiveness."),
        3, "Muslim 591"
    )
    private val antasSalam = Dhikr(
        "اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ، تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالْإِكْرَامِ",
        "Allahumma antas-salam wa minkas-salam, tabarakta ya dhal-jalali wal-ikram",
        T(
            "O Allah, Du bist der Friede, und von Dir kommt der Friede. Gesegnet bist Du, o Besitzer der Majestät und der Ehre.",
            "Allah'ım, Sen Selam'sın, selamet Sendendir. Ey celal ve ikram sahibi, Sen ne yücesin.",
            "O Allah, You are Peace and from You comes peace. Blessed are You, O Owner of majesty and honour."
        ),
        1, "Muslim 591"
    )
    private val laIlahaMani = Dhikr(
        "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، اللَّهُمَّ لَا مَانِعَ لِمَا أَعْطَيْتَ، وَلَا مُعْطِيَ لِمَا مَنَعْتَ، وَلَا يَنْفَعُ ذَا الْجَدِّ مِنْكَ الْجَدُّ",
        "La ilaha illallahu wahdahu la sharika lah, lahul-mulku wa lahul-hamdu wa huwa ʿala kulli shay'in qadir. Allahumma la maniʿa lima aʿtayt, wa la muʿtiya lima manaʿt, wa la yanfaʿu dhal-jaddi minkal-jadd",
        T(
            "Es gibt keinen Gott außer Allah, dem Einen ohne Teilhaber. Ihm gehört die Herrschaft und Ihm gebührt das Lob, und Er hat Macht über alles. O Allah, niemand kann verwehren, was Du gibst, und niemand geben, was Du verwehrst; und dem Vermögenden nützt sein Vermögen nichts vor Dir.",
            "Allah'tan başka ilah yoktur, O tektir, ortağı yoktur. Mülk O'nundur, hamd O'nadır ve O her şeye kadirdir. Allah'ım, Senin verdiğine engel olacak, engellediğini verecek kimse yoktur; servet sahibine servetinin Sana karşı faydası olmaz.",
            "There is no god but Allah alone, without partner. His is the dominion and His the praise, and He is over all things capable. O Allah, none can withhold what You give, nor give what You withhold, and the wealth of the wealthy avails nothing against You."
        ),
        1, "Bukhari 844, Muslim 593"
    )
    private val aAinni = Dhikr(
        "اللَّهُمَّ أَعِنِّي عَلَى ذِكْرِكَ وَشُكْرِكَ وَحُسْنِ عِبَادَتِكَ",
        "Allahumma aʿinni ʿala dhikrika wa shukrika wa husni ʿibadatik",
        T(
            "O Allah, hilf mir, Deiner zu gedenken, Dir zu danken und Dich auf schönste Weise anzubeten.",
            "Allah'ım, Seni anmak, Sana şükretmek ve Sana güzelce ibadet etmek için bana yardım et.",
            "O Allah, help me to remember You, to thank You and to worship You in the best manner."
        ),
        1, "Abu Dawud 1522"
    )
    private val ayatKursi = Dhikr(
        Quran.AYAT_KURSI_AR, Quran.AYAT_KURSI_TR,
        Quran.AYAT_KURSI_MEANING,
        1, "an-Nasa'i (as-Sunan al-Kubra), at-Tabarani",
        T(
            "Wer nach jedem Pflichtgebet Ayat al-Kursi liest, den hindert nur der Tod am Eintritt ins Paradies.",
            "Her farz namazdan sonra Âyetü'l-Kürsî okuyanı cennete girmekten ancak ölüm alıkoyar.",
            "Whoever recites Ayat al-Kursi after every obligatory prayer, nothing prevents him from entering Paradise except death."
        )
    )
    private val subhanallah = Dhikr(
        "سُبْحَانَ اللَّهِ", "Subhanallah",
        T("Gepriesen sei Allah.", "Allah'ı her türlü noksanlıktan tenzih ederim.", "Glory be to Allah."),
        33, "Muslim 597"
    )
    private val alhamdulillah = Dhikr(
        "الْحَمْدُ لِلَّهِ", "Alhamdulillah",
        T("Alles Lob gebührt Allah.", "Hamd Allah'a mahsustur.", "All praise is due to Allah."),
        33, "Muslim 597"
    )
    private val allahuAkbar = Dhikr(
        "اللَّهُ أَكْبَرُ", "Allahu akbar",
        T("Allah ist am größten.", "Allah en büyüktür.", "Allah is the Greatest."),
        33, "Muslim 597"
    )
    private val tahlil100 = Dhikr(
        "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
        "La ilaha illallahu wahdahu la sharika lah, lahul-mulku wa lahul-hamdu wa huwa ʿala kulli shay'in qadir",
        T(
            "Es gibt keinen Gott außer Allah, dem Einen ohne Teilhaber. Ihm gehört die Herrschaft und Ihm gebührt das Lob, und Er hat Macht über alles. (Ergänzt die 99 zu 100 – dem werden die Sünden vergeben, auch wenn sie so viel wie der Meeresschaum wären.)",
            "Allah'tan başka ilah yoktur, O tektir, ortağı yoktur. Mülk O'nundur, hamd O'nadır ve O her şeye kadirdir. (99'u 100'e tamamlar – günahları deniz köpüğü kadar olsa da bağışlanır.)",
            "There is no god but Allah alone, without partner. His is the dominion and His the praise, and He is over all things capable. (Completes the 99 to 100 – his sins are forgiven even if they were like the foam of the sea.)"
        ),
        1, "Muslim 597"
    )
    private fun muawwidhat(times: Int) = Dhikr(
        Quran.IKHLAS_AR + "\n\n" + Quran.FALAQ_AR + "\n\n" + Quran.NAS_AR,
        "Surat al-Ikhlas, al-Falaq, an-Nas",
        T(
            "Die Suren al-Ikhlas, al-Falaq und an-Nas" + if (times > 1) " – nach Fajr und Maghrib je dreimal." else ".",
            "İhlâs, Felak ve Nâs sureleri" + if (times > 1) " – sabah ve akşam namazlarından sonra üçer kez." else ".",
            "Surahs al-Ikhlas, al-Falaq and an-Nas" + if (times > 1) " – three times each after Fajr and Maghrib." else "."
        ),
        times, "Abu Dawud 1523, Tirmidhi 2903" + if (times > 1) "; Abu Dawud 5082" else ""
    )
    private val tahlil10 = Dhikr(
        "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، يُحْيِي وَيُمِيتُ، وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
        "La ilaha illallahu wahdahu la sharika lah, lahul-mulku wa lahul-hamd, yuhyi wa yumit, wa huwa ʿala kulli shay'in qadir",
        T(
            "Es gibt keinen Gott außer Allah, dem Einen ohne Teilhaber. Ihm gehört die Herrschaft und Ihm gebührt das Lob. Er gibt Leben und lässt sterben, und Er hat Macht über alles. (Nach Fajr und Maghrib zehnmal.)",
            "Allah'tan başka ilah yoktur, O tektir, ortağı yoktur. Mülk O'nundur, hamd O'nadır. Diriltir ve öldürür; O her şeye kadirdir. (Sabah ve akşamdan sonra on kez.)",
            "There is no god but Allah alone, without partner. His is the dominion and His the praise. He gives life and causes death, and He is over all things capable. (Ten times after Fajr and Maghrib.)"
        ),
        10, "Tirmidhi 3474, 3534"
    )
    private val ilmanNafia = Dhikr(
        "اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْمًا نَافِعًا، وَرِزْقًا طَيِّبًا، وَعَمَلًا مُتَقَبَّلًا",
        "Allahumma inni as'aluka ʿilman nafiʿa, wa rizqan tayyiba, wa ʿamalan mutaqabbala",
        T(
            "O Allah, ich bitte Dich um nützliches Wissen, gute Versorgung und angenommene Taten. (Nach dem Fajr-Gebet.)",
            "Allah'ım, Senden faydalı ilim, helal ve temiz rızık ve kabul edilmiş amel isterim. (Sabah namazından sonra.)",
            "O Allah, I ask You for beneficial knowledge, good provision and accepted deeds. (After Fajr.)"
        ),
        1, "Ibn Majah 925"
    )

    /** Adhkar nach dem Pflichtgebet, abhängig vom Gebet. */
    fun afterSalah(prayer: Int): List<Dhikr> {
        val fajrOrMaghrib = prayer == P.FAJR || prayer == P.MAGHRIB
        val list = mutableListOf(
            istighfar, antasSalam, laIlahaMani, aAinni, ayatKursi,
            subhanallah, alhamdulillah, allahuAkbar, tahlil100,
            muawwidhat(if (fajrOrMaghrib) 3 else 1),
        )
        if (fajrOrMaghrib) list.add(tahlil10)
        if (prayer == P.FAJR) list.add(ilmanNafia)
        return list
    }
}
