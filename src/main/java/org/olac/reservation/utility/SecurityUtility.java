package org.olac.reservation.utility;

import org.olac.reservation.utility.model.Account;

import java.util.List;
import java.util.Optional;

public interface SecurityUtility {

    String getCurrentUserName();

    List<Account> getAccounts();

    Account createAccount(String username, String password, boolean admin);

    boolean setPassword(String username, String newPassword);

    boolean validatePassword(String username, String password);

    Optional<Account> findAccount(String username);

    boolean updateAccount(Account account);

}
