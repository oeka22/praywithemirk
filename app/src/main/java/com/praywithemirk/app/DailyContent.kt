package com.praywithemirk.app

import java.time.LocalDate
import java.time.ZonedDateTime

class DailyItem(
    val isQuran: Boolean,
    val title: T,
    val arabic: String,
    val translit: String?,
    val meaning: T,
    val source: String,
)

/**
 * Tägliche Impulse: zweimal am Tag (ab Fajr und ab Maghrib) wird ein anderer
 * Hadith bzw. eine Sure/Ayah angezeigt. Koran und Hadith wechseln sich ab.
 */
object DailyContent {

    /** Umschrift der Hadithe (Schlüssel = arabischer Text). */
    private val HADITH_TR: Map<String, String> = mapOf(
        "إِنَّمَا الْأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى" to "Innamal-aʿmalu bin-niyyat, wa innama li-kullim-ri'in ma nawa",
        "لَا يُؤْمِنُ أَحَدُكُمْ حَتَّى يُحِبَّ لِأَخِيهِ مَا يُحِبُّ لِنَفْسِهِ" to "La yu'minu ahadukum hatta yuhibba li-akhihi ma yuhibbu li-nafsih",
        "مَنْ كَانَ يُؤْمِنُ بِاللَّهِ وَالْيَوْمِ الْآخِرِ فَلْيَقُلْ خَيْرًا أَوْ لِيَصْمُتْ" to "Man kana yu'minu billahi wal-yawmil-akhiri fal-yaqul khayran aw li-yasmut",
        "الطُّهُورُ شَطْرُ الْإِيمَانِ" to "At-tuhuru shatrul-iman",
        "أَحَبُّ الْأَعْمَالِ إِلَى اللَّهِ أَدْوَمُهَا وَإِنْ قَلَّ" to "Ahabbul-aʿmali ilallahi adwamuha wa in qall",
        "الْكَلِمَةُ الطَّيِّبَةُ صَدَقَةٌ" to "Al-kalimatut-tayyibatu sadaqah",
        "تَبَسُّمُكَ فِي وَجْهِ أَخِيكَ لَكَ صَدَقَةٌ" to "Tabassumuka fi wajhi akhika laka sadaqah",
        "لَيْسَ الشَّدِيدُ بِالصُّرَعَةِ، إِنَّمَا الشَّدِيدُ الَّذِي يَمْلِكُ نَفْسَهُ عِنْدَ الْغَضَبِ" to "Laysash-shadidu bis-surʿah, innamash-shadidul-ladhi yamliku nafsahu ʿindal-ghadab",
        "كَلِمَتَانِ خَفِيفَتَانِ عَلَى اللِّسَانِ، ثَقِيلَتَانِ فِي الْمِيزَانِ، حَبِيبَتَانِ إِلَى الرَّحْمَنِ: سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، سُبْحَانَ اللَّهِ الْعَظِيمِ" to "Kalimatani khafifatani ʿalal-lisan, thaqilatani fil-mizan, habibatani ilar-rahman: Subhanallahi wa bihamdih, subhanallahil-ʿazim",
        "مِنْ حُسْنِ إِسْلَامِ الْمَرْءِ تَرْكُهُ مَا لَا يَعْنِيهِ" to "Min husni islamil-mar'i tarkuhu ma la yaʿnih",
        "الدِّينُ النَّصِيحَةُ" to "Ad-dinun-nasihah",
        "اتَّقِ اللَّهَ حَيْثُمَا كُنْتَ، وَأَتْبِعِ السَّيِّئَةَ الْحَسَنَةَ تَمْحُهَا، وَخَالِقِ النَّاسَ بِخُلُقٍ حَسَنٍ" to "Ittaqillaha haythuma kunt, wa atbiʿis-sayyi'atal-hasanata tamhuha, wa khaliqin-nasa bi-khuluqin hasan",
        "الْمُسْلِمُ مَنْ سَلِمَ الْمُسْلِمُونَ مِنْ لِسَانِهِ وَيَدِهِ" to "Al-muslimu man salimal-muslimuna min lisanihi wa yadih",
        "أَقْرَبُ مَا يَكُونُ الْعَبْدُ مِنْ رَبِّهِ وَهُوَ سَاجِدٌ، فَأَكْثِرُوا الدُّعَاءَ" to "Aqrabu ma yakunul-ʿabdu min rabbihi wa huwa sajid, fa-aktharud-duʿa'",
        "الصَّلَوَاتُ الْخَمْسُ، وَالْجُمُعَةُ إِلَى الْجُمُعَةِ، كَفَّارَةٌ لِمَا بَيْنَهُنَّ، مَا لَمْ تُغْشَ الْكَبَائِرُ" to "As-salawatul-khams, wal-jumuʿatu ilal-jumuʿah, kaffaratun lima baynahunn, ma lam tughshal-kaba'ir",
        "مَنْ سَلَكَ طَرِيقًا يَلْتَمِسُ فِيهِ عِلْمًا، سَهَّلَ اللَّهُ لَهُ بِهِ طَرِيقًا إِلَى الْجَنَّةِ" to "Man salaka tariqan yaltamisu fihi ʿilman, sahhalallahu lahu bihi tariqan ilal-jannah",
        "إِنَّ اللَّهَ لَا يَنْظُرُ إِلَى صُوَرِكُمْ وَأَمْوَالِكُمْ، وَلَكِنْ يَنْظُرُ إِلَى قُلُوبِكُمْ وَأَعْمَالِكُمْ" to "Innallaha la yanzuru ila suwarikum wa amwalikum, wa lakin yanzuru ila qulubikum wa aʿmalikum",
        "خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْآنَ وَعَلَّمَهُ" to "Khayrukum man taʿallamal-qur'ana wa ʿallamah",
        "الرَّاحِمُونَ يَرْحَمُهُمُ الرَّحْمَنُ، ارْحَمُوا مَنْ فِي الْأَرْضِ يَرْحَمْكُمْ مَنْ فِي السَّمَاءِ" to "Ar-rahimuna yarhamuhumur-rahman, irhamu man fil-ardi yarhamkum man fis-sama'",
        "يَسِّرُوا وَلَا تُعَسِّرُوا، وَبَشِّرُوا وَلَا تُنَفِّرُوا" to "Yassiru wa la tuʿassiru, wa bashshiru wa la tunaffiru",
        "مَا نَقَصَتْ صَدَقَةٌ مِنْ مَالٍ" to "Ma naqasat sadaqatun min mal",
        "الْمُؤْمِنُ الْقَوِيُّ خَيْرٌ وَأَحَبُّ إِلَى اللَّهِ مِنَ الْمُؤْمِنِ الضَّعِيفِ، وَفِي كُلٍّ خَيْرٌ" to "Al-mu'minul-qawiyyu khayrun wa ahabbu ilallahi minal-mu'minid-daʿif, wa fi kullin khayr",
        "عَجَبًا لِأَمْرِ الْمُؤْمِنِ، إِنَّ أَمْرَهُ كُلَّهُ خَيْرٌ" to "ʿAjaban li-amril-mu'min, inna amrahu kullahu khayr",
        "لَا تَحْقِرَنَّ مِنَ الْمَعْرُوفِ شَيْئًا، وَلَوْ أَنْ تَلْقَى أَخَاكَ بِوَجْهٍ طَلْقٍ" to "La tahqiranna minal-maʿrufi shay'an, wa law an talqa akhaka bi-wajhin talq",
    )

