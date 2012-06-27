SLA Monitor Sample
==================

This example demonstrates how a Switchyard application can be instrumented, to intercept service
communications and report them to the BAM infrastructure.

An example Event Processor Network (EPN) has been defined to check the response time of the service
and report issues to an active collection, which accumulates the results and can be used to provide
active updates to any interested listeners.


Deploying the example
---------------------

1) Download and unpack a Switchyard AS7 distribution (http://www.jboss.org/switchyard/downloads)

2) Copy the contents of the ${bam}/modules folder into the ${as7}/modules folder

3) Startup the server, using the following command from the 'bin' folder:

./standalone.sh --server-config standalone-full.xml

This starts the full configuration, as this includes JMS.

4) Deploy the overlord-bam.war from the ${bam} top level folder into the ${as7}/standalone/deployments folder

5) Build the SLA Monitor sample, using the following command from the ${bam}/samples/slamonitor folder:

mvn clean install

6) Copy the sample archives for each of the components (acs, epn, monitor and orders) from
${bam}/samples/slamonitor/${component}/target/slamonitor-${component}.war file
into the ${as7}/standalone/deployments folder.

NOTE: The components are as follows:

- acs: this component represents the active collections responsible for collecting the information
to be presented via the REST service.

- epn: this component represents the Event Processor Network (EPN) used to calculate response times
and detect SLA violations.

- monitor: this component provides the REST service for colating the response time and SLA violation
information for access by a client application

- orders: this component represents the Switchyard application with additional 'Exchange Handler'
used to intercept service communications and report them to the BAM infrastructure.



Running the example
-------------------

Startup a SOAP client (e.g. SOAPUI) and send an example message to the URL:

http://127.0.0.1:8080/demo-orders/OrderService

The initial message could be:

<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
    <soap:Body>
        <orders:submitOrder xmlns:orders="urn:switchyard-quickstart-demo:orders:1.0">
            <order>
                <orderId>PO-19838-XYZ</orderId>
                <itemId>BUTTER</itemId>
                <quantity>400</quantity>
            </order>
        </orders:submitOrder>
    </soap:Body>
</soap:Envelope>

which should result in an 'order accepted'. Then send the following message:

<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
    <soap:Body>
        <orders:submitOrder xmlns:orders="urn:switchyard-quickstart-demo:orders:1.0">
            <order>
                <orderId>PO-19838-XYZ</orderId>
                <itemId>BUTTER</itemId>
                <quantity>1400</quantity>
            </order>
        </orders:submitOrder>
    </soap:Body>
</soap:Envelope>

which should result in an 'insufficient quantity' response.


To perform a request that will result in an SLA violation, change the <itemId> value to JAM
and re-issue the request.


To access the information being accumulated in the active collections, you will need a REST
client, e.g. http://restclient.net/ to issue requests from a browser. Use the following
URLs to obtain information from the REST service:

http://localhost:8080/slamonitor-monitor/monitor/responseTimes - returns all response times

http://localhost:8080/slamonitor-monitor/monitor/responseTimes?operation=submitOrder - returns only response times for this operation

http://localhost:8080/slamonitor-monitor/monitor/violations - returns all SLA violations

You can also view the SLA violations using a JMX console (e.g. jconsole). The example registers an MBean with the object name 'overlord.sample.slamonitor:name=SLAViolations'. Subscribe to the notifications for this management bean to also see the violations.



