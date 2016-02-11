/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package javax.xml.soap;


import junit.framework.AssertionFailedError;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Logger;

import static junit.framework.TestCase.assertTrue;

/*
 * test for JDK-8131334: SAAJ Plugability Layer: using java.util.ServiceLoader
 *
 * There are unsafe scenarios not to be run within the build (modifying jdk files).
 * To run those, following needs to be done:
 *   1. allow java to write into $JAVA_HOME/conf: mkdir $JAVA_HOME/conf; chmod a+rw $JAVA_HOME/conf
 *   2. use "runUnsafe" property: mvn clean test -DrunUnsafe=true
 */
@RunWith(Parameterized.class)
public class SAAJFactoryTest {

    static final Logger logger = Logger.getLogger(SAAJFactoryTest.class.getName());

    static final Boolean skipUnsafe = !Boolean.getBoolean("runUnsafe");

    // test configuration ------------------------------------------

    // test-classes directory (required for setup and for security settings)
    static final String classesDir = SAAJFactoryTest.class.getProtectionDomain().getCodeSource().getLocation().getFile();

    static {
        System.setProperty("classesDir", classesDir);
    }

    // configuration to be created by the test
    static Path providersDir = Paths.get(classesDir, "META-INF", "services");
    static Path providersFile = providersDir.resolve("javax.xml.soap.MessageFactory");

    // configuration to be created by the test
    static Path jdkDir = Paths.get(System.getProperty("java.home"), "conf");
    static Path jdkFile = jdkDir.resolve("jaxm.properties");

    // test instance -----------------------------------------------

    // scenario name - just for logging
    String scenario;

    // java policy file for testing w/security manager
    private String expectedFactory;
    private Class<?> expectedException;


    @Parameterized.Parameters
    public static Collection configurations() {
        return Arrays.asList(new Object[][]{
                // see SAAJFactoryTest constructor signature for paremeters meaning ...
                {null, "com.sun.xml.internal.messaging.saaj.soap.ver1_1.SOAPMessageFactory1_1Impl", null, "scenario2", null, null},
                {"saaj.factory.Valid", "saaj.factory.Valid", null, "scenario5", null, null},
                {"saaj.factory.NonExisting SAAJFactoryTest", null, javax.xml.soap.SOAPException.class, "scenario6", null, null},
                {"saaj.factory.Invalid SAAJFactoryTest", null, javax.xml.soap.SOAPException.class, "scenario7", null, null},
                {null, "saaj.factory.Valid", null, "scenario8", null, "saaj.factory.Valid"},
                {null, "saaj.factory.Valid", null, "scenario9", null, "saaj.factory.Valid"},
                {null, null, javax.xml.soap.SOAPException.class, "scenario10", null, "saaj.factory.NonExisting"},
                {null, null, javax.xml.soap.SOAPException.class, "scenario11", null, "saaj.factory.Invalid"},
                {null, "com.sun.xml.internal.messaging.saaj.soap.ver1_1.SOAPMessageFactory1_1Impl", null, "scenario12", null, null},
                {null, "saaj.factory.Valid", null, "scenario15", null, "saaj.factory.Valid"},

                {null, "saaj.factory.Valid", null, "unsafe-scenario1", "saaj.factory.Valid", null},
                {null, null, javax.xml.soap.SOAPException.class, "unsafe-scenario3", "non.existing.FactoryClass", null},
                {null, null, javax.xml.soap.SOAPException.class, "unsafe-scenario4", "saaj.factory.Invalid", null},
                {"saaj.factory.Valid3", "saaj.factory.Valid3", null, "unsafe-scenario13", "saaj.factory.Valid", "saaj.factory.Valid2"},
                {null, "saaj.factory.Valid", null, "unsafe-scenario14", "saaj.factory.Valid", "saaj.factory.Valid2"}
        });
    }

