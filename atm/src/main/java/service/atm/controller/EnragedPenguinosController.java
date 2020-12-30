package service.atm.controller;

import java.io.IOException;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import service.atm.Atm;
import service.message.TransactionType;

@Controller
public class EnragedPenguinosController {

  private Atm atm;
  private long accountId;
  private String bankId;
  private volatile boolean atmReady;
  private boolean launched;
  private boolean validated;

  @GetMapping("/")
  public String launchPage() {
    if (launched)
      return "index.html";
    atm = new Atm(UUID.randomUUID().toString(), 10000, "failover://tcp://localhost:61616");
    atm.start();
    launched = true;
    new Thread(() -> {
      try {
        Thread.sleep(5000);   // time for atm.start() to finish setting up first
      } catch (InterruptedException e) {
        throw new RuntimeException("Unexpected interrupt", e);
      }
      atmReady = true;
    }).start();
    return "index.html";
  }

  @GetMapping("/validate")
  public String validate(Model model, HttpServletResponse response)
      throws IOException, InterruptedException {
    if (!launched){
      response.sendRedirect("/");
      return null;
    }
    while (!atmReady) {
      Thread.onSpinWait();
    }
    model.addAttribute("addressBook", atm.getKnownBanks());
    return "validation.html";
  }

  @PostMapping("/validate")
  public String validate(long accountId, int pinNumber, String bankId, Model model) {
    this.accountId = accountId;
    this.bankId = bankId;
    String response = atm.validate(accountId, pinNumber, bankId);
    if (response.equals("Validation has completed successfully")){
      validated = true;
      return "transaction.html";
    }
    model.addAttribute("errorMessage", response);
    return "failure.html";
  }

  @GetMapping("/transact")
  public String transact(HttpServletResponse response) throws IOException {
    if (!launched){
      response.sendRedirect("/");
      return null;
    }
    if (!validated){
      response.sendRedirect("/validate");
      return null;
    }
    return "transaction.html";
  }

  @PostMapping("/transact")
  public String transact(String transactionTypeString, int amount, Model model) {
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
    if (response.equals("Transaction has been processed successfully")){
      return "success.html";
    }
    model.addAttribute("errorMessage", response);
    return "failure.html";
  }

}