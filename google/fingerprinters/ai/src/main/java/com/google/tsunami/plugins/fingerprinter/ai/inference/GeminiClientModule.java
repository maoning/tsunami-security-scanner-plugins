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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.FunctionDeclaration;
import com.google.cloud.vertexai.api.Schema;
import com.google.cloud.vertexai.api.Tool;
import com.google.cloud.vertexai.api.Type;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import java.io.IOException;
import javax.inject.Singleton;

/**
 * A client for interacting with Gemini. This is a wrapper around the Gemini API that provides
 * methods for common tasks such as generating text, reasoning, and code execution.
 */
public final class GeminiClientModule extends AbstractModule {
  private final AiWebServiceFingerprinterConfigs configs;

  @Override
  protected void configure() {
    bind(AiInferenceService.class).to(GeminiInferenceService.class);
  }

  @Inject
  GeminiClientModule(
    AiWebServiceFingerprinterConfigs configs) {
        this.configs = checkNotNull(configs);
  }

  @Provides
  @Singleton
  GenerativeModel provideGenerativeModel() throws IOException {
    VertexAI vertexAI = new VertexAI(configs.getGcpProjectId, configs.getGcpLocation);
    FunctionDeclaration functionDeclaration =
        FunctionDeclaration.newBuilder()
            .setName("fingerprintWebService")
            .setDescription("Identify web service based on the homepage http body content.")
            .setParameters(
                Schema.newBuilder()
                    .setType(Type.OBJECT)
                    .putProperties(
                        "applicationName", Schema.newBuilder().setType(Type.STRING).build())
                    .putProperties("version", Schema.newBuilder().setType(Type.STRING).build())
                    .putProperties(
                        "isLoginFormPresent", Schema.newBuilder().setType(Type.BOOLEAN).build())
                    .putProperties(
                        "isAuthenticationRequired",
                        Schema.newBuilder().setType(Type.BOOLEAN).build())
                    .putProperties("isAdminPage", Schema.newBuilder().setType(Type.BOOLEAN).build())
                    .putProperties(
                        "hasKnownDefaultUsernamePassword",
                        Schema.newBuilder().setType(Type.BOOLEAN).build())
                    .putProperties(
                        "defaultUsername", Schema.newBuilder().setType(Type.STRING).build())
                    .putProperties(
                        "defaultPassword", Schema.newBuilder().setType(Type.STRING).build())
                    .putProperties(
                        "allowsCoedExecution", Schema.newBuilder().setType(Type.BOOLEAN).build())
                    .putProperties(
                        "allowsFileSystemAccess", Schema.newBuilder().setType(Type.BOOLEAN).build())
                    .putProperties(
                        "isDataStorageSolution", Schema.newBuilder().setType(Type.BOOLEAN).build())
                    .putProperties(
                        "allowsWorkflowExecution",
                        Schema.newBuilder().setType(Type.BOOLEAN).build())
                    .putProperties(
                        "isExposedWebAdminUi", Schema.newBuilder().setType(Type.BOOLEAN).build())
                    .putProperties(
                        "userSignUpFormPresent", Schema.newBuilder().setType(Type.BOOLEAN).build())
                    .putProperties("rationale", Schema.newBuilder().setType(Type.STRING).build())
                    .addRequired("applicationName")
                    .addRequired("version")
                    .addRequired("isLoginFormPresent")
                    .addRequired("isAuthenticationRequired")
                    .addRequired("isAdminPage")
                    .addRequired("hasKnownDefaultUsernamePassword")
                    .addRequired("allowsCoedExecution")
                    .addRequired("allowsFileSystemAccess")
                    .addRequired("isDataStorageSolution")
                    .addRequired("allowsWorkflowExecution")
                    .addRequired("isExposedWebAdminUi")
                    .addRequired("userSignUpFormPresent")
                    .addRequired("rationale")
                    .build())
            .build();

    Tool tool = Tool.newBuilder().addFunctionDeclarations(functionDeclaration).build();

    return new GenerativeModel("gemini-2.5-pro", vertexAI).withTools(ImmutableList.of(tool));
  }
}
