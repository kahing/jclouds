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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.in;
import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.getFirst;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.Sets.filter;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jclouds.cloudstack.options.ListNetworksOptions.Builder.isDefault;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import org.jclouds.cloudstack.CloudStackApi;
import org.jclouds.cloudstack.domain.AsyncCreateResponse;
import org.jclouds.cloudstack.domain.AsyncJob;
import org.jclouds.cloudstack.domain.GuestIPType;
import org.jclouds.cloudstack.domain.NIC;
import org.jclouds.cloudstack.domain.Network;
import org.jclouds.cloudstack.domain.NetworkOffering;
import org.jclouds.cloudstack.domain.ServiceOffering;
import org.jclouds.cloudstack.domain.Template;
import org.jclouds.cloudstack.domain.VirtualMachine;
import org.jclouds.cloudstack.domain.Zone;
import org.jclouds.cloudstack.internal.BaseCloudStackApiLiveTest;
import org.jclouds.cloudstack.options.CreateNetworkOptions;
import org.jclouds.cloudstack.options.DeployVirtualMachineOptions;
import org.jclouds.cloudstack.options.ListNetworkOfferingsOptions;
import org.jclouds.cloudstack.options.ListNetworksOptions;
import org.jclouds.cloudstack.options.ListTemplatesOptions;
import org.jclouds.cloudstack.options.ListVirtualMachinesOptions;
import org.jclouds.util.InetAddresses2;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.Test;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.net.HostAndPort;
import com.google.common.net.HostSpecifier;

/**
 * Tests behavior of {@code VirtualMachineApiLiveTest}
 */
@Test(groups = "live", singleThreaded = true, testName = "VirtualMachineApiLiveTest")
public class VirtualMachineApiLiveTest extends BaseCloudStackApiLiveTest {
   private static final Logger logger = Logger.getAnonymousLogger();

   private VirtualMachine vm = null;

   static final Ordering<ServiceOffering> DEFAULT_SIZE_ORDERING = new Ordering<ServiceOffering>() {
      public int compare(ServiceOffering left, ServiceOffering right) {
         return ComparisonChain.start().compare(left.getCpuNumber(), right.getCpuNumber())
               .compare(left.getMemory(), right.getMemory()).result();
      }
   };

   public static VirtualMachine createVirtualMachine(CloudStackApi client, String defaultTemplate,
         Predicate<String> jobComplete, Predicate<VirtualMachine> virtualMachineRunning) {
      Set<Network> networks = client.getNetworkApi().listNetworks(isDefault(true));
      if (!networks.isEmpty()) {
         Network network = get(filter(networks, new Predicate<Network>() {
            @Override
            public boolean apply(Network network) {
               return network != null && network.getState().equals("Implemented");
            }
         }), 0);
         return createVirtualMachineInNetwork(network,
               defaultTemplateOrPreferredInZone(defaultTemplate, client, network.getZoneId()), client, jobComplete,
               virtualMachineRunning);
      } else {
         String zoneId = find(client.getZoneApi().listZones(), new Predicate<Zone>() {

            @Override
            public boolean apply(Zone arg0) {
               return arg0.isSecurityGroupsEnabled();
            }

         }).getId();
         return createVirtualMachineWithSecurityGroupInZone(zoneId,
               defaultTemplateOrPreferredInZone(defaultTemplate, client, zoneId),
               get(client.getSecurityGroupApi().listSecurityGroups(), 0).getId(), client, jobComplete,
               virtualMachineRunning);
      }
   }

   public static VirtualMachine createVirtualMachineWithSecurityGroupInZone(String zoneId, String templateId, String groupId,
         CloudStackApi client, Predicate<String> jobComplete,
         Predicate<VirtualMachine> virtualMachineRunning) {
      return createVirtualMachineWithOptionsInZone(new DeployVirtualMachineOptions().securityGroupId(groupId), zoneId,
            templateId, client, jobComplete, virtualMachineRunning);
   }

   public static VirtualMachine createVirtualMachineInNetwork(Network network, String templateId,
         CloudStackApi client, Predicate<String> jobComplete,
         Predicate<VirtualMachine> virtualMachineRunning) {
      DeployVirtualMachineOptions options = new DeployVirtualMachineOptions();
      String zoneId = network.getZoneId();
      options.networkId(network.getId());
      return createVirtualMachineWithOptionsInZone(options, zoneId, templateId, client, jobComplete,
            virtualMachineRunning);
   }

