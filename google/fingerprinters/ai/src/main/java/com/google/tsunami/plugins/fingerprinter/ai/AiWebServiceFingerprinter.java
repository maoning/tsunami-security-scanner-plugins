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
package com.google.tsunami.plugins.fingerprinters.web;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.tsunami.common.net.http.HttpRequest.get;

import com.google.common.flogger.GoogleLogger;
import com.google.tsunami.common.net.http.HttpClient;
import com.google.tsunami.common.net.http.HttpResponse;
import com.google.tsunami.plugin.PluginType;
import com.google.tsunami.plugin.ServiceFingerprinter;
import com.google.tsunami.plugin.annotations.ForWebService;
import com.google.tsunami.plugin.annotations.PluginInfo;
import com.google.tsunami.plugins.fingerprinters.ai.inference.AiInferenceService;
import com.google.tsunami.proto.FingerprintingReport;
import com.google.tsunami.proto.NetworkService;
import com.google.tsunami.proto.TargetInfo;
import javax.inject.Inject;

// import com.google.tsunami.proto.AiWebServiceContext;

/** A {@link ServiceFingerprinter} plugin that fingerprints web applications. */
@PluginInfo(
    type = PluginType.SERVICE_FINGERPRINT,
    name = "AiWebServiceFingerprinter",
    version = "0.1",
    description = "Identifies web application and versions via AI.",
    author = "Tsunami Team (tsunami-dev@google.com)",
    bootstrapModule = AiWebServiceFingerprinterBootstrapModule.class)
@ForWebService
public final class AiWebServiceFingerprinter implements ServiceFingerprinter {

  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private static final String PROMPT =
      "You are a web application fingerprinter. You will be given a web application's homepage. You"
          + " need to identify the web application and its version. If the web application is a"
          + " custom or generic web application like a hello world service, put 'Generic Web"
          + " Service' as its application name. Then you need to check the"
          + " following security attributes about the application:\n"
          +
"""
1. Is there a login form present on the homepage? Don't assume anything, only return true if the login form html is present on the homepage.
2. Is authentication required to access the application? Don't assume anything, if the application doesn't explicitly state that authentication is required, then assume that authentication is not required.
3. Is there an admin page? An admin page is a page that allows the user to perform admin/privileged actions such as updating user accounts, deleting data, or configuring settings.
4. Does the application have known default username and password? If yes, then return the default username and password. If no, set the default username and password to 'value_unknown'.
5. Does the application allow code execution, in other words, allows the user to run arbitrary code or system commands.
6. Does the application allow file system access, in other words, allows the user to read or write files in the file system.
7. Is the application a data storage solution, in other words, is the application a database or a file server.
8. Does the application allow workflow execution, in other words, allows the user to execute workflows or scripts.
9. Does this look like an exposed web admin UI/Panel/Dashboard that allows for admin actions or code executions without authentication?
10. Is there a user sign-up form present on the homepage? Don't assume anything, only return true if the user sign-up form html is present on the homepage.
"""
          + "\nPlease provide the rationale for your decision. If you failed to identify the web"
          + " application, please provide a rationale and set the"
          + " rest of the fields to either false for boolean or 'value_unknown' for string."
          + "\nThe content of the web page is as follows: %s";

  private final AiInferenceService aiInferenceService;
  private final HttpClient httpClient;

  @Inject
  AiWebServiceFingerprinter(AiInferenceService aiInferenceService, HttpClient httpClient) {
    this.aiInferenceService = checkNotNull(aiInferenceService);
    this.httpClient = checkNotNull(httpClient);
  }
  
  @Override
  public FingerprintingReport fingerprint(TargetInfo targetInfo, NetworkService networkService) {
    logger.atInfo().log("AiWebServiceFingerprinter networkService: %s", networkService.toString());
    logger.atInfo().log("AiWebServiceFingerprinter targetInfo: %s", targetInfo.toString());

    String url =
        String.format(
            "http://%s:%s",
            networkService.getNetworkEndpoint().getIpAddress().getAddress(),
            networkService.getNetworkEndpoint().getPort().getPortNumber());
    try {
      HttpResponse response = httpClient.send(get(url).withEmptyHeaders().build());

      logger.atInfo().log(
          "AiWebServiceFingerprinter http response headers and body : %s",
          response.headers().toString() + response.bodyString().get());

      NetworkService enrichedNetworkService =
          aiInferenceService.identifyWebApplication(
              networkService,
              String.format(PROMPT, response.headers().toString() + response.bodyString().get()));

      return FingerprintingReport.newBuilder().addNetworkServices(enrichedNetworkService).build();
    } catch (Exception e) {
      logger.atWarning().withCause(e).log("Unable to fetch web application: %s", url);
    }
    return FingerprintingReport.getDefaultInstance();
  }
}
