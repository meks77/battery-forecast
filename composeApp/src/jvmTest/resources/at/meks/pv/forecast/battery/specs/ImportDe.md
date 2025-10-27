# Import der Energiedaten

[//]: # (Ausführbar machen)
Um die Daten importieren zu können, wird eine Datei benötigt, welche die Daten enthält.

Während des Importvorgangs kann der Aufbau der zu importierenden Datei konfiguriert werden.
Dabei können das Trennzeichen und die Spaltennummern der Leistungsdaten bzw. der Zeitstempel angegeben werden.

Die bezogene Energiemenge muss in kWh angegeben sein. Als Kommazeichen kann entweder ein Punkt oder ein Komma verwendet
werden.

## Beispiel

Die Datei, welche von Netz NOE exportiert wurde enthält die 15-Minuten-Leistungsdaten. Die ersten 5 Zeilen der Datei
sehen wie folgt aus:

```text
Messzeitpunkt;Verbrauch (kWh);Qualität;Restnetzbezug (kWh);Qualität EG;Eigendeckung (kWh);Ideeller Anteil (kWh);Eigendeckung erneuerb. Energie (kWh);Eigendeckung (kWh) 00000000734;Ideeller Anteil (kWh) 00000000734;Eigendeckung erneuerb. Energie (kWh) 00000000734;
01.01.2025 00:15;0,063000;L1;;;;;;;;;
01.01.2025 00:30;0,083000;L1;;;;;;;;;
01.01.2025 00:45;0,092000;L1;;;;;;;;;
01.01.2025 01:00;0,078000;L1;;;;;;;;;
```

Während des Imports in der Konfiguration wird als Trennzeichen ein Semikolon(;) verwendet.

Als Spaltennummer für die Zeitstempel wird 0 angegeben.

Also Spaltennummer für die Leistungsdaten wird 1 angegeben.

Wenn der Import der Verbrauchsdaten durchgeführt wird, sind folgende Daten in der Applikation für die Berechnung
vorhanden:

| Zeitstempel      | Verbrauch (kWh) |
|------------------|-----------------| 
| 01.01.2025 00:15 | 0,063           |
| 01.01.2025 00:30 | 0,083           |
| 01.01.2025 00:45 | 0,092           |
| 01.01.2025 01:00 | 0,078           |


## Download der Daten von Netz NOE

Im Smartmeter-Portal von Netz NOE müssen sowohl die Verbrauchs-, als auch die Einspeisedaten heruntergeladen werden.

### Anleitung

* Zählpunkt auswählen
* Oberhalb des Diagrams auf das Tab Jahr klicken und das gewünschte Jahr wählen
* Rechts vom Diagrammtitel (z.B. Jahresverbrauch 2025) auf `Download` klicken
* In den erscheinenden Optionen `Als Tablelle Tages-/15min Werte(CSV)` wählen
* Warten, bis der Download startet und erfolgreich endet. Das kann durchaus etwas dauern


Weiter zur [Berechnung](CalculationDe.md)