   public static VirtualMachine createVirtualMachineInNetworkWithIp(
         CloudStackApi client, String templateId, Set<Network> networks, Map<String, String> ipToNetwork,
         Predicate<String> jobComplete, Predicate<VirtualMachine> virtualMachineRunning) {

      DeployVirtualMachineOptions options = new DeployVirtualMachineOptions();

      String zoneId = getFirst(networks, null).getZoneId();
      options.networkIds(Iterables.transform(networks, new Function<Network, String>() {
         @Override
         public String apply(Network network) {
            return network.getId();
         }
      }));
      options.ipsToNetworks(ipToNetwork);

      return createVirtualMachineWithOptionsInZone(options, zoneId, templateId,
         client, jobComplete, virtualMachineRunning);
   }

   public static VirtualMachine createVirtualMachineWithOptionsInZone(DeployVirtualMachineOptions options, String zoneId,
         String templateId, CloudStackApi client, Predicate<String> jobComplete,
         Predicate<VirtualMachine> virtualMachineRunning) {
      String serviceOfferingId = DEFAULT_SIZE_ORDERING.min(client.getOfferingApi().listServiceOfferings()).getId();

      System.out.printf("serviceOfferingId %s, templateId %s, zoneId %s, options %s%n", serviceOfferingId, templateId,
            zoneId, options);
      AsyncCreateResponse job = client.getVirtualMachineApi().deployVirtualMachineInZone(zoneId, serviceOfferingId,
            templateId, options);
      assertTrue(jobComplete.apply(job.getJobId()));
      AsyncJob<VirtualMachine> jobWithResult = client.getAsyncJobApi().<VirtualMachine> getAsyncJob(job.getJobId());
      if (jobWithResult.getError() != null)
         Throwables.propagate(new ExecutionException(String.format("job %s failed with exception %s", job.getId(),
               jobWithResult.getError().toString())) {
         });
      VirtualMachine vm = jobWithResult.getResult();
      if (vm.isPasswordEnabled()) {
         assertThat(vm.getPassword() != null).as(String.valueOf(vm)).isTrue();
      }
      assertTrue(virtualMachineRunning.apply(vm));
      assertEquals(vm.getServiceOfferingId(), serviceOfferingId);
      assertEquals(vm.getTemplateId(), templateId);
      assertEquals(vm.getZoneId(), zoneId);
      return vm;
   }

   @Test
   public void testCreateVirtualMachine() throws Exception {
      String defaultTemplate = template != null ? template.getImageId() : null;
      vm = createVirtualMachine(client, defaultTemplate, jobComplete, virtualMachineRunning);
      if (vm.getPassword() != null) {
         conditionallyCheckSSH();
      }
      assertThat(in(ImmutableSet.of("ROOT", "NetworkFilesystem", "IscsiLUN", "VMFS", "PreSetup"))
         .apply(vm.getRootDeviceType())).as(String.valueOf(vm)).isTrue();
      checkVm(vm);
   }

