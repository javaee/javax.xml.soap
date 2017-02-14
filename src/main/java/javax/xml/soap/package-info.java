/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005-2017 Oracle and/or its affiliates. All rights reserved.
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

/**
 * Provides the API for creating and building SOAP messages. This package
 * is defined in the <i>SOAP with Attachments API for Java<sup><font style="font-size:x-small">TM</font></sup> 
 * (SAAJ) 1.4</i> specification.
 * 
 * <p> The API in the <code>javax.xml.soap</code> package allows you to do the following:
 *  
 * <ul>
 *     <li>create a point-to-point connection to a specified endpoint   
 *     <li>create a SOAP message   
 *     <li>create an XML fragment   
 *     <li>add content to the header of a SOAP message   
 *     <li>add content to the body of a SOAP message   
 *     <li>create attachment parts and add content to them   
 *     <li>access/add/modify parts of a SOAP message   
 *     <li>create/add/modify SOAP fault information   
 *     <li>extract content from a SOAP message   
 *     <li>send a SOAP request-response message   
 * </ul>
 *
 * <p>
 * In addition the APIs in the <code>javax.xml.soap</code> package extend
 * their  counterparts in the <code>org.w3c.dom</code> package. This means that
 * the  <code>SOAPPart</code> of a <code>SOAPMessage</code> is also a DOM Level
 * 2 <code>Document</code>, and can be manipulated as such by applications,
 * tools and libraries that use DOM (see http://www.w3.org/DOM/ for more information).
 * It is important to note that, while it is possible to use DOM APIs to add
 * ordinary DOM nodes to a SAAJ tree, the SAAJ APIs are still required to return
 * SAAJ types when examining or manipulating the tree. In order to accomplish
 * this the SAAJ APIs (specifically {@link javax.xml.soap.SOAPElement#getChildElements()})
 * are allowed to silently replace objects that are incorrectly typed relative
 * to SAAJ requirements with equivalent objects of the required type. These
 * replacements must never cause the logical structure of the tree to change,
 * so from the perspective of the DOM APIs the tree will remain unchanged. However,
 * the physical composition of the tree will have changed so that references
 * to the nodes that were replaced will refer to nodes that are no longer a
 * part of the tree. The SAAJ APIs are not allowed to make these replacements
 * if they are not required so the replacement objects will never subsequently
 * be silently replaced by future calls to the SAAJ API.
 * <p>
 * What this means in practical terms is that an application that starts to use 
 * SAAJ APIs on a tree after manipulating it using DOM APIs must assume that the 
 * tree has been translated into an all SAAJ tree and that any references to objects 
 * within the tree that were obtained using DOM APIs are no longer valid. Switching
 * from SAAJ APIs to DOM APIs is not allowed to cause invalid references and
 * neither is using SAAJ APIs exclusively. It is only switching from using DOM
 * APIs on a particular SAAJ tree to using SAAJ APIs that causes the risk of
 * invalid references.
 *
 * <h3>Discovery of SAAJ implementation</h3>
 * <p>
 * There are several factories defined in the SAAJ API to discover and load specific implementation:
 *
 * <ul>
 *     <li>{@link javax.xml.soap.SOAPFactory}
 *     <li>{@link javax.xml.soap.MessageFactory}
 *     <li>{@link javax.xml.soap.SOAPConnectionFactory}
 *     <li>{@link javax.xml.soap.SAAJMetaFactory}
 * </ul>
 *
 * First three define {@code newInstance()} method which uses a common lookup procedure to determine
 * the implementation class:
 *
 * <ul>
 *  <li>Checks if a system property with the same name as the factory class is set (e.g.
 *  {@code javax.xml.soap.SOAPFactory}). If such property exists then its value is assumed to be the fully qualified
 *  name of the implementation class. This phase of the look up enables per-JVM override of the SAAJ implementation.
 *  <li>Use the configuration file "jaxm.properties". The file is in standard
 *  {@link java.util.Properties} format and typically located in the
 *  {@code conf} directory of the Java installation. It contains the fully qualified
 *  name of the implementation class with the key being the system property
 *  defined above.
 *  <li> Use the service-provider loading facilities, defined by the {@link java.util.ServiceLoader} class,
 *  to attempt to locate and load an implementation of the service using the {@linkplain
 *  java.util.ServiceLoader#load(java.lang.Class) default loading mechanism}.
 *  <li> Finally, if all the steps above fail, {@link javax.xml.soap.SAAJMetaFactory} instance is used
 *  to locate specific implementation (for {@link javax.xml.soap.MessageFactory} and {@link javax.xml.soap.SOAPFactory})
 *  or platform default implementation is used ({@link javax.xml.soap.SOAPConnectionFactory}).
 *  Whenever {@link javax.xml.soap.SAAJMetaFactory} is used, its lookup procedure to get actual instance is performed.
 * </ul>
 */
package javax.xml.soap;