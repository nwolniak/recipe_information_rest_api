package com.example.hw;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;


@Path("/form")
public class HTMLForm {
    @Context
    ServletContext servletContext;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getHtmlForm() {
        InputStream inputStream = servletContext.getResourceAsStream("WEB-INF/index.html");
        String text = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        return text;
    }

}