    private fun q(title: T, ar: String, tr: String, m: T, src: String) = DailyItem(true, title, ar, tr, m, src)
    private fun h(ar: String, m: T, src: String) =
        DailyItem(false, T("Hadith", "Hadis", "Hadith"), ar, HADITH_TR[ar], m, src)

    val quran: List<DailyItem> = listOf(
        q(
            T("Sure al-Fatiha (1)", "Fâtiha Suresi (1)", "Surah al-Fatihah (1)"),
            "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ ﴿١﴾ الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ ﴿٢﴾ الرَّحْمَٰنِ الرَّحِيمِ ﴿٣﴾ مَالِكِ يَوْمِ الدِّينِ ﴿٤﴾ إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ ﴿٥﴾ اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ ﴿٦﴾ صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ ﴿٧﴾",
            "Bismillahir-rahmanir-rahim. Al-hamdu lillahi rabbil-ʿalamin. Ar-rahmanir-rahim. Maliki yawmid-din. Iyyaka naʿbudu wa iyyaka nastaʿin. Ihdinas-siratal-mustaqim. Siratal-ladhina anʿamta ʿalayhim, ghayril-maghdubi ʿalayhim wa lad-dallin.",
            T(
                "Im Namen Allahs, des Allerbarmers, des Barmherzigen. Alles Lob gebührt Allah, dem Herrn der Welten, dem Allerbarmer, dem Barmherzigen, dem Herrscher am Tag des Gerichts. Dir allein dienen wir, und Dich allein bitten wir um Hilfe. Leite uns den geraden Weg, den Weg derer, denen Du Gnade erwiesen hast, nicht derer, die Deinen Zorn erregt haben, und nicht der Irregehenden.",
                "Rahman ve Rahim olan Allah'ın adıyla. Hamd, âlemlerin Rabbi Allah'a mahsustur. O Rahman'dır, Rahim'dir. Din gününün sahibidir. Yalnız Sana kulluk eder, yalnız Senden yardım dileriz. Bizi doğru yola ilet; nimet verdiklerinin yoluna, gazaba uğrayanların ve sapkınların yoluna değil.",
                "In the name of Allah, the Most Merciful, the Especially Merciful. All praise is for Allah, Lord of the worlds, the Most Merciful, the Especially Merciful, Master of the Day of Judgement. You alone we worship and You alone we ask for help. Guide us to the straight path, the path of those You have blessed, not of those who earned anger, nor of those who went astray."
            ),
            "Koran 1:1–7"
        ),
        q(
            T("Ayat al-Kursi", "Âyetü'l-Kürsî", "Ayat al-Kursi"),
            Quran.AYAT_KURSI_AR, Quran.AYAT_KURSI_TR, Quran.AYAT_KURSI_MEANING, "Koran 2:255"
        ),
        q(
            T("Sure al-Ikhlas (112)", "İhlâs Suresi (112)", "Surah al-Ikhlas (112)"),
            Quran.IKHLAS_AR,
            "Qul huwallahu ahad. Allahus-samad. Lam yalid wa lam yulad. Wa lam yakul-lahu kufuwan ahad.",
            T(
                "Sag: Er ist Allah, der Eine. Allah, der Absolute (auf den alle angewiesen sind). Er zeugt nicht und ist nicht gezeugt worden, und niemand ist Ihm gleich.",
                "De ki: O Allah birdir. Allah Samed'dir (her şey O'na muhtaçtır). Doğurmamış ve doğurulmamıştır. Hiçbir şey O'na denk değildir.",
                "Say: He is Allah, the One. Allah, the Eternal Refuge. He neither begets nor is born, and there is none comparable to Him."
            ),
            "Koran 112:1–4"
        ),
        q(
            T("Sure al-Falaq (113)", "Felak Suresi (113)", "Surah al-Falaq (113)"),
            Quran.FALAQ_AR,
            "Qul aʿudhu bi-rabbil-falaq. Min sharri ma khalaq. Wa min sharri ghasiqin idha waqab. Wa min sharrin-naffathati fil-ʿuqad. Wa min sharri hasidin idha hasad.",
            T(
                "Sag: Ich nehme Zuflucht beim Herrn des Tagesanbruchs vor dem Übel dessen, was Er erschaffen hat, vor dem Übel der Dunkelheit, wenn sie hereinbricht, vor dem Übel der Knotenanbläserinnen und vor dem Übel eines Neiders, wenn er neidet.",
                "De ki: Yarattığı şeylerin şerrinden, karanlığı çöktüğü zaman gecenin şerrinden, düğümlere üfleyenlerin şerrinden ve haset ettiği zaman hasetçinin şerrinden, sabahın Rabbine sığınırım.",
                "Say: I seek refuge in the Lord of daybreak from the evil of what He created, from the evil of darkness when it settles, from the evil of those who blow on knots, and from the evil of an envier when he envies."
            ),
            "Koran 113:1–5"
        ),
        q(
            T("Sure an-Nas (114)", "Nâs Suresi (114)", "Surah an-Nas (114)"),
            Quran.NAS_AR,
            "Qul aʿudhu bi-rabbin-nas. Malikin-nas. Ilahin-nas. Min sharril-waswasil-khannas. Alladhi yuwaswisu fi sudurin-nas. Minal-jinnati wan-nas.",
            T(
                "Sag: Ich nehme Zuflucht beim Herrn der Menschen, dem König der Menschen, dem Gott der Menschen, vor dem Übel des Einflüsterers, der sich zurückzieht, der in die Brüste der Menschen einflüstert – von den Dschinn und den Menschen.",
                "De ki: İnsanların Rabbine, insanların Melik'ine, insanların İlahına sığınırım; sinsi vesvesecinin şerrinden; o ki insanların göğüslerine vesvese verir, gerek cinlerden gerek insanlardan.",
                "Say: I seek refuge in the Lord of mankind, the King of mankind, the God of mankind, from the evil of the retreating whisperer who whispers into the hearts of mankind – from among jinn and mankind."
            ),
            "Koran 114:1–6"
        ),
        q(
            T("Sure al-ʿAsr (103)", "Asr Suresi (103)", "Surah al-'Asr (103)"),
            "وَالْعَصْرِ ﴿١﴾ إِنَّ الْإِنْسَانَ لَفِي خُسْرٍ ﴿٢﴾ إِلَّا الَّذِينَ آمَنُوا وَعَمِلُوا الصَّالِحَاتِ وَتَوَاصَوْا بِالْحَقِّ وَتَوَاصَوْا بِالصَّبْرِ ﴿٣﴾",
            "Wal-ʿasr. Innal-insana lafi khusr. Illal-ladhina amanu wa ʿamilus-salihati wa tawasaw bil-haqqi wa tawasaw bis-sabr.",
            T(
                "Bei der Zeit! Der Mensch ist wahrlich im Verlust, außer denen, die glauben, gute Werke tun, einander die Wahrheit ans Herz legen und einander die Geduld ans Herz legen.",
                "Asra yemin olsun ki insan gerçekten ziyandadır. Ancak iman edip salih ameller işleyenler, birbirlerine hakkı tavsiye edenler ve sabrı tavsiye edenler müstesnadır.",
                "By time! Indeed, mankind is in loss, except those who believe, do righteous deeds, and urge one another to truth and urge one another to patience."
            ),
            "Koran 103:1–3"
        ),
        q(
            T("Sure al-Kawthar (108)", "Kevser Suresi (108)", "Surah al-Kawthar (108)"),
            "إِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ ﴿١﴾ فَصَلِّ لِرَبِّكَ وَانْحَرْ ﴿٢﴾ إِنَّ شَانِئَكَ هُوَ الْأَبْتَرُ ﴿٣﴾",
            "Inna aʿtaynakal-kawthar. Fa-salli li-rabbika wanhar. Inna shani'aka huwal-abtar.",
            T(
                "Wir haben dir wahrlich die Fülle (al-Kauthar) gegeben. So bete zu deinem Herrn und opfere. Wahrlich, dein Hasser ist es, der abgeschnitten ist.",
                "Şüphesiz biz sana Kevser'i verdik. Öyleyse Rabbin için namaz kıl ve kurban kes. Asıl soyu kesik olan, sana kin besleyendir.",
                "Indeed, We have granted you al-Kawthar. So pray to your Lord and sacrifice. Indeed, it is your enemy who is cut off."
            ),
            "Koran 108:1–3"
        ),
        q(
            T("Sure an-Nasr (110)", "Nasr Suresi (110)", "Surah an-Nasr (110)"),
            "إِذَا جَاءَ نَصْرُ اللَّهِ وَالْفَتْحُ ﴿١﴾ وَرَأَيْتَ النَّاسَ يَدْخُلُونَ فِي دِينِ اللَّهِ أَفْوَاجًا ﴿٢﴾ فَسَبِّحْ بِحَمْدِ رَبِّكَ وَاسْتَغْفِرْهُ ۚ إِنَّهُ كَانَ تَوَّابًا ﴿٣﴾",
            "Idha ja'a nasrullahi wal-fath. Wa ra'aytan-nasa yadkhuluna fi dinillahi afwaja. Fa-sabbih bi-hamdi rabbika wastaghfirh, innahu kana tawwaba.",
            T(
                "Wenn die Hilfe Allahs und der Sieg kommen und du die Menschen in Scharen in Allahs Religion eintreten siehst, dann preise deinen Herrn mit Lob und bitte Ihn um Vergebung. Er nimmt die Reue stets an.",
                "Allah'ın yardımı ve fetih geldiğinde ve insanların bölük bölük Allah'ın dinine girdiğini gördüğünde, Rabbini hamd ile tesbih et ve O'ndan bağışlanma dile. Çünkü O, tövbeleri çokça kabul edendir.",
                "When the help of Allah and the victory come, and you see the people entering Allah's religion in multitudes, then glorify the praise of your Lord and ask His forgiveness. Indeed, He is ever Accepting of repentance."
            ),
            "Koran 110:1–3"
        ),
        q(
            T("Sure al-Kafirun (109)", "Kâfirûn Suresi (109)", "Surah al-Kafirun (109)"),
            "قُلْ يَا أَيُّهَا الْكَافِرُونَ ﴿١﴾ لَا أَعْبُدُ مَا تَعْبُدُونَ ﴿٢﴾ وَلَا أَنْتُمْ عَابِدُونَ مَا أَعْبُدُ ﴿٣﴾ وَلَا أَنَا عَابِدٌ مَا عَبَدْتُمْ ﴿٤﴾ وَلَا أَنْتُمْ عَابِدُونَ مَا أَعْبُدُ ﴿٥﴾ لَكُمْ دِينُكُمْ وَلِيَ دِينِ ﴿٦﴾",
            "Qul ya ayyuhal-kafirun. La aʿbudu ma taʿbudun. Wa la antum ʿabiduna ma aʿbud. Wa la ana ʿabidum-ma ʿabadtum. Wa la antum ʿabiduna ma aʿbud. Lakum dinukum wa liya din.",
            T(
                "Sag: O ihr Ungläubigen, ich diene nicht dem, dem ihr dient, und ihr dient nicht Dem, Dem ich diene. Und ich werde nicht dem dienen, dem ihr gedient habt, und ihr werdet nicht Dem dienen, Dem ich diene. Euch eure Religion und mir meine Religion.",
                "De ki: Ey kâfirler! Ben sizin taptıklarınıza tapmam. Siz de benim taptığıma tapmazsınız. Ben sizin taptıklarınıza tapacak değilim. Siz de benim taptığıma tapacak değilsiniz. Sizin dininiz size, benim dinim banadır.",
                "Say: O disbelievers, I do not worship what you worship, nor do you worship what I worship. Nor will I worship what you worship, nor will you worship what I worship. For you is your religion, and for me is mine."
            ),
            "Koran 109:1–6"
        ),
        q(
            T("Sure Quraisch (106)", "Kureyş Suresi (106)", "Surah Quraysh (106)"),
            "لِإِيلَافِ قُرَيْشٍ ﴿١﴾ إِيلَافِهِمْ رِحْلَةَ الشِّتَاءِ وَالصَّيْفِ ﴿٢﴾ فَلْيَعْبُدُوا رَبَّ هَٰذَا الْبَيْتِ ﴿٣﴾ الَّذِي أَطْعَمَهُمْ مِنْ جُوعٍ وَآمَنَهُمْ مِنْ خَوْفٍ ﴿٤﴾",
            "Li-ilafi Quraysh. Ilafihim rihlatash-shita'i was-sayf. Fal-yaʿbudu rabba hadhal-bayt. Alladhi atʿamahum min juʿin wa amanahum min khawf.",
            T(
                "Für die Vertrautheit der Quraisch, ihre Vertrautheit mit der Reise im Winter und im Sommer: So sollen sie dem Herrn dieses Hauses dienen, Der ihnen Speise gegeben hat gegen Hunger und Sicherheit gegen Angst.",
                "Kureyş'in alışkanlığı, kış ve yaz yolculuklarındaki alışkanlıkları için; onlar da bu Beyt'in Rabbine kulluk etsinler. O ki onları açlıktan doyurdu ve korkudan emin kıldı.",
                "For the security of Quraysh, their security in the caravans of winter and summer: let them worship the Lord of this House, who fed them against hunger and made them safe from fear."
            ),
            "Koran 106:1–4"
        ),
        q(
            T("Sure asch-Scharh (94)", "İnşirah Suresi (94)", "Surah ash-Sharh (94)"),
            "أَلَمْ نَشْرَحْ لَكَ صَدْرَكَ ﴿١﴾ وَوَضَعْنَا عَنْكَ وِزْرَكَ ﴿٢﴾ الَّذِي أَنْقَضَ ظَهْرَكَ ﴿٣﴾ وَرَفَعْنَا لَكَ ذِكْرَكَ ﴿٤﴾ فَإِنَّ مَعَ الْعُسْرِ يُسْرًا ﴿٥﴾ إِنَّ مَعَ الْعُسْرِ يُسْرًا ﴿٦﴾ فَإِذَا فَرَغْتَ فَانْصَبْ ﴿٧﴾ وَإِلَىٰ رَبِّكَ فَارْغَبْ ﴿٨﴾",
            "A-lam nashrah laka sadrak. Wa wadaʿna ʿanka wizrak. Alladhi anqada zahrak. Wa rafaʿna laka dhikrak. Fa-inna maʿal-ʿusri yusra. Inna maʿal-ʿusri yusra. Fa-idha faraghta fansab. Wa ila rabbika farghab.",
            T(
                "Haben Wir dir nicht deine Brust geweitet und dir deine Last abgenommen, die deinen Rücken niederdrückte, und dein Ansehen erhöht? Wahrlich, mit der Erschwernis kommt Erleichterung. Wahrlich, mit der Erschwernis kommt Erleichterung. Wenn du fertig bist, so mühe dich weiter, und deinem Herrn wende dich mit Sehnsucht zu.",
                "Senin göğsünü açıp genişletmedik mi? Belini büken yükünü üzerinden kaldırmadık mı? Senin şanını yüceltmedik mi? Şüphesiz zorlukla beraber bir kolaylık vardır. Gerçekten zorlukla beraber bir kolaylık vardır. Bir işi bitirince diğerine koyul ve yalnız Rabbine yönel.",
                "Did We not expand your breast for you and remove from you your burden which weighed upon your back, and raise high your mention? Indeed, with hardship comes ease. Indeed, with hardship comes ease. So when you have finished, strive on, and to your Lord turn with longing."
            ),
            "Koran 94:1–8"
        ),
        q(
            T("Die letzte Ayah von al-Baqara", "Bakara Suresi'nin son ayeti", "The last verse of al-Baqarah"),
            "لَا يُكَلِّفُ اللَّهُ نَفْسًا إِلَّا وُسْعَهَا ۚ لَهَا مَا كَسَبَتْ وَعَلَيْهَا مَا اكْتَسَبَتْ ۗ رَبَّنَا لَا تُؤَاخِذْنَا إِنْ نَسِينَا أَوْ أَخْطَأْنَا ۚ رَبَّنَا وَلَا تَحْمِلْ عَلَيْنَا إِصْرًا كَمَا حَمَلْتَهُ عَلَى الَّذِينَ مِنْ قَبْلِنَا ۚ رَبَّنَا وَلَا تُحَمِّلْنَا مَا لَا طَاقَةَ لَنَا بِهِ ۖ وَاعْفُ عَنَّا وَاغْفِرْ لَنَا وَارْحَمْنَا ۚ أَنْتَ مَوْلَانَا فَانْصُرْنَا عَلَى الْقَوْمِ الْكَافِرِينَ",
            "La yukallifullahu nafsan illa wusʿaha. Laha ma kasabat wa ʿalayha maktasabat. Rabbana la tu'akhidhna in nasina aw akhta'na. Rabbana wa la tahmil ʿalayna isran kama hamaltahu ʿalal-ladhina min qablina. Rabbana wa la tuhammilna ma la taqata lana bih. Waʿfu ʿanna waghfir lana warhamna. Anta mawlana fansurna ʿalal-qawmil-kafirin.",
            T(
                "Allah erlegt keiner Seele mehr auf, als sie zu leisten vermag. Ihr kommt zugute, was sie (an Gutem) verdient hat, und gegen sie ist, was sie (an Bösem) erworben hat. Unser Herr, belange uns nicht, wenn wir vergessen oder Fehler machen. Unser Herr, lege uns keine Last auf, wie Du sie denen vor uns auferlegt hast. Unser Herr, bürde uns nichts auf, wozu wir keine Kraft haben. Verzeih uns, vergib uns und erbarme Dich unser. Du bist unser Schutzherr, so hilf uns gegen das ungläubige Volk.",
                "Allah hiç kimseye gücünün yetmeyeceği bir yük yüklemez. Herkesin kazandığı iyilik kendi lehine, işlediği kötülük kendi aleyhinedir. Rabbimiz! Unutur ya da yanılırsak bizi sorumlu tutma. Rabbimiz! Bizden öncekilere yüklediğin gibi bize ağır yük yükleme. Rabbimiz! Bize gücümüzün yetmediği şeyleri yükleme. Bizi affet, bizi bağışla, bize merhamet et. Sen bizim Mevlâmızsın; kâfirler topluluğuna karşı bize yardım et.",
                "Allah does not burden a soul beyond its capacity. It will have the good it has earned and bear the evil it has earned. Our Lord, do not take us to task if we forget or err. Our Lord, do not lay upon us a burden like that You laid upon those before us. Our Lord, do not burden us with what we cannot bear. Pardon us, forgive us and have mercy on us. You are our Protector, so grant us victory over the disbelieving people."
            ),
            "Koran 2:286"
        ),
        q(
            T("Ruhe der Herzen", "Kalplerin huzuru", "Rest for the hearts"),
            "الَّذِينَ آمَنُوا وَتَطْمَئِنُّ قُلُوبُهُمْ بِذِكْرِ اللَّهِ ۗ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
            "Alladhina amanu wa tatma'innu qulubuhum bi-dhikrillah. Ala bi-dhikrillahi tatma'innul-qulub.",
            T(
                "Diejenigen, die glauben und deren Herzen im Gedenken Allahs Ruhe finden – wahrlich, im Gedenken Allahs finden die Herzen Ruhe.",
                "Onlar iman edenler ve kalpleri Allah'ı anmakla huzur bulanlardır. Bilesiniz ki kalpler ancak Allah'ı anmakla huzur bulur.",
                "Those who believe and whose hearts find rest in the remembrance of Allah – verily, in the remembrance of Allah do hearts find rest."
            ),
            "Koran 13:28"
        ),
        q(
            T("Ich bin nahe", "Ben yakınım", "I am near"),
            "وَإِذَا سَأَلَكَ عِبَادِي عَنِّي فَإِنِّي قَرِيبٌ ۖ أُجِيبُ دَعْوَةَ الدَّاعِ إِذَا دَعَانِ ۖ فَلْيَسْتَجِيبُوا لِي وَلْيُؤْمِنُوا بِي لَعَلَّهُمْ يَرْشُدُونَ",
            "Wa idha sa'alaka ʿibadi ʿanni fa-inni qarib. Ujibu daʿwatad-daʿi idha daʿan. Fal-yastajibu li wal-yu'minu bi laʿallahum yarshudun.",
            T(
                "Und wenn dich Meine Diener nach Mir fragen, so bin Ich nahe. Ich erhöre den Ruf des Bittenden, wenn er Mich anruft. So sollen sie auf Mich hören und an Mich glauben, damit sie den rechten Weg finden.",
                "Kullarım sana Beni sorarlarsa, şüphesiz Ben çok yakınım. Bana dua edenin duasına karşılık veririm. O hâlde onlar da Benim davetime uysunlar ve Bana iman etsinler ki doğru yolu bulsunlar.",
                "And when My servants ask you about Me – indeed I am near. I respond to the call of the caller when he calls upon Me. So let them respond to Me and believe in Me, that they may be rightly guided."
            ),
            "Koran 2:186"
        ),
        q(
            T("Verzweifelt nicht an Allahs Barmherzigkeit", "Allah'ın rahmetinden ümit kesmeyin", "Do not despair of Allah's mercy"),
            "قُلْ يَا عِبَادِيَ الَّذِينَ أَسْرَفُوا عَلَىٰ أَنْفُسِهِمْ لَا تَقْنَطُوا مِنْ رَحْمَةِ اللَّهِ ۚ إِنَّ اللَّهَ يَغْفِرُ الذُّنُوبَ جَمِيعًا ۚ إِنَّهُ هُوَ الْغَفُورُ الرَّحِيمُ",
            "Qul ya ʿibadiyal-ladhina asrafu ʿala anfusihim la taqnatu min rahmatillah. Innallaha yaghfirudh-dhunuba jamiʿa. Innahu huwal-ghafurur-rahim.",
            T(
                "Sag: O Meine Diener, die ihr euch selbst gegenüber maßlos gewesen seid, verzweifelt nicht an Allahs Barmherzigkeit. Gewiss, Allah vergibt alle Sünden. Er ist ja der Allvergebende, der Barmherzige.",
                "De ki: Ey kendi aleyhlerine haddi aşan kullarım! Allah'ın rahmetinden ümidinizi kesmeyin. Şüphesiz Allah bütün günahları bağışlar. Çünkü O, çok bağışlayan, çok merhamet edendir.",
                "Say: O My servants who have transgressed against themselves, do not despair of the mercy of Allah. Indeed, Allah forgives all sins. He is the Forgiving, the Merciful."
            ),
            "Koran 39:53"
        ),
        q(
            T("Gedenkt Meiner", "Beni anın", "Remember Me"),
            "فَاذْكُرُونِي أَذْكُرْكُمْ وَاشْكُرُوا لِي وَلَا تَكْفُرُونِ",
            "Fadhkuruni adhkurkum washkuru li wa la takfurun.",
            T(
                "So gedenkt Meiner, dann gedenke Ich euer. Und seid Mir dankbar und seid nicht undankbar.",
                "Öyleyse siz Beni anın ki Ben de sizi anayım. Bana şükredin, nankörlük etmeyin.",
                "So remember Me; I will remember you. And be grateful to Me and do not deny Me."
            ),
            "Koran 2:152"
        ),
    )

