# Eigene Quellen für „Pray with Emir K.“

In der App unter *Einstellungen › Quellen › Quelle per URL hinzufügen* eine **https-Adresse** eintragen.
Die App lädt die Datei herunter und nimmt die Einträge in den Tagesimpuls auf (wöchentlich automatisch aktualisiert).

## Beispiel direkt nutzen
Wenn dieses Projekt auf GitHub liegt, funktioniert z. B.:

    https://raw.githubusercontent.com/oeka22/praywithemirk/main/quellen/beispiel.json

## Formate
- **JSON** (empfohlen): Liste von Einträgen mit `ar` (Arabisch, optional), `de`, `tr`, `en` (mind. eins) und `source`.
  Auch übliche Feldnamen wie `arabic`, `text`, `translation`, `reference` werden erkannt.
- **Text** (`.txt`): Einträge durch Leerzeilen getrennt; arabische Zeilen werden erkannt; letzte Zeile mit „— …“ = Quellenangabe.
- **Normale Webseite**: wird in Absätze zerlegt (40–700 Zeichen). Funktioniert, ist aber weniger sauber.
