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
package org.savara.bam.tests.platforms.jbossas;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPMessage;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class JBossASTest {

    @Deployment(name="savara-bam", order=1)
    public static WebArchive createDeployment1() {
        String version=System.getProperty("bam.version");
        String platform=System.getProperty("bam.platform");

        java.io.File[] archiveFiles=DependencyResolvers.use(MavenDependencyResolver.class)
                .artifacts("org.savara.bam.distribution:savara-bam:war:"+platform+":"+version)
                .resolveAsFiles();
        
        return ShrinkWrap.createFromZipFile(WebArchive.class,
                copyToTmpFile(archiveFiles[0],"savara-bam.war"));
    }
    
    @Deployment(name="orders", order=2)
    public static WebArchive createDeployment2() {
        String version=System.getProperty("bam.version");

        java.io.File[] archiveFiles=DependencyResolvers.use(MavenDependencyResolver.class)
                .artifacts("org.savara.bam.tests.platforms.jbossas:tests-jbossas-orders:war:"+version)
                .resolveAsFiles();
        
        return ShrinkWrap.createFromZipFile(WebArchive.class, archiveFiles[0]);
    }
    
    @Deployment(name="epn", order=3)
    public static WebArchive createDeployment3() {
        String version=System.getProperty("bam.version");

        java.io.File[] archiveFiles=DependencyResolvers.use(MavenDependencyResolver.class)
                .artifacts("org.savara.bam.tests.platforms:tests-platforms-epn:war:"+version)
                .resolveAsFiles();
        
        return ShrinkWrap.createFromZipFile(WebArchive.class, archiveFiles[0]);
    }
    
    private static java.io.File copyToTmpFile(java.io.File source, String filename) {
        String tmpdir=System.getProperty("java.io.tmpdir");
        java.io.File dir=new java.io.File(tmpdir+java.io.File.separator+"bamtests"+System.currentTimeMillis());
        
        dir.mkdir();
        
        dir.deleteOnExit();
        
        java.io.File ret=new java.io.File(dir, filename);
        ret.deleteOnExit();
        
        // Copy contents to the tmp file
        try {
            java.io.FileInputStream fis=new java.io.FileInputStream(source);
            java.io.FileOutputStream fos=new java.io.FileOutputStream(ret);
            
            byte[] b=new byte[10240];
            int len=0;
            
            while ((len=fis.read(b)) > 0) {
                fos.write(b, 0, len);
            }
            
            fis.close();
            
            fos.flush();
            fos.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to copy file '"+filename+"': "+e);
        }
        
        return(ret);
    }

    @Test @OperateOnDeployment("savara-bam")
    public void testMethod() {
        
        // TODO: Register node listener to check that epn events are processed

        try {
            SOAPConnectionFactory factory=SOAPConnectionFactory.newInstance();
            SOAPConnection con=factory.createConnection();
            
            java.net.URL url=new java.net.URL("http://127.0.0.1:18001/demo-orders/OrderService");
            
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
            Thread.sleep(2000);
            
        } catch (Exception e) {
            fail("Failed to invoke service via SOAP: "+e);
        }
    }
}