# Import energy data

[//]: # (Make executable)
To import the data, you need a file that contains the data.

During the import process, the structure of the file to be imported can be configured.
You can specify the delimiter as well as the column numbers for the energy values and the timestamps.

The imported energy amount must be given in kWh. For the decimal separator you can use either a dot or a comma.

## Example

The file exported by Netz NOE contains 15-minute energy values. The first 5 lines of the file
look like this:

```
Messzeitpunkt;Verbrauch (kWh);Qualität;Restnetzbezug (kWh);Qualität EG;Eigendeckung (kWh);Ideeller Anteil (kWh);Eigendeckung erneuerb. Energie (kWh);Eigendeckung (kWh) 00000000734;Ideeller Anteil (kWh) 00000000734;Eigendeckung erneuerb. Energie (kWh) 00000000734;
01.01.2025 00:15;0,063000;L1;;;;;;;;;
01.01.2025 00:30;0,083000;L1;;;;;;;;;
01.01.2025 00:45;0,092000;L1;;;;;;;;;
01.01.2025 01:00;0,078000;L1;;;;;;;;;
```

In the import configuration, use a semicolon (;) as the delimiter.

Set the timestamp column index to 0.

Set the energy values column index to 1.

After importing the consumption data, the following data is available in the application for the calculation:

| Timestamp        | Consumption (kWh) |
|------------------|-------------------| 
| 01.01.2025 00:15 | 0,063             |
| 01.01.2025 00:30 | 0,083             |
| 01.01.2025 00:45 | 0,092             |
| 01.01.2025 01:00 | 0,078             |


## Downloading data from Netz NOE

In the Netz NOE smart meter portal, you need to download both the consumption and the feed-in data.

### Instructions

* Select the metering point
* Above the chart, click the Year tab and choose the desired year
* To the right of the chart title (e.g., Annual consumption 2025) click `Download`
* In the options that appear, choose `As table daily/15min values (CSV)`
* Wait until the download starts and completes successfully. This may take a while


Continue to the [calculation](CalculationEn.md)