   @Test
   public void testCreateVirtualMachineWithSpecificIp() throws Exception {
      skipIfNotGlobalAdmin();

      String defaultTemplate = template != null ? template.getImageId() : null;
      Network network = null;

      try {
         Template template = getOnlyElement(
            client.getTemplateApi().listTemplates(ListTemplatesOptions.Builder.id(defaultTemplate)));
         logger.info("Using template: " + template);

         Set<Network> allSafeNetworksInZone = adminClient.getNetworkApi().listNetworks(
            ListNetworksOptions.Builder.zoneId(template.getZoneId()).isSystem(false));
         for (Network net : allSafeNetworksInZone) {
            if (net.getName().equals(prefix + "-ip-network")) {
               logger.info("Deleting VMs in network: " + net);

               Set<VirtualMachine> machinesInNetwork = adminClient.getVirtualMachineApi().listVirtualMachines(
                  ListVirtualMachinesOptions.Builder.networkId(net.getId()));

               for (VirtualMachine machine : machinesInNetwork) {
                  if (machine.getState().equals(VirtualMachine.State.RUNNING)) {
                     logger.info("Deleting VM: " + machine);
                     destroyMachine(machine);
                  }
               }

               assertTrue(adminJobComplete.apply(
                  adminClient.getNetworkApi().deleteNetwork(net.getId())), net.toString());
            }
         }

         NetworkOffering offering = getFirst(
            client.getOfferingApi().listNetworkOfferings(
               ListNetworkOfferingsOptions.Builder.zoneId(template.getZoneId()).specifyVLAN(true)), null);
         checkNotNull(offering, "No network offering found");
         logger.info("Using network offering: " + offering);

         network = adminClient.getNetworkApi().createNetworkInZone(
            template.getZoneId(), offering.getId(), prefix + "-ip-network", "",
            CreateNetworkOptions.Builder.startIP("192.168.0.1").endIP("192.168.0.5")
               .netmask("255.255.255.0").gateway("192.168.0.1").vlan("21"));
         logger.info("Created network: " + network);

         Network requiredNetwork = getOnlyElement(filter(adminClient.getNetworkApi().listNetworks(
            ListNetworksOptions.Builder.zoneId(template.getZoneId())), new Predicate<Network>() {
            @Override
            public boolean apply(Network network) {
               return network.isDefault() &&
                  network.getGuestIPType() == GuestIPType.VIRTUAL;
            }
         }));
         logger.info("Required network: " + requiredNetwork);

         String ipAddress = "192.168.0.4";

         Map<String, String> ipsToNetworks = Maps.newHashMap();
         ipsToNetworks.put(ipAddress, network.getId());

         vm = createVirtualMachineInNetworkWithIp(
            adminClient, defaultTemplate, ImmutableSet.of(requiredNetwork, network),
            ipsToNetworks, adminJobComplete, adminVirtualMachineRunning);
         logger.info("Created VM: " + vm);

         boolean hasStaticIpNic = false;
         for (NIC nic : vm.getNICs()) {
            if (Objects.equal(nic.getNetworkId(), network.getId())) {
               hasStaticIpNic = true;
               assertEquals(nic.getIPAddress(), ipAddress);
            }
         }
         assertThat(hasStaticIpNic).isTrue();
         checkVm(vm);

      } finally {
         if (vm != null) {
            destroyMachine(vm);
            vm = null;
         }
         if (network != null) {
            String jobId = adminClient.getNetworkApi().deleteNetwork(network.getId());
            adminJobComplete.apply(jobId);
            network = null;
         }
      }
   }

   private void destroyMachine(VirtualMachine virtualMachine) {
      assertTrue(adminJobComplete.apply(
         adminClient.getVirtualMachineApi().destroyVirtualMachine(virtualMachine.getId())), virtualMachine.toString());
      assertTrue(adminVirtualMachineDestroyed.apply(virtualMachine));
   }

   private void conditionallyCheckSSH() {
      if (vm.getPassword() != null && !loginCredentials.getOptionalPassword().isPresent())
         loginCredentials = loginCredentials.toBuilder().password(vm.getPassword()).build();
      assertThat(HostSpecifier.isValid(vm.getIPAddress())).isTrue();
      if (!InetAddresses2.isPrivateIPAddress(vm.getIPAddress())) {
         // not sure if the network is public or not, so we have to test
         HostAndPort socket = HostAndPort.fromParts(vm.getIPAddress(), 22);
         System.err.printf("testing socket %s%n", socket);
         System.err.printf("testing ssh %s%n", socket);
         checkSSH(socket);
      } else {
         System.err.printf("skipping ssh %s, as private%n", vm.getIPAddress());
      }
   }

   @Test(dependsOnMethods = "testCreateVirtualMachine")
   public void testLifeCycle() throws Exception {
      String job = client.getVirtualMachineApi().stopVirtualMachine(vm.getId());
      assertTrue(jobComplete.apply(job));
      vm = client.getVirtualMachineApi().getVirtualMachine(vm.getId());
      assertEquals(vm.getState(), VirtualMachine.State.STOPPED);

      if (vm.isPasswordEnabled()) {
         job = client.getVirtualMachineApi().resetPasswordForVirtualMachine(vm.getId());
         assertTrue(jobComplete.apply(job));
         vm = client.getAsyncJobApi().<VirtualMachine> getAsyncJob(job).getResult();
         if (vm.getPassword() != null) {
            conditionallyCheckSSH();
         }
      }

      job = client.getVirtualMachineApi().startVirtualMachine(vm.getId());
      assertTrue(jobComplete.apply(job));
      vm = client.getVirtualMachineApi().getVirtualMachine(vm.getId());
      assertEquals(vm.getState(), VirtualMachine.State.RUNNING);

      job = client.getVirtualMachineApi().rebootVirtualMachine(vm.getId());
      assertTrue(jobComplete.apply(job));
      vm = client.getVirtualMachineApi().getVirtualMachine(vm.getId());
      assertEquals(vm.getState(), VirtualMachine.State.RUNNING);
   }

   @AfterGroups(groups = "live")
   @Override
   protected void tearDownContext() {
      if (vm != null) {
         destroyMachine(vm);
         vm = null;
      }
      super.tearDownContext();
   }

