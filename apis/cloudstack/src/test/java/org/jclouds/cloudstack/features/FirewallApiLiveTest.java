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

import static com.google.common.collect.Iterables.find;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jclouds.cloudstack.predicates.NetworkPredicates.supportsPortForwarding;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.NoSuchElementException;
import java.util.Set;

import org.jclouds.cloudstack.domain.AsyncCreateResponse;
import org.jclouds.cloudstack.domain.FirewallRule;
import org.jclouds.cloudstack.domain.Network;
import org.jclouds.cloudstack.domain.PortForwardingRule;
import org.jclouds.cloudstack.domain.PublicIPAddress;
import org.jclouds.cloudstack.domain.VirtualMachine;
import org.jclouds.cloudstack.internal.BaseCloudStackApiLiveTest;
import org.jclouds.cloudstack.options.CreateFirewallRuleOptions;
import org.jclouds.logging.Logger;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.net.HostAndPort;

/**
 * Tests behavior of {@code FirewallApiLiveTest}
 */
@Test(groups = "live", singleThreaded = true, testName = "FirewallApiLiveTest")
public class FirewallApiLiveTest extends BaseCloudStackApiLiveTest {
   private PublicIPAddress ip = null;
   private VirtualMachine vm;

   private FirewallRule firewallRule;
   private FirewallRule egressFirewallRule;
   private PortForwardingRule portForwardingRule;

   private Network network;
   private boolean networksDisabled;

   @BeforeGroups(groups = "live")
   public void setupContext() {
      super.setupContext();
      prefix += "rule";
      try {
         network = find(client.getNetworkApi().listNetworks(), Predicates.and(supportsPortForwarding(),
            new Predicate<Network>() {
               @Override
               public boolean apply(Network network) {
                  return network.isDefault()
                     && !network.isSecurityGroupEnabled()
                     && network.getAccount().equals(user.getAccount());
               }
            }));

         String defaultTemplate = template != null ? template.getImageId() : null;

         vm = VirtualMachineApiLiveTest.createVirtualMachineInNetwork(network,
            defaultTemplateOrPreferredInZone(defaultTemplate, client, network.getZoneId()),
            client, jobComplete, virtualMachineRunning);

         if (vm.getPassword() != null && !loginCredentials.getOptionalPassword().isPresent())
            loginCredentials = loginCredentials.toBuilder().password(vm.getPassword()).build();

      } catch (NoSuchElementException e) {
         networksDisabled = true;
      }
   }

   public void testCreatePortForwardingRule() throws Exception {
      if (networksDisabled)
         return;
      while (portForwardingRule == null) {
         ip = reuseOrAssociate.apply(network);
         try {
            AsyncCreateResponse job = client.getFirewallApi()
               .createPortForwardingRuleForVirtualMachine(ip.getId(), PortForwardingRule.Protocol.TCP, 22, vm.getId(), 22);
            assertTrue(jobComplete.apply(job.getJobId()));
            portForwardingRule = client.getFirewallApi().getPortForwardingRule(job.getId());

         } catch (IllegalStateException e) {
            Logger.CONSOLE.error("Failed while trying to allocate ip: " + e);
            // very likely an ip conflict, so retry;
         }
      }

      assertEquals(portForwardingRule.getIPAddressId(), ip.getId());
      assertEquals(portForwardingRule.getVirtualMachineId(), vm.getId());
      assertEquals(portForwardingRule.getPublicPort(), 22);
      assertEquals(portForwardingRule.getProtocol(), PortForwardingRule.Protocol.TCP);

      checkPortForwardingRule(portForwardingRule);
      checkSSH(HostAndPort.fromParts(ip.getIPAddress(), 22));
   }

   @Test(dependsOnMethods = "testCreatePortForwardingRule")
   public void testListPortForwardingRules() throws Exception {
      Set<PortForwardingRule> response = client.getFirewallApi().listPortForwardingRules();
      assertThat(null != response).isTrue();
      assertTrue(response.size() > 0);
      for (final PortForwardingRule rule : response) {
         checkPortForwardingRule(rule);
      }
   }

   @Test(dependsOnMethods = "testCreatePortForwardingRule")
   public void testCreateFirewallRule() {
      if (networksDisabled)
         return;

      AsyncCreateResponse job = client.getFirewallApi().createFirewallRuleForIpAndProtocol(
         ip.getId(), FirewallRule.Protocol.TCP, CreateFirewallRuleOptions.Builder.startPort(30).endPort(35));
      assertTrue(jobComplete.apply(job.getJobId()));
      firewallRule = client.getFirewallApi().getFirewallRule(job.getId());

      assertEquals(firewallRule.getStartPort(), 30);
      assertEquals(firewallRule.getEndPort(), 35);
      assertEquals(firewallRule.getProtocol(), FirewallRule.Protocol.TCP);

      checkFirewallRule(firewallRule);
   }

