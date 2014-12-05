/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ambari.server.controller.metrics;

import org.apache.ambari.server.configuration.ComponentSSLConfiguration;
import org.apache.ambari.server.controller.internal.AbstractPropertyProvider;
import org.apache.ambari.server.controller.internal.PropertyInfo;
import org.apache.ambari.server.controller.metrics.ganglia.GangliaReportPropertyProvider;
import org.apache.ambari.server.controller.metrics.timeline.AMSReportPropertyProvider;
import org.apache.ambari.server.controller.spi.Predicate;
import org.apache.ambari.server.controller.spi.Request;
import org.apache.ambari.server.controller.spi.Resource;
import org.apache.ambari.server.controller.spi.SystemException;
import org.apache.ambari.server.controller.utilities.StreamProvider;

import java.util.Map;
import java.util.Set;

import static org.apache.ambari.server.controller.metrics.MetricsServiceProvider.MetricsService;
import static org.apache.ambari.server.controller.metrics.MetricsServiceProvider.MetricsService.GANGLIA;
import static org.apache.ambari.server.controller.metrics.MetricsServiceProvider.MetricsService.TIMELINE_METRICS;

public class MetricsReportPropertyProviderProxy extends AbstractPropertyProvider {
  private MetricsReportPropertyProvider amsMetricsReportProvider;
  private MetricsReportPropertyProvider gangliaMetricsReportProvider;
  private final MetricsServiceProvider metricsServiceProvider;

  public MetricsReportPropertyProviderProxy(
    Map<String, Map<String, PropertyInfo>> componentPropertyInfoMap,
    StreamProvider streamProvider,
    ComponentSSLConfiguration configuration,
    MetricHostProvider hostProvider,
    MetricsServiceProvider serviceProvider,
    String clusterNamePropertyId) {


    super(componentPropertyInfoMap);
    this.metricsServiceProvider = serviceProvider;

    createReportPropertyProviders(componentPropertyInfoMap,
      streamProvider,
      configuration,
      hostProvider,
      clusterNamePropertyId);
  }

  private void createReportPropertyProviders(Map<String, Map<String, PropertyInfo>> componentPropertyInfoMap,
                                             StreamProvider streamProvider,
                                             ComponentSSLConfiguration configuration,
                                             MetricHostProvider hostProvider,
                                             String clusterNamePropertyId) {

    this.amsMetricsReportProvider = new AMSReportPropertyProvider(
      componentPropertyInfoMap,
      streamProvider,
      configuration,
      hostProvider,
      clusterNamePropertyId);

    this.gangliaMetricsReportProvider = new GangliaReportPropertyProvider(
      componentPropertyInfoMap,
      streamProvider,
      configuration,
      hostProvider,
      clusterNamePropertyId);
  }

  @Override
  public Set<Resource> populateResources(Set<Resource> resources, Request request,
                                         Predicate predicate) throws SystemException {

    MetricsService metricsService = metricsServiceProvider.getMetricsServiceType();

    if (metricsService != null) {
      if (metricsService.equals(GANGLIA)) {
        return gangliaMetricsReportProvider.populateResources(resources, request, predicate);
      } else if (metricsService.equals(TIMELINE_METRICS)) {
        return amsMetricsReportProvider.populateResources(resources, request, predicate);
      }
    }

    return resources;
  }
}
