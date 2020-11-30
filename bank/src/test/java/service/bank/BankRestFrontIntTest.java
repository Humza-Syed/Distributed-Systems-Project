package service.bank;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import service.bankfrontend.BankRestFront;
import service.core.ActorStatus;
import service.core.Status;
import service.message.Message;
import service.message.TransactionRequest;
import service.message.TransactionResponse;
import service.message.TransactionType;
import service.message.ValidationRequest;
import service.message.ValidationResponse;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = BankRestFront.class)
public class BankRestFrontIntTest {

  private ObjectMapper objectMapper;
  private MockMvc mvc;

  @Before
  public void setup() {
    objectMapper = new ObjectMapper();
    ActorSystem bankActorSystem = ActorSystem.create();

    // preparing bank actors for testing
    int i = 0;
    int ACTOR_COUNT = 3;
    while (i < ACTOR_COUNT) {
      String actorId = UUID.randomUUID().toString();
      ActorRef newActor = bankActorSystem.actorOf(Props.create(BankActor.class), actorId);
      BankRestFront.actors.put(newActor, ActorStatus.AVAILABLE);
      ++i;
    }
    mvc = MockMvcBuilders.standaloneSetup(BankRestFront.class).build();
  }

  @Test
  public void ValidationHandling_RequestMapped_ResponseCreated()
      throws Exception {
    setup();

    ValidationRequest validationRequest = new ValidationRequest("1", 1, 1000);
    mvc.perform(post("/validation")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(validationRequest)))
        .andExpect(status().isCreated());
  }

  @Test
  public void ValidationHandling_LegalValidation_IsSuccessful()
      throws Exception {
    setup();

    ValidationRequest legalValidationRequest = new ValidationRequest("1", 1, 1000);
    MvcResult result = getResult("/validation", legalValidationRequest);
    ValidationResponse legalValidationResponse = objectMapper.readValue(
        result.getResponse().getContentAsString(), ValidationResponse.class);

    Assert.assertEquals(legalValidationResponse.getStatus(), Status.SUCCESS);
  }

  @Test
  public void ValidationHandling_TwoLegalValidations_ReturnDifferentTokens()
      throws Exception {
    setup();

    ValidationRequest validationRequest = new ValidationRequest("1", 1, 1000);
    MvcResult result = getResult("/validation", validationRequest);
    ValidationResponse validationResponse = objectMapper.readValue(
        result.getResponse().getContentAsString(), ValidationResponse.class);
    String originalLegalToken = validationResponse.getValidationToken();

    validationRequest = new ValidationRequest("2", 2, 2000);
    result = getResult("/validation", validationRequest);
    ValidationResponse secondLegalValidationResponse = objectMapper.readValue(
        result.getResponse().getContentAsString(), ValidationResponse.class);

    Assert.assertEquals(secondLegalValidationResponse.getStatus(), Status.SUCCESS);
    Assert.assertNotEquals(originalLegalToken, secondLegalValidationResponse.getValidationToken());
  }

  @Test
  public void ValidationHandling_IncorrectValidation_IsFailure()
      throws Exception {
    setup();

    ValidationRequest illegalValidationRequest = new ValidationRequest("3", 1, 2000);
    MvcResult result = getResult("/validation", illegalValidationRequest);
    ValidationResponse illegalValidationResponse = objectMapper.readValue(
        result.getResponse().getContentAsString(), ValidationResponse.class);

    Assert.assertEquals(illegalValidationResponse.getStatus(), Status.FAILURE);
    Assert.assertNull(illegalValidationResponse.getValidationToken());
  }

  @Test
  public void TransactionHandling_LegalTransaction_IsSuccessful()
      throws Exception {
    setup();

    ValidationRequest validationRequest = new ValidationRequest("1", 1, 1000);
    MvcResult validationResult = getResult("/validation", validationRequest);
    ValidationResponse validationResponse = objectMapper.readValue(
        validationResult.getResponse().getContentAsString(), ValidationResponse.class);

    TransactionRequest transactionRequest = new TransactionRequest(
        validationResponse.getMessageId(),
        validationRequest.getAccountId(), TransactionType.DEPOSIT,
        220.5, validationResponse.getValidationToken());
    MvcResult transactionResult = mvc.perform(post("/transaction")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(transactionRequest)))
        .andExpect(status().isCreated()).andReturn();
    TransactionResponse legalTransactionResponse = objectMapper.readValue(
        transactionResult.getResponse().getContentAsString(), TransactionResponse.class);

    Assert.assertEquals(legalTransactionResponse.getStatus(), Status.SUCCESS);
  }

  @Test
  public void TransactionHandling_TransactionReusingAToken_IsFailure()
      throws Exception {
    setup();

    ValidationRequest validationRequest = new ValidationRequest("1", 1, 1000);
    MvcResult validationResult = getResult("/validation", validationRequest);
    ValidationResponse originalValidationResponse = objectMapper.readValue(
        validationResult.getResponse().getContentAsString(), ValidationResponse.class);

    TransactionRequest transactionRequest1 = new TransactionRequest(
        originalValidationResponse.getMessageId(),
        validationRequest.getAccountId(), TransactionType.DEPOSIT,
        220.5, originalValidationResponse.getValidationToken());
    MvcResult transactionResult1 = getResult("/transaction", transactionRequest1);

    TransactionRequest transactionRequest2 = new TransactionRequest("2",
        validationRequest.getAccountId(), TransactionType.DEPOSIT,
        220.5, originalValidationResponse.getValidationToken());
    MvcResult transactionResult2 = getResult("/transaction", transactionRequest2);
    TransactionResponse copyCatTransactionResponse = objectMapper.readValue(
        transactionResult2.getResponse().getContentAsString(), TransactionResponse.class);

    Assert.assertEquals(copyCatTransactionResponse.getStatus(), Status.FAILURE);
  }

  @Test
  public void TransactionHandling_TransactionWithMadeUpToken_IsFailure()
      throws Exception {
    setup();

    TransactionRequest transactionRequest = new TransactionRequest("2", 2,
        TransactionType.DEPOSIT, 220.5, "fjfhfdkfhih");
    MvcResult transactionResult = getResult("/transaction", transactionRequest);
    TransactionResponse transactionResponse = objectMapper.readValue(
        transactionResult.getResponse().getContentAsString(), TransactionResponse.class);

    Assert.assertEquals(transactionResponse.getStatus(), Status.FAILURE);
  }

  @Test
  public void TransactionHandling_TransactionWithNullToken_IsFailure()
      throws Exception {
    setup();

    TransactionRequest transactionRequest = new TransactionRequest("3", 2,
        TransactionType.DEPOSIT, 220.5, null);
    MvcResult transactionResult = getResult("/transaction", transactionRequest);
    TransactionResponse transactionResponse = objectMapper.readValue(
        transactionResult.getResponse().getContentAsString(), TransactionResponse.class);

    Assert.assertEquals(transactionResponse.getStatus(), Status.FAILURE);
  }

  @Test
  public void TransactionHandling_TransactionWithExistingButMismatchedCredentials_IsFailure()
      throws Exception {
    setup();

    ValidationRequest validationRequest = new ValidationRequest("4", 4, 4000);
    MvcResult validationResult = getResult("/validation", validationRequest);
    ValidationResponse validationResponse = objectMapper.readValue(
        validationResult.getResponse().getContentAsString(), ValidationResponse.class);

    TransactionRequest mismatchedTransactionRequest = new TransactionRequest("4", 5,
        TransactionType.DEPOSIT, 220.5, validationResponse.getValidationToken());
    MvcResult transactionResult = getResult("/transaction", mismatchedTransactionRequest);
    TransactionResponse mismatchedTransactionResponse = objectMapper.readValue(
        transactionResult.getResponse().getContentAsString(), TransactionResponse.class);

    Assert.assertEquals(mismatchedTransactionResponse.getStatus(), Status.FAILURE);
  }

  private MvcResult getResult(String url, Message requestMessage) throws Exception {
    return mvc.perform(post(url)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestMessage)))
        .andReturn();
  }
}