   @Test(dependsOnMethods = "testCreateFirewallRule")
   public void testListFirewallRules() {
      Set<FirewallRule> rules = client.getFirewallApi().listFirewallRules();

      assertThat(rules != null).isTrue();
      assertTrue(!rules.isEmpty());

      for (FirewallRule rule : rules) {
         checkFirewallRule(rule);
      }
   }

   @Test(dependsOnMethods = "testCreatePortForwardingRule")
   public void testCreateEgressFirewallRule() {
      if (networksDisabled)
         return;

      AsyncCreateResponse job = client.getFirewallApi().createEgressFirewallRuleForNetworkAndProtocol(
              network.getId(), FirewallRule.Protocol.TCP, CreateFirewallRuleOptions.Builder.startPort(30).endPort(35));
      assertTrue(jobComplete.apply(job.getJobId()));
      egressFirewallRule = client.getFirewallApi().getEgressFirewallRule(job.getId());

      assertEquals(egressFirewallRule.getStartPort(), 30);
      assertEquals(egressFirewallRule.getEndPort(), 35);
      assertEquals(egressFirewallRule.getProtocol(), FirewallRule.Protocol.TCP);

      checkEgressFirewallRule(egressFirewallRule);
   }

   @Test(dependsOnMethods = "testCreateEgressFirewallRule")
   public void testListEgressFirewallRules() {
      Set<FirewallRule> rules = client.getFirewallApi().listEgressFirewallRules();

      assertThat(rules != null).isTrue();
      assertTrue(!rules.isEmpty());

      for (FirewallRule rule : rules) {
         checkEgressFirewallRule(rule);
      }
   }
   @AfterGroups(groups = "live")
   @Override
   protected void tearDownContext() {
      if (firewallRule != null) {
         client.getFirewallApi().deleteFirewallRule(firewallRule.getId());
      }
      if (egressFirewallRule != null) {
         client.getFirewallApi().deleteEgressFirewallRule(egressFirewallRule.getId());
      }
      if (portForwardingRule != null) {
         client.getFirewallApi().deletePortForwardingRule(portForwardingRule.getId());
      }
      if (vm != null) {
         jobComplete.apply(client.getVirtualMachineApi().destroyVirtualMachine(vm.getId()));
      }
      if (ip != null) {
         client.getAddressApi().disassociateIPAddress(ip.getId());
      }
      super.tearDownContext();
   }

   protected void checkFirewallRule(FirewallRule rule) {
      assertEquals(rule,
         client.getFirewallApi().getFirewallRule(rule.getId()));
      assertThat(rule.getId() != null).as(String.valueOf(rule)).isTrue();
      assertThat(rule.getStartPort() > 0).as(String.valueOf(rule)).isTrue();
      assertThat(rule.getEndPort() >= rule.getStartPort()).as(String.valueOf(rule)).isTrue();
      assertThat(rule.getProtocol() != null).isTrue();
   }

   protected void checkEgressFirewallRule(FirewallRule rule) {
      assertEquals(rule,
              client.getFirewallApi().getEgressFirewallRule(rule.getId()));
      assertThat(rule.getId() != null).as(String.valueOf(rule)).isTrue();
      assertThat(rule.getStartPort() > 0).as(String.valueOf(rule)).isTrue();
      assertThat(rule.getEndPort() >= rule.getStartPort()).as(String.valueOf(rule)).isTrue();
      assertThat(rule.getProtocol() != null).isTrue();
   }

   protected void checkPortForwardingRule(PortForwardingRule rule) {
      assertEquals(rule,
         client.getFirewallApi().getPortForwardingRule(rule.getId()));
      assertThat(rule.getId() != null).as(String.valueOf(rule)).isTrue();
      assertThat(rule.getIPAddress() != null).as(String.valueOf(rule)).isTrue();
      assertThat(rule.getIPAddressId() != null).as(String.valueOf(rule)).isTrue();
      assertThat(rule.getPrivatePort() > 0).as(String.valueOf(rule)).isTrue();
      assertThat(rule.getProtocol() != null).as(String.valueOf(rule)).isTrue();
      assertThat(rule.getPublicPort() > 0).as(String.valueOf(rule)).isTrue();
      assertThat(rule.getState() != null).as(String.valueOf(rule)).isTrue();
      assertThat(rule.getVirtualMachineId() != null).as(String.valueOf(rule)).isTrue();
      assertThat(rule.getVirtualMachineName() != null).as(String.valueOf(rule)).isTrue();
   }
}
