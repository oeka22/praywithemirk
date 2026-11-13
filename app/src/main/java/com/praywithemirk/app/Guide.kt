package com.praywithemirk.app

/** Ein Schritt der Anleitung, optional mit arabischem Text zum Sprechen. */
class GuideStep(
    val title: T,
    val text: T,
    val arabic: String? = null,
    val translit: String? = null,
    val meaning: T? = null,
)

/**
 * Einfache Anleitung für Gebetsanfänger: Gebetswaschung (Wudu) und Ablauf des Gebets.
 * Bewusst knapp gehalten; kleine Unterschiede zwischen den Rechtsschulen werden nur erwähnt.
 */
object Guide {

    val wuduIntro = T(
        "Vor dem Gebet machst du die kleine Waschung (Wudu). Sie dauert etwa zwei Minuten. Nimm sauberes Wasser und beginne immer mit der rechten Seite.",
        "Namazdan önce abdest alırsın. Yaklaşık iki dakika sürer. Temiz su kullan ve her zaman sağ taraftan başla.",
        "Before praying you perform the small ablution (wudu). It takes about two minutes. Use clean water and always start with the right side."
    )

    val wudu = listOf(
        GuideStep(
            T("Hände – 3×", "Eller – 3 kez", "Hands – 3×"),
            T("Wasche beide Hände bis zu den Handgelenken, auch zwischen den Fingern.",
                "Ellerini bileklere kadar, parmak aralarıyla birlikte yıka.",
                "Wash both hands up to the wrists, including between the fingers.")
        ),
        GuideStep(
            T("Mund – 3×", "Ağız – 3 kez", "Mouth – 3×"),
            T("Nimm mit der rechten Hand Wasser in den Mund, spüle ihn aus und spucke es aus.",
                "Sağ elinle ağzına su al, çalkala ve dök.",
                "Take water into your mouth with your right hand, rinse and spit it out.")
        ),
        GuideStep(
            T("Nase – 3×", "Burun – 3 kez", "Nose – 3×"),
            T("Ziehe mit der rechten Hand etwas Wasser in die Nase und schnaube es mit der linken Hand aus.",
                "Sağ elinle burnuna su çek, sol elinle sümkür.",
                "Sniff a little water into your nose with the right hand and blow it out with the left.")
        ),
        GuideStep(
            T("Gesicht – 3×", "Yüz – 3 kez", "Face – 3×"),
            T("Wasche das ganze Gesicht: vom Haaransatz bis unter das Kinn und von Ohr zu Ohr.",
                "Yüzünün tamamını yıka: saç bitiminden çene altına, bir kulaktan diğerine kadar.",
                "Wash the whole face: from the hairline to below the chin and from ear to ear.")
        ),
        GuideStep(
            T("Arme – 3×", "Kollar – 3 kez", "Arms – 3×"),
            T("Wasche zuerst den rechten, dann den linken Arm – von den Fingerspitzen bis einschließlich Ellbogen.",
                "Önce sağ, sonra sol kolunu parmak uçlarından dirsekler dahil yıka.",
                "Wash first the right, then the left arm – from the fingertips up to and including the elbow.")
        ),
        GuideStep(
            T("Kopf – 1×", "Baş – 1 kez", "Head – 1×"),
            T("Streiche mit den nassen Händen einmal über den Kopf, von der Stirn nach hinten und wieder zurück.",
                "Islak ellerinle başını bir kez mesh et: alından arkaya ve tekrar öne.",
                "Wipe over your head once with wet hands, from the forehead to the back and back again.")
        ),
        GuideStep(
            T("Ohren – 1×", "Kulaklar – 1 kez", "Ears – 1×"),
            T("Reinige die Ohren innen mit den Zeigefingern und außen mit den Daumen.",
                "Kulakların içini işaret parmaklarınla, arkasını başparmaklarınla mesh et.",
                "Wipe the inside of the ears with your index fingers and the back with your thumbs.")
        ),
        GuideStep(
            T("Füße – 3×", "Ayaklar – 3 kez", "Feet – 3×"),
            T("Wasche zuerst den rechten, dann den linken Fuß bis einschließlich der Knöchel, auch zwischen den Zehen.",
                "Önce sağ, sonra sol ayağını topuklar dahil, parmak aralarıyla birlikte yıka.",
                "Wash first the right, then the left foot up to and including the ankles, also between the toes.")
        ),
        GuideStep(
            T("Bittgebet danach", "Abdestten sonra dua", "Supplication afterwards"),
            T("Nach der Waschung sprichst du das Glaubensbekenntnis (Muslim 234):",
                "Abdestten sonra kelime-i şehadet getirilir (Müslim 234):",
                "After wudu recite the testimony of faith (Muslim 234):"),
            "أَشْهَدُ أَنْ لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ",
            "Ashhadu an la ilaha illallahu wahdahu la sharika lah, wa ashhadu anna Muhammadan ʿabduhu wa rasuluh",
            T("Ich bezeuge, dass es keinen Gott gibt außer Allah, dem Einen ohne Teilhaber, und ich bezeuge, dass Muhammad Sein Diener und Gesandter ist.",
                "Şehadet ederim ki Allah'tan başka ilah yoktur, O tektir, ortağı yoktur; ve şehadet ederim ki Muhammed O'nun kulu ve elçisidir.",
                "I bear witness that there is no god but Allah alone, without partner, and I bear witness that Muhammad is His servant and messenger.")
        ),
    )

