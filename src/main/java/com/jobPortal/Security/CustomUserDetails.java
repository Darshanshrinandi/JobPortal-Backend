package com.jobPortal.Security;

import com.jobPortal.Model.Company;
import com.jobPortal.Model.User;
import com.jobPortal.Repository.CompanyRepository;
import com.jobPortal.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetails implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        return userRepository.findByEmail(email)
                .map(user -> (UserDetails) new UserDetailsImpl(user))
                .orElseGet(() ->
                        companyRepository.findByEmail(email)
                                .map(company -> (UserDetails) new CompanyDetailsImpl(company))
                                .orElseThrow(() -> new UsernameNotFoundException("User or Company not found"))
                );
    }
}