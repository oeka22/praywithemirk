package com.praywithemirk.app

import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField

enum class Lang(val id: String, val label: String) {
    DE("de", "Deutsch"), TR("tr", "Türkçe"), EN("en", "English");

    companion object {
        fun byId(id: String?): Lang = entries.firstOrNull { it.id == id } ?: DE
    }
}

/** Ein Text in drei Sprachen. */
class T(val de: String, val tr: String, val en: String) {
    operator fun get(l: Lang): String = when (l) {
        Lang.DE -> de
        Lang.TR -> tr
        Lang.EN -> en
    }
}

/** Oberflächentexte. */
object S {
    val prayerNames = arrayOf(
        T("Fajr", "İmsak", "Fajr"),
        T("Sonnenaufgang", "Güneş", "Sunrise"),
        T("Dhuhr", "Öğle", "Dhuhr"),
        T("Asr", "İkindi", "Asr"),
        T("Maghrib", "Akşam", "Maghrib"),
        T("Isha", "Yatsı", "Isha"),
    )
    val prayerArabic = arrayOf("الفجر", "الشروق", "الظهر", "العصر", "المغرب", "العشاء")

    val nextPrayer = T("Nächstes Gebet", "Sıradaki vakit", "Next prayer")
    val remaining = T("verbleibend", "kaldı", "remaining")
    val morningImpulse = T("Impuls am Morgen", "Sabah tefekkürü", "Morning reflection")
    val eveningImpulse = T("Impuls am Abend", "Akşam tefekkürü", "Evening reflection")
    val quran = T("Koran", "Kur'an", "Qur'an")
    val hadith = T("Hadith", "Hadis", "Hadith")
    val openAdhkar = T("Adhkar & Sunnah zum Gebet", "Namaz zikirleri ve sünnetler", "Adhkar & Sunnah for this prayer")
    val settings = T("Einstellungen", "Ayarlar", "Settings")
    val noLocation = T(
        "Kein Standort gesetzt – tippe hier, um ihn festzulegen.",
        "Konum ayarlanmadı – ayarlamak için dokun.",
        "No location set – tap here to set it."
    )
    val timeFor = T("Zeit für %s", "%s vakti", "Time for %s")
    val adhanPlaying = T("Gebetsruf läuft", "Ezan okunuyor", "Adhan playing")
    val stop = T("Stopp", "Durdur", "Stop")
    val tapForAdhkar = T(
        "Tippen für Adhkar und Sunnah-Gebete",
        "Zikirler ve sünnetler için dokun",
        "Tap for adhkar and sunnah prayers"
    )
    val reminderIn = T("%s in %d Minuten", "%s vaktine %d dakika", "%s in %d minutes")
    val adhanOn = T("Gebetsruf an", "Ezan açık", "Adhan on")
    val adhanOff = T("Gebetsruf aus", "Ezan kapalı", "Adhan off")

    // Adhkar-Bildschirm
    val atAdhan = T("Beim Gebetsruf", "Ezan okunurken", "During the adhan")
    val sunnahPrayers = T("Sunnah-Gebete", "Sünnet namazları", "Sunnah prayers")
    val afterSalah = T("Nach dem Gebet", "Namazdan sonra", "After the prayer")
    val tapToCount = T("Tippen zum Zählen", "Saymak için dokun", "Tap to count")
    val reset = T("Zurücksetzen", "Sıfırla", "Reset")
    val source = T("Quelle", "Kaynak", "Source")