    val wuduNote = T(
        "Pflicht sind: Gesicht, Arme mit Ellbogen, Kopf benetzen und Füße mit Knöcheln – der Rest ist Sunnah. Die Waschung wird ungültig durch Toilettengang, Wind, tiefen Schlaf oder Bewusstlosigkeit. Nach Geschlechtsverkehr, Menstruation oder Wochenbett ist die Ganzkörperwaschung (Ghusl) nötig.",
        "Farz olanlar: yüzü, dirseklerle birlikte kolları, başı mesh etmek ve topuklarla birlikte ayakları yıkamak – diğerleri sünnettir. Abdest; tuvalete gitmek, yel çıkması, derin uyku veya bayılma ile bozulur. Cünüplük, hayız veya lohusalıktan sonra gusül gerekir.",
        "Obligatory are: face, arms including elbows, wiping the head and feet including ankles – the rest is sunnah. Wudu is broken by using the toilet, passing wind, deep sleep or unconsciousness. After intercourse, menstruation or post-natal bleeding the full bath (ghusl) is required."
    )

    val salahIntro = T(
        "Voraussetzungen: gültige Waschung, saubere Kleidung und sauberer Platz, bedeckter Körper (Männer mindestens vom Bauchnabel bis zu den Knien, Frauen alles außer Gesicht und Händen), Blick Richtung Qibla und die Gebetszeit ist eingetreten. Ein Gebet besteht aus Einheiten (Rakʿa). Jede Rakʿa: Stehen – Verbeugen – Aufrichten – zwei Niederwerfungen.",
        "Şartlar: geçerli abdest, temiz elbise ve temiz yer, avret yerlerinin örtülmesi (erkekler en az göbek ile diz arası, kadınlar yüz ve eller hariç tüm beden), kıbleye yönelmek ve vaktin girmiş olması. Namaz rekâtlardan oluşur. Her rekât: kıyam – rükû – doğrulma – iki secde.",
        "Requirements: valid wudu, clean clothes and a clean place, covered body (men at least navel to knees, women everything except face and hands), facing the qibla and the prayer time has begun. A prayer consists of units (rak'ah). Each rak'ah: standing – bowing – rising – two prostrations."
    )

