/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jclouds.cloudstack.features;

import static com.google.common.collect.Iterables.getOnlyElement;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Set;

import org.jclouds.cloudstack.domain.IPForwardingRule;
import org.jclouds.cloudstack.internal.BaseCloudStackApiLiveTest;
import org.jclouds.cloudstack.options.ListIPForwardingRulesOptions;
import org.testng.annotations.Test;

/**
 * Tests behavior of {@code NATApiLiveTest}
 */
@Test(groups = "live", singleThreaded = true, testName = "NATApiLiveTest")
public class NATApiLiveTest extends BaseCloudStackApiLiveTest {

   @Test(enabled = false)
   // takes too long
   public void testListIPForwardingRules() throws Exception {
      Set<IPForwardingRule> response = client.getNATApi().listIPForwardingRules();
      assertThat(null != response).isTrue();
      assertTrue(response.size() > 0);
      for (IPForwardingRule rule : response) {
         IPForwardingRule newDetails = getOnlyElement(client.getNATApi().listIPForwardingRules(
               ListIPForwardingRulesOptions.Builder.id(rule.getId())));
         assertEquals(rule.getId(), newDetails.getId());
         checkRule(rule);
      }
   }

   protected void checkRule(IPForwardingRule rule) {
      assertEquals(rule.getId(), client.getNATApi().getIPForwardingRule(rule.getId()).getId());
      assertThat(rule.getId() != null).as(String.valueOf(rule)).isTrue();
      assertThat(rule.getIPAddress() != null).as(String.valueOf(rule)).isTrue();
      assertThat(rule.getIPAddressId() != null).as(String.valueOf(rule)).isTrue();
      assertThat(rule.getStartPort() > 0).as(String.valueOf(rule)).isTrue();
      assertThat(rule.getProtocol() != null).as(String.valueOf(rule)).isTrue();
      assertThat(rule.getEndPort() > 0).as(String.valueOf(rule)).isTrue();
      assertThat(rule.getState() != null).as(String.valueOf(rule)).isTrue();
      assertThat(rule.getVirtualMachineId() != null).as(String.valueOf(rule)).isTrue();
      assertThat(rule.getVirtualMachineName() != null).as(String.valueOf(rule)).isTrue();

   }
}
