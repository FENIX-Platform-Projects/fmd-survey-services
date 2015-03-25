package org.fao.fenix.fmd.tools.rest;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.core.NoContentException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.MalformedInputException;

@Provider
public class DefaultErrorManager implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception e) {

        Throwable exception = e;
        while (exception.getCause()!=null && exception instanceof RuntimeException)
            exception = exception.getCause();

        String exClassName = exception.getClass().getName();

        if (exClassName.equals(BadRequestException.class.getName()))
            return Response.status(Response.Status.BAD_REQUEST).build();
        else if (exClassName.equals(NoContentException.class.getName()))
            return Response.noContent().build();
        else if (exClassName.equals(ForbiddenException.class.getName()))
            return Response.status(Response.Status.FORBIDDEN).entity(exception.getMessage()).build();
        else {
            e.printStackTrace();
            return Response.serverError().entity(exception.getMessage()).build();
        }

    }
}