    val salah = listOf(
        GuideStep(
            T("1. Absicht und Eröffnung", "1. Niyet ve iftitah tekbiri", "1. Intention and opening"),
            T("Stehe aufrecht Richtung Qibla. Fasse im Herzen die Absicht (z. B. „das Fard-Gebet von Dhuhr“). Hebe die Hände bis zu den Ohren (Frauen bis zu den Schultern) und sprich:",
                "Kıbleye dönerek dik dur. Kalbinden niyet et (örn. „öğle namazının farzı“). Ellerini kulak hizasına (kadınlar omuz hizasına) kaldır ve şöyle de:",
                "Stand upright facing the qibla. Make the intention in your heart (e.g. “the fard of Dhuhr”). Raise your hands to your ears (women to the shoulders) and say:"),
            "اللَّهُ أَكْبَرُ", "Allahu akbar", T("Allah ist am größten", "Allah en büyüktür", "Allah is the Greatest")
        ),
        GuideStep(
            T("2. Stehen: Eröffnungsgebet", "2. Kıyam: Sübhaneke", "2. Standing: opening supplication"),
            T("Lege die rechte Hand auf die linke (unter oder über dem Bauchnabel) und sprich leise (nur in der ersten Rakʿa):",
                "Sağ elini sol elinin üzerine koy (göbek altı veya üstü) ve sessizce oku (sadece ilk rekâtta):",
                "Place the right hand over the left (below or above the navel) and say quietly (only in the first rak'ah):"),
            "سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ، وَتَبَارَكَ اسْمُكَ، وَتَعَالَى جَدُّكَ، وَلَا إِلَهَ غَيْرُكَ",
            "Subhanakallahumma wa bihamdik, wa tabarakasmuk, wa taʿala jadduk, wa la ilaha ghayruk",
            T("Gepriesen bist Du, o Allah, und gelobt. Gesegnet ist Dein Name, erhaben Deine Majestät, und es gibt keinen Gott außer Dir. (Abu Dawud 775)",
                "Allah'ım! Seni her türlü noksanlıktan tenzih eder ve Sana hamd ederim. İsmin mübarektir, şanın yücedir ve Senden başka ilah yoktur. (Ebu Davud 775)",
                "Glory be to You, O Allah, and praise. Blessed is Your name, exalted Your majesty, and there is no god but You. (Abu Dawud 775)")
        ),
        GuideStep(
            T("3. Stehen: al-Fatiha und eine Sure", "3. Kıyam: Fâtiha ve sure", "3. Standing: al-Fatiha and a surah"),
            T("Sprich „Aʿudhu billahi minash-shaytanir-rajim, Bismillahir-rahmanir-rahim“, dann die Sure al-Fatiha und „Amin“. In den ersten zwei Rakʿa folgt eine kurze Sure, z. B. al-Ikhlas. Al-Fatiha findest du im Tagesimpuls.",
                "„Eûzü billâhi mine'ş-şeytâni'r-racîm, Bismillâhirrahmânirrahîm“ de, sonra Fâtiha'yı oku ve „Âmin“ de. İlk iki rekâtta kısa bir sure eklenir, örn. İhlâs.",
                "Say “A'udhu billahi minash-shaytanir-rajim, Bismillahir-rahmanir-rahim”, then Surah al-Fatiha and “Amin”. In the first two rak'ahs add a short surah, e.g. al-Ikhlas."),
            Quran.IKHLAS_AR,
            "Qul huwallahu ahad. Allahus-samad. Lam yalid wa lam yulad. Wa lam yakul-lahu kufuwan ahad.",
            T("Beispiel: Sure al-Ikhlas", "Örnek: İhlâs Suresi", "Example: Surah al-Ikhlas")
        ),
        GuideStep(
            T("4. Verbeugung (Rukuʿ)", "4. Rükû", "4. Bowing (ruku')"),
            T("Sprich „Allahu akbar“ und verbeuge dich: Rücken gerade, Hände auf den Knien. Sprich dreimal:",
                "„Allahu ekber“ diyerek eğil: sırt düz, eller dizlerde. Üç kez de:",
                "Say “Allahu akbar” and bow: back straight, hands on the knees. Say three times:"),
            "سُبْحَانَ رَبِّيَ الْعَظِيمِ", "Subhana rabbiyal-ʿazim",
            T("Gepriesen sei mein Herr, der Allgewaltige", "Yüce Rabbimi tenzih ederim", "Glory be to my Lord, the Magnificent")
        ),
        GuideStep(
            T("5. Aufrichten", "5. Doğrulma (kavme)", "5. Rising"),
            T("Richte dich auf und sprich dabei den ersten Satz, im Stehen den zweiten:",
                "Doğrulurken ilk cümleyi, ayakta ikincisini söyle:",
                "Rise saying the first phrase, then standing the second:"),
            "سَمِعَ اللَّهُ لِمَنْ حَمِدَهُ\nرَبَّنَا وَلَكَ الْحَمْدُ",
            "Samiʿallahu liman hamidah – Rabbana wa lakal-hamd",
            T("Allah hört den, der Ihn lobt – Unser Herr, Dir gebührt das Lob",
                "Allah kendisine hamd edeni işitir – Rabbimiz, hamd Sana mahsustur",
                "Allah hears the one who praises Him – Our Lord, to You belongs all praise")
        ),
        GuideStep(
            T("6. Niederwerfung (Sajda) – 2×", "6. Secde – 2 kez", "6. Prostration (sajdah) – 2×"),
            T("Mit „Allahu akbar“ in die Niederwerfung: Stirn und Nase, beide Hände, Knie und Zehen berühren den Boden. Sprich dreimal den Satz unten. Mit „Allahu akbar“ kurz aufsetzen (dabei z. B. „Rabbighfir li“ – Herr, vergib mir), dann eine zweite Niederwerfung genauso.",
                "„Allahu ekber“ diyerek secdeye var: alın ve burun, iki el, dizler ve ayak parmakları yere değer. Aşağıdakini üç kez söyle. „Allahu ekber“ ile kısa otur („Rabbiğfir lî“ – Rabbim beni bağışla), sonra aynı şekilde ikinci secdeyi yap.",
                "With “Allahu akbar” go into prostration: forehead and nose, both hands, knees and toes touch the ground. Say the phrase below three times. Sit up briefly with “Allahu akbar” (e.g. “Rabbighfir li” – Lord, forgive me), then make a second prostration the same way."),
            "سُبْحَانَ رَبِّيَ الْأَعْلَى", "Subhana rabbiyal-aʿla",
            T("Gepriesen sei mein Herr, der Höchste", "Yüceler yücesi Rabbimi tenzih ederim", "Glory be to my Lord, the Most High")
        ),
        GuideStep(
            T("7. Nächste Rakʿa", "7. Sonraki rekât", "7. Next rak'ah"),
            T("Mit „Allahu akbar“ aufstehen. Die zweite Rakʿa ist wie die erste, nur ohne Eröffnungsgebet (Schritt 2).",
                "„Allahu ekber“ diyerek kalk. İkinci rekât birinci gibidir, sadece Sübhaneke okunmaz.",
                "Stand up with “Allahu akbar”. The second rak'ah is like the first, only without the opening supplication (step 2)."),
        ),
        GuideStep(
            T("8. Sitzen nach der 2. Rakʿa: Taschahhud", "8. İkinci rekâttan sonra oturuş: Tahiyyat", "8. Sitting after the 2nd rak'ah: tashahhud"),
            T("Nach der zweiten Niederwerfung der 2. Rakʿa setzt du dich und sprichst den Taschahhud. Hat das Gebet 3 oder 4 Rakʿa, stehst du danach mit „Allahu akbar“ wieder auf. In der 3. und 4. Rakʿa eines Pflichtgebets liest du nur al-Fatiha.",
                "İkinci rekâtın secdelerinden sonra oturup Tahiyyat'ı oku. Namaz 3 veya 4 rekâtlı ise ardından „Allahu ekber“ diyerek kalk. Farzların 3. ve 4. rekâtında sadece Fâtiha okunur.",
                "After the prostrations of the 2nd rak'ah sit and recite the tashahhud. If the prayer has 3 or 4 rak'ahs, stand up again with “Allahu akbar”. In the 3rd and 4th rak'ah of an obligatory prayer recite only al-Fatiha."),
            "التَّحِيَّاتُ لِلَّهِ وَالصَّلَوَاتُ وَالطَّيِّبَاتُ، السَّلَامُ عَلَيْكَ أَيُّهَا النَّبِيُّ وَرَحْمَةُ اللَّهِ وَبَرَكَاتُهُ، السَّلَامُ عَلَيْنَا وَعَلَى عِبَادِ اللَّهِ الصَّالِحِينَ، أَشْهَدُ أَنْ لَا إِلَهَ إِلَّا اللَّهُ، وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ",
            "At-tahiyyatu lillahi was-salawatu wat-tayyibat. As-salamu ʿalayka ayyuhan-nabiyyu wa rahmatullahi wa barakatuh. As-salamu ʿalayna wa ʿala ʿibadillahis-salihin. Ashhadu an la ilaha illallah, wa ashhadu anna Muhammadan ʿabduhu wa rasuluh.",
            T("Alle Grüße, Gebete und guten Worte gebühren Allah. Friede sei mit dir, o Prophet, und Allahs Barmherzigkeit und Segen. Friede sei mit uns und den rechtschaffenen Dienern Allahs. Ich bezeuge, dass es keinen Gott gibt außer Allah, und ich bezeuge, dass Muhammad Sein Diener und Gesandter ist. (Bukhari 831)",
                "Bütün tahiyyeler, dualar ve güzel sözler Allah'a mahsustur. Ey Peygamber! Allah'ın selamı, rahmeti ve bereketi senin üzerine olsun. Selam bizim ve Allah'ın salih kullarının üzerine olsun. Şehadet ederim ki Allah'tan başka ilah yoktur ve yine şehadet ederim ki Muhammed O'nun kulu ve elçisidir. (Buhârî 831)",
                "All greetings, prayers and good words are for Allah. Peace be upon you, O Prophet, and the mercy of Allah and His blessings. Peace be upon us and upon the righteous servants of Allah. I bear witness that there is no god but Allah, and I bear witness that Muhammad is His servant and messenger. (Bukhari 831)")
        ),
        GuideStep(
            T("9. Letztes Sitzen: Segenswünsche", "9. Son oturuş: Salli-Barik", "9. Final sitting: blessings"),
            T("Im letzten Sitzen sprichst du nach dem Taschahhud die Segenswünsche auf den Propheten ﷺ und danach gern ein kurzes Bittgebet, z. B. „Rabbana atina fid-dunya hasanah, wa fil-akhirati hasanah, wa qina ʿadhaban-nar“.",
                "Son oturuşta Tahiyyat'tan sonra Salli ve Barik dualarını, ardından kısa bir dua oku, örn. „Rabbenâ âtinâ fi'd-dünyâ haseneten ve fi'l-âhireti haseneten ve kınâ azâbe'n-nâr“.",
                "In the final sitting after the tashahhud recite the blessings on the Prophet ﷺ, then a short supplication, e.g. “Rabbana atina fid-dunya hasanah, wa fil-akhirati hasanah, wa qina 'adhaban-nar”."),
            "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ، كَمَا صَلَّيْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ، إِنَّكَ حَمِيدٌ مَجِيدٌ، اللَّهُمَّ بَارِكْ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ، كَمَا بَارَكْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ، إِنَّكَ حَمِيدٌ مَجِيدٌ",
            "Allahumma salli ʿala Muhammadin wa ʿala ali Muhammad, kama sallayta ʿala Ibrahima wa ʿala ali Ibrahim, innaka hamidun majid. Allahumma barik ʿala Muhammadin wa ʿala ali Muhammad, kama barakta ʿala Ibrahima wa ʿala ali Ibrahim, innaka hamidun majid.",
            T("O Allah, segne Muhammad und die Familie Muhammads, wie Du Ibrahim und die Familie Ibrahims gesegnet hast; Du bist der Lobenswerte, der Ruhmreiche. O Allah, gib Muhammad und der Familie Muhammads Segen, wie Du Ibrahim und der Familie Ibrahims Segen gegeben hast; Du bist der Lobenswerte, der Ruhmreiche. (Bukhari 3370)",
                "Allah'ım! İbrahim'e ve ailesine rahmet ettiğin gibi Muhammed'e ve ailesine de rahmet et; şüphesiz Sen övülmeye layık ve şanı yüce olansın. Allah'ım! İbrahim'e ve ailesine bereket verdiğin gibi Muhammed'e ve ailesine de bereket ver; şüphesiz Sen övülmeye layık ve şanı yüce olansın. (Buhârî 3370)",
                "O Allah, send blessings upon Muhammad and the family of Muhammad as You sent blessings upon Ibrahim and the family of Ibrahim; You are Praiseworthy, Glorious. O Allah, bless Muhammad and the family of Muhammad as You blessed Ibrahim and the family of Ibrahim; You are Praiseworthy, Glorious. (Bukhari 3370)")
        ),
        GuideStep(
            T("10. Friedensgruß – Ende", "10. Selam – bitiş", "10. Salam – end"),
            T("Drehe den Kopf nach rechts und sprich den Gruß, dann nach links und sprich ihn noch einmal. Das Gebet ist beendet. Danach folgen die Adhkar.",
                "Başını sağa çevirip selam ver, sonra sola çevirip tekrar ver. Namaz bitmiştir. Ardından tesbihat yapılır.",
                "Turn your head to the right and say the greeting, then to the left and say it again. The prayer is complete. Then come the adhkar."),
            "السَّلَامُ عَلَيْكُمْ وَرَحْمَةُ اللَّهِ", "As-salamu ʿalaykum wa rahmatullah",
            T("Friede sei mit euch und die Barmherzigkeit Allahs", "Allah'ın selamı ve rahmeti üzerinize olsun", "Peace be upon you and the mercy of Allah")
        ),
    )