    val hadith: List<DailyItem> = listOf(
        h(
            "إِنَّمَا الْأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى",
            T(
                "Die Taten werden nur nach den Absichten beurteilt, und jedem Menschen steht nur das zu, was er beabsichtigt hat.",
                "Ameller ancak niyetlere göredir. Herkese ancak niyet ettiği şey vardır.",
                "Actions are only by intentions, and every person will have only what he intended."
            ),
            "Bukhari 1, Muslim 1907"
        ),
        h(
            "لَا يُؤْمِنُ أَحَدُكُمْ حَتَّى يُحِبَّ لِأَخِيهِ مَا يُحِبُّ لِنَفْسِهِ",
            T(
                "Keiner von euch ist wirklich gläubig, bis er für seinen Bruder liebt, was er für sich selbst liebt.",
                "Sizden biriniz kendisi için istediğini kardeşi için de istemedikçe gerçek anlamda iman etmiş olmaz.",
                "None of you truly believes until he loves for his brother what he loves for himself."
            ),
            "Bukhari 13, Muslim 45"
        ),
        h(
            "مَنْ كَانَ يُؤْمِنُ بِاللَّهِ وَالْيَوْمِ الْآخِرِ فَلْيَقُلْ خَيْرًا أَوْ لِيَصْمُتْ",
            T(
                "Wer an Allah und den Jüngsten Tag glaubt, soll Gutes sprechen oder schweigen.",
                "Allah'a ve ahiret gününe iman eden ya hayır söylesin ya da sussun.",
                "Whoever believes in Allah and the Last Day, let him speak good or remain silent."
            ),
            "Bukhari 6018, Muslim 47"
        ),
        h(
            "الطُّهُورُ شَطْرُ الْإِيمَانِ",
            T("Die Reinheit ist die Hälfte des Glaubens.", "Temizlik imanın yarısıdır.", "Purity is half of faith."),
            "Muslim 223"
        ),
        h(
            "أَحَبُّ الْأَعْمَالِ إِلَى اللَّهِ أَدْوَمُهَا وَإِنْ قَلَّ",
            T(
                "Die Allah liebsten Taten sind die beständigsten, auch wenn sie wenige sind.",
                "Allah'a en sevimli gelen amel, az da olsa devamlı olanıdır.",
                "The deeds most beloved to Allah are the most consistent, even if they are few."
            ),
            "Bukhari 6464, Muslim 783"
        ),
        h(
            "الْكَلِمَةُ الطَّيِّبَةُ صَدَقَةٌ",
            T("Das gute Wort ist eine Sadaqa.", "Güzel söz sadakadır.", "A good word is charity."),
            "Bukhari 2989, Muslim 1009"
        ),
        h(
            "تَبَسُّمُكَ فِي وَجْهِ أَخِيكَ لَكَ صَدَقَةٌ",
            T(
                "Dein Lächeln im Gesicht deines Bruders ist für dich eine Sadaqa.",
                "Kardeşinin yüzüne gülümsemen senin için bir sadakadır.",
                "Your smile in the face of your brother is charity for you."
            ),
            "Tirmidhi 1956"
        ),
        h(
            "لَيْسَ الشَّدِيدُ بِالصُّرَعَةِ، إِنَّمَا الشَّدِيدُ الَّذِي يَمْلِكُ نَفْسَهُ عِنْدَ الْغَضَبِ",
            T(
                "Der Starke ist nicht der, der andere niederringt; stark ist vielmehr, wer sich im Zorn beherrscht.",
                "Güçlü kimse, güreşte rakibini yenen değildir; asıl güçlü, öfkelendiğinde kendine hâkim olandır.",
                "The strong one is not the one who overcomes others in wrestling; the strong one is he who controls himself when angry."
            ),
            "Bukhari 6114, Muslim 2609"
        ),
        h(
            "كَلِمَتَانِ خَفِيفَتَانِ عَلَى اللِّسَانِ، ثَقِيلَتَانِ فِي الْمِيزَانِ، حَبِيبَتَانِ إِلَى الرَّحْمَنِ: سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، سُبْحَانَ اللَّهِ الْعَظِيمِ",
            T(
                "Zwei Worte sind leicht auf der Zunge, schwer auf der Waage und dem Allerbarmer lieb: Subhanallahi wa bihamdihi, subhanallahil-ʿazim.",
                "İki kelime vardır ki dile hafif, mizanda ağır ve Rahman'a sevimlidir: Sübhânallâhi ve bihamdihî, sübhânallâhi'l-azîm.",
                "Two words are light on the tongue, heavy on the scale and beloved to the Most Merciful: Subhanallahi wa bihamdihi, subhanallahil-'azim."
            ),
            "Bukhari 6406, Muslim 2694"
        ),
        h(
            "مِنْ حُسْنِ إِسْلَامِ الْمَرْءِ تَرْكُهُ مَا لَا يَعْنِيهِ",
            T(
                "Zum guten Islam eines Menschen gehört, dass er lässt, was ihn nichts angeht.",
                "Kişinin kendisini ilgilendirmeyen şeyleri terk etmesi, Müslümanlığının güzelliğindendir.",
                "Part of the excellence of a person's Islam is leaving what does not concern him."
            ),
            "Tirmidhi 2317"
        ),
        h(
            "الدِّينُ النَّصِيحَةُ",
            T(
                "Die Religion ist aufrichtiger Rat (Nasiha).",
                "Din nasihattir (samimiyettir).",
                "The religion is sincere counsel (nasihah)."
            ),
            "Muslim 55"
        ),
        h(
            "اتَّقِ اللَّهَ حَيْثُمَا كُنْتَ، وَأَتْبِعِ السَّيِّئَةَ الْحَسَنَةَ تَمْحُهَا، وَخَالِقِ النَّاسَ بِخُلُقٍ حَسَنٍ",
            T(
                "Fürchte Allah, wo immer du bist. Lass auf eine schlechte Tat eine gute folgen, die sie auslöscht, und begegne den Menschen mit gutem Charakter.",
                "Nerede olursan ol Allah'tan kork. Kötülüğün ardından onu silecek bir iyilik yap ve insanlara güzel ahlakla davran.",
                "Be mindful of Allah wherever you are. Follow a bad deed with a good one to erase it, and treat people with good character."
            ),
            "Tirmidhi 1987"
        ),
        h(
            "الْمُسْلِمُ مَنْ سَلِمَ الْمُسْلِمُونَ مِنْ لِسَانِهِ وَيَدِهِ",
            T(
                "Der Muslim ist derjenige, vor dessen Zunge und Hand die Muslime sicher sind.",
                "Müslüman, diğer Müslümanların elinden ve dilinden emin olduğu kimsedir.",
                "The Muslim is the one from whose tongue and hand the Muslims are safe."
            ),
            "Bukhari 10, Muslim 40"
        ),
        h(
            "أَقْرَبُ مَا يَكُونُ الْعَبْدُ مِنْ رَبِّهِ وَهُوَ سَاجِدٌ، فَأَكْثِرُوا الدُّعَاءَ",
            T(
                "Am nächsten ist der Diener seinem Herrn, wenn er sich niederwirft – so macht dabei viele Bittgebete.",
                "Kulun Rabbine en yakın olduğu an secdede olduğu andır; öyleyse secdede çokça dua edin.",
                "The closest a servant is to his Lord is when he is in prostration, so increase your supplications."
            ),
            "Muslim 482"
        ),
        h(
            "الصَّلَوَاتُ الْخَمْسُ، وَالْجُمُعَةُ إِلَى الْجُمُعَةِ، كَفَّارَةٌ لِمَا بَيْنَهُنَّ، مَا لَمْ تُغْشَ الْكَبَائِرُ",
            T(
                "Die fünf Gebete und ein Freitagsgebet bis zum nächsten sind eine Sühne für das, was zwischen ihnen liegt, solange keine großen Sünden begangen werden.",
                "Beş vakit namaz ve bir cuma diğer cumaya kadar, büyük günahlardan kaçınıldığı sürece aralarında işlenen günahlara kefarettir.",
                "The five prayers and from one Jumu'ah to the next are an expiation for what is between them, as long as major sins are avoided."
            ),
            "Muslim 233"
        ),
        h(
            "مَنْ سَلَكَ طَرِيقًا يَلْتَمِسُ فِيهِ عِلْمًا، سَهَّلَ اللَّهُ لَهُ بِهِ طَرِيقًا إِلَى الْجَنَّةِ",
            T(
                "Wer einen Weg beschreitet, um Wissen zu suchen, dem erleichtert Allah dadurch einen Weg ins Paradies.",
                "Kim ilim öğrenmek için bir yola girerse, Allah ona bu sebeple cennete giden yolu kolaylaştırır.",
                "Whoever travels a path in search of knowledge, Allah makes easy for him a path to Paradise."
            ),
            "Muslim 2699"
        ),
        h(
            "إِنَّ اللَّهَ لَا يَنْظُرُ إِلَى صُوَرِكُمْ وَأَمْوَالِكُمْ، وَلَكِنْ يَنْظُرُ إِلَى قُلُوبِكُمْ وَأَعْمَالِكُمْ",
            T(
                "Allah schaut nicht auf euer Äußeres und euren Besitz, sondern auf eure Herzen und eure Taten.",
                "Allah sizin suretlerinize ve mallarınıza bakmaz; kalplerinize ve amellerinize bakar.",
                "Allah does not look at your appearance or your wealth, but He looks at your hearts and your deeds."
            ),
            "Muslim 2564"
        ),
        h(
            "خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْآنَ وَعَلَّمَهُ",
            T(
                "Der Beste unter euch ist, wer den Koran lernt und ihn lehrt.",
                "Sizin en hayırlınız Kur'an'ı öğrenen ve öğretendir.",
                "The best of you are those who learn the Qur'an and teach it."
            ),
            "Bukhari 5027"
        ),
        h(
            "الرَّاحِمُونَ يَرْحَمُهُمُ الرَّحْمَنُ، ارْحَمُوا مَنْ فِي الْأَرْضِ يَرْحَمْكُمْ مَنْ فِي السَّمَاءِ",
            T(
                "Den Barmherzigen erweist der Allerbarmer Barmherzigkeit. Seid barmherzig zu denen auf der Erde, dann wird Der im Himmel euch gegenüber barmherzig sein.",
                "Merhametli olanlara Rahman merhamet eder. Yeryüzündekilere merhamet edin ki gökteki de size merhamet etsin.",
                "The merciful are shown mercy by the Most Merciful. Be merciful to those on earth, and the One above the heavens will be merciful to you."
            ),
            "Tirmidhi 1924, Abu Dawud 4941"
        ),
        h(
            "يَسِّرُوا وَلَا تُعَسِّرُوا، وَبَشِّرُوا وَلَا تُنَفِّرُوا",
            T(
                "Macht es leicht und macht es nicht schwer; verkündet frohe Botschaft und schreckt nicht ab.",
                "Kolaylaştırın, zorlaştırmayın; müjdeleyin, nefret ettirmeyin.",
                "Make things easy and do not make them difficult; give glad tidings and do not drive people away."
            ),
            "Bukhari 69, Muslim 1734"
        ),
        h(
            "مَا نَقَصَتْ صَدَقَةٌ مِنْ مَالٍ",
            T(
                "Keine Sadaqa hat jemals Vermögen vermindert.",
                "Sadaka maldan hiçbir şey eksiltmez.",
                "Charity never decreases wealth."
            ),
            "Muslim 2588"
        ),
        h(
            "الْمُؤْمِنُ الْقَوِيُّ خَيْرٌ وَأَحَبُّ إِلَى اللَّهِ مِنَ الْمُؤْمِنِ الضَّعِيفِ، وَفِي كُلٍّ خَيْرٌ",
            T(
                "Der starke Gläubige ist besser und Allah lieber als der schwache Gläubige – und in beiden ist Gutes.",
                "Güçlü mümin, zayıf müminden daha hayırlı ve Allah'a daha sevimlidir; ama her ikisinde de hayır vardır.",
                "The strong believer is better and more beloved to Allah than the weak believer, and in both there is good."
            ),
            "Muslim 2664"
        ),
        h(
            "عَجَبًا لِأَمْرِ الْمُؤْمِنِ، إِنَّ أَمْرَهُ كُلَّهُ خَيْرٌ",
            T(
                "Wie wunderbar ist die Angelegenheit des Gläubigen: Alles an ihr ist gut für ihn. Trifft ihn Freude, ist er dankbar; trifft ihn Leid, ist er geduldig – und beides ist gut für ihn.",
                "Müminin durumu ne hoştur! Her hâli kendisi için hayırdır. Sevindirici bir şey olursa şükreder, sıkıntı gelirse sabreder; ikisi de onun için hayırdır.",
                "How wonderful is the affair of the believer: all of it is good for him. If something good happens he is grateful; if hardship befalls him he is patient – and both are good for him."
            ),
            "Muslim 2999"
        ),
        h(
            "لَا تَحْقِرَنَّ مِنَ الْمَعْرُوفِ شَيْئًا، وَلَوْ أَنْ تَلْقَى أَخَاكَ بِوَجْهٍ طَلْقٍ",
            T(
                "Unterschätze keine gute Tat, und sei es nur, dass du deinem Bruder mit freundlichem Gesicht begegnest.",
                "İyilikten hiçbir şeyi küçümseme; kardeşini güler yüzle karşılaman bile olsa.",
                "Do not belittle any good deed, even meeting your brother with a cheerful face."
            ),
            "Muslim 2626"
        ),
    )

