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

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;

import java.util.Set;

import org.jclouds.gogrid.domain.Job;
import org.jclouds.gogrid.options.GetJobListOptions;
import org.testng.annotations.Test;

import com.google.common.collect.Iterables;

// NOTE:without testName, this will not call @Before* and fail w/NPE during surefire
@Test(groups = "unit", testName = "GridJobApiLiveTest")
public class GridJobApiLiveTest extends BaseGoGridApiLiveTest {

   public void testListJobs() throws Exception {
      Set<Job> response = api.getJobServices().getJobList(GetJobListOptions.Builder.maxItems(10));
      assertThat(null != response).isTrue();
      assertThat(response.size() <= 10).as(String.valueOf(response)).isTrue();
      for (Job job : response) {
         assertThat(job.getId() >= 0).as(String.valueOf(job)).isTrue();
         checkJob(job);

         Job query = Iterables.getOnlyElement(api.getJobServices().getJobsById(job.getId()));
         assertEquals(query.getId(), job.getId());

         checkJob(query);
      }
   }

   private void checkJob(Job job) {
      assertThat(job.getAttempts() >= 0).as(String.valueOf(job)).isTrue();
      assertThat(job.getCommand() != null).as(String.valueOf(job)).isTrue();
      assertThat(job.getCreatedOn() != null).as(String.valueOf(job)).isTrue();
      assertThat(job.getCreatedOn() != null).as(String.valueOf(job)).isTrue();
      assertThat(job.getDetails() != null).as(String.valueOf(job)).isTrue();
      assertThat(job.getHistory() != null).as(String.valueOf(job)).isTrue();
      assertThat(job.getId() >= 0).as(String.valueOf(job)).isTrue();
      assertThat(job.getLastUpdatedOn() != null).as(String.valueOf(job)).isTrue();
      assertThat(job.getObjectType() != null).as(String.valueOf(job)).isTrue();
      assertThat(job.getOwner() != null).as(String.valueOf(job)).isTrue();
   }
}