    val salahNote = T(
        "Kleine Unterschiede (z. B. Handhaltung, Eröffnungsgebet) gibt es je nach Rechtsschule – alle sind gültig. Frag bei Unsicherheiten gern in deiner Moschee nach. Am Anfang darfst du die Texte auch vom Handy ablesen, während du übst.",
        "Mezheplere göre küçük farklar vardır (örn. ellerin bağlanması, açılış duası) – hepsi geçerlidir. Emin olmadığında camiindeki hocaya sorabilirsin. Başlangıçta öğrenirken metinleri telefondan okuyabilirsin.",
        "There are small differences between the schools of law (e.g. hand position, opening supplication) – all are valid. If unsure, ask at your mosque. At the beginning you may read the texts from your phone while practising."
    )

    /** Aufbau eines Gebets: Liste aus (Bezeichnung, Anzahl Rakʿa). */
    class Part(val label: T, val rakat: String, val main: Boolean = false)

    private val sunnahBefore = T("Sunnah davor", "İlk sünnet", "Sunnah before")
    private val sunnahAfter = T("Sunnah danach", "Son sünnet", "Sunnah after")
    private val fard = T("Fard (Pflicht)", "Farz", "Fard (obligatory)")
    private val optBefore = T("Sunnah davor (freiwillig)", "İlk sünnet (gayr-i müekked)", "Sunnah before (optional)")

