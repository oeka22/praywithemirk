package com.praywithemirk.app

/**
 * Vorgeschlagene Gebetsrufe von Wikimedia Commons (nur frei lizenzierte Medien).
 * Special:FilePath leitet auf die eigentliche Datei weiter; heruntergeladen wird erst auf Wunsch.
 */
object AdhanPresets {
    class Preset(val title: String, val file: String) {
        val url: String get() = "https://commons.wikimedia.org/wiki/Special:FilePath/" + file.replace(' ', '_')
        val page: String get() = "https://commons.wikimedia.org/wiki/File:" + file.replace(' ', '_')
    }

    val list = listOf(
        Preset("Adhan – Prophetenmoschee, Medina", "33937_ejaz215_call-to-prayer-from-the-prophet-s-mo.ogg"),
        Preset("The Adhan – Aaqib Azeez", "The_Adhan_-_Muslim_Call_to_Prayer_-_Aaqib_Azeez.mp3"),
        Preset("Beautiful Adhan", "Beautiful_adhan.ogg"),
        Preset("Azan", "Azan.ogg"),
        Preset("Adhan (Wikipedia)", "Adhan_wiki.oga"),
    )
}
