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
package org.jclouds.compute.predicates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jclouds.compute.predicates.OperatingSystemPredicates.supportsApt;
import static org.jclouds.compute.predicates.OperatingSystemPredicates.supportsYum;
import static org.jclouds.compute.predicates.OperatingSystemPredicates.supportsZypper;

import org.jclouds.cim.OSType;
import org.jclouds.compute.domain.CIMOperatingSystem;
import org.jclouds.compute.domain.OperatingSystem;
import org.jclouds.compute.domain.OsFamily;
import org.testng.annotations.Test;

/**
 * Tests possible uses of OperatingSystemPredicates
 */
@Test
public class OperatingSystemPredicatesTest {

   public void testCIMCENTOSDoesntSupportApt() {
      assertThat(!supportsApt().apply(new CIMOperatingSystem(OSType.CENTOS, "", null, "description"))).isTrue();
      assertThat(!supportsApt().apply(new CIMOperatingSystem(OSType.CENTOS_64, "", null, "description"))).isTrue();
   }

   public void testCIMRHELDoesntSupportApt() {
      assertThat(!supportsApt().apply(new CIMOperatingSystem(OSType.RHEL, "", null, "description"))).isTrue();
      assertThat(!supportsApt().apply(new CIMOperatingSystem(OSType.RHEL_64, "", null, "description"))).isTrue();
   }

   public void testCIMDEBIANSupportsApt() {
      assertThat(supportsApt().apply(new CIMOperatingSystem(OSType.DEBIAN, "", null, "description"))).isTrue();
      assertThat(supportsApt().apply(new CIMOperatingSystem(OSType.DEBIAN_64, "", null, "description"))).isTrue();
   }

   public void testCIMUBUNTUSupportsApt() {
      assertThat(supportsApt().apply(new CIMOperatingSystem(OSType.UBUNTU, "", null, "description"))).isTrue();
      assertThat(supportsApt().apply(new CIMOperatingSystem(OSType.UBUNTU_64, "", null, "description"))).isTrue();
   }

   public void testUbuntuNameSupportsApt() {
      assertThat(supportsApt().apply(new OperatingSystem(null, "Ubuntu", "", null, "description", false))).isTrue();
   }

   public void testCIMCENTOSSupportsYum() {
      assertThat(supportsYum().apply(new CIMOperatingSystem(OSType.CENTOS, "", null, "description"))).isTrue();
      assertThat(supportsYum().apply(new CIMOperatingSystem(OSType.CENTOS_64, "", null, "description"))).isTrue();
   }

   public void testCIMRHELSupportsYum() {
      assertThat(supportsYum().apply(new CIMOperatingSystem(OSType.RHEL, "", null, "description"))).isTrue();
      assertThat(supportsYum().apply(new CIMOperatingSystem(OSType.RHEL_64, "", null, "description"))).isTrue();
   }

   public void testCIMDEBIANDoesntSupportYum() {
      assertThat(!supportsYum().apply(new CIMOperatingSystem(OSType.DEBIAN, "", null, "description"))).isTrue();
      assertThat(!supportsYum().apply(new CIMOperatingSystem(OSType.DEBIAN_64, "", null, "description"))).isTrue();
   }

   public void testCIMUBUNTUDoesntSupportYum() {
      assertThat(!supportsYum().apply(new CIMOperatingSystem(OSType.UBUNTU, "", null, "description"))).isTrue();
      assertThat(!supportsYum().apply(new CIMOperatingSystem(OSType.UBUNTU_64, "", null, "description"))).isTrue();
   }

   public void testSuseTypeSupportsZypper() {
      assertThat(supportsZypper().apply(new OperatingSystem(OsFamily.SUSE, null, "", null, "description", false))).isTrue();
   }

   public void testSuseDescriptionSupportsZypper() {
      assertThat(supportsZypper().apply(new OperatingSystem(null, "", null, null, "Suse", false))).isTrue();
   }

   public void testSuseNameSupportsZypper() {
      assertThat(supportsZypper().apply(new OperatingSystem(null, "Suse", "", null, "description", false))).isTrue();
   }

   public void testCentosTypeSupportsYum() {
      assertThat(supportsYum().apply(new OperatingSystem(OsFamily.CENTOS, null, "", null, "description", false))).isTrue();
   }

   public void testAmzTypeSupportsYum() {
      assertThat(supportsYum().apply(new OperatingSystem(OsFamily.AMZN_LINUX, null, "", null, "description", false))).isTrue();
   }

   public void testRhelTypeSupportsYum() {
      assertThat(supportsYum().apply(new OperatingSystem(OsFamily.RHEL, null, "", null, "description", false))).isTrue();
   }

   public void testFedoraTypeSupportsYum() {
      assertThat(supportsYum().apply(new OperatingSystem(OsFamily.FEDORA, null, "", null, "description", false))).isTrue();
   }

   public void testCentosNameSupportsYum() {
      assertThat(supportsYum().apply(new OperatingSystem(null, "Centos", "", null, "description", false))).isTrue();
   }

   public void testRhelNameSupportsYum() {
      assertThat(supportsYum().apply(new OperatingSystem(null, "RHEL", "", null, "description", false))).isTrue();
   }

   public void testFedoraNameSupportsYum() {
      assertThat(supportsYum().apply(new OperatingSystem(null, "Fedora", "", null, "description", false))).isTrue();
   }

   public void testRedHatEnterpriseLinuxNameSupportsYum() {
      assertThat(supportsYum().apply(new OperatingSystem(null, "Red Hat Enterprise Linux", "", null, "description", false))).isTrue();
   }

   public void testCentosDescriptionSupportsYum() {
      assertThat(supportsYum().apply(new OperatingSystem(null, "", null, null, "Centos", false))).isTrue();
   }

   public void testRhelDescriptionSupportsYum() {
      assertThat(supportsYum().apply(new OperatingSystem(null, "", null, null, "RHEL", false))).isTrue();
   }

   public void testFedoraDescriptionSupportsYum() {
      assertThat(supportsYum().apply(new OperatingSystem(null, "", null, null, "Fedora", false))).isTrue();
   }

   public void testRedHatEnterpriseLinuxDescriptionSupportsYum() {
      assertThat(supportsYum().apply(new OperatingSystem(null, "", null, null, "Red Hat Enterprise Linux", false))).isTrue();
   }
}
