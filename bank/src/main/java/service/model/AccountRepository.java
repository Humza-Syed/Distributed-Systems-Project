package service.model;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

  Optional<Account> findByAccountIdAndPinNumber(Long accountId, Integer pinNumber);

  List<Account> findAllByBankId(Long bankId);
}
