/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

/**
* The access point for the implementation classes of the factories defined in the
* SAAJ API. The {@code newInstance} methods defined on factories {@link SOAPFactory} and
* {@link MessageFactory} in SAAJ 1.3 defer to instances of this class to do the actual object creation.
* The implementations of {@code newInstance()} methods (in {@link SOAPFactory} and {@link MessageFactory})
* that existed in SAAJ 1.2 have been updated to also delegate to the SAAJMetaFactory when the SAAJ 1.2
* defined lookup fails to locate the Factory implementation class name.
*
* <p>
* SAAJMetaFactory is a service provider interface and it uses similar lookup mechanism as other SAAJ factories
* to get actual instance:
*
* <ul>
*  <li>If a system property with name {@code javax.xml.soap.SAAJMetaFactory} exists then its value is assumed
*  to be the fully qualified name of the implementation class. This phase of the look up enables per-JVM
*  override of the SAAJ implementation.
*  <li>If a system property with name {@code javax.xml.soap.MetaFactory} exists then its value is assumed
*  to be the fully qualified name of the implementation class. This property, defined by previous specifications
 * (up to 1.3), is still supported, but it is strongly recommended to migrate to new property
 * {@code javax.xml.soap.SAAJMetaFactory}.
*  <li>Use the configuration file "jaxm.properties". The file is in standard {@link java.util.Properties} format
*  and typically located in the {@code conf} directory of the Java installation. It contains the fully qualified
*  name of the implementation class with key {@code javax.xml.soap.SAAJMetaFactory}. If no such property is defined,
 * again, property with key {@code javax.xml.soap.MetaFactory} is used. It is strongly recommended to migrate to
 * new property {@code javax.xml.soap.SAAJMetaFactory}.
*  <li> Use the service-provider loading facilities, defined by the {@link java.util.ServiceLoader} class,
*  to attempt to locate and load an implementation of the service using the {@linkplain
*  java.util.ServiceLoader#load(java.lang.Class) default loading mechanism}.
*  <li> Finally, if all the steps above fail, platform default implementation is used.
* </ul>
*
* <p>
* There are no public methods on this
* class.
*
* @author SAAJ RI Development Team
* @since 1.6, SAAJ 1.3
*/
public abstract class SAAJMetaFactory {

    private static final String META_FACTORY_DEPRECATED_CLASS_PROPERTY =
            "javax.xml.soap.MetaFactory";

    private static final String DEFAULT_META_FACTORY_CLASS =
            "com.sun.xml.internal.messaging.saaj.soap.SAAJMetaFactoryImpl";

    /**
     * Creates a new instance of a concrete {@code SAAJMetaFactory} object.
     * The SAAJMetaFactory is an SPI, it pulls the creation of the other factories together into a
     * single place. Changing out the SAAJMetaFactory has the effect of changing out the entire SAAJ
     * implementation. Service providers provide the name of their {@code SAAJMetaFactory}
     * implementation.
     *
     * This method uses the lookup procedure specified in {@link javax.xml.soap} to locate and load the
     * {@link javax.xml.soap.SAAJMetaFactory} class.
     *
     * @return a concrete {@code SAAJMetaFactory} object
     * @exception SOAPException if there is an error in creating the {@code SAAJMetaFactory}
     */
    static SAAJMetaFactory getInstance() throws SOAPException {
            try {
                return FactoryFinder.find(
                        SAAJMetaFactory.class,
                        DEFAULT_META_FACTORY_CLASS,
                        true,
                        META_FACTORY_DEPRECATED_CLASS_PROPERTY);

            } catch (Exception e) {
                throw new SOAPException(
                    "Unable to create SAAJ meta-factory: " + e.getMessage());
            }
    }

    protected SAAJMetaFactory() { }

     /**
      * Creates a {@code MessageFactory} object for
      * the given {@code String} protocol.
      *
      * @param protocol a {@code String} indicating the protocol
      * @return a {@link MessageFactory}, not null
      * @exception SOAPException if there is an error in creating the
      *            MessageFactory
      * @see SOAPConstants#SOAP_1_1_PROTOCOL
      * @see SOAPConstants#SOAP_1_2_PROTOCOL
      * @see SOAPConstants#DYNAMIC_SOAP_PROTOCOL
      */
    protected abstract MessageFactory newMessageFactory(String protocol)
        throws SOAPException;

     /**
      * Creates a {@code SOAPFactory} object for
      * the given {@code String} protocol.
      *
      * @param protocol a {@code String} indicating the protocol
      * @return a {@link SOAPFactory}, not null
      * @exception SOAPException if there is an error in creating the
      *            SOAPFactory
      * @see SOAPConstants#SOAP_1_1_PROTOCOL
      * @see SOAPConstants#SOAP_1_2_PROTOCOL
      * @see SOAPConstants#DYNAMIC_SOAP_PROTOCOL
      */
    protected abstract SOAPFactory newSOAPFactory(String protocol)
        throws SOAPException;
}