    // Einstellungen
    val language = T("Sprache", "Dil", "Language")
    val showTranslit = T("Umschrift anzeigen", "Okunuşu göster", "Show transliteration")
    val location = T("Standort", "Konum", "Location")
    val useGps = T("Aktuellen Standort verwenden", "Mevcut konumu kullan", "Use current location")
    val searchCity = T("Stadt suchen", "Şehir ara", "Search city")
    val cityHint = T("z. B. Köln", "örn. Köln", "e.g. Cologne")
    val manualCoords = T("Koordinaten (Breite, Länge)", "Koordinatlar (enlem, boylam)", "Coordinates (lat, lng)")
    val save = T("Übernehmen", "Uygula", "Apply")
    val method = T("Berechnungsmethode", "Hesaplama yöntemi", "Calculation method")
    val asrMadhhab = T("Asr-Berechnung", "İkindi hesabı", "Asr calculation")
    val asrStandard = T("Standard (Shafi'i, Maliki, Hanbali)", "Standart (Şafii, Maliki, Hanbeli)", "Standard (Shafi'i, Maliki, Hanbali)")
    val asrHanafi = T("Hanafi", "Hanefi (Asr-ı sani)", "Hanafi")
    val highLat = T("Regel für hohe Breiten", "Yüksek enlem kuralı", "High-latitude rule")
    val highLatNames = arrayOf(
        T("Winkelbasiert (empfohlen)", "Açıya dayalı (önerilen)", "Angle-based (recommended)"),
        T("Nachtmitte", "Gecenin ortası", "Middle of the night"),
        T("Ein Siebtel der Nacht", "Gecenin yedide biri", "One seventh of the night"),
        T("Keine", "Yok", "None"),
    )
    val offsets = T("Korrektur in Minuten", "Dakika düzeltmesi", "Adjustment in minutes")
    val adhanSection = T("Gebetsruf", "Ezan", "Adhan")
    val adhanFor = T("Gebetsruf bei", "Ezan okunacak vakitler", "Adhan for")
    val adhanSound = T("Gebetsruf-Audiodatei", "Ezan ses dosyası", "Adhan audio file")
    val fajrSound = T("Eigene Datei für Fajr", "İmsak için ayrı dosya", "Separate file for Fajr")
    val chooseFile = T("Datei wählen", "Dosya seç", "Choose file")
    val defaultSound = T("Standard-Weckton des Geräts", "Cihazın varsayılan alarm sesi", "Device default alarm sound")
    val sameAsOthers = T("Wie bei den anderen Gebeten", "Diğer vakitlerle aynı", "Same as other prayers")
    val test = T("Testen", "Dene", "Test")
    val reminderBefore = T("Vorab-Erinnerung (Minuten, 0 = aus)", "Önceden hatırlatma (dakika, 0 = kapalı)", "Reminder before (minutes, 0 = off)")
    val display = T("Anzeige", "Görünüm", "Display")
    val keepScreenOn = T("Bildschirm anlassen (Wanduhr-Modus)", "Ekranı açık tut (duvar saati)", "Keep screen on (wall clock mode)")
    val hijriOffset = T("Hijri-Datum Korrektur (Tage)", "Hicri tarih düzeltmesi (gün)", "Hijri date adjustment (days)")
    val permissions = T("Berechtigungen", "İzinler", "Permissions")
    val exactAlarmInfo = T(
        "Für pünktliche Gebetsrufe die Berechtigung „Wecker & Erinnerungen“ erlauben.",
        "Ezanın tam vaktinde okunması için „Alarmlar ve hatırlatıcılar“ iznini verin.",
        "Allow “Alarms & reminders” so the adhan plays exactly on time."
    )
    val openSettings = T("Erlauben", "İzin ver", "Allow")
    val batteryInfo = T(
        "Tipp: Akku-Optimierung für Pray with Emir K. deaktivieren, damit der Gebetsruf zuverlässig kommt.",
        "İpucu: Ezanın güvenilir okunması için Pray with Emir K. uygulamasında pil optimizasyonunu kapatın.",
        "Tip: disable battery optimisation for Pray with Emir K. so the adhan is reliable."
    )
    val locating = T("Standort wird ermittelt …", "Konum belirleniyor …", "Locating …")
    val locationFailed = T("Standort nicht gefunden.", "Konum bulunamadı.", "Location not found.")
    val locationSet = T("Standort gesetzt: %s", "Konum ayarlandı: %s", "Location set: %s")
    val nextAdhan = T("Nächster Gebetsruf: %s", "Sıradaki ezan: %s", "Next adhan: %s")
    val audioHint = T(
        "Wähle eine MP3-Datei eines Gebetsrufs von deinem Gerät. Ohne Datei wird der Standard-Weckton verwendet.",
        "Cihazınızdan bir ezan MP3 dosyası seçin. Dosya seçilmezse varsayılan alarm sesi çalar.",
        "Pick an adhan MP3 from your device. Without a file the default alarm sound is used."
    )

