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

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.NoSuchElementException;
import java.util.Set;

import org.jclouds.cloudstack.domain.DiskOffering;
import org.jclouds.cloudstack.domain.NetworkOffering;
import org.jclouds.cloudstack.domain.ServiceOffering;
import org.jclouds.cloudstack.domain.StorageType;
import org.jclouds.cloudstack.domain.TrafficType;
import org.jclouds.cloudstack.internal.BaseCloudStackApiLiveTest;
import org.jclouds.cloudstack.options.ListDiskOfferingsOptions;
import org.jclouds.cloudstack.options.ListNetworkOfferingsOptions;
import org.jclouds.cloudstack.options.ListServiceOfferingsOptions;
import org.testng.annotations.Test;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

/**
 * Tests behavior of {@code OfferingApi}
 */
@Test(groups = "live", singleThreaded = true, testName = "OfferingApiLiveTest")
public class OfferingApiLiveTest extends BaseCloudStackApiLiveTest {

   public void testListDiskOfferings() throws Exception {
      Set<DiskOffering> response = client.getOfferingApi().listDiskOfferings();
      assertThat(null != response).isTrue();
      long offeringCount = response.size();
      assertTrue(offeringCount >= 0);
      for (DiskOffering offering : response) {
         try {
           DiskOffering newDetails = Iterables.getOnlyElement(client.getOfferingApi().listDiskOfferings(
               ListDiskOfferingsOptions.Builder.id(offering.getId())));
           assertEquals(offering, newDetails);
           assertEquals(offering, client.getOfferingApi().getDiskOffering(offering.getId()));
           assertThat(offering.getId() != null).as(String.valueOf(offering)).isTrue();
           assertThat(offering.getName() != null).as(String.valueOf(offering)).isTrue();
           assertThat(offering.getCreated() != null).as(String.valueOf(offering)).isTrue();
           assertThat(offering.getDisplayText() != null).as(String.valueOf(offering)).isTrue();
           assertThat(offering.getDiskSize() > 0 || (offering.getDiskSize() == 0 && offering.isCustomized())).as(String.valueOf(offering)).isTrue();
           assertThat(offering.getTags() != null).as(String.valueOf(offering)).isTrue();

         } catch (NoSuchElementException e) {
            // This bug is present both in 2.2.8 and 2.2.12
            assertTrue(Predicates.in(ImmutableSet.of("2.2.8", "2.2.12")).apply(apiVersion));
         }
      }
   }

   public void testListServiceOfferings() throws Exception {
      Set<ServiceOffering> response = client.getOfferingApi().listServiceOfferings();
      assertThat(null != response).isTrue();
      long offeringCount = response.size();
      assertTrue(offeringCount >= 0);
      for (ServiceOffering offering : response) {
         ServiceOffering newDetails = Iterables.getOnlyElement(client.getOfferingApi().listServiceOfferings(
               ListServiceOfferingsOptions.Builder.id(offering.getId())));
         assertEquals(offering, newDetails);

         assertThat(offering.getId() != null).as(String.valueOf(offering)).isTrue();
         assertThat(offering.getName() != null).as(String.valueOf(offering)).isTrue();
         assertThat(offering.getDisplayText() != null).as(String.valueOf(offering)).isTrue();
         assertThat(offering.getCpuNumber() > 0).as(String.valueOf(offering)).isTrue();
         assertThat(offering.getCpuSpeed() > 0).as(String.valueOf(offering)).isTrue();
         assertThat(offering.getMemory() > 0).as(String.valueOf(offering)).isTrue();
         assertThat(offering.getStorageType() != null && StorageType.UNRECOGNIZED != offering.getStorageType()).as(String.valueOf(offering)).isTrue();
         assertThat(offering.getTags() != null).as(String.valueOf(offering)).isTrue();
      }
   }

   public void testListNetworkOfferings() throws Exception {
      Set<NetworkOffering> response = client.getOfferingApi().listNetworkOfferings();
      assertThat(null != response).isTrue();
      long offeringCount = response.size();
      assertTrue(offeringCount >= 0);
      for (NetworkOffering offering : response) {
         NetworkOffering newDetails = Iterables.getOnlyElement(client.getOfferingApi().listNetworkOfferings(
               ListNetworkOfferingsOptions.Builder.id(offering.getId())));
         assertEquals(offering, newDetails);
         assertEquals(offering, client.getOfferingApi().getNetworkOffering(offering.getId()));
         assertThat(offering.getId() != null).as(String.valueOf(offering)).isTrue();
         assertThat(offering.getName() != null).as(String.valueOf(offering)).isTrue();
         assertThat(offering.getDisplayText() != null).as(String.valueOf(offering)).isTrue();
         assertThat(offering.getMaxConnections() == null || offering.getMaxConnections() > 0).as(String.valueOf(offering)).isTrue();
         assertThat(offering.getTrafficType() != null && TrafficType.UNRECOGNIZED != offering.getTrafficType()).as(String.valueOf(offering)).isTrue();
         assertThat(offering.getTags() != null).as(String.valueOf(offering)).isTrue();
      }
   }
}
