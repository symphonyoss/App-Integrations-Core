/**
 * Copyright 2016-2017 Symphony Integrations - Symphony LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.symphonyoss.integration.pod.api.model;

import java.util.Map;

/**
 * Holds the POD information.
 *
 * Created by robson on 29/08/17.
 */
public class PodInfo {

  private static final String POD_ID = "podId";

  private static final String EXTERNAL_POD_ID = "externalPodId";

  public PodInfo(Map<String, Object> data) {
    if (data.containsKey(POD_ID)) {
      this.podId =  data.get(POD_ID).toString();
    }

    if (data.containsKey(EXTERNAL_POD_ID)) {
      this.externalPodId = data.get(EXTERNAL_POD_ID).toString();
    }
  }

  private String podId;

  private String externalPodId;

  public String getPodId() {
    return podId;
  }

  public String getExternalPodId() {
    return externalPodId;
  }

  public boolean verifyPodId(String podId) {
    if (podId == null) {
      return false;
    }

    return podId.equals(this.podId) || podId.equals(this.externalPodId);
  }
}
