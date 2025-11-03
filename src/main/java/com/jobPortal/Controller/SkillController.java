package com.jobPortal.Controller;

import com.jobPortal.DTO.ApiResponse;
import com.jobPortal.DTO.SkillDTO;
import com.jobPortal.Model.Skill;
import com.jobPortal.Service.SkillService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/JobPortal/skills")
public class SkillController {

    @Autowired
    private SkillService skillService;

    @PreAuthorize("permitAll()")
    @GetMapping("/getSkill/{id}")
    public ResponseEntity<ApiResponse<Skill>> getSkill(@PathVariable Long id) {
        Skill skill = skillService.findSkillById(id);

        ApiResponse<Skill> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Skill retrieved successfully",
                skill
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/allSkills")
    public ResponseEntity<ApiResponse<List<Skill>>> getAllSkills() {
        List<Skill> allSkills = skillService.getAllSkills();

        ApiResponse<List<Skill>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Fetched All Skills",
                allSkills
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/addSkill")
    public ResponseEntity<ApiResponse<Skill>> addSkill(@RequestBody SkillDTO skillDTO) {
        Skill createdSkill = skillService.saveSkill(skillDTO);

        ApiResponse<Skill> response = new ApiResponse<>(
                HttpStatus.CREATED.value(),
                "Skill added successfully",
                createdSkill
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/updateSkill/{id}")
    public ResponseEntity<ApiResponse<Skill>> updateSkill(@PathVariable Long id,
                                                          @RequestBody SkillDTO skillDTO) {
        Skill updatedSkill = skillService.updateSkill(id, skillDTO);

        ApiResponse<Skill> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Skill updated successfully",
                updatedSkill
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/deleteSkill/{id}")
    public ResponseEntity<?> deleteSkill(@PathVariable Long id) {
        skillService.deleteSkill(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
