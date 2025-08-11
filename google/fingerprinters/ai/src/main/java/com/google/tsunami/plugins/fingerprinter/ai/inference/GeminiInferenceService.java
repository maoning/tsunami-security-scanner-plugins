/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.tsunami.plugins.fingerprinters.ai.inference;

import com.google.cloud.vertexai.api.FunctionCall;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.google.common.flogger.GoogleLogger;
import com.google.protobuf.Value;
import com.google.tsunami.proto.AiWebServiceContext;
import com.google.tsunami.proto.NetworkService;
import com.google.tsunami.proto.ServiceContext;
import com.google.tsunami.proto.Software;
import com.google.tsunami.proto.Version;
import com.google.tsunami.proto.Version.VersionType;
import com.google.tsunami.proto.VersionSet;
import java.util.Map;
import javax.inject.Inject;

/** An implementation of {@link AiInferenceService} that uses Gemini. */
final class GeminiInferenceService implements AiInferenceService {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private final GenerativeModel generativeModel;

  @Inject
  GeminiInferenceService(GenerativeModel generativeModel) {
    this.generativeModel = generativeModel;
  }

  @Override
  public NetworkService identifyWebApplication(NetworkService networkService, String prompt) {
    try {
      GenerateContentResponse response = generativeModel.generateContent(prompt);
      FunctionCall functionCall = ResponseHandler.getFunctionCalls(response).get(0);
      Map<String, Value> responseContent = functionCall.getArgs().getFieldsMap();

      if (responseContent.get("applicationName") == null) {
        return networkService;
      }
      String applicationName = responseContent.get("applicationName").getStringValue();
      String version = responseContent.get("version").getStringValue();
      boolean isLoginFormPresent = responseContent.get("isLoginFormPresent").getBoolValue();
      boolean isAuthenticationRequired =
          responseContent.get("isAuthenticationRequired").getBoolValue();
      boolean hasKnownDefaultUsernamePassword =
          responseContent.get("hasKnownDefaultUsernamePassword").getBoolValue();
      String defaultUsername = "";
      if (responseContent.get("defaultUsername") != null) {
        defaultUsername = responseContent.get("defaultUsername").getStringValue();
      }
      String defaultPassword = "";
      if (responseContent.get("defaultPassword") != null) {
        defaultPassword = responseContent.get("defaultPassword").getStringValue();
      }
      boolean isAdminPage = responseContent.get("isAdminPage").getBoolValue();
      boolean isMetricsDashboard = responseContent.get("isMetricsDashboard").getBoolValue();
      boolean isKubernetesDeployment = responseContent.get("isKubernetesDeployment").getBoolValue();
      boolean isDemoInstance = responseContent.get("isDemoInstance").getBoolValue();
      boolean allowsCoedExecution = responseContent.get("allowsCoedExecution").getBoolValue();
      boolean allowsFileSystemAccess = responseContent.get("allowsFileSystemAccess").getBoolValue();
      boolean isDataStorageSolution = responseContent.get("isDataStorageSolution").getBoolValue();
      boolean allowsWorkflowExecution =
          responseContent.get("allowsWorkflowExecution").getBoolValue();
      boolean isUserSignUpFormPresent =
          responseContent.get("isUserSignUpFormPresent").getBoolValue();
      boolean isExposedWebAdminUi = responseContent.get("isExposedWebAdminUi").getBoolValue();
      String rationale = "";
      if (responseContent.get("rationale") != null) {
        rationale = responseContent.get("rationale").getStringValue();
      }

      logger.atInfo().log(
          "AI output:\n"
              + "--------------------------------------------------------------------------------\n"
              + "ip: %s\n"
              + "port: %s\n"
              + "applicationName: %s\n"
              + "version: %s\n"
              + "isLoginFormPresent: %s\n"
              + "isAuthenticationRequired: %s\n"
              + "hasKnownDefaultUsernamePassword: %s\n"
              + "defaultUsername: %s\n"
              + "defaultPassword: %s\n"
              + "isAdminPage: %s\n"
              + "isMetricsDashboard: %s\n"
              + "isKubernetesDeployment: %s\n"
              + "isDemoInstance: %s\n"
              + "allowsCoedExecution: %s\n"
              + "allowsFileSystemAccess: %s\n"
              + "isDataStorageSolution: %s\n"
              + "allowsWorkflowExecution: %s\n"
              + "isUserSignUpFormPresent: %s\n"
              + "isExposedWebAdminUi: %s\n"
              + "rationale: %s\n"
              + "--------------------------------------------------------------------------------\n",
          networkService.getNetworkEndpoint().getIpAddress().getAddress(),
          networkService.getNetworkEndpoint().getPort().getPortNumber(),
          applicationName,
          version,
          isLoginFormPresent,
          isAuthenticationRequired,
          hasKnownDefaultUsernamePassword,
          defaultUsername,
          defaultPassword,
          isAdminPage,
          isMetricsDashboard,
          isKubernetesDeployment,
          isDemoInstance,
          allowsCoedExecution,
          allowsFileSystemAccess,
          isDataStorageSolution,
          allowsWorkflowExecution,
          isUserSignUpFormPresent,
          isExposedWebAdminUi,
          rationale);

      logger.atInfo().log(
          "CSV: %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s",
          networkService.getNetworkEndpoint().getIpAddress().getAddress(),
          networkService.getNetworkEndpoint().getPort().getPortNumber(),
          applicationName,
          version,
          isLoginFormPresent,
          isAuthenticationRequired,
          hasKnownDefaultUsernamePassword,
          defaultUsername,
          defaultPassword,
          isAdminPage,
          isMetricsDashboard,
          isKubernetesDeployment,
          isDemoInstance,
          allowsCoedExecution,
          allowsFileSystemAccess,
          isDataStorageSolution,
          allowsWorkflowExecution,
          isUserSignUpFormPresent,
          isExposedWebAdminUi,
          rationale);

      if (applicationName.equals("value_unknown")) {
        return networkService;
      }

      NetworkService.Builder networkServiceBuilder =
          networkService.toBuilder()
              .setServiceContext(
                  ServiceContext.newBuilder()
                      .setAiWebServiceContext(
                          AiWebServiceContext.newBuilder()
                              .setSoftware(Software.newBuilder().setName(applicationName).build())
                              .setVersion(version)
                              .setIsLoginFormPresent(isLoginFormPresent)
                              .setIsAuthenticationRequired(isAuthenticationRequired)
                              .setHasKnownDefaultUsernamePassword(hasKnownDefaultUsernamePassword)
                              .setDefaultUsername(defaultUsername)
                              .setDefaultPassword(defaultPassword)
                              .setIsAdminPage(isAdminPage)
                              .setIsMetricsDashboard(isMetricsDashboard)
                              .setIsKubernetesDeployment(isKubernetesDeployment)
                              .setIsDemoInstance(isDemoInstance)
                              .setAllowsCodeExecution(allowsCoedExecution)
                              .setAllowsFileSystemAccess(allowsFileSystemAccess)
                              .setIsDataStorageSolution(isDataStorageSolution)
                              .setAllowsWorkflowExecution(allowsWorkflowExecution)
                              .setIsUserSignUpFormPresent(isUserSignUpFormPresent)
                              .setIsExposedWebAdminUi(isExposedWebAdminUi)
                              .setRationale(rationale)
                              .build()));

      if (!version.equals("value_unknown")) {
        networkServiceBuilder.setVersionSet(
            VersionSet.newBuilder()
                .addVersions(
                    Version.newBuilder()
                        .setType(VersionType.NORMAL)
                        .setFullVersionString(version)
                        .build())
                .build());
      }
      return networkServiceBuilder.build();
    } catch (Exception e) {
      logger.atWarning().withCause(e).log("Failed to generate content: %s", e.getMessage());
    }
    return networkService;
  }
}
