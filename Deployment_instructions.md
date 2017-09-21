# Symphony App Integrations

Symphony App Integrations allow 3rd party services like JIRA and GitHub to feed content straight into Symphony chats. To protect customer confidentiality and simplify the provisioning process, a new customer-controlled component, the Integration Bridge, is required. The Integration Bridge is a software component running on Spring Boot that controls a set of Webhook Integration Bots (one for each 3rd-party service).

## Getting Started

These instructions will generate an instance of the Integration Bridge for testing or production purposes. The same set of instructions can be used for both cloud and on-prem deployments.

### Prerequisites

The following items are required to install the Integration Bridge:

* Centos (6.5 or 7.0)
* Java 7
* Open SSL
* CA certificate
* CA key
* Admin user certificate (steps to generate included below)
* Symphony Agent supporting messageML V2

The provisioning tool makes use of a service account to provision data on the POD. You must create this account on the POD with the "User Provisioning" role, and then generate a user certificate to allow the provisioning tool to connect to the POD via that user.

You can use the following commands to generate the service account certificate. The CA Cert must be imported on the POD via the Admin Console:

1. openssl genrsa -aes256 -passout pass:$PASSWORD -out admin-key.pem 2048
2. openssl req -new -key admin-key.pem -passin pass:$PASSWORD -subj "/CN=$USERNAME/O=Symphony Communications LLC/OU=NOT FOR PRODUCTION USE/C=US" -out admin-req.pem
3. openssl x509 -req -sha256 -days 2922 -in admin-req.pem -CA $CA_CERT -CAkey $CA_KEY -passin pass:$CA_PASSWORD -out admin-cert.pem -set_serial 0x1
4. openssl pkcs12 -export -out admin.p12 -aes256 -in admin-cert.pem -inkey admin-key.pem -passin pass:$PASSWORD -passout pass:$OUTPUT_PASSWORD

* USERNAME = Service account username
* PASSWORD = Service account key password
* CA_CERT = CA certificate file
* CA_KEY = CA key file
* CA_PASSWORD = CA key password
* OUTPUT_PASSWORD = PKCS12 file password

Confirm that the CA certificate file, CA key file, and newly generated admin.p12 file are available on the machine from which you will perform the steps below. In the event you are installing the Integration Bridge on multiple machines, you will only need the aforementioned files on one of the machines. The provisioning process must only be performed a single time, from a single machine, whereas the deployment of the bridge can be performed any number of times, on any number of machines that comprise your Symphony pod.

## Installing

1. Get the latest version of the "app-integrations-{build_version}-{build_number}.tar.gz" tarball file containing the packages to install the available Apps, and extract its contents to a local directory on each machine that will run the Integration Bridge.

   ```
   tar -xvzf app-integrations-{build_version}-{build_number}.tar.gz
   ```

2. The tarball contains the following structure:

	* app-integrations-{build_version}/
	* app-integrations-{build_version}/README.md
	* app-integrations-{build_version}/cert_gen.sh
	* app-integrations-{build_version}/install.sh
	* app-integrations-{build_version}/data/
	* app-integrations-{build_version}/data/symphony/
	* app-integrations-{build_version}/data/symphony/ib/
	* app-integrations-{build_version}/data/symphony/ib/certs/
	* app-integrations-{build_version}/data/symphony/ib/application.yaml.example
	* app-integrations-{build_version}/data/symphony/ib/logs/
	* app-integrations-{build_version}/data/symphony/ib/bin/
	* app-integrations-{build_version}/data/symphony/ib/bin/taillog.sh
	* app-integrations-{build_version}/data/symphony/ib-provisioning/
	* app-integrations-{build_version}/data/symphony/ib-provisioning/logs/
	* app-integrations-{build_version}/opt/
	* app-integrations-{build_version}/opt/symphony/
	* app-integrations-{build_version}/opt/symphony/ib/
	* app-integrations-{build_version}/opt/symphony/ib/integration.jar
	* app-integrations-{build_version}/opt/symphony/ib/libs/
	* app-integrations-{build_version}/opt/symphony/ib/libs/integration-salesforce-{build_version}.jar
	* app-integrations-{build_version}/opt/symphony/ib/libs/integration-zapier-{build_version}.jar
	* app-integrations-{build_version}/opt/symphony/ib/libs/integration-jira-{build_version}.jar
	* app-integrations-{build_version}/opt/symphony/ib/libs/integration-trello-{build_version}.jar
	* app-integrations-{build_version}/opt/symphony/ib/libs/integration-universal-whi-{build_version}.jar
	* app-integrations-{build_version}/opt/symphony/ib/libs/integration-github-{build_version}.jar
	* app-integrations-{build_version}/opt/symphony/ib-provisioning/
	* app-integrations-{build_version}/opt/symphony/ib-provisioning/bin/
	* app-integrations-{build_version}/opt/symphony/ib-provisioning/bin/provisioning.jar
	* app-integrations-{build_version}/opt/symphony/ib-provisioning/library/
	* app-integrations-{build_version}/opt/symphony/ib-provisioning/library/startup.sh.shell.install.template

   If cloud logging is going to be enabled, also install the cloud logging library(integration-logging-{build_version}.jar) at app-integrations-{build_version}/opt/symphony/ib/libs/

