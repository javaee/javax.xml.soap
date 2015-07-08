/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2015 Oracle and/or its affiliates. All rights reserved.
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

import java.io.*;
import java.util.Properties;


class FactoryFinder {

    /**
     * Creates an instance of the specified class using the specified
     * {@code ClassLoader} object.
     *
     * @exception SOAPException if the given class could not be found
     *            or could not be instantiated
     */
    private static Object newInstance(String className,
                                      ClassLoader classLoader)
            throws SOAPException
    {
        try {
            Class spiClass = safeLoadClass(className, classLoader);
            return spiClass.newInstance();

        } catch (ClassNotFoundException x) {
            throw new SOAPException("Provider " + className + " not found", x);
        } catch (Exception x) {
            throw new SOAPException("Provider " + className + " could not be instantiated: " + x, x);
        }
    }

    /**
     * Finds the implementation {@code Class} object for the given
     * factory name, or null if that fails.
     * <P>
     * This method is package private so that this code can be shared.
     *
     * @return the {@code Class} object of the specified message factory;
     *         or {@code null}
     *
     * @param factoryId             the name of the factory to find, which is
     *                              a system property
     * @exception SOAPException if there is a SOAP error
     */
    static Object find(String factoryId)
            throws SOAPException
    {
        return find(factoryId, null, false);
    }

    /**
     * Finds the implementation {@code Class} object for the given
     * factory name, or if that fails, finds the {@code Class} object
     * for the given fallback class name. The arguments supplied must be
     * used in order. If using the first argument is successful, the second
     * one will not be used.
     * <P>
     * This method is package private so that this code can be shared.
     *
     * @return the {@code Class} object of the specified message factory;
     *         may be {@code null}
     *
     * @param factoryId             the name of the factory to find, which is
     *                              a system property
     * @param fallbackClassName     the implementation class name, which is
     *                              to be used only if nothing else
     *                              is found; {@code null} to indicate that
     *                              there is no fallback class name
     * @exception SOAPException if there is a SOAP error
     */
    static Object find(String factoryId, String fallbackClassName)
            throws SOAPException
    {
        return find(factoryId, fallbackClassName, true);
    }

    /**
     * Finds the implementation {@code Class} object for the given
     * factory name, or if that fails, finds the {@code Class} object
     * for the given default class name, but only if {@code tryFallback}
     * is {@code true}.  The arguments supplied must be used in order
     * If using the first argument is successful, the second one will not
     * be used.  Note the default class name may be needed even if fallback
     * is not to be attempted, so certain error conditions can be handled.
     * <P>
     * This method is package private so that this code can be shared.
     *
     * @return the {@code Class} object of the specified message factory;
     *         may not be {@code null}
     *
     * @param factoryId             the name of the factory to find, which is
     *                              a system property
     * @param defaultClassName      the implementation class name, which is
     *                              to be used only if nothing else
     *                              is found; {@code null} to indicate
     *                              that there is no default class name
     * @param tryFallback           whether to try the default class as a
     *                              fallback
     * @exception SOAPException if there is a SOAP error
     */
    static Object find(String factoryId, String defaultClassName,
                       boolean tryFallback) throws SOAPException {
        ClassLoader classLoader;
        try {
            classLoader = Thread.currentThread().getContextClassLoader();
        } catch (Exception x) {
            throw new SOAPException(x.toString(), x);
        }

        // Use the system property first
        try {
            String systemProp =
                    System.getProperty( factoryId );
            if( systemProp!=null) {
                return newInstance(systemProp, classLoader);
            }
        } catch (SecurityException se) {
        }

        // try to read from $java.home/lib/jaxm.properties
        try {
            String javah=System.getProperty( "java.home" );
            String configFile = javah + File.separator +
                    "lib" + File.separator + "jaxm.properties";
            File f=new File( configFile );
            if( f.exists()) {
                Properties props=new Properties();
                props.load( new FileInputStream(f));
                String factoryClassName = props.getProperty(factoryId);
                return newInstance(factoryClassName, classLoader);
            }
        } catch(Exception ex ) {
        }

        String serviceId = "META-INF/services/" + factoryId;
        // try to find services in CLASSPATH
        try {
            InputStream is=null;
            if (classLoader == null) {
                is=ClassLoader.getSystemResourceAsStream(serviceId);
            } else {
                is=classLoader.getResourceAsStream(serviceId);
            }

            if( is!=null ) {
                BufferedReader rd =
                        new BufferedReader(new InputStreamReader(is, "UTF-8"));

                String factoryClassName = rd.readLine();
                rd.close();

                if (factoryClassName != null &&
                        ! "".equals(factoryClassName)) {
                    return newInstance(factoryClassName, classLoader);
                }
            }
        } catch( Exception ex ) {
        }

        // If not found and fallback should not be tried, return a null result.
        if (!tryFallback)
            return null;

        // We didn't find the class through the usual means so try the default
        // (built in) factory if specified.
        if (defaultClassName == null) {
            throw new SOAPException(
                    "Provider for " + factoryId + " cannot be found", null);
        }
        return newInstance(defaultClassName, classLoader);
    }

    /**
     * Loads the class, provided that the calling thread has an access to the
     * class being loaded. If this is the specified default factory class and it
     * is restricted by package.access we get a SecurityException and can do a
     * Class.forName() on it so it will be loaded by the bootstrap class loader.
     */
    private static Class safeLoadClass(String className,
                                       ClassLoader classLoader)
            throws ClassNotFoundException {
        try {
            // make sure that the current thread has an access to the package of the given name.
            SecurityManager s = System.getSecurityManager();
            if (s != null) {
                int i = className.lastIndexOf('.');
                if (i != -1) {
                    s.checkPackageAccess(className.substring(0, i));
                }
            }

            if (classLoader == null)
                return Class.forName(className);
            else
                return classLoader.loadClass(className);
        } catch (SecurityException se) {
            // (only) default implementation can be loaded
            // using bootstrap class loader:
            if (isDefaultImplementation(className))
                return Class.forName(className);

            throw se;
        }
    }

    private static boolean isDefaultImplementation(String className) {
        return MessageFactory.DEFAULT_MESSAGE_FACTORY.equals(className) ||
                SOAPFactory.DEFAULT_SOAP_FACTORY.equals(className) ||
                SOAPConnectionFactory.DEFAULT_SOAP_CONNECTION_FACTORY.equals(className) ||
                SAAJMetaFactory.DEFAULT_META_FACTORY_CLASS.equals(className);
    }
}
