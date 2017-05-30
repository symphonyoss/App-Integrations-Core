[![Symphony Software Foundation - Incubating](https://cdn.rawgit.com/symphonyoss/contrib-toolbox/master/images/ssf-badge-incubating.svg)](https://symphonyoss.atlassian.net/wiki/display/FM/Incubating)
[![Build Status](https://travis-ci.org/symphonyoss/App-Integrations-Core.svg?branch=dev)](https://travis-ci.org/symphonyoss/App-Integrations-Core)
[![Dependencies](https://www.versioneye.com/user/projects/58f3a67b8fa4276401425d93/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/58f3a67b8fa4276401425d93)
[![Validation Status](https://scan.coverity.com/projects/12823/badge.svg?flat=1)](https://scan.coverity.com/projects/symphonyoss-app-integrations-core)

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
