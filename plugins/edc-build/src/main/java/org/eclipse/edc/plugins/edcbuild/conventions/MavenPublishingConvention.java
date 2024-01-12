/*
 *  Copyright (c) 2022 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *
 */

package org.eclipse.edc.plugins.edcbuild.conventions;

import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.publish.PublishingExtension;

import static org.eclipse.edc.plugins.edcbuild.conventions.ConventionFunctions.requireExtension;

/**
 * Configures the Maven repos for publishing depending on the project's version
 */
class MavenPublishingConvention implements EdcConvention {

    @Override
    public void apply(Project target) {
        var pubExt = requireExtension(target, PublishingExtension.class);

        var repoName = System.getProperty("publishRepoName");
        var repoUrl = System.getProperty("publishRepoUrl");
        var userName = System.getProperty("publishUserName");
        var userPassword = System.getProperty("publishUserPassword");

        RepositoryHandler repositories = pubExt.getRepositories();
        repositories.add(repositories.maven(repo -> {
            repo.setName(repoName);
            repo.setUrl(repoUrl);
            repo.credentials(cred -> {
                cred.setUsername(userName);
                cred.setPassword(userPassword);
            });
        }));
    }

}