3. Fill in your YAML config file based on the sample. The sample is located at [app-integrations-{build_version}/data/symphony/ib/application.yaml.example](app-integrations-{build_version}/data/symphony/ib/application.yaml.example). You should copy this sample file and customize your own with the settings for your specific deployment, including the passwords for any certificates used by the Integration Bridge during provisioning and runtime. The YAML file contains comments for how to fill in the required information. If installing the Integration Bridge on multiple machines, copy this modified YAML file so it can be found on each machine.

4a. A bash script is used to provision the Integration Bridge and all of the apps configured in the YAML file.

   The following command will display instructions.

   ```
   app-integrations-{build_version}/install.sh -h
   ```
   
   If installing the Integration Bridge on multiple machines, run the following command first on one of the machines and refer to step 4b. 
   Parameter --config-location should be followed by the path of the application.yaml file created on step 3.
   Parameter --cloud-logging should be followed by the POD host and port. If cloud logging should not be enabled, --no-cloud-logging can be used instead.
   Parameter --generate-certs should be used to generate certificates. If you already have the certificates, you shouldn't use this parameter. Instead, put those certificates in app-integrations-{build_version}/data/symphony/ib/certs/.

   ```
   app-integrations-{build_version}/install.sh --cloud-logging example.symphony.com:443 --config-location app-integrations-{build_version}/data/symphony/ib --action provision --generate-certs
   ```

   If you have certificates and should not enable cloud logging:

   
   ```
   app-integrations-{build_version}/install.sh --no-cloud-logging --config-location app-integrations-{build_version}/data/symphony/ib --action provision
   ```
   
4b. The following section is necessary only if installing on multiple machines. After you have run the appropriate install command on one machine, copy the application.yaml and the certificates located at app-integrations-{build_version}/data/symphony/ib/certs/ and recreate it on any remaining machines. After doing so, run a modified version of the command on each remaining machine. This modified version will skip the "provisioning" step, which is now complete, and will only "setup" the Integration Bridge and apps:

   ```
   app-integrations-{build_version}/install.sh --cloud-logging example.symphony.com:443 --config-location app-integrations-{build_version}/data/symphony/ib --action setup
   ```

Note: The following directories contain the deployment of the Integration Bridge.

   * app-integrations-{build_version}/opt/symphony/ib contains the Integration Bridge application
   * app-integrations-{build_version}/opt/symphony/ib/libs contains the applications served by the Spring Boot
   * app-integrations-{build_version}/data/symphony/ib contains application configuration, certs and logs

5. After install, start the Integration Bridge process on each machine
   
   ```
   app-integrations-{build_version}/data/symphony/ib/bin/startup.sh
   ```

6. You can check the Integration Bridge logs at app-integrations-{build_version}/data/symphony/ib/logs/integration-bridge.log

## Upgrading version

Those steps must be followed on each machine you have deployed Integration Bridge.

1. Get the new version of the "app-integrations-{build_version}-{build_number}.tar.gz" tarball file containing the packages to install the available Apps, and extract its contents to a local directory on each machine that will run the Integration Bridge.

   ```
   tar -xvzf app-integrations-{build_version}-{build_number}.tar.gz
   ```

2. Copy the certificate files and YAML config file from the old version to the new version.

   ```
   cp app-integrations-{old_version}/data/symphony/ib/application.yaml app-integrations-{build_version}/data/symphony/ib/application.yaml
   cp app-integrations-{old_version}/data/symphony/ib/certs/* app-integrations-{build_version}/data/symphony/ib/certs/

   ```