   @Test
   public void testListVirtualMachines() throws Exception {
      Set<VirtualMachine> response = client.getVirtualMachineApi().listVirtualMachines();
      assertThat(null != response).isTrue();
      assertTrue(response.size() > 0);
      for (VirtualMachine vm : response) {
         VirtualMachine newDetails = getOnlyElement(client.getVirtualMachineApi().listVirtualMachines(
               ListVirtualMachinesOptions.Builder.id(vm.getId())));
         assertEquals(vm.getId(), newDetails.getId());
         checkVm(vm);
      }
   }

   protected void checkVm(VirtualMachine vm) {
      assertEquals(vm.getId(), client.getVirtualMachineApi().getVirtualMachine(vm.getId()).getId());
      assertThat(vm.getId() != null).as(String.valueOf(vm)).isTrue();
      assertThat(vm.getName() != null).as(String.valueOf(vm)).isTrue();
      // vm.getDisplayName() can be null, so skip that check.
      assertThat(vm.getAccount() != null).as(String.valueOf(vm)).isTrue();
      assertThat(vm.getDomain() != null).as(String.valueOf(vm)).isTrue();
      assertThat(vm.getDomainId() != null).as(String.valueOf(vm)).isTrue();
      assertThat(vm.getCreated() != null).as(String.valueOf(vm)).isTrue();
      assertThat(vm.getState() != null).as(String.valueOf(vm)).isTrue();
      assertThat(vm.getZoneId() != null).as(String.valueOf(vm)).isTrue();
      assertThat(vm.getZoneName() != null).as(String.valueOf(vm)).isTrue();
      assertThat(vm.getTemplateId() != null).as(String.valueOf(vm)).isTrue();
      assertThat(vm.getTemplateName() != null).as(String.valueOf(vm)).isTrue();
      assertThat(vm.getServiceOfferingId() != null).as(String.valueOf(vm)).isTrue();
      assertThat(vm.getServiceOfferingName() != null).as(String.valueOf(vm)).isTrue();
      assertThat(vm.getCpuCount() > 0).as(String.valueOf(vm)).isTrue();
      assertThat(vm.getCpuSpeed() > 0).as(String.valueOf(vm)).isTrue();
      assertThat(vm.getMemory() > 0).as(String.valueOf(vm)).isTrue();
      assertThat(vm.getGuestOSId() != null).as(String.valueOf(vm)).isTrue();
      assertThat(vm.getRootDeviceId() != null).as(String.valueOf(vm)).isTrue();
      // assert vm.getRootDeviceType() != null : vm;
      if (vm.getJobId() != null)
         assertThat(vm.getJobStatus() != null).as(String.valueOf(vm)).isTrue();
      assertThat(vm.getNICs() != null && !vm.getNICs().isEmpty()).as(String.valueOf(vm)).isTrue();
      for (NIC nic : vm.getNICs()) {
         assertThat(nic.getId() != null).as(String.valueOf(vm)).isTrue();
         assertThat(nic.getNetworkId() != null).as(String.valueOf(vm)).isTrue();
         assertThat(nic.getTrafficType() != null).as(String.valueOf(vm)).isTrue();
         assertThat(nic.getGuestIPType() != null).as(String.valueOf(vm)).isTrue();
         switch (vm.getState()) {
         case RUNNING:
            assertThat(nic.getNetmask() != null).as(String.valueOf(vm)).isTrue();
            assertThat(nic.getGateway() != null).as(String.valueOf(vm)).isTrue();
            assertThat(nic.getIPAddress() != null).as(String.valueOf(vm)).isTrue();
            break;
         case STARTING:
            assertThat(nic.getNetmask() == null).as(String.valueOf(vm)).isTrue();
            assertThat(nic.getGateway() == null).as(String.valueOf(vm)).isTrue();
            assertThat(nic.getIPAddress() == null).as(String.valueOf(vm)).isTrue();
            break;
         default:
            assertThat(nic.getNetmask() != null).as(String.valueOf(vm)).isTrue();
            assertThat(nic.getGateway() != null).as(String.valueOf(vm)).isTrue();
            assertThat(nic.getIPAddress() != null).as(String.valueOf(vm)).isTrue();
         }

      }
      assertThat(vm.getSecurityGroups() != null && vm.getSecurityGroups().size() > 0).as(String.valueOf(vm)).isTrue();
      assertThat(vm.getHypervisor() != null).as(String.valueOf(vm)).isTrue();
   }
}