    // Neu: Designs, Quellen, Audio-Links
    val design = T("Design", "Tasarım", "Design")
    val sources = T("Quellen", "Kaynaklar", "Sources")
    val sourcesInfo = T(
        "Deaktivierte Quellen werden nicht mehr für Tagesimpulse und Adhkar verwendet. Ein Inhalt bleibt sichtbar, solange mindestens eine seiner Quellen aktiv ist.",
        "Kapatılan kaynaklar artık günlük tefekkür ve zikirler için kullanılmaz. Bir içerik, kaynaklarından en az biri açık olduğu sürece gösterilir.",
        "Disabled sources are no longer used for daily reflections and adhkar. An item stays visible as long as at least one of its sources is enabled."
    )
    val itemsCount = T("%d Inhalte", "%d içerik", "%d items")
    val allSourcesOff = T(
        "Alle Quellen für den Tagesimpuls sind deaktiviert.",
        "Günlük tefekkür için tüm kaynaklar kapalı.",
        "All sources for the daily reflection are disabled."
    )
    val defaultForAll = T("Standard für alle Gebete", "Tüm vakitler için varsayılan", "Default for all prayers")
    val perPrayer = T("Individuell je Gebet", "Vakte göre ayrı", "Individual per prayer")
    val useDefault = T("wie Standard", "varsayılan gibi", "same as default")
    val file = T("Datei", "Dosya", "File")
    val link = T("Link", "Bağlantı", "Link")
    val enterLink = T("Audio-Link eingeben", "Ses bağlantısı girin", "Enter audio link")
    val linkHint = T(
        "Direkter https-Link zu einer Audiodatei (z. B. …/adhan.mp3). Die Datei wird einmal heruntergeladen und offline abgespielt.",
        "Bir ses dosyasına doğrudan https bağlantısı (örn. …/ezan.mp3). Dosya bir kez indirilir ve çevrimdışı çalınır.",
        "Direct https link to an audio file (e.g. …/adhan.mp3). The file is downloaded once and played offline."
    )
    val downloading = T("Wird heruntergeladen …", "İndiriliyor …", "Downloading …")
    val downloadOk = T("Gebetsruf gespeichert", "Ezan kaydedildi", "Adhan saved")
    val downloadFailed = T(
        "Download fehlgeschlagen. Ist es ein direkter Link zu einer Audiodatei?",
        "İndirme başarısız. Bağlantı doğrudan bir ses dosyasına mı?",
        "Download failed. Is it a direct link to an audio file?"
    )
    val youtubeNotSupported = T(
        "YouTube-Links werden nicht unterstützt: YouTube erlaubt nicht, nur den Ton aus Videos abzuspielen. Bitte einen direkten Link zu einer MP3-Datei verwenden.",
        "YouTube bağlantıları desteklenmez: YouTube, videolardan yalnızca sesin çalınmasına izin vermez. Lütfen doğrudan bir MP3 bağlantısı kullanın.",
        "YouTube links are not supported: YouTube does not allow playing only the audio of videos. Please use a direct link to an MP3 file."
    )
    val httpsOnly = T("Bitte einen https-Link verwenden.", "Lütfen https bağlantısı kullanın.", "Please use an https link.")
    val cancel = T("Abbrechen", "İptal", "Cancel")
    val ok = T("OK", "Tamam", "OK")