3. Run the install script using the 'setup' action.

   ```
   app-integrations-{build_version}/install.sh --config-location app-integrations-{build_version}/data/symphony/ib --no-cloud-logging --action setup
   ```

4. Stop the Integration Bridge process that already running.

5. After install, start the Integration Bridge process on each machine
   
   ```
   app-integrations-{build_version}/data/symphony/ib/bin/startup.sh
   ```

## Enable SSL (HTTPS) in your application

If you want to enable SSL in your application, some further configuration in the [application.yaml](app-integrations-{build_version}/data/symphony/ib/application.yaml.example) file is necessary. Set the attribute ``server.ssl.enabled`` to ``true`` and fill remaining information, such as: ``key-alias, key-store, key-store-type, key-store-password and key-password``. *Note: If the integration bridge server is not behind a [proxy](#setup-integration-bridge-proxy), the attribute ``server.port`` must be set to ``443``, which is the default HTTPS port and is used on the webhook URL's created by the Configurator Apps.*

## Setup Integration Bridge Proxy

The Integration Bridge runs as a Spring Boot application, serving requests on the server port indicated by the "server.port" property in the "application.yaml" file.

If [enabling SSL directly in your application](#enable-ssl-https-in-your-application) is not an option, you can setup a proxy to provide access to the Integration Bridge over HTTPS. The proxy should be accessible at the address indicated by the "integration_bridge.host" property. The port for the proxy must be 443. For instance, if "integration_bridge.host" is "example.symphony.com", the proxy should be available at "https://example.symphony.com/".

The proxy should be configured with the proper certificates and should provide access to the routes "/integration" and "/apps".  

For example, if using NGINX, and assuming the "server.port" is 8186 in "application.yaml", the routes would be specified as follows:

```
location /integration {
   proxy_pass http://127.0.0.1:8186/integration/;
   proxy_set_header X-Real-IP $remote_addr;
   proxy_set_header Connection 'upgrade';
   proxy_set_header Host host;
   proxy_set_header Upgrade $http_upgrade;
   proxy_http_version 1.1;
}

location /apps {
   proxy_pass http://127.0.0.1:8186/apps/;
   proxy_set_header X-Real-IP $remote_addr;
   proxy_set_header Connection 'upgrade';
   proxy_set_header Host host;
   proxy_set_header Upgrade $http_upgrade;
   proxy_http_version 1.1;
}
```

### High-Availability deployments
In the event that the Integration Bridge is running on more than a single machine, due to high availability or high throughput requirements, the proxy settings described above should be done on the load-balancer that distributes traffic to the Integration Bridge instances.

## Verifying Installation

After the installation process is finished, perform the following steps to confirm the provisioning and deployment of the Integration Bridge have been completed successfully.

1. Check the Integration Bridge health by opening the health-check URL on the browser and making sure the Integration Bridge status is "UP", and the applications are all "ACTIVE":

   ```
   https://example.symphony.com/integration/health
   ```

2. Open Symphony Client in a web browser and login with a standard (non-admin) Symphony user.

3. Go to APPLICATIONS > **Symphony Market** to verify that the applications that have been provisioned are listed, and that it is possible to add and configure them. For instance, click "Add" for the JIRA application and then click "Configure". The configuration page for "JIRA" should be opened.

4. Using the instructions available on the configuration page, create a new webhook for the JIRA application. Once the webhook is created, JIRA Bot will send a welcome message to the room you specified, or directly to you in an IM.

5. In JIRA, configure your JIRA project to post to the provided webhook when a specific event occurs. Upon triggering this event in JIRA, confirm that JIRA Bot has posted to the specified room/IM.

6. This validation should be repeated for all the installed integrations. For the Universal Webhook Integration, you can send a test message to the webhook URL using curl, as indicated below. Replace the URL specified in the below curl with the URL provided by the integration at the time of configuration.

   ```
   curl -X POST "https://example.symphony.com/integration/v1/whi/universalWebHookIntegration/5820d5c5e4b06c3c6763dc1d/5823ae87e4b0601ca442c31f" --data "<messageML>Hello</messageML>" -H "Content-Type: application/xml"
   ```