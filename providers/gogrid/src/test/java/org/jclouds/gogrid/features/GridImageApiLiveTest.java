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
package org.jclouds.gogrid.features;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jclouds.util.Predicates2.retry;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Date;
import java.util.Set;
import java.util.logging.Logger;

import org.jclouds.gogrid.domain.Ip;
import org.jclouds.gogrid.domain.Server;
import org.jclouds.gogrid.domain.ServerImage;
import org.jclouds.gogrid.domain.ServerImageState;
import org.jclouds.gogrid.options.SaveImageOptions;
import org.jclouds.gogrid.predicates.ServerLatestJobCompleted;
import org.testng.annotations.Test;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

// NOTE:without testName, this will not call @Before* and fail w/NPE during
// surefire
@Test(groups = "unit", testName = "GridImageApiLiveTest")
public class GridImageApiLiveTest extends BaseGoGridApiLiveTest {

   public void testListImages() throws Exception {
      Set<ServerImage> response = api.getImageServices().getImageList();
      assertThat(null != response).isTrue();
      for (ServerImage image : response) {
         assertThat(image.getId() >= 0).as(String.valueOf(image)).isTrue();
         checkImage(image);

         ServerImage query = Iterables.getOnlyElement(api.getImageServices()
               .getImagesById(image.getId()));
         assertEquals(query.getId(), image.getId());

         checkImage(query);
      }
   }

   private void checkImage(ServerImage image) {
      assertThat(image.getArchitecture() != null).as(String.valueOf(image)).isTrue();
      assertThat(image.getBillingTokens() != null).as(String.valueOf(image)).isTrue();
      if (image.getCreatedTime() == null)
         Logger.getAnonymousLogger().warning("image " + image.getId() + " is missing the createdTime field");
      assertThat(image.getDescription() != null).as(String.valueOf(image)).isTrue();
      assertThat(image.getFriendlyName() != null).as(String.valueOf(image)).isTrue();
      assertThat(image.getId() >= 0).as(String.valueOf(image)).isTrue();
      assertThat(image.getLocation() != null).as(String.valueOf(image)).isTrue();
      assertThat(image.getName() != null).as(String.valueOf(image)).isTrue();
      assertThat(image.getOs() != null).as(String.valueOf(image)).isTrue();
      assertThat(image.getOwner() != null).as(String.valueOf(image)).isTrue();
      assertThat(image.getPrice() >= 0).as(String.valueOf(image)).isTrue();
      assertThat(image.getState() != null).as(String.valueOf(image)).isTrue();
      assertThat(image.getType() != null).as(String.valueOf(image)).isTrue();
      if (image.getUpdatedTime() == null)
         Logger.getAnonymousLogger().warning("image " + image.getId() + " is missing the updatedTime field");
   }

   @Test
   public void testSaveServerToImage() throws IOException {
      Predicate<Server> serverLatestJobCompleted = retry(new ServerLatestJobCompleted(api
            .getJobServices()), 800, 20, SECONDS);

      final String nameOfServer = "Server" + String.valueOf(new Date().getTime()).substring(6);
      ServerImage image = null;
      try {
         Set<Ip> availableIps = api.getIpServices().getUnassignedPublicIpList();
         Ip availableIp = Iterables.getLast(availableIps);

         Server createdServer = api.getServerServices()
               .addServer(nameOfServer, "5489", "1", availableIp.getIp());
         assertNotNull(createdServer);
         assertThat(serverLatestJobCompleted.apply(createdServer)).isTrue();
         image = api
               .getImageServices()
               .saveImageFromServer("friendlyName", createdServer.getName(),
                     SaveImageOptions.Builder.withDescription("description"));
         
         assertEquals(image.getFriendlyName(), "friendlyName");
         assertEquals(image.getDescription(), "description");
         assertFalse(image.isPublic());

         assertEventuallyImageStateEquals(image, ServerImageState.AVAILABLE);
         
         api.getImageServices().deleteById(image.getId());

         assertEventuallyImageStateEquals(image, ServerImageState.TRASH);
         
         image = null;
      } finally {
         if (image != null)
            try {
               api.getImageServices().deleteById(image.getId());
            } catch (Exception e) {
               // not failing so that we can ensure server below deletes
               e.printStackTrace();
            }
         // delete the server
         api.getServerServices().deleteByName(nameOfServer);
      }

   }

   protected void assertEventuallyImageStateEquals(ServerImage image, final ServerImageState state) {
      assertTrue(retry(new Predicate<ServerImage>() {
         public boolean apply(ServerImage input) {
            return Iterables.getOnlyElement(api.getImageServices().getImagesById(input.getId()))
                  .getState() == state;
         }
      }, 600, 1, SECONDS).apply(image));
   }
}
