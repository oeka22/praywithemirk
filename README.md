# Pray with Emir K. – Islamische Gebetsuhr für Android

Ruhige Gebetsuhr mit großer Uhr, Countdown-Ring bis zum nächsten Gebet, Gebetsruf (Adhan)
zur Gebetszeit, Sunnah-Adhkar zu jedem Gebet und zweimal täglich einem Hadith bzw. einer Koranstelle.

## Funktionen
- **Hauptbildschirm**: Ort, Datum (gregorianisch + Hijri) und Uhrzeit in einer Kopfzeile; darunter links der
  Ring mit nächstem Gebet, großer Gebetszeit und Countdown, rechts daneben der Tagesimpuls (Hadith/Sure).
  Darunter alle Tageszeiten mit Glocke je Gebet. Auf Tablets/im Querformat als Kacheln nebeneinander.
- **Kreis-Anzeige einstellbar** (Einstellungen › Anzeige): großer Wert = Countdown (Standard), Gebetszeit
  oder aktuelle Uhrzeit; Uhr oben an/aus, Größe klein/mittel/groß.
- **Qibla-Kompass**: zeigt mit Kaaba-Symbol Richtung Mekka (inkl. Missweisung), vibriert bei korrekter
  Ausrichtung, Hinweis zur Kalibrierung.
- **Gebetsanleitung für Anfänger**: Aufbau jedes Gebets (Rakʿa Sunnah/Fard/Witr), Gebetswaschung Schritt für
  Schritt und Ablauf des Gebets mit Arabisch, Umschrift und Bedeutung. Öffnen über „Anleitung“ oder Tipp auf ein Gebet.
- **Stil** (zusätzlich zur Farbe): Klassisch, Glas & Licht, Soft 3D, Leuchtende Nacht.
- **Gebetsruf-Bildschirm**: Vollbild mit schwingender, leuchtender Glocke (10 Sekunden), Stopp-Knopf;
  optional Stoppen durch 3× Klatschen (Mikrofon, nur solange der Bildschirm angezeigt wird).
- **Eigene Aufnahmen**: Gebetsruf per Mikrofon aufnehmen (max. 1 Minute), anhören, löschen, zuweisen.
- **Quellen**: eingebaute Quellen deaktivieren oder löschen (wiederherstellbar), eigene Quellen per URL
  hinzufügen (JSON/Text/Webseite, siehe `quellen/LIESMICH.md`).
- **Design-Seite** (Einstellungen › Design › „Alle Designs ansehen“):
  - **Klassisch** mit Ring: 5 Farben – Nacht (Standard), Mitternacht, Abenddämmerung, Sandstein (hell), Minze (hell) – und 4 Stile.
  - **Fanus** („Laternenlicht“) und **Mashrabiya** („Gitterfenster“), je in Morgenlicht (hell), Abendgold (mittel), Laternennacht (dunkel): eine Zeile Uhr | Timer | Gebetszeit, leuchtende Quadrat-Leiste, orientalische Sterne, Laternen und Gitter.
  - Typografie pur, Mondnacht, Andalus, Kaminabend, Andalus bei Nacht.
- Timer passt sich bei Stunden (H:MM:SS) automatisch dem Platz an.
- **Arabische Schrift** und **Umschrift** in den Einstellungen getrennt ein-/ausschaltbar.
- **Gebetsruf**: läuft als Vordergrund-Dienst auch bei gesperrtem Bildschirm, mit „Stopp“-Knopf
  in der Benachrichtigung. Standard-Gebetsruf plus individuell je Gebet: Datei vom Handy oder direkter
  https-Audio-Link (wird einmal heruntergeladen, danach offline). Im Link-Dialog gibt es Vorschläge frei
  lizenzierter Aufnahmen von Wikimedia Commons. YouTube-Links werden bewusst nicht
  unterstützt (YouTube erlaubt kein reines Abspielen des Tons). Optional Vorab-Erinnerung.
- **Quellen**: Liste aller verwendeten Quellen (Koran, Bukhari, Muslim, Abu Dawud, Tirmidhi, Nasa'i,
  Ibn Madscha, Tabarani) – einzeln abschaltbar für Tagesimpulse und Adhkar.
- **Adhkar & Sunnah** (tippen auf die Benachrichtigung oder den Button): Bittgebete beim Adhan,
  Sunnah-Gebete (Rawatib) und die Adhkar nach dem Gebet mit Tipp-Zähler (z. B. 33× Subhanallah),
  inkl. der Besonderheiten nach Fajr und Maghrib. Alle mit Quellenangabe.
- **Tagesimpuls**: ab Fajr und ab Maghrib je ein Hadith oder eine Sure/Ayah (Arabisch, Umschrift, Bedeutung).
- **Sprachen**: Deutsch, Türkçe, English (Arabisch + Umschrift immer verfügbar, Umschrift abschaltbar).
- **Berechnung** (offline): Diyanet, MWL, ISNA, Ägypten, Umm al-Qura, Karachi, UOIF; Asr Standard/Hanafi;
  Regel für hohe Breiten; Minutenkorrektur je Zeit.
- **Standort**: automatisch (grobe Ortung reicht), Stadtsuche oder Koordinaten.
- Keine Werbung, kein Tracking, keine Zusatzbibliotheken, keine Internetverbindung nötig.

## Installieren
Schritt-für-Schritt-Anleitung: **INSTALLATION.md** (über GitHub ohne PC-Installation oder mit Android Studio).

## Nach der Installation
1. Standort und Benachrichtigungen erlauben.
2. Unter Android 12+ den Hinweis „Wecker & Erinnerungen“ antippen und erlauben – sonst kann der
   Adhan einige Minuten zu spät kommen.
3. *Einstellungen › Gebetsruf*: für alle oder je Gebet eine MP3 wählen (*Datei*) oder einen direkten
   https-Link eintragen (*Link*). Ohne Auswahl ertönt der Standard-Weckton. Mit ▶ testen, mit ■ stoppen.
4. Für Zuverlässigkeit die Akku-Optimierung für Pray with Emir K. ausschalten (vor allem bei Samsung, Xiaomi, Huawei).

## Hinweise
- Gebetszeiten werden astronomisch berechnet und können um 1–3 Minuten von den Kalendern deiner
  Moschee abweichen. Mit der Minutenkorrektur in den Einstellungen anpassen.
- Die Übersetzungen der Koranverse sind sinngemäße Bedeutungen, keine wörtliche Übersetzung.
- Das Hijri-Datum basiert auf Umm al-Qura und lässt sich um ±2 Tage korrigieren.

## Projektstruktur
```
app/src/main/java/com/praywithemirk/app/
  PrayTimes.kt        Berechnung der Gebetszeiten
  Schedule.kt         nächstes/letztes Gebet
  AlarmScheduler.kt   Planung der Alarme
  Receivers.kt        Alarm- und Boot-Empfänger
  AdhanService.kt     Abspielen des Gebetsrufs
  Notifications.kt    Benachrichtigungen
  LocationHelper.kt   Standort & Stadtsuche
  Prefs.kt            Einstellungen
  I18n.kt             Texte DE/TR/EN, Datumsformate
  Adhkar.kt, Quran.kt, DailyContent.kt   Inhalte
  Guide.kt, AdhanPresets.kt                Anleitung, Gebetsruf-Vorschläge
  MainActivity.kt, DhikrActivity.kt, SettingsActivity.kt, QiblaActivity.kt, GuideActivity.kt, Ui.kt   Oberfläche
```
