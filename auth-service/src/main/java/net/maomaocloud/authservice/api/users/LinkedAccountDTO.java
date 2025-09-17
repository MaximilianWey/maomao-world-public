package net.maomaocloud.authservice.api.users;

public record LinkedAccountDTO(String externalId,
                               String providerName,
                               String preferredName,
                               String email) {

    public LinkedAccountDTO(LinkedAccount linkedAccount) {
        this(linkedAccount.getExternalId(),
             linkedAccount.getProviderName(),
             linkedAccount.getPreferredName(),
             linkedAccount.getEmail());
    }
}