    // Neu: Qibla, Anleitung, Anzeige, Vorschläge
    val qibla = T("Qibla-Kompass", "Kıble pusulası", "Qibla compass")
    val adhkarShort = T("Adhkar", "Zikirler", "Adhkar")
    val qiblaShort = T("Qibla", "Kıble", "Qibla")
    val qiblaInfo = T("Qibla: %d° · %,d km bis Mekka", "Kıble: %d° · Mekke'ye %,d km", "Qibla: %d° · %,d km to Makkah")
    val compassHint = T(
        "Handy flach halten und drehen, bis die Kaaba oben am Pfeil steht.",
        "Telefonu düz tut ve Kâbe oku üstteki işarete gelene kadar dön.",
        "Hold the phone flat and turn until the Kaaba points to the top marker."
    )
    val calibrate = T(
        "Kompass ungenau: Handy einige Male in Form einer liegenden 8 bewegen und von Metall/Magneten fernhalten.",
        "Pusula hassas değil: telefonu birkaç kez yatay 8 çizerek hareket ettir, metal ve mıknatıslardan uzak tut.",
        "Compass inaccurate: move the phone in a figure-8 a few times and keep it away from metal/magnets."
    )
    val noCompass = T("Dieses Gerät hat keinen Kompass-Sensor.", "Bu cihazda pusula sensörü yok.", "This device has no compass sensor.")
    val guide = T("Gebetsanleitung", "Namaz rehberi", "Prayer guide")
    val guideShort = T("Anleitung", "Rehber", "Guide")
    val structure = T("Aufbau des %s-Gebets", "%s namazının yapısı", "Structure of the %s prayer")
    val rakat = T("Rakʿa", "rekât", "rak'ah")
    val wuduTitle = T("Gebetswaschung (Wudu)", "Abdest", "Ablution (wudu)")
    val salahTitle = T("So wird gebetet – Schritt für Schritt", "Namaz nasıl kılınır – adım adım", "How to pray – step by step")

    val ringBig = T("Großer Wert im Kreis", "Halkadaki büyük değer", "Large value in the ring")
    val ringOptions = arrayOf(
        T("Countdown bis zum Gebet", "Vakte kalan süre", "Countdown to prayer"),
        T("Uhrzeit des Gebets", "Vaktin saati", "Time of the prayer"),
        T("Aktuelle Uhrzeit", "Şu anki saat", "Current time"),
    )
    val headerClock = T("Uhrzeit oben anzeigen", "Saati üstte göster", "Show clock at the top")
    val showSeconds = T("Sekunden anzeigen", "Saniyeleri göster", "Show seconds")
    val clockSize = T("Größe der Uhrzeit oben", "Üstteki saatin boyutu", "Size of the top clock")
    val sizes = arrayOf(T("Klein", "Küçük", "Small"), T("Mittel", "Orta", "Medium"), T("Groß", "Büyük", "Large"))
    val at = T("um %s", "saat %s", "at %s")

    val presets = T("Vorschläge (Wikimedia Commons)", "Öneriler (Wikimedia Commons)", "Suggestions (Wikimedia Commons)")
    val presetInfo = T(
        "Frei lizenzierte Aufnahmen. Tippen lädt die Datei herunter. Lizenz und Urheber: Dateiseite auf commons.wikimedia.org.",
        "Serbest lisanslı kayıtlar. Dokununca dosya indirilir. Lisans ve sahibi: commons.wikimedia.org dosya sayfası.",
        "Freely licensed recordings. Tap to download. Licence and author: file page on commons.wikimedia.org."
    )
    val orOwnLink = T("Oder eigener Link:", "Veya kendi bağlantın:", "Or your own link:")

