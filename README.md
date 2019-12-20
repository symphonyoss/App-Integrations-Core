[![FINOS - Incubating](https://cdn.jsdelivr.net/gh/finos/contrib-toolbox@master/images/badge-incubating.svg)](https://finosfoundation.atlassian.net/wiki/display/FINOS/Incubating)
[![Build Status](https://travis-ci.org/symphonyoss/App-Integrations-Core.svg?branch=dev)](https://travis-ci.org/symphonyoss/App-Integrations-Core)
[![Dependencies](https://www.versioneye.com/user/projects/58f3a67b8fa4276401425d93/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/58f3a67b8fa4276401425d93)
[![Validation Status](https://scan.coverity.com/projects/12823/badge.svg?flat=1)](https://scan.coverity.com/projects/symphonyoss-app-integrations-core)
[![codecov](https://codecov.io/gh/symphonyoss/App-Integrations-Core/branch/dev/graph/badge.svg)](https://codecov.io/gh/symphonyoss/App-Integrations-Core)

# Integrations Core Documentation

This document provides a brief overview of Integration Core components and how to build them from scratch.

# Overview

Integration Core is responsible for managing active integrations and provides key services to allow third party services the ability to post messages into a configurable set of streams

The key services provided to the registered integrations are:

* Authentication proxy - each integration should be configured with credentials, but the implementation never needs
to deal with them.  Once bootstrapped, the integration can use integration bridge services as if it's unauthenticated.
The bridge itself proxies those services to the cloud with the proper authentication.
* Send messages to a stream
* Read and write configuration information to the cloud configuration services
* Read user information to the cloud user services
* Health check

The Integration Core exposes the implemented Integrations through its web module, [integration-web](integration-web/pom.xml).
It expects messages to be posted on a specific URL format and then it will try to determine for who this message is for using the information in the URL itself.

# Build instructions for the Java developer

## What you’ll build
You’ll build a simple java web application that provides the key services described above.
It also builds the other integration modules we currently have, making them available to parse requests from any of the supported and configured integrations.

If you add a new integration, to get it up and running you also need to add it to the integration-web [pom.xml](integration-web/pom.xml)

## What you’ll need
* JDK 1.8
* Maven 3.0.5+
* Node 6.10
* Gulp (globally installed)
* Webpack (globally installed)

# 2-Way Integrations
In addition to receiving notifications through webhooks, some integrations (Apps) can also be used to perform actions within their associated third-party services. Currently, only the [JIRA integration](github.com/symphonyoss/App-Integrations-Jira) supports this functionality.

It is important to understand two concepts related to the integrations: the bot user and the app itself.

## The bot user
The bot user is the user on your pod from which all webhook notifications will be sent. Once you have completed the provisioning process, you will see a pair of certificate files inside the certs directory (/data/symphony/ib/certs). Using JIRA as an example, they are ```jira.p12``` and ```jira.pem```. These files are used to authenticate/authorize the bot user for JIRA on your pod.

## The App on Symphony Market
In order to use our JIRA App in Symphony to perform actions within a JIRA instance, we must also authenticate and authorize the App itself. To do this, we will need an extra pair of certificates, which we can generate by following these steps (replace the ```$variable``` with appropriate values):

1. Create a private key:
```
openssl genrsa -out $podCertsDir/${app_name}_app_key.pem 1024
```
2. Generate a certificate request:
```
openssl req -new -key $podCertsDir/${app_name}_app_key.pem \
       -subj "/CN=$username/O=Symphony Communications LLC/OU=NOT FOR PRODUCTION USE/C=US" \
       -out $podCertsDir/${app_name}_app_req.pem -days 3650
```
3. Generate the certificate:
```
openssl x509 -req -sha256 -days 3650 -in $podCertsDir/${app_name}_app_req.pem \
        -CA $caCert -CAkey $caKey -passin pass:$_param_pass \
        -out $podCertsDir/${app_name}_app.pem -set_serial 0x1
```
4. Generate the p12 keystore file:
```
openssl pkcs12 -export -out $podCertsDir/${app_name}_app.p12 \
        -in $podCertsDir/${app_name}_app.pem -inkey $podCertsDir/${app_name}_app_key.pem \
        -passout pass:$_param_pass
```

Now, we have generated the same kinds of certificates used by the bot users. Next, we will generate the necessary keys from them. These keys will be used to enable communication between the JIRA App in Symphony and any configured JIRA instance using OAuth.

## The OAuth 1.0 authorization
In order to call JIRA's APIs, you must be logged into your JIRA account and have allowed Symphony to call these APIs on your behalf. This is possible using the [OAuth](https://en.wikipedia.org/wiki/OAuth) mechanism, which uses [RSA Keys](https://en.wikipedia.org/wiki/RSA_(cryptosystem)) to establish a secure channel. Using the previously generated certificates, we can create these keys by performing the following steps:

1. Generate the PKCS8 private key:
```
openssl pkcs8 -topk8 -nocrypt -in $podCertsDir/${app_name}_app_key.pem \
        -out $podCertsDir/${app_name}_app.pkcs8
```
2. Generate the public key:
```
openssl x509 -pubkey -noout -in $podCertsDir/${app_name}_app.pem > $podCertsDir/${app_name}_app_pub.pem
```

**IMPORTANT: All of these files must be named according to the pattern outlined in the previous steps, otherwise your OAuth *dance* will not work properly.**

## Keys and certificates final result
Using jira as our "app_name", we will have generated the following six files:
* ```jira_app.p12```
* ```jira_app.pem```
* ```jira_app.pkcs8```
* ```jira_app_key.pem```
* ```jira_app_pub.pem```
* ```jira_app_req.pem```

Only three of these files will be used by the Integration Bridge, the others being intermediary files used to generate the necessary three. We can remove ```jira_app.pem```, ```jira_app_key.pem``` and ```jira_app_req.pem```. The remaining files are described as follows:

* Authenticate/authorize the app on our Pod:
  * ```jira_app.p12```
* Private key used by the OAuth mechanism in order to authorize the JIRA App to call JIRA APIs (this file is recognized by naming convention and is not specified inside our ```application.yaml```):
  * ```jira_app.pkcs8```
* Public key used to configure the [JIRA application link](https://integrations.symphony.com/v1.0/docs/jira-application-link-configuration#section-installation-and-configuration-on-jira). This file's content is shown in the AC Portal (also recognized by naming convention and is not specified inside our ```application.yaml```):
  * ```jira_app_pub.pem```

## Update Integration Bridge Config File
Finally, we can locate this section in our ```application.yaml``` file:
```
applications:
  jira:
    state: PROVISIONED
    keystore: 
      file: jira.p12 
      password: some_password
      type: pkcs12
```
We can enable our 2-Way configuration within the Integration Bridge by adding a few lines, so that the end result looks like this:
```
applications:
  jira:
    state: PROVISIONED
    keystore: 
      file: jira.p12 
      password: some_password
      type: pkcs12
    app_keystore:
      file: jira_app.p12 
      password: some_password
      type: pkcs12
```

If you want to check the webhook origin, you can add the allowed_origins
section as the example bellow and define the list of trusted origins. You can define a local whitelist for each application.

The whitelist may have the origin host name, IP address or both. The IP address can be a range using CIDR notation.
```
applications:
  jira:
    state: PROVISIONED
    keystore: 
      file: jira.p12 
      password: some_password
      type: pkcs12
      allowedOrigins:
        - host: ec2-107-23-104-115.compute-1.amazonaws.com
          address: 107.23.104.115
        - address: 192.30.252.0/22
```
P.S. You must include a character '-' for each new entry in the list.

We must now enable the 2-Way configuration within JIRA as well. Refer to [these steps](https://integrations.symphony.com/v1.0/docs/jira-application-link-configuration#section-installation-and-configuration-on-jira) for the remaining setup.
