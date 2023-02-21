package org.olac.reservation.utility.spring;

import lombok.RequiredArgsConstructor;
import org.olac.reservation.utility.SecurityUtility;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SpringSecurityUtility implements SecurityUtility {

    @Override
    public String getCurrentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return "anonymous";
        }
        return authentication.getName();
    }

}
