version-check
----
Validate the mobile application version. The response to this service includes an upgrade status flag with a state for the RDS app

The upgrade status is determined using the supplied POST data.
  
* **URL**

  `/mobile-version-check/rds`

* **Method:**
  
  `POST`

*  **URL Params**
 
   `journeyId=[journeyId]`
   
   The journey Id may be supplied for logging and diagnostic purposes.
     
*  **JSON**

Current version information of the application. The "os" attribute can be either ios or android.

```json
{
    "os": "ios",
    "version" : "0.1.0"
}
```

* **Success Response:**

  * **Code:** 200 <br />
    **Content:** 

##### Active
```json
{
    "upgradeRequired": true,
    "appState": {
        "state": "ACTIVE"
    }
}
```
##### Inactive
```json
{
    "upgradeRequired": true,
    "appState": {
        "state": "INACTIVE",
        "endDate": "2019-11-01T00:00:00"
    }
}
```
##### Shuttered
```json
{
    "upgradeRequired": true,
    "appState": {
        "state": "SHUTTERED",
        "endDate": "2019-11-01T00:00:00"
    }
}
```


