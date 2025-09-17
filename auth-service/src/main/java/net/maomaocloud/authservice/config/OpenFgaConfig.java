//package net.maomaocloud.authservice.config;
//
//import dev.openfga.sdk.api.client.OpenFgaClient;
//import dev.openfga.sdk.api.configuration.ClientConfiguration;
//import dev.openfga.sdk.errors.FgaInvalidParameterException;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class OpenFgaConfig {
//
//    @Value("${FGA_API_URL}")
//    private String apiUrl;
//
//    @Value("${FGA_STORE_ID}")
//    private String storeId;
//
//    @Value("${FGA_MODEL_ID}")
//    private String modelId;
//
//    @Value("{ENABLE_FGA:false}")
//    private boolean isFgaEnabled;
//
//    @Bean
//    public OpenFgaClient openFgaClient() throws FgaInvalidParameterException {
//        if (isFgaEnabled) {
//            ClientConfiguration config = new ClientConfiguration()
//                    .apiUrl(apiUrl)
//                    .storeId(storeId)
//                    .authorizationModelId(modelId);
//
//            return new OpenFgaClient(config);
//        }
//        return null;
//    }
//}