    fun parts(prayer: Int): List<Part> = when (prayer) {
        P.FAJR -> listOf(Part(sunnahBefore, "2"), Part(fard, "2", true))
        P.DHUHR -> listOf(Part(sunnahBefore, "4"), Part(fard, "4", true), Part(sunnahAfter, "2"))
        P.ASR -> listOf(Part(optBefore, "4"), Part(fard, "4", true))
        P.MAGHRIB -> listOf(Part(fard, "3", true), Part(sunnahAfter, "2"))
        else -> listOf(
            Part(optBefore, "4"), Part(fard, "4", true), Part(sunnahAfter, "2"),
            Part(T("Witr", "Vitir", "Witr"), "3")
        )
    }

    fun prayerNote(prayer: Int): T = when (prayer) {
        P.FAJR -> T(
            "Zeit: vom Morgengrauen bis kurz vor Sonnenaufgang. Das kürzeste Gebet: 2 Rakʿa Pflicht, mit Taschahhud nach der 2. Rakʿa direkt zum Abschluss. Der Imam rezitiert laut.",
            "Vakit: tan yerinin ağarmasından güneş doğmadan az öncesine kadar. En kısa namaz: 2 rekât farz, ikinci rekâttan sonra Tahiyyat, Salli-Barik ve selam. İmam sesli okur.",
            "Time: from dawn until shortly before sunrise. The shortest prayer: 2 rak'ahs obligatory, after the 2nd rak'ah tashahhud and directly the ending. The imam recites aloud."
        )
        P.DHUHR -> T(
            "Zeit: kurz nach dem Mittag bis zum Asr. 4 Rakʿa Pflicht mit Taschahhud nach der 2. und am Ende. Die Rezitation ist leise. Freitags ersetzt das Freitagsgebet (Dschumuʿa) mit Predigt in der Moschee das Dhuhr.",
            "Vakit: öğleden hemen sonra ikindiye kadar. 4 rekât farz; 2. rekâttan sonra ve sonda oturuş. Kıraat gizlidir. Cuma günü camide hutbeli Cuma namazı öğlenin yerine geçer.",
            "Time: just after midday until Asr. 4 rak'ahs obligatory with tashahhud after the 2nd and at the end. Recitation is quiet. On Fridays the Friday prayer (Jumu'ah) with sermon at the mosque replaces Dhuhr."
        )
        P.ASR -> T(
            "Zeit: am Nachmittag bis Sonnenuntergang (nicht zu spät beten, wenn die Sonne gelb wird). 4 Rakʿa Pflicht, aufgebaut wie Dhuhr, leise Rezitation.",
            "Vakit: ikindiden güneş batana kadar (güneş sararmadan kılmaya çalış). 4 rekât farz, öğle gibi kılınır, kıraat gizlidir.",
            "Time: afternoon until sunset (don't delay until the sun turns yellow). 4 rak'ahs obligatory, like Dhuhr, quiet recitation."
        )
        P.MAGHRIB -> T(
            "Zeit: direkt nach Sonnenuntergang – möglichst bald beten. 3 Rakʿa Pflicht: Taschahhud nach der 2., dann eine 3. Rakʿa nur mit al-Fatiha und Abschluss. Die ersten zwei Rakʿa rezitiert der Imam laut.",
            "Vakit: güneş batar batmaz – mümkün olduğunca erken kıl. 3 rekât farz: 2. rekâttan sonra Tahiyyat, sonra sadece Fâtiha ile 3. rekât ve selam. İlk iki rekâtta imam sesli okur.",
            "Time: right after sunset – pray it soon. 3 rak'ahs obligatory: tashahhud after the 2nd, then a 3rd rak'ah with only al-Fatiha and the ending. The imam recites the first two aloud."
        )
        else -> T(
            "Zeit: wenn es ganz dunkel ist, bis Mitternacht (spätestens vor Fajr). 4 Rakʿa Pflicht, die ersten zwei laut. Danach Witr: nach hanafitischer Praxis 3 Rakʿa mit dem Bittgebet Qunut in der 3. Rakʿa, nach anderen Schulen 1 oder 3 Rakʿa.",
            "Vakit: hava tamamen karardığında gece yarısına kadar (en geç imsaktan önce). 4 rekât farz, ilk ikisi sesli. Sonra vitir: Hanefi'de 3 rekât, 3. rekâtta Kunut duası; diğer mezheplerde 1 veya 3 rekât.",
            "Time: when it is fully dark until midnight (at the latest before Fajr). 4 rak'ahs obligatory, the first two aloud. Then Witr: in Hanafi practice 3 rak'ahs with the Qunut supplication in the 3rd, in other schools 1 or 3 rak'ahs."
        )
    }
}
