/*
 * Copyright (c) 2013 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.scenicview.utils.attach;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import static org.scenicview.utils.attach.AttachHandlerFactory.doBasicJdkSearch;

/**
 *
 */
public class MacAttachHandler implements AttachHandler {

    @Override public void getOrderedJDKPaths(List<JDKToolsJarPair> jdkPaths) {
        doBasicJdkSearch(jdkPaths);
        
        // go down mac special path
        getToolsClassPathOnMAC(jdkPaths);
    }
    
    private File getToolsClassPathOnMAC(List<JDKToolsJarPair> jdkPaths) {
        Process process = null;
        try {
            Runtime runtime = Runtime.getRuntime();
            process = runtime.exec("/usr/libexec/java_home -V");
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
            
        try(
            InputStream inputStream = process.getErrorStream();
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader)
        ) {
            if (bufferedReader.ready()) {
                bufferedReader.readLine();
            }
            while (bufferedReader.ready()) {
                String versionString = bufferedReader.readLine();
                versionString = versionString.trim();
                
                String version;
                String name;
                String path;
                String[] splitted = versionString.split(" ");
                if (splitted.length == 3) {
                    version = splitted[0];
                    name = splitted[1];
                    path = splitted[2];
                    
                    if (version.endsWith(":")) {
                        version = version.substring(0, version.length() - 1);
                    }
//                    versions.add(new JavaVersion(splitted[0], splitted[1], splitted[2]));
                    
                    final File jdkHome = new File(path);
                    final File toolsFile = new File(jdkHome, "Contents/Home/lib/tools.jar");
                    if (toolsFile.exists()) {
//                        debug("Tools file found on Mac OS in:" + toolsFile.getAbsolutePath());
                        jdkPaths.add(new JDKToolsJarPair(jdkHome, toolsFile));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    @Override public File resolveToolsJarPath(JDKToolsJarPair jdkPath) {
        // TODO
        throw new RuntimeException("Not yet implemented");
    }
}