    public SAAJFactoryTest(String systemProperty,
                           String expectedFactory,
                           Class<?> expectedException,
                           String scenario,
                           String jdkConfClass,
                           String spiClass) {

        // ensure setup may be done ...
        System.setSecurityManager(null);

        if (systemProperty != null) {
            System.setProperty("javax.xml.soap.MessageFactory", systemProperty);
        } else {
            System.clearProperty("javax.xml.soap.MessageFactory");
        }

        this.scenario = scenario;
        this.expectedFactory = expectedFactory;
        this.expectedException = expectedException;

        if (skipUnsafe && scenario.startsWith("unsafe")) {
            log("Skipping unsafe scenario:" + scenario);
            return;
        }

        prepare(jdkConfClass, spiClass);
    }

    @Test
    public void testFactoryDiscovery() throws IOException {

        if (skipUnsafe && scenario.startsWith("unsafe")) {
            log("Skipping unsafe scenario:" + scenario);
            return;
        }

        logConfigurations();
        try {
            MessageFactory factory = factory();
            assertTrue("No factory found.", factory != null);
            String className = factory.getClass().getName();
            assertTrue("Incorrect factory: [" + className + "], Expected: [" + expectedFactory + "]",
                    expectedFactory.equals(className));

        } catch (Throwable throwable) {
            Class<?> throwableClass = throwable.getClass();
            boolean correctException = throwableClass.equals(expectedException);
            if (!correctException) {
                throwable.printStackTrace();
            }
            if (expectedException == null) {
                throw new AssertionFailedError("Unexpected exception:" + throwableClass);
            }
            assertTrue("Got unexpected exception: [" + throwableClass + "], expected: [" + expectedException + "]",
                    correctException);
        } finally {
            cleanResource(providersFile);
            cleanResource(providersDir);

            // unsafe; not running:
            cleanResource(jdkFile);
            System.setSecurityManager(null);
        }
    }

    @Test
    public void testFactoryDiscoverySM() throws IOException {
        enableSM();
        testFactoryDiscovery();
    }

    private void enableSM() {
        System.setSecurityManager(null);
        System.setProperty("java.security.policy", classesDir + "javax/xml/soap/test.policy");
        System.setSecurityManager(new SecurityManager());
    }

    protected MessageFactory factory() throws Throwable {
        try {
            MessageFactory factory = MessageFactory.newInstance();
            log("     TEST: factory class = [" + factory.getClass().getName() + "]\n");
            return factory;
        } catch (Throwable t) {
            log("     TEST: Throwable [" + t.getClass().getName() + "] thrown.\n");
            t.printStackTrace();
            throw t;
        }
    }

    private void cleanResource(Path resource) {
        try {
            if (Files.exists(resource)) {
                Files.deleteIfExists(resource);
            }
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
    }

    private void prepare(String propertiesClassName, String providerClassName) {

        try {
            log("providerClassName = " + providerClassName);
            log("propertiesClassName = " + propertiesClassName);

            setupFile(providersFile, providersDir, providerClassName);


            // unsafe; not running:
            if (propertiesClassName != null) {
                setupFile(jdkFile, jdkDir, "javax.xml.soap.MessageFactory=" + propertiesClassName);
            } else {
                cleanResource(jdkFile);
            }

            log(" SETUP OK.");

        } catch (IOException e) {
            log(" SETUP FAILED.");
            e.printStackTrace();
        }
    }

    private void logConfigurations() throws IOException {
        logFile(providersFile);
        logFile(jdkFile);
    }

    private void logFile(Path path) throws IOException {
        if (Files.exists(path)) {
            log("File [" + path + "] exists: [");
            log(new String(Files.readAllBytes(path)));
            log("]");
        }
    }

    private void setupFile(Path file, Path dir, String value) throws IOException {
        cleanResource(file);
        if (value != null) {
            log("writing configuration [" + value + "] into file [" + file.toAbsolutePath() + "]");
            Files.createDirectories(dir);
            Files.write(
                    file,
                    value.getBytes(),
                    StandardOpenOption.CREATE);
        }
    }

    private void log(String msg) {
        logger.info("[" + scenario + "] " + msg);
    }

}