    // Neu: Stil, Adhan-Bildschirm, Aufnahmen, Quellen
    val styleLabel = T("Stil", "Stil", "Style")
    val choose = T("Auswählen", "Seç", "Choose")
    val more = T("Weitere", "Diğer", "More")
    val fileFromPhone = T("Datei vom Handy …", "Telefondan dosya …", "File from phone …")
    val clapHint = T("3× klatschen zum Stoppen", "Durdurmak için 3 kez alkışla", "Clap 3× to stop")
    val clapSetting = T("Gebetsruf durch 3× Klatschen stoppen", "Ezanı 3 kez alkışlayarak durdur", "Stop the adhan by clapping 3×")
    val clapInfo = T(
        "Funktioniert, solange der Gebetsruf-Bildschirm angezeigt wird. Das Mikrofon hört nur in dieser Zeit zu; nichts wird gespeichert. Bei sehr lautem Gebetsruf direkt neben dem Handy kann die Erkennung unzuverlässig sein.",
        "Ezan ekranı açıkken çalışır. Mikrofon yalnızca bu sırada dinler; hiçbir şey kaydedilmez. Ezan telefonun hemen yanında çok yüksek sesle çalıyorsa algılama güvenilir olmayabilir.",
        "Works while the adhan screen is shown. The microphone only listens during that time; nothing is stored. With a very loud adhan right next to the phone detection may be unreliable."
    )
    val fullScreenInfo = T(
        "Vollbild beim Gebetsruf erlauben (zeigt die Glocke auch bei gesperrtem Bildschirm).",
        "Ezan sırasında tam ekrana izin ver (kilitli ekranda da zili gösterir).",
        "Allow full screen for the adhan (shows the bell even on the lock screen)."
    )
    val recordings = T("Eigene Aufnahmen", "Kendi kayıtlarım", "My recordings")
    val recordingsInfo = T(
        "Nimm einen Gebetsruf über das Mikrofon auf (max. 1 Minute). Aufnahmen erscheinen bei „Auswählen“ in der Liste der Gebetsrufe.",
        "Mikrofonla bir ezan kaydet (en fazla 1 dakika). Kayıtlar „Seç“ listesinde görünür.",
        "Record an adhan with the microphone (max. 1 minute). Recordings appear in the list under “Choose”."
    )
    val noRecordings = T("Noch keine Aufnahmen.", "Henüz kayıt yok.", "No recordings yet.")
    val newRecording = T("Neue Aufnahme", "Yeni kayıt", "New recording")
    val startRecording = T("Aufnahme starten", "Kaydı başlat", "Start recording")
    val stopRecording = T("Aufnahme beenden", "Kaydı durdur", "Stop recording")
    val recordAgain = T("Neu aufnehmen", "Yeniden kaydet", "Record again")
    val recordHint = T("Halte das Handy nah an die Tonquelle.", "Telefonu ses kaynağına yakın tut.", "Hold the phone close to the sound source.")
    val recording = T("Aufnahme läuft …", "Kaydediliyor …", "Recording …")
    val recordDone = T("Fertig. Gib der Aufnahme einen Namen und speichere sie.", "Bitti. Kayda bir ad ver ve kaydet.", "Done. Give the recording a name and save it.")
    val recordFailed = T("Aufnahme fehlgeschlagen.", "Kayıt başarısız.", "Recording failed.")
    val recordingName = T("Name, z. B. Adhan Papa", "Ad, örn. Babamın ezanı", "Name, e.g. Dad's adhan")
    val deleteRecording = T("Aufnahme löschen?", "Kayıt silinsin mi?", "Delete recording?")
    val delete = T("Löschen", "Sil", "Delete")
    val restoreSources = T("Gelöschte Quellen wiederherstellen", "Silinen kaynakları geri yükle", "Restore deleted sources")
    val deleteSource = T("Quelle löschen?", "Kaynak silinsin mi?", "Delete source?")
    val deleteSourceInfo = T(
        "Ihre Inhalte werden nicht mehr verwendet.",
        "İçerikleri artık kullanılmayacak.",
        "Its content will no longer be used."
    )
    val addSource = T("Quelle per URL hinzufügen", "URL ile kaynak ekle", "Add source by URL")
    val sourceName = T("Name der Quelle", "Kaynağın adı", "Name of the source")
    val sourceUrlHint = T(
        "https-Adresse einer Text- oder JSON-Datei mit Hadithen/Versen. Am besten eine Datei im Beispiel-Format (siehe quellen/beispiel.json im Projekt). Normale Webseiten werden in Absätze zerlegt – das Ergebnis ist dann weniger sauber.",
        "Hadis/ayet içeren bir metin veya JSON dosyasının https adresi. En iyisi örnek formattaki bir dosya (projede quellen/beispiel.json). Normal web sayfaları paragraflara ayrılır – sonuç daha az düzenli olur.",
        "https address of a text or JSON file with hadiths/verses. Best is a file in the example format (see quellen/beispiel.json in the project). Normal web pages are split into paragraphs – the result is less clean."
    )
    val sourceLoaded = T("%d Inhalte geladen", "%d içerik yüklendi", "%d items loaded")
    val sourceFailed = T("Keine Inhalte gefunden.", "İçerik bulunamadı.", "No content found.")
    val refresh = T("Aktualisieren", "Yenile", "Refresh")
    val ownSource = T("Eigene Quelle", "Kendi kaynağın", "Own source")

