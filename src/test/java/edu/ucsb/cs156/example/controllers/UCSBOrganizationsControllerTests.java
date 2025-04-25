package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBDiningCommons;
import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsRepository;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = UCSBOrganizationsController.class)
@Import(TestConfig.class)
public class UCSBOrganizationsControllerTests extends ControllerTestCase {
    
    @MockBean
    UCSBOrganizationRepository ucsbOrganizationRepository;

    @MockBean
    UserRepository userRepository;


    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
            mockMvc.perform(get("/api/ucsborganizations/all"))
                            .andExpect(status().is(403)); // logged out users can't get all
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all() throws Exception {
            mockMvc.perform(get("/api/ucsborganizations/all"))
                            .andExpect(status().is(200)); // logged
    }

    @Test
    public void logged_out_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/ucsborganizations/post"))
                            .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/ucsborganizations/post"))
                            .andExpect(status().is(403)); // only admins can post
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_user_can_get_all_ucsborganizations() throws Exception {
            // arrange

            UCSBOrganization organization1 = UCSBOrganization.builder()
                .orgCode("zpr")
                .orgTranslationShort("Zeta Phi Rho")
                .orgTranslation("Zeta Phi Rho")
                .inactive(false)
                .build();

            ArrayList<UCSBOrganization> expectedOrganizations = new ArrayList<>();
            expectedOrganizations.add(organization1);

            when(ucsbOrganizationRepository .findAll()).thenReturn(expectedOrganizations);
            // act
            MvcResult response = mockMvc.perform(get("/api/ucsborganizations/all"))
                            .andExpect(status().isOk()).andReturn();
            // assert
            verify(ucsbOrganizationRepository, times(1)).findAll();
            String expectedJson = mapper.writeValueAsString(expectedOrganizations);
            String responseString = response.getResponse().getContentAsString();
            assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_a_new_organization() throws Exception {
            // arrange

            UCSBOrganization organization1 = UCSBOrganization.builder()
                            .orgCode("zpr")
                            .orgTranslationShort("Zeta Phi Rho")
                            .orgTranslation("Zeta Phi Rho")
                            .inactive(false)
                            .build();

            when(ucsbOrganizationRepository.save(eq(organization1))).thenReturn(organization1);

            // act
            MvcResult response = mockMvc.perform(
                            post("/api/ucsborganizations/post?orgCode=zpr&orgTranslationShort=Zeta Phi Rho&orgTranslation=Zeta Phi Rho&inactive=false")
                                            .with(csrf()))
                            .andExpect(status().isOk()).andReturn();

            // assert
            verify(ucsbOrganizationRepository, times(1)).save(eq(organization1));
            String expectedJson = mapper.writeValueAsString(organization1);
            String responseString = response.getResponse().getContentAsString();
            assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_a_new_organization_with_inactive_true() throws Exception {
        // arrange
        UCSBOrganization organization1 = UCSBOrganization.builder()
                        .orgCode("zpr")
                        .orgTranslationShort("Zeta Phi Rho")
                        .orgTranslation("Zeta Phi Rho")
                        .inactive(true)
                        .build();

        when(ucsbOrganizationRepository.save(eq(organization1))).thenReturn(organization1);

        // act
        MvcResult response = mockMvc.perform(
                        post("/api/ucsborganizations/post?orgCode=zpr&orgTranslationShort=Zeta Phi Rho&orgTranslation=Zeta Phi Rho&inactive=true")
                                        .with(csrf()))
                        .andExpect(status().isOk()).andReturn();

        // assert
        verify(ucsbOrganizationRepository, times(1)).save(eq(organization1));
        String expectedJson = mapper.writeValueAsString(organization1);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @Test
    public void logged_out_users_cannot_get_by_id() throws Exception {
            mockMvc.perform(get("/api/ucsborganizations?code=carrillo"))
                            .andExpect(status().is(403)); // logged out users can't get by id
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

            // arrange

            UCSBOrganization organization1 = UCSBOrganization.builder()
                        .orgCode("zpr")
                        .orgTranslationShort("Zeta Phi Rho")
                        .orgTranslation("Zeta Phi Rho")
                        .inactive(true)
                        .build();

            when(ucsbOrganizationRepository.findById(eq("zpr"))).thenReturn(Optional.of(organization1));

            // act
            MvcResult response = mockMvc.perform(get("/api/ucsborganizations?code=zpr"))
                            .andExpect(status().isOk()).andReturn();

            // assert

            verify(ucsbOrganizationRepository, times(1)).findById(eq("zpr"));
            String expectedJson = mapper.writeValueAsString(organization1);
            String responseString = response.getResponse().getContentAsString();
            assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

            // arrange

            when(ucsbOrganizationRepository.findById(eq("munger-hall"))).thenReturn(Optional.empty());

            // act
            MvcResult response = mockMvc.perform(get("/api/ucsborganizations?code=munger-hall"))
                            .andExpect(status().isNotFound()).andReturn();

            // assert

            verify(ucsbOrganizationRepository, times(1)).findById(eq("munger-hall"));
            Map<String, Object> json = responseToJson(response);
            assertEquals("EntityNotFoundException", json.get("type"));
            assertEquals("UCSBOrganization with id munger-hall not found", json.get("message"));
    }




}
