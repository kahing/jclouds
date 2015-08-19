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

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jclouds.cloudstack.domain.OSType;
import org.jclouds.cloudstack.internal.BaseCloudStackApiLiveTest;
import org.jclouds.cloudstack.options.ListOSTypesOptions;
import org.testng.annotations.Test;

/**
 * Tests behavior of {@code GuestOSApiLiveTest}
 */
@Test(groups = "live", singleThreaded = true, testName = "GuestOSApiLiveTest")
public class GuestOSApiLiveTest extends BaseCloudStackApiLiveTest {

   public void testListOSTypes() throws Exception {
      Set<OSType> response = client.getGuestOSApi().listOSTypes();
      assertThat(null != response).isTrue();
      assertTrue(response.size() > 0);
      for (OSType type : response) {
         OSType newDetails = getOnlyElement(client.getGuestOSApi().listOSTypes(
               ListOSTypesOptions.Builder.id(type.getId())));
         assertEquals(type.getId(), newDetails.getId());
         checkOSType(type);
      }
   }

   public void testListOSCategories() throws Exception {
      Map<String, String> response = client.getGuestOSApi().listOSCategories();
      assertThat(null != response).isTrue();
      assertTrue(response.size() > 0);
      for (Entry<String, String> category : response.entrySet()) {
         checkOSCategory(category);
      }
   }

   protected void checkOSCategory(Entry<String, String> category) {
      assertEquals(category, client.getGuestOSApi().getOSCategory(category.getKey()));
      assertThat(category.getKey() != null).as(String.valueOf(category)).isTrue();
      assertThat(category.getValue() != null).as(String.valueOf(category)).isTrue();
   }

   protected void checkOSType(OSType type) {
      assertEquals(type.getId(), client.getGuestOSApi().getOSType(type.getId()).getId());
      assertThat(type.getId() != null).as(String.valueOf(type)).isTrue();
      assertThat(type.getOSCategoryId() != null).as(String.valueOf(type)).isTrue();
      assertThat(type.getDescription() != null).as(String.valueOf(type)).isTrue();

   }

}
