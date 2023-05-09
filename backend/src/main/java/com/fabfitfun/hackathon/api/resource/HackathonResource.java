package com.fabfitfun.hackathon.api.resource;

import static com.fabfitfun.auth.shared.PermissionIds.BACKEND_OPERATION;
import static com.fabfitfun.auth.shared.PermissionLevel.DELETE;
import static com.fabfitfun.customize.shared.api.ApiVersion.V1;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fabfitfun.auth.shared.PermissionAllowed;
import com.fabfitfun.hackathon.biz.manager.HackathonManager;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.extern.jbosslog.JBossLog;
import lombok.val;

/**
 * Endpoint(s) to handle assignment/reassignment for both users and groups of users.
 */
@Produces({V1, MediaType.APPLICATION_JSON})
@Consumes(MediaType.APPLICATION_JSON)
@AllArgsConstructor
@Path("/hackathon")
@JBossLog
public class HackathonResource {
  private HackathonManager hackathonManager;

  @Operation(summary = "Boilerplate test endpoint")
  @ApiResponse(responseCode = "200", description = "Success!",
      content = @Content(schema = @Schema(implementation = Response.class)))
  @ApiResponse(responseCode = "400", description = "Error!",
      content = @Content(schema = @Schema(implementation = Response.class)))
  @GET
  @Path("/test")
  @PermissionAllowed(id = BACKEND_OPERATION, level = DELETE)
  public Response saveAssignmentAnswers() {
    val data = hackathonManager.manageData();
    return Response.ok().build();
  }
}
