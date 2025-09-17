package net.maomaocloud.authservice.api.auth.common;

import com.nimbusds.oauth2.sdk.pkce.CodeChallenge;
import com.nimbusds.oauth2.sdk.pkce.CodeChallengeMethod;
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier;

public class PkceUtils {

    public static PkcePair generatePkce() {
        CodeVerifier verifier = new CodeVerifier();
        CodeChallenge challenge = CodeChallenge.compute(CodeChallengeMethod.S256, verifier);
        return new PkcePair(verifier.getValue(), challenge.getValue());
    }


    public record PkcePair(String verifier, String challenge) {}

}
