package tf.demo.fido2.fido.api;

import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tf.demo.fido2.fido.application.FidoService;

@Profile("!prod")
@RestController
@RequestMapping("/api/fido/users")
class FidoCredentialManagementController {
  private final FidoService fidoService;

  FidoCredentialManagementController(FidoService fidoService) {
    this.fidoService = fidoService;
  }

  @GetMapping("/{username}/credentials")
  List<FidoService.CredentialSummary> credentials(@PathVariable String username) {
    return fidoService.credentials(username);
  }

  @DeleteMapping("/{username}/credentials/{credentialId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void deleteCredential(@PathVariable String username, @PathVariable String credentialId) {
    fidoService.deleteCredential(username, credentialId);
  }
}
