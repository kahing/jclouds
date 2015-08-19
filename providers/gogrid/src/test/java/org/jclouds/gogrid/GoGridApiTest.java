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
package org.jclouds.gogrid;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.jclouds.gogrid.features.BaseGoGridApiTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests behavior of {@code GoGridApi}
 */
// NOTE:without testName, this will not call @Before* and fail w/NPE during surefire
@Test(groups = "unit", testName = "GoGridApiTest")
public class GoGridApiTest extends BaseGoGridApiTest<GoGridApi> {

   private GoGridApi syncClient;

   public void testSync() throws SecurityException, NoSuchMethodException, InterruptedException,
            ExecutionException {
      assertThat(syncClient.getImageServices() != null).isTrue();
      assertThat(syncClient.getIpServices() != null).isTrue();
      assertThat(syncClient.getJobServices() != null).isTrue();
      assertThat(syncClient.getLoadBalancerServices() != null).isTrue();
      assertThat(syncClient.getServerServices() != null).isTrue();
   }

   @BeforeClass
   @Override
   protected void setupFactory() throws IOException {
      super.setupFactory();
      syncClient = injector.getInstance(GoGridApi.class);
   }
}