    /**
     * Liefert den Impuls für den angegebenen Zeitpunkt.
     * @param fajr, maghrib die Gebetszeiten des aktuellen Tages
     * @param allowed Filter (z. B. deaktivierte Quellen)
     * @return Paar aus (istAbend, Impuls oder null, wenn alle Quellen deaktiviert sind)
     */
    fun current(
        now: ZonedDateTime, fajr: ZonedDateTime, maghrib: ZonedDateTime,
        allowed: (DailyItem) -> Boolean = { true },
        extra: List<DailyItem> = emptyList(),
    ): Pair<Boolean, DailyItem?> {
        val date: LocalDate
        val evening: Boolean
        when {
            now.isBefore(fajr) -> { date = now.toLocalDate().minusDays(1); evening = true }
            now.isBefore(maghrib) -> { date = now.toLocalDate(); evening = false }
            else -> { date = now.toLocalDate(); evening = true }
        }
        // Pro Tag ein Hadith und eine Koranstelle; morgens/abends wechselt täglich die Reihenfolge.
        val day = date.toEpochDay()
        val hadithFirst = Math.floorMod(day, 2L) == 0L
        val showHadith = if (evening) !hadithFirst else hadithFirst
        val h = hadith.filter(allowed) + extra // eigene Quellen zählen zum Hadith-/Textanteil
        val q = quran.filter(allowed)
        val pool = when {
            showHadith && h.isNotEmpty() -> h
            !showHadith && q.isNotEmpty() -> q
            h.isNotEmpty() -> h
            else -> q
        }
        if (pool.isEmpty()) return evening to null
        // bei nur einer Sorte morgens und abends unterschiedliche Einträge
        val idx = if (pool === h && !showHadith || pool === q && showHadith) day * 2 + 1 else day
        return evening to pool[Math.floorMod(idx, pool.size.toLong()).toInt()]
    }
}
