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

import org.jclouds.cloudstack.domain.Account;
import org.jclouds.cloudstack.domain.User;
import org.jclouds.cloudstack.internal.BaseCloudStackApiLiveTest;
import org.testng.annotations.Test;

/**
 * Tests behavior of {@code AccountApi}
 */
@Test(groups = "live", singleThreaded = true, testName = "AccountApiLiveTest")
public class AccountApiLiveTest extends BaseCloudStackApiLiveTest {

   @Test
   public void testListAccounts() throws Exception {
      for (Account securityAccount : client.getAccountApi().listAccounts())
         checkAccount(securityAccount);
   }

   protected void checkAccount(Account account) {
      assertThat(account.getId() != null).as(String.valueOf(account)).isTrue();
      assertEquals(account.toString(), client.getAccountApi().getAccount(account.getId()).toString());
      assertThat(account.getName() != null).as(String.valueOf(account)).isTrue();
      assertThat(account.getType() != null && account.getType() != Account.Type.UNRECOGNIZED).as(String.valueOf(account)).isTrue();
      assertThat(account.getDomain() != null).as(String.valueOf(account)).isTrue();
      assertThat(account.getDomainId() != null).as(String.valueOf(account)).isTrue();
      assertThat(account.getUsers() != null).as(String.valueOf(account)).isTrue();
      for (User user : account.getUsers()) {
         assertThat(user.getName() != null).as(String.valueOf(user)).isTrue();
         assertThat(user.getAccountType().equals(account.getType())).as(String.valueOf(user)).isTrue();
         assertThat(user.getDomain().equals(account.getDomain())).as(String.valueOf(user)).isTrue();
         assertThat(user.getDomainId().equals(account.getDomainId())).as(String.valueOf(user)).isTrue();
         assertThat(user.getCreated() != null).as(String.valueOf(user)).isTrue();
         assertThat(user.getEmail() != null).as(String.valueOf(user)).isTrue();
         assertThat(user.getLastName() != null).as(String.valueOf(user)).isTrue();
         assertThat(user.getFirstName() != null).as(String.valueOf(user)).isTrue();
         assertThat(user.getId() != null).as(String.valueOf(user)).isTrue();
         assertThat(user.getState() != null).as(String.valueOf(user)).isTrue();
      }
      assertThat(account.getIPsAvailable() == null || account.getIPsAvailable() >= 0).as(String.valueOf(account)).isTrue();
      assertThat(account.getIPLimit() == null || account.getIPLimit() >= 0).as(String.valueOf(account)).isTrue();
      assertThat(account.getIPs() >= 0).as(String.valueOf(account)).isTrue();
      assertThat(account.getReceivedBytes() >= 0).as(String.valueOf(account)).isTrue();
      assertThat(account.getSentBytes() >= 0).as(String.valueOf(account)).isTrue();
      assertThat(account.getSnapshotsAvailable() == null || account.getSnapshotsAvailable() >= 0).as(String.valueOf(account)).isTrue();
      assertThat(account.getSnapshotLimit() == null || account.getSnapshotLimit() >= 0).as(String.valueOf(account)).isTrue();
      assertThat(account.getSnapshots() >= 0).as(String.valueOf(account)).isTrue();
      assertThat(account.getState() != null && account.getState() != Account.State.UNRECOGNIZED).as(String.valueOf(account)).isTrue();
      assertThat(account.getTemplatesAvailable() == null || account.getTemplatesAvailable() >= 0).as(String.valueOf(account)).isTrue();
      assertThat(account.getTemplateLimit() == null || account.getTemplateLimit() >= 0).as(String.valueOf(account)).isTrue();
      assertThat(account.getTemplates() >= 0).as(String.valueOf(account)).isTrue();
      assertThat(account.getVMsAvailable() == null || account.getVMsAvailable() >= 0).as(String.valueOf(account)).isTrue();
      assertThat(account.getVMLimit() == null || account.getVMLimit() >= 0).as(String.valueOf(account)).isTrue();
      assertThat(account.getVMsRunning() >= 0).as(String.valueOf(account)).isTrue();
      assertThat(account.getVMsStopped() >= 0).as(String.valueOf(account)).isTrue();
      assertThat(account.getVMs() >= 0).as(String.valueOf(account)).isTrue();
      assertThat(account.getVolumesAvailable() == null || account.getVolumesAvailable() >= 0).as(String.valueOf(account)).isTrue();
      assertThat(account.getVolumeLimit() == null || account.getVolumeLimit() >= 0).as(String.valueOf(account)).isTrue();
      assertThat(account.getVolumes() >= 0).as(String.valueOf(account)).isTrue();
   }

}
