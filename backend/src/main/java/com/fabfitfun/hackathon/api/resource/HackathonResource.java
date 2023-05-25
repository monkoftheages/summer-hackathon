package com.fabfitfun.hackathon.api.resource;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fabfitfun.hackathon.biz.manager.HackathonManager;

import com.fabfitfun.hackathon.data.QuestionListDto;
import com.google.gson.Gson;
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
  @Path("/questions/{questionId}")
  public Response getQuestionData(@PathParam("questionId") @NotNull String questionId) {
    val question = hackathonManager.getQuestionData(questionId);
    val questionJson = new Gson().toJson(question);
    return Response.ok().entity(questionJson).build();
  }

  @Operation(summary = "Boilerplate test endpoint")
  @ApiResponse(responseCode = "200", description = "Success!",
      content = @Content(schema = @Schema(implementation = Response.class)))
  @ApiResponse(responseCode = "400", description = "Error!",
      content = @Content(schema = @Schema(implementation = Response.class)))
  @GET
  @Path("/questions")
  public Response getResults() {
    val questions = hackathonManager.getQuestions();
    val questionJson = new Gson().toJson(questions);
    return Response.ok().entity(questionJson).build();
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
