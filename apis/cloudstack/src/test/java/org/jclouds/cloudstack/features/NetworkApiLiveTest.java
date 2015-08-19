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
import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.getOnlyElement;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jclouds.cloudstack.options.CreateNetworkOptions.Builder.vlan;
import static org.jclouds.cloudstack.options.ListNetworkOfferingsOptions.Builder.specifyVLAN;
import static org.jclouds.cloudstack.options.ListNetworksOptions.Builder.accountInDomain;
import static org.jclouds.cloudstack.options.ListNetworksOptions.Builder.id;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.net.URI;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jclouds.cloudstack.domain.GuestIPType;
import org.jclouds.cloudstack.domain.Network;
import org.jclouds.cloudstack.domain.NetworkOffering;
import org.jclouds.cloudstack.domain.Zone;
import org.jclouds.cloudstack.internal.BaseCloudStackApiLiveTest;
import org.jclouds.cloudstack.predicates.NetworkOfferingPredicates;
import org.jclouds.cloudstack.predicates.ZonePredicates;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

/**
 * Tests behavior of {@code NetworkApiLiveTest}
 */
@Test(groups = "live", singleThreaded = true, testName = "NetworkApiLiveTest")
public class NetworkApiLiveTest extends BaseCloudStackApiLiveTest {

   private boolean networksSupported;
   private Zone zone;

   @BeforeGroups(groups = "live")
   public void setupContext() {
      super.setupContext();

      try {
         zone = find(client.getZoneApi().listZones(), ZonePredicates.supportsAdvancedNetworks());
         networksSupported = true;
      } catch (NoSuchElementException e) {
      }
   }

   @Test
   public void testCreateGuestVirtualNetwork() {
      if (!networksSupported)
         return;
      final NetworkOffering offering;
      try {
         offering = find(client.getOfferingApi().listNetworkOfferings(),
               NetworkOfferingPredicates.supportsGuestVirtualNetworks());

      } catch (NoSuchElementException e) {
         Logger.getAnonymousLogger().log(Level.SEVERE, "guest networks not supported, skipping test");
         return;
      }
      String name = prefix + "-virtual";

      Network network = null;
      try {
         network = client.getNetworkApi().createNetworkInZone(zone.getId(), offering.getId(), name, name);
         checkNetwork(network);
      } catch (IllegalStateException e) {
         Logger.getAnonymousLogger().log(Level.SEVERE, "couldn't create a network, skipping test", e);
      } finally {
         if (network != null) {
            String jobId = client.getNetworkApi().deleteNetwork(network.getId());
            if (jobId != null)
               jobComplete.apply(jobId);
         }
      }
   }

   @Test
   public void testCreateVLANNetwork() {
      skipIfNotDomainAdmin();
      if (!networksSupported)
         return;

      final NetworkOffering offering;
      try {
         offering = get(
               cloudStackContext.getApi().getOfferingApi().listNetworkOfferings(specifyVLAN(true).zoneId(zone.getId())), 0);
      } catch (NoSuchElementException e) {
         Logger.getAnonymousLogger().log(Level.SEVERE, "VLAN networks not supported, skipping test");
         return;
      }
      String name = prefix + "-vlan";

      Network network = null;
      try {
         network = domainAdminClient
               .getNetworkApi()
               // startIP/endIP/netmask/gateway must be specified together
               .createNetworkInZone(zone.getId(), offering.getId(), name, name,
                     vlan("65").startIP("192.168.1.2").netmask("255.255.255.0").gateway("192.168.1.1"));
         checkNetwork(network);
      } catch (IllegalStateException e) {
         Logger.getAnonymousLogger().log(Level.SEVERE, "couldn't create a network, skipping test", e);
      } finally {
         if (network != null) {
            String jobId = adminClient.getNetworkApi().deleteNetwork(network.getId());
            if (jobId != null)
               adminJobComplete.apply(jobId);
         }
      }
   }

   @Test
   public void testListNetworks() throws Exception {
      if (!networksSupported)
         return;
      Set<Network> response = client.getNetworkApi().listNetworks(
            accountInDomain(user.getAccount(), user.getDomainId()));
      assertThat(null != response).isTrue();
      long networkCount = response.size();
      assertTrue(networkCount >= 0);
      for (Network network : response) {
         Network newDetails = getOnlyElement(client.getNetworkApi().listNetworks(id(network.getId())));
         assertEquals(network, newDetails);
         assertEquals(network, client.getNetworkApi().getNetwork(network.getId()));
         checkNetwork(network);
      }
   }

   private void checkNetwork(Network network) {
      assertThat(network.getId() != null).as(String.valueOf(network)).isTrue();
      assertThat(network.getName() != null).as(String.valueOf(network)).isTrue();
      assertThat(network.getDNS().size() != 0).as(String.valueOf(network)).isTrue();
      assertThat(network.getGuestIPType() != null && network.getGuestIPType() != GuestIPType.UNRECOGNIZED).as(String.valueOf(network)).isTrue();
      assertThat(network.getBroadcastDomainType() != null).as(String.valueOf(network)).isTrue();
      assertThat(network.getDisplayText() != null).as(String.valueOf(network)).isTrue();
      // Network domain can be null sometimes
      // assert network.getNetworkDomain() != null : network;
      assertThat(network.getNetworkOfferingAvailability() != null).as(String.valueOf(network)).isTrue();
      assertThat(network.getNetworkOfferingDisplayText() != null).as(String.valueOf(network)).isTrue();
      assertThat(network.getNetworkOfferingId() != null).as(String.valueOf(network)).isTrue();
      assertThat(network.getNetworkOfferingName() != null).as(String.valueOf(network)).isTrue();
      assertThat(network.getRelated() != null).as(String.valueOf(network)).isTrue();
      assertThat(network.getServices().size() != 0).as(String.valueOf(network)).isTrue();
      assertThat(network.getState() != null).as(String.valueOf(network)).isTrue();
      assertThat(network.getTrafficType() != null).as(String.valueOf(network)).isTrue();
      assertThat(network.getZoneId() != null).as(String.valueOf(network)).isTrue();
      assertThat(network.getDomain() != null).as(String.valueOf(network)).isTrue();
      switch (network.getGuestIPType()) {
      case VIRTUAL:
         assertThat(network.getNetmask() == null).as(String.valueOf(network)).isTrue();
         assertThat(network.getGateway() == null).as(String.valueOf(network)).isTrue();
         assertThat(network.getVLAN() == null).as(String.valueOf(network)).isTrue();
         assertThat(network.getStartIP() == null).as(String.valueOf(network)).isTrue();
         assertThat(network.getEndIP() == null).as(String.valueOf(network)).isTrue();
         break;
      case DIRECT:
         // TODO: I've found a network that doesn't have a netmask associated
         assertThat(network.getNetmask() != null).as(String.valueOf(network)).isTrue();
         assertThat(network.getGateway() != null).as(String.valueOf(network)).isTrue();
         assertThat(network.getVLAN() != null).as(String.valueOf(network)).isTrue();
         assertEquals(network.getBroadcastURI(), URI.create("vlan://" + network.getVLAN()));
         assertThat(network.getStartIP() != null).as(String.valueOf(network)).isTrue();
         assertThat(network.getEndIP() != null).as(String.valueOf(network)).isTrue();
         break;
      }
   }

}
