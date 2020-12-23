package service.model;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<BankAccount, Long> {

  Optional<BankAccount> findByAccountIdAndPinNumber(long accountId, int pinNumber);

  Optional<BankAccount> findByAccountId(long accountId);
  List<BankAccount> findAllByBankId(long bankId);
}
