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

import java.util.Iterator;

import javax.xml.namespace.QName;

/**
 * A container for {@code DetailEntry} objects. {@code DetailEntry}
 * objects give detailed error information that is application-specific and
 * related to the {@code SOAPBody} object that contains it.
 *<P>
 * A {@code Detail} object, which is part of a {@code SOAPFault}
 * object, can be retrieved using the method {@code SOAPFault.getDetail}.
 * The {@code Detail} interface provides two methods. One creates a new
 * {@code DetailEntry} object and also automatically adds it to
 * the {@code Detail} object. The second method gets a list of the
 * {@code DetailEntry} objects contained in a {@code Detail}
 * object.
 * <P>
 * The following code fragment, in which <i>sf</i> is a {@code SOAPFault}
 * object, gets its {@code Detail} object (<i>d</i>), adds a new
 * {@code DetailEntry} object to <i>d</i>, and then gets a list of all the
 * {@code DetailEntry} objects in <i>d</i>. The code also creates a
 * {@code Name} object to pass to the method {@code addDetailEntry}.
 * The variable <i>se</i>, used to create the {@code Name} object,
 * is a {@code SOAPEnvelope} object.
 * <pre>{@code
 *    Detail d = sf.getDetail();
 *    Name name = se.createName("GetLastTradePrice", "WOMBAT",
 *                                "http://www.wombat.org/trader");
 *    d.addDetailEntry(name);
 *    Iterator it = d.getDetailEntries();
 * }</pre>
 *
 * @since 1.6
 */
public interface Detail extends SOAPFaultElement {

    /**
     * Creates a new {@code DetailEntry} object with the given
     * name and adds it to this {@code Detail} object.
     *
     * @param name a {@code Name} object identifying the
     *         new {@code DetailEntry} object
     *
     * @return the new {@code DetailEntry} object that was
     *         created
     *
     * @exception SOAPException thrown when there is a problem in adding a
     * DetailEntry object to this Detail object.
     *
     * @see Detail#addDetailEntry(QName qname)
     */
    public DetailEntry addDetailEntry(Name name) throws SOAPException;

    /**
     * Creates a new {@code DetailEntry} object with the given
     * QName and adds it to this {@code Detail} object. This method
     * is the preferred over the one using Name.
     *
     * @param qname a {@code QName} object identifying the
     *         new {@code DetailEntry} object
     *
     * @return the new {@code DetailEntry} object that was
     *         created
     *
     * @exception SOAPException thrown when there is a problem in adding a
     * DetailEntry object to this Detail object.
     *
     * @see Detail#addDetailEntry(Name name)
     * @since 1.6, SAAJ 1.3
     */
    public DetailEntry addDetailEntry(QName qname) throws SOAPException;

    /**
     * Gets an Iterator over all of the {@code DetailEntry}s in this {@code Detail} object.
     *
     * @return an {@code Iterator} object over the {@code DetailEntry}
     *             objects in this {@code Detail} object
     */
    public Iterator getDetailEntries();
}
