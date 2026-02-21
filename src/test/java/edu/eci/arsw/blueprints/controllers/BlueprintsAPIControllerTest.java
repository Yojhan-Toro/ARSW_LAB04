package edu.eci.arsw.blueprints.controllers;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BlueprintsAPIController.class)
public class BlueprintsAPIControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BlueprintsServices services;

    @Test
    void getAllBlueprints_returns200() throws Exception {
        Blueprint bp = new Blueprint("john", "house", List.of(new Point(0, 0)));
        when(services.getAllBlueprints()).thenReturn(Set.of(bp));

        mockMvc.perform(get("/api/v1/blueprints"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("execute ok"));
    }

    @Test
    void getByAuthor_returns200() throws Exception {
        Blueprint bp = new Blueprint("john", "house", List.of(new Point(0, 0)));
        when(services.getBlueprintsByAuthor("john")).thenReturn(Set.of(bp));

        mockMvc.perform(get("/api/v1/blueprints/john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void getByAuthor_notFound_returns404() throws Exception {
        when(services.getBlueprintsByAuthor("unknown"))
                .thenThrow(new BlueprintNotFoundException("No blueprints for author: unknown"));

        mockMvc.perform(get("/api/v1/blueprints/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void getByAuthorAndName_returns200() throws Exception {
        Blueprint bp = new Blueprint("john", "house", List.of(new Point(0, 0)));
        when(services.getBlueprint("john", "house")).thenReturn(bp);

        mockMvc.perform(get("/api/v1/blueprints/john/house"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void addBlueprint_returns201() throws Exception {
        String body = """
                {"author":"john","name":"newhouse","points":[{"x":0,"y":0}]}
                """;

        mockMvc.perform(post("/api/v1/blueprints")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201));
    }

    @Test
    void addBlueprint_duplicate_returns403() throws Exception {
        doThrow(new BlueprintPersistenceException("Blueprint already exists"))
                .when(services).addNewBlueprint(any());

        String body = """
                {"author":"john","name":"house","points":[{"x":0,"y":0}]}
                """;

        mockMvc.perform(post("/api/v1/blueprints")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void addPoint_returns202() throws Exception {
        mockMvc.perform(put("/api/v1/blueprints/john/house/points")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"x\":5,\"y\":5}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.code").value(202));
    }

    @Test
    void addPoint_notFound_returns404() throws Exception {
        doThrow(new BlueprintNotFoundException("Blueprint not found"))
                .when(services).addPoint(eq("unknown"), eq("house"), anyInt(), anyInt());

        mockMvc.perform(put("/api/v1/blueprints/unknown/house/points")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"x\":5,\"y\":5}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }
}