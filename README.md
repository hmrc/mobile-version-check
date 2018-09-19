# mobile-version-check

[![Build Status](https://travis-ci.org/hmrc/mobile-version-check.svg)](https://travis-ci.org/hmrc/mobile-version-check) [ ![Download](https://api.bintray.com/packages/hmrc/releases/mobile-version-check/images/download.svg) ](https://bintray.com/hmrc/releases/mobile-version-check/_latestVersion)


HMRC Mobile version Check service.
This service can be used to determine whether an upgrade is required for a given HMRC app version.

API
---

The service exposes the following end point:

| *Task* | *Supported Methods* | *Description* |
|--------|----|----|
| ```/mobile-version-check``` | POST | Validates the mobile application version [More...](docs/version-check.md) |


# Sandbox
The live endpoint is accessible in sandbox with the `/sandbox` prefix:
```
    POST /sandbox/mobile-version-check
```

To trigger the sandbox endpoints locally, use the "X-MOBILE-USER-ID" header with one of the following values:
208606423740 or 167927702220

To test different scenarios, add a header "SANDBOX-CONTROL" with one of the following values:

| *Value* | *Description* |
|--------|----|
| "UPGRADE-REQUIRED" | Happy path, returns a json payload with upgradeRequired == true |
| "ERROR-500" | Unhappy path, trigger a 500 Internal Server Error response |
| Not set or any other value | Happy path, returns a json payload with upgradeRequired == false |

To start the service locally either use service-manager or clone this repo and use sbt to start:
```
 cd $WORKSPACE/mobile-version-check
 sbt start 
```

Once its running then to test the default behaviour (returns a json payload with upgradeRequired == false):
```
curl -i -H"Accept: application/vnd.hmrc.1.0+json" localhost:8244/sandbox/mobile-version-check
```


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
