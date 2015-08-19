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
import static org.testng.Assert.assertTrue;

import java.util.Set;

import org.jclouds.cloudstack.domain.Event;
import org.jclouds.cloudstack.internal.BaseCloudStackApiLiveTest;
import org.testng.annotations.Test;

/**
 * Tests behavior of {@code EventApi}
 */
@Test(groups = "live", singleThreaded = true, testName = "EventApiLiveTest")
public class EventApiLiveTest extends BaseCloudStackApiLiveTest {

   public void testlistEventTypes() throws Exception {
      final Set<String> response = client.getEventApi().listEventTypes();
      assertThat(null != response).isTrue();
      assertTrue(response.size() > 0);
      for (String type : response) {
         checkEventType(type);
      }
   }

   public void testlistEvents() throws Exception {
      final Set<Event> response = client.getEventApi().listEvents();
      assertThat(null != response).isTrue();
      assertTrue(response.size() > 0);
      for (Event event : response) {
         checkEvent(event);
      }
   }

   private void checkEvent(Event event) {
      assertThat(event.getAccount() != null).as(String.valueOf(event)).isTrue();
      assertThat(event.getCreated() != null).as(String.valueOf(event)).isTrue();
      assertThat(event.getDescription() != null).as(String.valueOf(event)).isTrue();
      assertThat(event.getDomain() != null).as(String.valueOf(event)).isTrue();
      assertThat(event.getId() != null).as(String.valueOf(event)).isTrue();
      assertThat(event.getLevel() != null).as(String.valueOf(event)).isTrue();
      assertThat(event.getState() != null).as(String.valueOf(event)).isTrue();
      assertThat(event.getType() != null).as(String.valueOf(event)).isTrue();
      assertThat(event.getUsername() != null).as(String.valueOf(event)).isTrue();
   }

   protected void checkEventType(String eventType) {
      assertThat(eventType != null).as(eventType).isTrue();
   }

}
