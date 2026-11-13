# Pray with Emir K. auf dem Android-Handy installieren

Es gibt zwei Wege. **Weg A** braucht nur einen Browser, **Weg B** Android Studio auf dem PC.

---

## Weg A – über GitHub (empfohlen, ohne Installation auf dem PC)

GitHub baut die App kostenlos in der Cloud. Danach lädst du sie direkt aufs Handy.

### 1. Projekt hochladen (einmalig, ca. 5 Minuten)
1. Auf **github.com** ein kostenloses Konto anlegen und anmelden.
2. Oben rechts **＋ › New repository** wählen.
   Name: `praywithemirk`, Sichtbarkeit: **Public** (dann ist die Download-Seite ohne Anmeldung erreichbar)
   oder **Private** (dann musst du auf dem Handy bei GitHub angemeldet sein).
   Auf **Create repository** klicken.
3. Auf der neuen Seite den Link **„uploading an existing file“** anklicken.
4. `PrayWithEmirK.zip` auf dem PC entpacken, den Ordner `PrayWithEmirK` öffnen und **seinen gesamten Inhalt**
   (alle Dateien und Ordner, auch `.github`) in das Browserfenster ziehen.
   > Tipp: Der Ordner `.github` ist unter Windows evtl. ausgeblendet. Im Explorer
   > *Ansicht › Anzeigen › Ausgeblendete Elemente* einschalten.
5. Unten auf **Commit changes** klicken.

### 2. Warten, bis die App gebaut ist (ca. 5 Minuten)
- Oben auf den Reiter **Actions** klicken. Der Lauf „Pray with Emir K. – APK bauen“ startet automatisch.
- Fertig, wenn ein grüner Haken ✓ erscheint.
- Falls kein Lauf startet: links „Pray with Emir K. – APK bauen“ wählen › **Run workflow**.

### 3. Auf dem Handy installieren
1. Auf dem Handy im Browser öffnen:
   `https://github.com/DEIN-NAME/praywithemirk/releases/latest`
2. **PrayWithEmirK.apk** antippen und herunterladen.
3. Die heruntergeladene Datei öffnen. Android fragt einmalig, ob der Browser
   **„Apps aus unbekannten Quellen installieren“** darf → **Erlauben**, zurück, **Installieren**.
4. Falls Google Play Protect warnt („Unbekannte App“): **Trotzdem installieren** wählen.
   Die Warnung kommt, weil die App nicht aus dem Play Store stammt.

### Updates
Neue Dateien ins Repository hochladen → GitHub baut automatisch neu → auf dem Handy wieder
`…/releases/latest` öffnen und installieren. Deine Einstellungen bleiben erhalten.

---

## Weg B – mit Android Studio und USB-Kabel

1. **Android Studio** installieren (developer.android.com/studio).
2. Auf dem Handy die **Entwickleroptionen** aktivieren:
   *Einstellungen › Telefoninfo › Softwareinformationen › 7× auf „Build-Nummer“ tippen.*
3. In *Einstellungen › Entwickleroptionen* **USB-Debugging** einschalten.
4. Handy per USB anschließen und die Nachfrage „USB-Debugging zulassen?“ bestätigen.
5. In Android Studio *File › Open* → Ordner `PrayWithEmirK` öffnen, Gradle-Sync abwarten.
6. Oben das Handy auswählen und **Run ▶** drücken. Die App wird installiert und startet.

Alternativ ohne Kabel: *Build › Generate App Bundles or APKs › Build APK(s)* – die Datei
`app/build/outputs/apk/debug/app-debug.apk` z. B. per Google Drive, E-Mail oder WhatsApp an
dich selbst schicken und auf dem Handy öffnen (Schritte 3–4 aus Weg A).

---

## Nach der ersten Installation
1. **Standort** und **Benachrichtigungen** erlauben.
2. Den goldenen Hinweis **„Wecker & Erinnerungen“** antippen und erlauben (für pünktlichen Gebetsruf).
3. *Einstellungen › Gebetsruf*: einen Vorschlag über **Link** wählen oder eine eigene MP3 über **Datei**.
   Mit ▶ testen.
4. *Einstellungen › Berechtigungen*: Akku-Optimierung für Pray with Emir K. ausschalten
   (besonders bei Samsung, Xiaomi, Huawei wichtig).
