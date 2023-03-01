package org.olac.reservation.utility;

import org.olac.reservation.utility.model.Account;

import java.util.List;
import java.util.Optional;

public interface SecurityUtility {

    String getCurrentUserName();

    List<Account> getAccounts();

    Account createAccount(String username, String email, boolean admin);

    Optional<Account> findAccount(String username);

    boolean updateAccount(Account account);

    String validateUserWithGoogleIdentity(String credential);

}
