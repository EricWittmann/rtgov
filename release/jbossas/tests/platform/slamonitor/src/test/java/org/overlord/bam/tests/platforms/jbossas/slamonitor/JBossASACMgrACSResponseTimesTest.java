/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008-12, Red Hat Middleware LLC, and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.overlord.bam.tests.platforms.jbossas.slamonitor;

import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPMessage;

import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.overlord.bam.active.collection.predicate.MVEL;
import org.overlord.bam.active.collection.QuerySpec;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class JBossASACMgrACSResponseTimesTest {

    private static ObjectMapper MAPPER=new ObjectMapper();
    
    private static final String ORDER_SERVICE_URL = "http://127.0.0.1:8080/demo-orders/OrderService";

    private static final String SERVICE_RESPONSE_TIMES = "ServiceResponseTimes";
    
    @Deployment(name="overlord-bam", order=1)
    public static WebArchive createDeployment1() {
        String version=System.getProperty("bam.version");

        java.io.File[] archiveFiles=DependencyResolvers.use(MavenDependencyResolver.class)
                .artifacts("org.overlord.bam.release.jbossas:overlord-bam:war:"+version)
                .resolveAsFiles();
        
        return ShrinkWrap.createFromZipFile(WebArchive.class,
                TestUtils.copyToTmpFile(archiveFiles[0],"overlord-bam.war"));
    }
    
    @Deployment(name="overlord-bam-acs", order=2)
    public static WebArchive createDeployment2() {
        String version=System.getProperty("bam.version");

        java.io.File[] archiveFiles=DependencyResolvers.use(MavenDependencyResolver.class)
                .artifacts("org.overlord.bam.content:overlord-bam-acs:war:"+version)
                .resolveAsFiles();
        
        return ShrinkWrap.createFromZipFile(WebArchive.class,
                TestUtils.copyToTmpFile(archiveFiles[0],"overlord-bam-acs.war"));
    }
    
    @Deployment(name="overlord-bam-epn", order=3)
    public static WebArchive createDeployment3() {
        String version=System.getProperty("bam.version");

        java.io.File[] archiveFiles=DependencyResolvers.use(MavenDependencyResolver.class)
                .artifacts("org.overlord.bam.content:overlord-bam-epn:war:"+version)
                .resolveAsFiles();
        
        return ShrinkWrap.createFromZipFile(WebArchive.class,
                TestUtils.copyToTmpFile(archiveFiles[0],"overlord-bam-epn.war"));
    }
    
    @Deployment(name="orders", order=4)
    public static WebArchive createDeployment4() {
        String version=System.getProperty("bam.version");

        java.io.File[] archiveFiles=DependencyResolvers.use(MavenDependencyResolver.class)
                .artifacts("org.overlord.bam.samples.jbossas.slamonitor:samples-jbossas-slamonitor-orders:war:"+version)
                .resolveAsFiles();
        
        return ShrinkWrap.createFromZipFile(WebArchive.class, archiveFiles[0]);
    }
    
    @Deployment(name="epn", order=5)
    public static WebArchive createDeployment5() {
        String version=System.getProperty("bam.version");

        java.io.File[] archiveFiles=DependencyResolvers.use(MavenDependencyResolver.class)
                .artifacts("org.overlord.bam.samples.jbossas.slamonitor:samples-jbossas-slamonitor-epn:war:"+version)
                .resolveAsFiles();
        
        return ShrinkWrap.createFromZipFile(WebArchive.class, archiveFiles[0]);
    }
    
    @Deployment(name="monitor", order=6)
    public static WebArchive createDeployment6() {
        String version=System.getProperty("bam.version");

        java.io.File[] archiveFiles=DependencyResolvers.use(MavenDependencyResolver.class)
                .artifacts("org.overlord.bam.samples.jbossas.slamonitor:samples-jbossas-slamonitor-monitor:war:"+version)
                .resolveAsFiles();
        
        return ShrinkWrap.createFromZipFile(WebArchive.class,
                        TestUtils.copyToTmpFile(archiveFiles[0],"slamonitor.war"));
    }
    
    @Test @OperateOnDeployment("overlord-bam")
    public void testResponseTimes() {
        
        try {
            SOAPConnectionFactory factory=SOAPConnectionFactory.newInstance();
            SOAPConnection con=factory.createConnection();
            
            java.net.URL url=new java.net.URL(ORDER_SERVICE_URL);
            
            String mesg="<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
                        "   <soap:Body>"+
                        "       <orders:submitOrder xmlns:orders=\"urn:switchyard-quickstart-demo:orders:1.0\">"+
                        "            <order>"+
                        "                <orderId>PO-19838-XYZ</orderId>"+
                        "                <itemId>BUTTER</itemId>"+
                        "                <quantity>200</quantity>"+
                        "            </order>"+
                        "        </orders:submitOrder>"+
                        "    </soap:Body>"+
                        "</soap:Envelope>";
            
            java.io.InputStream is=new java.io.ByteArrayInputStream(mesg.getBytes());
            
            SOAPMessage request=MessageFactory.newInstance().createMessage(null, is);
            
            is.close();
            
            SOAPMessage response=con.call(request, url);

            java.io.ByteArrayOutputStream baos=new java.io.ByteArrayOutputStream();
            
            response.writeTo(baos);
            
            String resp=baos.toString();

            baos.close();
            
            if (!resp.contains("<accepted>true</accepted>")) {
                fail("Order was not accepted: "+resp);
            }
            
            // Wait for events to propagate
            Thread.sleep(4000);
            
            QuerySpec qs1=new QuerySpec();
            qs1.setCollection(SERVICE_RESPONSE_TIMES);
            
            java.util.List<?> result1 = performACMQuery(qs1);
            
            System.out.println("RETRIEVED RESULTS 1="+result1);
            
            if (result1 == null) {
                fail("Result 1 is null");
            }
            
            if (result1.size() != 2) {
                fail("2 events expected, but got: "+result1.size());
            }

            QuerySpec qs2=new QuerySpec();
            qs2.setCollection("OrderService");
            qs2.setParent(SERVICE_RESPONSE_TIMES);
            qs2.setPredicate(new MVEL("serviceType == \"{urn:switchyard-quickstart-demo:orders:0.1.0}OrderService\" && "
                    +"operation == \"submitOrder\""));
            
            java.util.List<?> result2 = performACMQuery(qs2);
            
            System.out.println("RETRIEVED RESULTS 2="+result2);
            
            if (result2 == null) {
                fail("Result 2 is null");
            }
            
            if (result2.size() != 1) {
                fail("1 event expected, but got: "+result2.size());
            }

        } catch (Exception e) {
            fail("Failed to invoke service: "+e);
        }
    }

    public static java.util.List<?> performACMQuery(QuerySpec qs) throws Exception {
        URL getUrl = new URL("http://localhost:8080/overlord-bam/acm/query");
        HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection();
        connection.setRequestMethod("POST");

        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);
        connection.setAllowUserInteraction(false);
        connection.setRequestProperty("Content-Type",
                    "application/json");

        java.io.OutputStream os=connection.getOutputStream();
        
        MAPPER.writeValue(os, qs);
        
        os.flush();
        os.close();
        
        java.io.InputStream is=connection.getInputStream();
        
        java.util.List<?> result=MAPPER.readValue(is, java.util.List.class);
        
        is.close();
        
        return (result);
    }
}