# Batterie-Prognose – Ausführbare Spezifikation (Deutsch)

Diese Spezifikation beschreibt die Berechnung der möglichen Einsparungen, wenn eine Photovoltaik-Anlage um eine Batterie
ergänzt wird. Sie prüft sowohl die Energieflüsse des Systems (Laden, Entladen und Netz-Interaktion) als auch die
Geldberechnung der Prognose.

Anwendungsfall: Aus historischen Daten ist je Zeitscheibe bekannt, wie viel Energie aus dem Netz bezogen und wie viel
ins Netz eingespeist wurde. Mit einer Batterie wird Überschuss zuerst in die Batterie geladen; **späterer** Verbrauch wird
zuerst aus der Batterie gedeckt, nur der Rest kommt aus dem Netz. Ist die Batterie voll, wird weiterer Überschuss ins
Netz eingespeist.

Wie im Szenario ersichtlich waren um 11:15 3 kWh Einspeisung vorhanden. Das führt zu einer Aufladung der Batterie. Der Verbrauch zur selben Uhrzeit wird noch nicht von der Batterie gedeckt. Erst bei der nächsten Zeitscheibe, um 11:30, kann der Verbrauch mit der gespeicherten Energie aus der Batterie gedeckt werden.

## Szenario

Es sind folgende Messpunkte vorhanden:

| [][addEnergyData] [Zeitpunkt][timestamp] | [Verbrauch (kWh)][consumption] | [Einspeisung (kWh)][feedIn] |
|------------------------------------------|-------------------------------:|----------------------------:|
| 01.01.2024 11:00                         |                            0.5 |                         0.0 |
| 01.01.2024 11:15                         |                            0.5 |                         3.0 |
| 01.01.2024 11:30                         |                            1.0 |                         3.0 |
| 01.01.2024 11:45                         |                            0.0 |                         3.0 |
| 01.01.2024 12:00                         |                            3.0 |                         0.0 |

[addEnergyData]: - "addEnergyData(#timestamp, #consumption, #feedIn)"
[timestamp]: - "#timestamp"
[consumption]: - "#consumption"
[feedIn]: - "#feedIn"

Wenn die Prognose für das Jahr [2024][calculatedYear] mit

* Strompreis [0.30][consumptionPrice] €/kWh
* Batteriekapazität [5.0][batteryCapacity] kWh
* Einspeisetarif Netz [0.08][feedInPriceGrid] €/kWh 
* Tarif der Energiegemeinschaft [0.15][feedInPriceCommunity] €/kWh 
* [0][deliveryToCommunityPercent] % Lieferung an die Gemeinschaft.

[calculatedYear]: - "#calculatedYear"
[consumptionPrice]: - "#consumptionPrice"
[batteryCapacity]: - "#batteryCapacity"
[feedInPriceGrid]: - "#feedInPriceGrid"
[feedInPriceCommunity]: - "#feedInPriceCommunity"
[deliveryToCommunityPercent]: - "#deliveryToCommunityPercent"

[berechnet wird]( - "calculateForecast(#calculatedYear, #batteryCapacity, #consumptionPrice, #feedInPriceGrid, #feedInPriceCommunity, #deliveryToCommunityPercent)")
dann ergibt das folgendes Ergebnis:

## Energie-Ergebnisse

- Gesamt-Netzbezug (kWh): [1.0](- "?=consumptionFromGrid()")
- Gesamteinspeisung ins Netz (kWh): [3.0](- "?=feedInToContractPartner()")
- Aus der Batterie beszogen (kWh): [4.0](- "?=consumptionFromBattery()")
- Verbleibende Batterieenergie (kWh): [2.0](- "?=resumingEnergyInBattery()")

## Geld-Ergebnisse

- Entgangene Einspeisevergütung (€) = 4,0 kWh(aus Batterie bezogen) × 0,08 €/kWh (Einspeisetarif Netz) (0%
  Gemeinschaft): [0.32](- "?=lostMoneyNotFedIn()")
- Gespartes Geld pro Jahr (€) = 0,3 (Strompreis) × 4,0 (kWh aus Batterie) − 0,32 (entgangene
  Einspeisevergütung): [0.88](- "?=savedMoney()")
