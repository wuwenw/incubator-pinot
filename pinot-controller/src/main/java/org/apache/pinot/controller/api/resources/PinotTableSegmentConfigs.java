/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pinot.controller.api.resources;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.inject.Inject;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.pinot.common.metrics.ControllerMetrics;
import org.apache.pinot.controller.api.access.AccessType;
import org.apache.pinot.controller.api.access.Authenticate;
import org.apache.pinot.controller.helix.core.PinotHelixResourceManager;
import org.apache.pinot.segment.local.utils.TableConfigUtils;
import org.apache.pinot.spi.config.table.TableConfig;
import org.apache.pinot.spi.data.Schema;
import org.apache.pinot.spi.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Api(tags = Constants.TABLE_TAG)
@Path("/")
public class PinotTableSegmentConfigs {
  private static final Logger LOGGER = LoggerFactory.getLogger(PinotTableSegmentConfigs.class);

  @Inject
  PinotHelixResourceManager pinotHelixResourceManager;
  @Inject
  ControllerMetrics metrics;

  @Deprecated
  @PUT
  @Path("/tables/{tableName}/segmentConfigs")
  @Authenticate(AccessType.UPDATE)
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Update segments configuration", notes = "Updates segmentsConfig section (validation and retention) of a table")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 404, message = "Table not found"), @ApiResponse(code = 500, message = "Internal server error")})
  public SuccessResponse put(@ApiParam(value = "Table name", required = true) @PathParam("tableName") String tableName,
      String tableConfigString) {
    TableConfig tableConfig;
    try {
      tableConfig = JsonUtils.stringToObject(tableConfigString, TableConfig.class);
      Schema schema = pinotHelixResourceManager.getSchemaForTableConfig(tableConfig);
      TableConfigUtils.validate(tableConfig, schema);
    } catch (Exception e) {
      String msg = String.format("Invalid table config: %s", tableName);
      throw new ControllerApplicationException(LOGGER, msg, Response.Status.BAD_REQUEST, e);
    }
    try {
      pinotHelixResourceManager
          .updateSegmentsValidationAndRetentionConfigFor(tableConfig.getTableName(), tableConfig.getTableType(),
              tableConfig.getValidationConfig());
      return new SuccessResponse("Update segmentsConfig for table: " + tableName);
    } catch (Exception e) {
      throw new ControllerApplicationException(LOGGER,
          String.format("Failed to update segments config for table: %s", tableName),
          Response.Status.INTERNAL_SERVER_ERROR, e);
    }
  }
}
