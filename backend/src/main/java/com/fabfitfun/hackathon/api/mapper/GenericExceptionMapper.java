package com.fabfitfun.hackathon.api.mapper;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class GenericExceptionMapper<E extends Exception> implements ExceptionMapper<E> {
  private final Response.Status status;

  public GenericExceptionMapper() {
    this.status = INTERNAL_SERVER_ERROR;
  }

  public GenericExceptionMapper(Response.Status status) {
    this.status = status;
  }

  @Override
  public Response toResponse(E exception) {
    ErrorResponse entity = new ErrorResponse(exception.getMessage());
    return Response.status(status).entity(entity).build();
  }
}

