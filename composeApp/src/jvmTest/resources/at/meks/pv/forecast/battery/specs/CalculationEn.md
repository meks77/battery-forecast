# Battery Forecast – Executable Specification (English)

This specification describes the calculation of potential savings when a photovoltaic (PV) system is complemented by a
battery. It verifies both the system's energy flows (charging, discharging, and grid interaction) and the money
calculation of the forecast.

Use case: From historical data, it is known for each time slice how much energy was drawn from the grid and how much was
fed into the grid. With a battery, surplus energy is first stored in the battery; later consumption is covered first
from the battery, with only the remainder coming from the grid. If the battery is full, further surplus is fed into the
grid.

As visible in the scenario, there was a 3 kWh feed-in at 11:15. This leads to charging the battery. The consumption at
that same time is not yet covered by the battery. Only in the next time slice, at 11:30, can the consumption be covered
with the stored energy from the battery.

## Scenario

The following measurements are available:

| [][addEnergyData] [Timestamp][timestamp] | [Consumption (kWh)][consumption] | [Feed-in (kWh)][feedIn] |
|------------------------------------------|---------------------------------:|------------------------:|
| 01.01.2024 11:00                         |                              0.5 |                     0.0 |
| 01.01.2024 11:15                         |                              0.5 |                     3.0 |
| 01.01.2024 11:30                         |                              1.0 |                     3.0 |
| 01.01.2024 11:45                         |                              0.0 |                     3.0 |
| 01.01.2024 12:00                         |                              3.0 |                     0.0 |

[addEnergyData]: - "addEnergyData(#timestamp, #consumption, #feedIn)"
[timestamp]: - "#timestamp"
[consumption]: - "#consumption"
[feedIn]: - "#feedIn"

When the forecast for the year [2024][calculatedYear] is calculated with

* Electricity price [0.30][consumptionPrice] €/kWh
* Battery capacity [5.0][batteryCapacity] kWh
* Grid feed-in tariff [0.08][feedInPriceGrid] €/kWh
* Energy community tariff [0.15][feedInPriceCommunity] €/kWh
* [0][deliveryToCommunityPercent] % delivery to the community.

[calculatedYear]: - "#calculatedYear"
[consumptionPrice]: - "#consumptionPrice"
[batteryCapacity]: - "#batteryCapacity"
[feedInPriceGrid]: - "#feedInPriceGrid"
[feedInPriceCommunity]: - "#feedInPriceCommunity"
[deliveryToCommunityPercent]: - "#deliveryToCommunityPercent"

[is calculated]( - "calculateForecast(#calculatedYear, #batteryCapacity, #consumptionPrice, #feedInPriceGrid, #feedInPriceCommunity, #deliveryToCommunityPercent)")
then the following result is obtained:

## Energy results

- Total consumption from grid (kWh): [1.0](- "?=consumptionFromGrid()")
- Total feed-in to contract partner (kWh): [3.0](- "?=feedInToContractPartner()")
- Consumed from battery (kWh): [4.0](- "?=consumptionFromBattery()")
- Remaining energy in the battery (kWh): [2.0](- "?=resumingEnergyInBattery()")

## Money results

- Lost feed-in remuneration (€) = 4.0 kWh (consumed from battery) × 0.08 €/kWh (grid feed-in tariff) (0%
  community): [0.32](- "?=lostMoneyNotFedIn()")
- Saved money per year (€) = 0.3 (electricity price) × 4.0 (kWh from battery) − 0.32 (lost feed-in
  remuneration): [0.88](- "?=savedMoney()")
