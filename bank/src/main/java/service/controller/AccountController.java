package service.controller;

import service.model.Account;
import service.model.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class AccountController {
  @Autowired
  private AccountRepository accountRepository;

}
