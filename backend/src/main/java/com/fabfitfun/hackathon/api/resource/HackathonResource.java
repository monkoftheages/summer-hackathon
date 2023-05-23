package com.fabfitfun.hackathon.api.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
  public Response saveAssignmentAnswers() {
    val string = "";
    System.out.println("Connection returning: " + string);
    return Response.ok().entity(string).build();
  }

  @Operation(summary = "Boilerplate test endpoint")
  @ApiResponse(responseCode = "200", description = "Success!",
      content = @Content(schema = @Schema(implementation = Response.class)))
  @ApiResponse(responseCode = "400", description = "Error!",
      content = @Content(schema = @Schema(implementation = Response.class)))
  @POST
  @Path("/job")
  public Response runSentimentJob(String query) {
    System.out.println("Running sentiment job");
    hackathonManager.runSentimentJob(query);
    return Response.ok().build();
  }
}
