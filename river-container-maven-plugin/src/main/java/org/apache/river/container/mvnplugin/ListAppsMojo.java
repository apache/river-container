package org.apache.river.container.mvnplugin;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Goal which touches a timestamp file.
 *
 * 
 */
@Mojo( name = "listApps", requiresDirectInvocation=true, requiresProject=false)
public class ListAppsMojo
    extends AbstractMojo
{
    /**
     * Location of the file.
     */
    @Parameter( property = "river.container.home", required = true )
    private File containerHome;

    /**
     * Uses the client admin listApps script to list the applications that are
     * in the currently running container.
     * @throws MojoExecutionException 
     */
    @Override
    public void execute()
        throws MojoExecutionException
    {
        System.out.println("Container home is " + containerHome.getAbsolutePath());
    }
}
