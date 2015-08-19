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

import java.util.Set;

import org.jclouds.cloudstack.domain.NetworkType;
import org.jclouds.cloudstack.domain.Zone;
import org.jclouds.cloudstack.internal.BaseCloudStackApiLiveTest;
import org.jclouds.cloudstack.options.ListZonesOptions;
import org.testng.annotations.Test;

import com.google.common.collect.Iterables;

/**
 * Tests behavior of {@code ZoneApiLiveTest}
 */
@Test(groups = "live", singleThreaded = true, testName = "ZoneApiLiveTest")
public class ZoneApiLiveTest extends BaseCloudStackApiLiveTest {

   public void testListZones() throws Exception {
      Set<Zone> response = client.getZoneApi().listZones();
      assertThat(null != response).isTrue();
      long zoneCount = response.size();
      assertTrue(zoneCount >= 0);
      for (Zone zone : response) {
         Zone newDetails = Iterables.getOnlyElement(client.getZoneApi().listZones(
               ListZonesOptions.Builder.id(zone.getId())));
         assertEquals(zone, newDetails);
         assertEquals(zone, client.getZoneApi().getZone(zone.getId()));
         assertThat(zone.getId() != null).as(String.valueOf(zone)).isTrue();
         assertThat(zone.getName() != null).as(String.valueOf(zone)).isTrue();
         assertThat(zone.getNetworkType() != null && zone.getNetworkType() != NetworkType.UNRECOGNIZED).as(String.valueOf(zone)).isTrue();
         switch (zone.getNetworkType()) {
         case ADVANCED:
            // TODO
            // assert zone.getVLAN() != null : zone;
            // assert zone.getDomain() == null : zone;
            // assert zone.getDomainId() == null : zone;
            // assert zone.getGuestCIDRAddress() != null : zone;
            break;
         case BASIC:
            assertThat(zone.getVLAN() == null).as(String.valueOf(zone)).isTrue();
            assertThat(zone.getDNS().size() > 0).as(String.valueOf(zone)).isTrue();
            assertThat(zone.getInternalDNS().size() > 0).as(String.valueOf(zone)).isTrue();
            assertThat(zone.getDomain() == null).as(String.valueOf(zone)).isTrue();
            assertThat(zone.getDomainId() == null).as(String.valueOf(zone)).isTrue();
            assertThat(zone.getGuestCIDRAddress() == null).as(String.valueOf(zone)).isTrue();
            break;
         }

      }
   }

}
