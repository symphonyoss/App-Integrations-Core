_Note that this project depends on internal Symphony infrastructure (repository.symphony.com), and therefore it can only be built by Symphony LLC employees/partners._

[![Build Status](https://travis-ci.org/symphonyoss/App-Integrations-Core.svg?branch=dev)](https://travis-ci.org/symphonyoss/App-Integrations-Core)

# Run locally

1. Define your certificate paths and passwords
```
cp env.sh.sample env.sh
open env.sh
```

Make sure that
- Paths and passwords are correct
- You can reach all Symphony Pod endpoints
- Service accounts exists and cert CNs match with account's usernames
- `env.sh` and `certs/` are ignored by Git and don't end up in any code repository (especially if public!)

2. Run the integrations
```
./run.sh
```

This command will create an `application.yaml` file (should be ignored by Git, as with `env.sh` and `certs/`) in the project root folder, using `docs/configuration/application.yaml.template` as template

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

## General Integration Workflow
As of providing the mentioned structure above, we'll detail here what is the general workflow when the core receives a message from an integrated app, let's say GitHub, for this example:

> 1. Expose an endpoint where it will receive the message.
> 2. Identify where this message is coming from through the URL parameters it received (configurationId and instanceId)
> 3. If the message is trying to reach a valid integration and a configured instance, it will delegate the message to the specific integration code implemented separately across the other Integration repositories.
> 4. The integration GitHub logic will now determine which event it is dealing with through the received message header parameter, and based on this will determine which [parser](#parsers) it must use to treat the message properly.
> 5. The parser will then convert the message to a [Message ML format](#the-message-ml-format), extracting the needed information from the payload received.
> 6. The parsed message will return to the Integration Core and post the message to the Symphony platform

### Parsers
Integrations will most of the times need a parser to work properly.
Those special classes will need to deal with the content coming from the related application, parsing this data into a format readable by the Symphony platform.

This format is called Symphony Message ML and it may contain a set of tags. More details below.

### The Message ML format
A Message ML is a Symphony XML format that defines XML elements and attributes necessary to compose a message that can be posted within a chat room.
The most basic message one can send may be as simple as ``<messageML>simple message</messageML>`` or as detailed as it can get. What determines this is what system we are integrating with.

These elements and attributes will be briefly detailed in the next topics as reference. The specific integration formats can be found in their separate repositories "Readme" files.

### Entity
An entity is a special element contained in a ``<messageML>``, it may also be nested within other entities as another element, and so on.

Entities must have a "type" and a "version", and may also have a "name" for itself, all of those as XML attributes.

The first entity in a messageML MUST have an element called "presentationML".

The ``<presentationML>`` is a special element that must hold all content that would be otherwise drawn on Symphony by other elements, represented as a single string on its content.
This particular text must follow the rules presented [here](https://rest-api.symphony.com/docs/message-format/).

It is important that it contains matching information as it is used for visualising a message when a specific renderer is not present, on Symphony mobile apps or content export.

Entities may also have ``<attribute>``s as their XML elements, which in turn must have a "name", a "type" and a "value" as attributes.

Here's an example of a valid MessageML, containing all of the mentioned above:

```xml
<messageML>
    <entity type="sample.event.core" version="1.0">
        <presentationML>test message for:<br/>application core</presentationML>
        <attribute name="message" type="org.symphonyoss.string" value="test message"/>
        <entity name="application" type="sample.application">
            <attribute name="appName" type="org.symphonyoss.string" value="core"/>
        </entity>
    </entity>
</messageML>
```

# Build instructions for the Java developer

### What you’ll build
You’ll build a simple java web application that provides the key services described above.
It also builds the other integration modules we currently have, making them available to parse requests from any of the supported and configured integrations.

If you add a new integration, to get it up and running you also need to add it to the integration-web [pom.xml](integration-web/pom.xml)

### What you’ll need
* JDK 1.7
* Maven 3.0.5+

### Build with maven
Integration Core is compatible with Apache Maven 3.0.5 or above. If you don’t already have Maven installed you can follow the instructions at maven.apache.org.

To start from scratch, do the following:

1. Build the _App-Integrations-Core_ dependencies, on this order (so you have them in your Maven local repository):

> 1. [_App-Integrations-Commons_](https://github.com/symphonyoss/App-Integrations-Commons)
> 2. [_App-Integrations-Universal_](https://github.com/symphonyoss/App-Integrations-Universal)
> 3. [_App-Integrations-Github_](https://github.com/symphonyoss/App-Integrations-Github)
> 4. [_App-Integrations-Jira_](https://github.com/symphonyoss/App-Integrations-Jira)
> 5. [_App-Integrations-Salesforce_](https://github.com/symphonyoss/App-Integrations-Salesforce)
> 6. [_App-Integrations-Trello_](https://github.com/symphonyoss/App-Integrations-Trello)
> 7. [_App-Integrations-Zapier_](https://github.com/symphonyoss/App-Integrations-Zapier)

2. Clone the source repository using Git: `git clone git@github.com:symphonyoss/App-Integrations-Core.git`
3. cd into _App-Integrations-Core_
4. Build using maven: `mvn clean install`

# Configuring the project
Here are the initial steps to get your project configured to run using the Intellij IDEA IDE.

1. Import this project into your IDE as a ``maven`` project and be sure to choose JDK 1.7 to run it with.
2. Import any other Integration projects the same way as above (like, let's say, App-Integrations-Github or App-Integrations-Commons), but those are not required.
3. Copy [this file](docs/configuration/idea/IntegrationBridgeApplication.xml) to your "project source folder"/.idea/runConfigurations (feel free to create the last folder if you don't have it yet).
4. Go to ``Run > Edit Configurations...`` and select check the one called "IntegrationBridgeApplication"
5. Check that the referenced folders do exist, they should all be pointing to ``docs/configuration/boot/`` folders, to exemplify the structure you need
6. Obtain valid, PKCS#12 user certificates to your POD and copy those to ``docs/configuration/boot/certs``, you'll need one for each integration.
7. Configure valid addresses to connect the application to on the file [application.yaml](docs/configuration/boot/application.yaml)
8. Run ``IntegrationBridgeApplication`` from the "Run" menu and start watching Intellij run output at the botton of your IDE, if everything works you should see last a message like this one:

> INFO  [org.symphonyoss.integration.core.bootstrap.IntegrationBootstrapContext] (pool-5-thread-1) lMXpkb:d8Gma6:rinXAT INFO Integration salesforceWebHookIntegration bootstrapped successfully
> INFO  [org.symphonyoss.integration.core.bootstrap.IntegrationBootstrapContext] (pool-5-thread-5) lMXpkb:d8Gma6:oOHPJ3 INFO Integration simpleWebHookIntegration bootstrapped successfully
> INFO  [org.symphonyoss.integration.core.bootstrap.IntegrationBootstrapContext] (pool-5-thread-2) lMXpkb:d8Gma6:YNMo9n INFO Integration zapierWebHookIntegration bootstrapped successfully
> INFO  [org.symphonyoss.integration.core.bootstrap.IntegrationBootstrapContext] (pool-5-thread-6) lMXpkb:d8Gma6:uAGbXe INFO Integration jiraWebHookIntegration bootstrapped successfully
> INFO  [org.symphonyoss.integration.core.bootstrap.IntegrationBootstrapContext] (pool-5-thread-3) lMXpkb:d8Gma6:5NWnjN INFO Integration trelloWebHookIntegration bootstrapped successfully
> INFO  [org.symphonyoss.integration.core.bootstrap.IntegrationBootstrapContext] (pool-5-thread-4) lMXpkb:d8Gma6:O9H1Te INFO Integration githubWebHookIntegration bootstrapped successfully
