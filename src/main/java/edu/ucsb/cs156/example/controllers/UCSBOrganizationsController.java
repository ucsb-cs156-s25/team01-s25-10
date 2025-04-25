package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * This is a REST controller for UCSBOrganizations
 */

@Tag(name = "UCSBOrganizations")
@RequestMapping("/api/ucsborganizations")
@RestController
@Slf4j

public class UCSBOrganizationsController extends ApiController {

    

    @Autowired
    UCSBOrganizationRepository ucsbOrganizationRepository;

    /**
     * This method returns a list of all ucsborganizations.
     * @return a list of all ucsborganizations
     */

    @Operation(summary = "List all UCSB Organizations")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<UCSBOrganization> allUCSBOrganizations() {
        Iterable<UCSBOrganization> ucsbOrganizations = ucsbOrganizationRepository.findAll();
        return ucsbOrganizations;
    }

    /**
     * This method creates a new ucsborganization. Accessible only to users with the role "ROLE_ADMIN".
     * @param orgCode code of the organization
     * @param orgTranslationShort shortened translation of the organization
     * @param orgTranslation full translation of the organization
     * @param inactive whether or not the organization is inactive
     * @return the saved ucsborganization
     */
    @Operation(summary= "Create a new ucsborganization")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public UCSBOrganization postOrganizations(
        @Parameter(name="orgCode") @RequestParam String orgCode,
        @Parameter(name="orgTranslationShort") @RequestParam String orgTranslationShort,
        @Parameter(name="orgTranslation") @RequestParam String orgTranslation,
        @Parameter(name="inactive") @RequestParam boolean inactive 
        )
        {

        UCSBOrganization ucsbOrganization = new UCSBOrganization();
        ucsbOrganization.setOrgCode(orgCode);
        ucsbOrganization.setOrgTranslationShort(orgTranslationShort);
        ucsbOrganization.setOrgTranslation(orgTranslation);
        ucsbOrganization.setInactive(inactive);

        UCSBOrganization savedUCSBOrganization = ucsbOrganizationRepository.save(ucsbOrganization);

        return savedUCSBOrganization;
    }

    /**
     * This method returns a single ucsborganization.
     * @param code code of the ucsborganization
     * @return a single ucsborganization
     */
    @Operation(summary= "Get a single organization")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public UCSBOrganization getById(
            @Parameter(name="code") @RequestParam String code) {
        UCSBOrganization organization = ucsbOrganizationRepository.findById(code)
                .orElseThrow(() -> new EntityNotFoundException(UCSBOrganization.class, code));

        return organization;
    }

     /**
     * Update a single organization. Accessible only to users with the role "ROLE_ADMIN".
     * @param code code of the organization
     * @param incoming the new organization contents
     * @return the updated organization object
     */
    @Operation(summary= "Update a single organization")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public UCSBOrganization updateOrganization(
            @Parameter(name="code") @RequestParam String code,
            @RequestBody @Valid UCSBOrganization incoming) {

        UCSBOrganization organization = ucsbOrganizationRepository.findById(code)
                .orElseThrow(() -> new EntityNotFoundException(UCSBOrganization.class, code));


        organization.setOrgCode(incoming.getOrgCode());  
        organization.setOrgTranslationShort(incoming.getOrgTranslationShort());
        organization.setOrgTranslation(incoming.getOrgTranslation());
        organization.setInactive(incoming.getInactive());

        ucsbOrganizationRepository.save(organization);

        return organization;
    }

    /**
     * Delete an organization. Accessible only to users with the role "ROLE_ADMIN".
     * @param code code of the organization
     * @return a message indiciating the organization was deleted
     */
    @Operation(summary= "Delete a UCSBOrganization")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteOrganization(
            @Parameter(name="code") @RequestParam String code) {
        UCSBOrganization organization = ucsbOrganizationRepository.findById(code)
                .orElseThrow(() -> new EntityNotFoundException(UCSBOrganization.class, code));

        ucsbOrganizationRepository.delete(organization);
        return genericMessage("UCSBOrganization with id %s deleted".formatted(code));
    }

}
