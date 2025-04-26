package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;


@WebMvcTest(controllers = RecommendationRequestsController.class)
@Import(TestConfig.class)
public class RecommendationRequestsControllerTests extends ControllerTestCase{

    @MockBean
    RecommendationRequestRepository recommendationRequestRepository;

    @MockBean
    UserRepository userRepository;

    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
            mockMvc.perform(get("/api/recommendationrequests/all"))
                            .andExpect(status().is(403)); // logged out users can't get all
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all() throws Exception {
            mockMvc.perform(get("/api/recommendationrequests/all"))
                            .andExpect(status().is(200)); // logged
    }

    @Test
    public void logged_out_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/recommendationrequests/all"))
                            .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/recommendationrequests/all"))
                            .andExpect(status().is(403)); // only admins can post
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_user_can_get_all_recommendationrequests() throws Exception {

        // arrange
        LocalDateTime ldt1 = LocalDateTime.parse("2025-01-03T00:00:00");
        LocalDateTime ldt2 = LocalDateTime.parse("2025-05-15T09:15:00");

        RecommendationRequest request1 = RecommendationRequest.builder()
        .requesterEmail("mihirkondapalli@ucsb.edu")
        .professorEmail("pconrad@ucsb.edu")
        .explanation("Test")
        .dateRequested(ldt1)
        .dateNeeded(ldt2)
        .done(false)
        .build();

        ArrayList<RecommendationRequest> expectedRecommendationRequests = new ArrayList<>();
        expectedRecommendationRequests.add(request1);

        when(recommendationRequestRepository.findAll()).thenReturn(expectedRecommendationRequests);

        // act
        MvcResult response = mockMvc.perform(get("/api/recommendationrequests/all"))
                        .andExpect(status().isOk()).andReturn();

        // assert

        verify(recommendationRequestRepository, times(1)).findAll();
        String expectedJson = mapper.writeValueAsString(expectedRecommendationRequests);

        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_a_new_recommendationrequest() throws Exception {
        // arrange

        LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
        LocalDateTime ldt2 = LocalDateTime.parse("2025-01-03T00:00:00");

        RecommendationRequest request1 = RecommendationRequest.builder()
        .requesterEmail("mihirkondapalli@ucsb.edu")
        .professorEmail("pconrad@ucsb.edu")
        .explanation("Test")
        .dateRequested(ldt1)
        .dateNeeded(ldt2)
        .done(false)
        .build();

        when(recommendationRequestRepository.save(eq(request1))).thenReturn(request1);

        // act
        MvcResult response = mockMvc.perform(
                        post("/api/recommendationrequests/post?requesterEmail=mihirkondapalli@ucsb.edu&professorEmail=pconrad@ucsb.edu&"
                        + "explanation=Test&dateRequested=2022-01-03T00:00:00&dateNeeded=2025-01-03T00:00:00&done=false")
                                        .with(csrf()))
                        .andExpect(status().isOk()).andReturn();

        // assert
        verify(recommendationRequestRepository, times(1)).save(eq(request1));
        String expectedJson = mapper.writeValueAsString(request1);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @Test
    public void logged_out_users_cannot_get_by_id() throws Exception {
        mockMvc.perform(get("/api/recommendationrequests?id=7"))
                        .andExpect(status().is(403)); // logged out users can't get by id
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

        // arrange

        when(recommendationRequestRepository.findById(eq(7L))).thenReturn(Optional.empty());

        // act
        MvcResult response = mockMvc.perform(get("/api/recommendationrequests?id=7"))
                        .andExpect(status().isNotFound()).andReturn();

        // assert

        verify(recommendationRequestRepository, times(1)).findById(eq(7L));
        Map<String, Object> json = responseToJson(response);
        assertEquals("EntityNotFoundException", json.get("type"));
        assertEquals("RecommendationRequest with id 7 not found", json.get("message"));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_does_exist() throws Exception {

        // arrange
        LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
        LocalDateTime ldt2 = LocalDateTime.parse("2025-01-03T00:00:00");

        RecommendationRequest request1 = RecommendationRequest.builder()
                .requesterEmail("mihirkondapalli@ucsb.edu")
                .professorEmail("pconrad@ucsb.edu")
                .explanation("Test")
                .dateRequested(ldt1)
                .dateNeeded(ldt2)
                .done(false)
                .build();

        when(recommendationRequestRepository.findById(eq(7L))).thenReturn(Optional.of(request1));

        // act
        MvcResult response = mockMvc.perform(get("/api/recommendationrequests?id=7"))
                        .andExpect(status().isOk()).andReturn();

        // assert
        verify(recommendationRequestRepository, times(1)).findById(eq(7L));
        String expectedJson = mapper.writeValueAsString(request1);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }
}