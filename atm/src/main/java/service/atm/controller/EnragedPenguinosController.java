package service.atm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import service.atm.Atm;
import service.message.TransactionType;

@Controller
public class EnragedPenguinosController {

  private Atm atm;
  private long accountId;
  private String bankId;

  @GetMapping("/")
  public String index() {
    return "index.html";
  }

  @PostMapping("/validate")
  public String validate(long accountId, int pinNumber, String bankId) throws InterruptedException {
    this.accountId = accountId;
    this.bankId = bankId;
    atm = new Atm(123, 10000);
    atm.start();
    Thread.sleep(5000);   // time for atm.start() to finish setting up first
    String response = atm.validate(accountId, pinNumber, bankId);
    return response.equals("Validation has completed successfully")?"transaction.html":"failure.html";
  }

  @PostMapping("/transact")
  public String transact(String transactionTypeString, int amount) {
    TransactionType transactionType;
    if (transactionTypeString.equals("Deposit")){
      transactionType = TransactionType.DEPOSIT;
    } else if (transactionTypeString.equals("Withdrawal")){
      transactionType = TransactionType.WITHDRAW;
    } else {
      System.out.println("TransactionType must be either Withdrawal or Deposit. Something has gone wrong.");
      return "failure.html";
    }
    String response = atm.transact(accountId, transactionType, amount, bankId);
    return response.equals("Transaction has been processed successfully")?"success.html":"failure.html";
  }

}