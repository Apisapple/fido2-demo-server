package tf.demo.fido2.fido.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tf.demo.fido2.fido.application.FidoService;

@RestController
@RequestMapping("/api/fido")
class FidoController {
    private final FidoService fidoService;

    FidoController(FidoService fidoService) {
        this.fidoService = fidoService;
    }

    @PostMapping("/registration/options")
    FidoService.CeremonyOptions registrationOptions(@Valid @RequestBody RegistrationStart request) {
        return fidoService.startRegistration(request.username(), request.displayName());
    }

    @PostMapping("/registration/verify")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void registrationVerify(@Valid @RequestBody Finish request) {
        fidoService.finishRegistration(request.ceremonyId(), request.credentialJson());
    }

    @PostMapping("/authentication/options")
    FidoService.CeremonyOptions authenticationOptions(
            @Valid @RequestBody AuthenticationStart request) {
        return fidoService.startAuthentication(request.username());
    }

    @PostMapping("/authentication/verify")
    FidoService.AuthenticationResult authenticationVerify(@Valid @RequestBody Finish request) {
        return fidoService.finishAuthentication(request.ceremonyId(), request.credentialJson());
    }

    @PostMapping("/authentication/discoverable/options")
    FidoService.CeremonyOptions discoverableAuthenticationOptions() {
        return fidoService.startDiscoverableAuthentication();
    }

    @PostMapping("/authentication/discoverable/verify")
    FidoService.AuthenticationResult discoverableAuthenticationVerify(
            @Valid @RequestBody Finish request) {
        return fidoService.finishDiscoverableAuthentication(
                request.ceremonyId(), request.credentialJson());
    }

    record RegistrationStart(@NotBlank String username, @NotBlank String displayName) {}

    record AuthenticationStart(@NotBlank String username) {}

    record Finish(@NotNull UUID ceremonyId, @NotBlank String credentialJson) {}
}
