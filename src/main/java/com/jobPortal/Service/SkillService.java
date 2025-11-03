package com.jobPortal.Service;

import com.jobPortal.DTO.SkillDTO;
import com.jobPortal.Model.Skill;
import com.jobPortal.Repository.SkillRepository;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SkillService {

    @Autowired
    private SkillRepository skillRepository;

    public Skill findSkillById(Long id) {
        return skillRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Skill not found with id: " + id));
    }

    @Transactional
    public List<Skill> getAllSkills() {
        return skillRepository.findAll();
    }

    @Transactional
    public Optional<Skill> findSkillBySkillName(String skillName) {
        return skillRepository.findByName(skillName);
    }

    @Transactional
    public Skill saveSkill(SkillDTO skillDTO) {
        Optional<Skill> existingSkill = skillRepository.findByName(skillDTO.getName());
        if (existingSkill.isPresent()) {
            return existingSkill.get();
        }

        Skill skill = new Skill();
        skill.setName(skillDTO.getName());
        skill.setActive(true); // default active
        return skillRepository.save(skill);
    }

    @Transactional
    public Skill updateSkill(Long id, SkillDTO skillDTO) {
        Skill existingSkill = skillRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Skill not found"));
        existingSkill.setName(skillDTO.getName());
        return skillRepository.save(existingSkill);
    }

    @Transactional
    public void deleteSkill(Long id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Skill not found"));
        skill.setActive(false); // soft delete
        skillRepository.save(skill);
    }
}
