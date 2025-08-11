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
package com.google.tsunami.plugins.fingerprinters.ai;

import static com.google.common.base.Preconditions.checkNotNull;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParameterException;
import com.google.tsunami.common.cli.CliOption;
import javax.inject.Inject;

/** Configuration options for {@link AiWebServiceFingerprinter}. */
public final class AiWebServiceFingerprinterConfigs {
  final AiWebServiceFingerprinterCliOptions cliOptions;

  @Inject
  AiWebServiceFingerprinterConfigs(AiWebServiceFingerprinterCliOptions cliOptions) {
    this.cliOptions = checkNotNull(cliOptions);
  }

  public String getGcpProjectId() {
    return cliOptions.gcpProjectId;
  }

  public String getGcpLocation() {
    return cliOptions.gcpLocation;
  }

  /** CLI options for {@link WebServiceFingerprinter}. */
  @Parameters(separators = "=")
  public static final class AiWebServiceFingerprinterCliOptions implements CliOption {

    @Parameter(
        names = "--gcp-project-id",
        description =
            "The GCP project ID to use for AI inference, VertexAI API needs to be enabled there and"
                + " a service account with 'Verte AI User' permission needs to be present in the"
                + " runtime environment.")
    String gcpProjectId;

    @Parameter(
        names = "--gcp-location",
        description = "The GCP location to use for AI inference, for example us-central1.")
    String gcpLocation;

    @Override
    public void validate() {
      if (gcpProjectId == null) {
        throw new ParameterException(
            "--gcp-project-id is required for AI web service fingerprinter.");
      }
      if (gcpLocation == null) {
        throw new ParameterException(
            "--gcp-location is required for AI web service fingerprinter.");
      }
    }
  }
}
