package org.olac.reservation.utility;

import org.olac.reservation.utility.model.Account;
import org.olac.reservation.utility.model.ValidateUserResponse;

import java.util.List;
import java.util.Optional;

public interface SecurityUtility {

    String getCurrentUserName();

    List<Account> getAccounts();

    Account createAccount(String username, String email, boolean admin);

    Optional<Account> findAccount(String username);

    boolean updateAccount(Account account);

    ValidateUserResponse validateUserWithGoogleIdentity(String credential);

}
