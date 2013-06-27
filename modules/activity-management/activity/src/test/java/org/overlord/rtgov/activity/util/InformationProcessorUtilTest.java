/*
 * 2012-3 Red Hat Inc. and/or its affiliates and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.overlord.rtgov.activity.util;

import static org.junit.Assert.*;

import org.junit.Test;
import org.overlord.rtgov.activity.model.Context;
import org.overlord.rtgov.activity.processor.InformationProcessor;
import org.overlord.rtgov.activity.processor.TypeProcessor;
import org.overlord.rtgov.activity.processor.mvel.MVELExpressionEvaluator;
import org.overlord.rtgov.activity.processor.xpath.XPathExpressionEvaluator;
import org.overlord.rtgov.activity.util.InformationProcessorUtil;

public class InformationProcessorUtilTest {

    private static final String TYPE1 = "Type1";
    private static final String TYPE2 = "Type2";
    private static final String TEST_VERSION = "TestVersion";
    private static final String TEST_IP = "TestIP";

    @Test
    public void test() {
        InformationProcessor ip=new InformationProcessor();
        ip.setName(TEST_IP);
        ip.setVersion(TEST_VERSION);
        
        TypeProcessor tp1=new TypeProcessor();

        XPathExpressionEvaluator evaluator1=new XPathExpressionEvaluator();
        
        evaluator1.setExpression("/ns1:mydoc/ns2:field");
        
        evaluator1.getNamespaces().put("ns1", "http://www.mynamespace");
        evaluator1.getNamespaces().put("ns2", "http://www.myothernamespace");
        
        TypeProcessor.ContextEvaluator ce1=new TypeProcessor.ContextEvaluator();
        ce1.setEvaluator(evaluator1);
        ce1.setType(Context.Type.Conversation);
        
        tp1.getContexts().add(ce1);
        
        XPathExpressionEvaluator evaluator2=new XPathExpressionEvaluator();        
        evaluator2.setExpression("/mydoc/field");
        
        TypeProcessor.PropertyEvaluator pe1=new TypeProcessor.PropertyEvaluator();
        pe1.setEvaluator(evaluator2);
        pe1.setName("Prop1");
        
        tp1.getProperties().add(pe1);        

        ip.getTypeProcessors().put(TYPE1, tp1);
        
        TypeProcessor tp2=new TypeProcessor();

        MVELExpressionEvaluator evaluator3=new MVELExpressionEvaluator();        
        evaluator3.setExpression("mydoc.field");
        
        TypeProcessor.PropertyEvaluator pe2=new TypeProcessor.PropertyEvaluator();
        pe2.setEvaluator(evaluator3);
        pe2.setName("Prop2");
        
        tp2.getProperties().add(pe2);        

        ip.getTypeProcessors().put(TYPE2, tp2);

        try {
            java.util.List<InformationProcessor> ips=new java.util.Vector<InformationProcessor>();
            ips.add(ip);
            
            byte[] b=InformationProcessorUtil.serializeInformationProcessorList(ips);
            
            if (b == null) {
                fail("null returned");
            }
            
            java.io.InputStream is=InformationProcessorUtilTest.class.getResourceAsStream("/json/ipList.json");
            byte[] inb2=new byte[is.available()];
            is.read(inb2);
            is.close();
            
            java.util.List<InformationProcessor> ips2=
                    InformationProcessorUtil.deserializeInformationProcessorList(inb2);
            
            byte[] b2=InformationProcessorUtil.serializeInformationProcessorList(ips2);            
            
            String s1=new String(b);
            String s2=new String(b2);
            
            if (!s1.equals(s2)) {
                fail("JSON is different: created="+s1+" stored="+s2);
            }

        } catch(Exception e) {
            e.printStackTrace();
            fail("Failed to serialize: "+e);
        }
    }
  
}