    val weekdays = arrayOf(
        T("Montag", "Pazartesi", "Monday"), T("Dienstag", "Salı", "Tuesday"),
        T("Mittwoch", "Çarşamba", "Wednesday"), T("Donnerstag", "Perşembe", "Thursday"),
        T("Freitag", "Cuma", "Friday"), T("Samstag", "Cumartesi", "Saturday"),
        T("Sonntag", "Pazar", "Sunday"),
    )
    val months = arrayOf(
        T("Januar", "Ocak", "January"), T("Februar", "Şubat", "February"),
        T("März", "Mart", "March"), T("April", "Nisan", "April"),
        T("Mai", "Mayıs", "May"), T("Juni", "Haziran", "June"),
        T("Juli", "Temmuz", "July"), T("August", "Ağustos", "August"),
        T("September", "Eylül", "September"), T("Oktober", "Ekim", "October"),
        T("November", "Kasım", "November"), T("Dezember", "Aralık", "December"),
    )
    val hijriMonths = arrayOf(
        T("Muharram", "Muharrem", "Muharram"), T("Safar", "Safer", "Safar"),
        T("Rabi' al-Awwal", "Rebiülevvel", "Rabi' al-Awwal"), T("Rabi' ath-Thani", "Rebiülahir", "Rabi' al-Thani"),
        T("Dschumada al-Ula", "Cemaziyelevvel", "Jumada al-Ula"), T("Dschumada al-Akhira", "Cemaziyelahir", "Jumada al-Akhirah"),
        T("Radschab", "Recep", "Rajab"), T("Scha'ban", "Şaban", "Sha'ban"),
        T("Ramadan", "Ramazan", "Ramadan"), T("Schawwal", "Şevval", "Shawwal"),
        T("Dhu l-Qa'da", "Zilkade", "Dhu al-Qa'dah"), T("Dhu l-Hiddscha", "Zilhicce", "Dhu al-Hijjah"),
    )

    fun gregorian(d: java.time.LocalDate, l: Lang): String {
        val wd = weekdays[d.dayOfWeek.value - 1][l]
        val mo = months[d.monthValue - 1][l]
        return when (l) {
            Lang.EN -> "$wd, $mo ${d.dayOfMonth}, ${d.year}"
            Lang.TR -> "${d.dayOfMonth} $mo ${d.year}, $wd"
            Lang.DE -> "$wd, ${d.dayOfMonth}. $mo ${d.year}"
        }
    }

    fun hijri(d: java.time.LocalDate, offsetDays: Int, l: Lang): String = try {
        val h = HijrahDate.from(d.plusDays(offsetDays.toLong()))
        val day = h.get(ChronoField.DAY_OF_MONTH)
        val month = h.get(ChronoField.MONTH_OF_YEAR)
        val year = h.get(ChronoField.YEAR_OF_ERA)
        "$day ${hijriMonths[month - 1][l]} $year"
    } catch (e: Exception) {
        ""
    }
}
