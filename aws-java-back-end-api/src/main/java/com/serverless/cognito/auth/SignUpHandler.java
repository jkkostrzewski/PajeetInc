package com.serverless.cognito.auth;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AdminAddUserToGroupRequest;
import com.amazonaws.services.cognitoidp.model.AdminAddUserToGroupResult;
import com.amazonaws.services.cognitoidp.model.SignUpRequest;
import com.amazonaws.services.cognitoidp.model.SignUpResult;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.UsernameExistsException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.ApiGatewayResponse;
import com.serverless.Response;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.serverless.cognito.CognitoConfig;
import com.serverless.cognito.UserManagement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;


public class SignUpHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {
    private static final Logger LOG = LogManager.getLogger(SignUpHandler.class);
    private static final CognitoConfig cognitoConfig = new CognitoConfig();
    private static final AWSCognitoIdentityProvider cognitoClient = new UserManagement()
            .getAmazonCognitoIdentityClient();

    private AdminAddUserToGroupResult addUserToGroup(String email, String group) {
        try {
            AdminAddUserToGroupRequest addRequest = new AdminAddUserToGroupRequest();
            addRequest.setUserPoolId(cognitoConfig.getUserPoolId());
            addRequest.setUsername(email);
            addRequest.setGroupName(group);

            AdminAddUserToGroupResult addResult = cognitoClient.adminAddUserToGroup(addRequest);
            LOG.info(addResult);
            return addResult;
        } catch(Exception ex) {
            LOG.error("Error in adding user to a group " + ex);
        }
        return null;
    }

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        try {
            JsonNode body = new ObjectMapper().readValue((String) input.get("body"), JsonNode.class);

            SignUpRequest signUpRequest = new SignUpRequest();
            signUpRequest.setClientId(cognitoConfig.getClientId());
            signUpRequest.setUsername(body.get("email").asText());
            signUpRequest.setPassword(body.get("password").asText());

            List<AttributeType> cognitoAttrs = new LinkedList<>();
            cognitoAttrs.add(new AttributeType()
                    .withName("profile")
                    .withValue(body.get("profile").asText()));
            signUpRequest.setUserAttributes(cognitoAttrs);

            try {
                SignUpResult signUpResult = cognitoClient.signUp(signUpRequest);

                String group = "";
                if ((body.get("profile").asText()).equals("Candidate")) {
                    group = "Candidates";
                    LOG.info(addUserToGroup(body.get("email").asText(), group));
                } else if ((body.get("profile").asText()).equals("Recruiter")) {
                    group = "Recruiters";
                    LOG.info(addUserToGroup(body.get("email").asText(), group));
                }
                return ApiGatewayResponse.builder()
                        .setStatusCode(200)
                        .setObjectBody(signUpResult)
                        .build();
            } catch(UsernameExistsException ex) {
                LOG.error(ex.getMessage());
                return ApiGatewayResponse.builder()
                        .setStatusCode(ex.getStatusCode())
                        .setRawBody(ex.getErrorCode() + ": " + ex.getErrorMessage())
                        .build();
            }
        } catch (Exception ex) {
            LOG.error("Error in processing input request: " + ex);
            return ApiGatewayResponse.builder()
                    .setStatusCode(500)
                    .setObjectBody(new Response("Error in processing input request: ", input))
                    .build();
        }
    }
}
