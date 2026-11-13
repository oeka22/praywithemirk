package com.praywithemirk.app

/** Die Quellen, aus denen die Inhalte der App stammen. */
enum class Src(val id: String, val key: String, val title: T, val info: T) {
    QURAN(
        "quran", "Koran",
        T("Der Koran", "Kur'an-ı Kerim", "The Qur'an"),
        T("Suren und Verse für den Tagesimpuls", "Günlük tefekkür için sureler ve ayetler", "Surahs and verses for the daily reflection")
    ),
    BUKHARI(
        "bukhari", "Bukhari",
        T("Sahih al-Bukhari", "Sahih-i Buhârî", "Sahih al-Bukhari"),
        T("Imam al-Bukhari (gest. 870)", "İmam Buhârî (ö. 870)", "Imam al-Bukhari (d. 870)")
    ),
    MUSLIM(
        "muslim", "Muslim",
        T("Sahih Muslim", "Sahih-i Müslim", "Sahih Muslim"),
        T("Imam Muslim (gest. 875)", "İmam Müslim (ö. 875)", "Imam Muslim (d. 875)")
    ),
    ABU_DAWUD(
        "abudawud", "Abu Dawud",
        T("Sunan Abi Dawud", "Sünen-i Ebû Dâvûd", "Sunan Abi Dawud"),
        T("Abu Dawud as-Sidschistani (gest. 889)", "Ebû Dâvûd es-Sicistânî (ö. 889)", "Abu Dawud al-Sijistani (d. 889)")
    ),
    TIRMIDHI(
        "tirmidhi", "Tirmidhi",
        T("Dschami' at-Tirmidhi", "Sünen-i Tirmizî", "Jami' at-Tirmidhi"),
        T("Imam at-Tirmidhi (gest. 892)", "İmam Tirmizî (ö. 892)", "Imam al-Tirmidhi (d. 892)")
    ),
    NASAI(
        "nasai", "Nasa'i",
        T("Sunan an-Nasa'i", "Sünen-i Nesâî", "Sunan an-Nasa'i"),
        T("Imam an-Nasa'i (gest. 915)", "İmam Nesâî (ö. 915)", "Imam an-Nasa'i (d. 915)")
    ),
    IBN_MAJAH(
        "ibnmajah", "Ibn Majah",
        T("Sunan Ibn Madscha", "Sünen-i İbn Mâce", "Sunan Ibn Majah"),
        T("Ibn Madscha al-Qazwini (gest. 887)", "İbn Mâce el-Kazvînî (ö. 887)", "Ibn Majah al-Qazwini (d. 887)")
    ),
    TABARANI(
        "tabarani", "Tabarani",
        T("al-Mu'dscham (at-Tabarani)", "el-Mu'cem (Taberânî)", "al-Mu'jam (al-Tabarani)"),
        T("Imam at-Tabarani (gest. 971)", "İmam Taberânî (ö. 971)", "Imam al-Tabarani (d. 971)")
    );

    companion object {
        /** Ermittelt die Quellen aus einer Quellenangabe wie "Bukhari 1, Muslim 1907". */
        fun parse(source: String): List<Src> = entries.filter { source.contains(it.key, ignoreCase = true) }
    }
}
