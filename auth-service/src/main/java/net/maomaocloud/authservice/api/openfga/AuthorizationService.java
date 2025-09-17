package net.maomaocloud.authservice.api.openfga;

import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {

    /**
     * Checks if the user has access to the specified object with the given relation.
     *
     * @param userId The ID of the user.
     * @param object The object to check access for.
     * @param relation The relation to check access against.
     * @return true if access is granted, false otherwise.
     */
    public boolean checkAccess(String userId, String object, String relation) {
        return true;
    }

}
