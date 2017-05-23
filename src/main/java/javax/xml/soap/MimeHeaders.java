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

import java.util.Iterator;
import java.util.Vector;

/**
 * A container for {@code MimeHeader} objects, which represent
 * the MIME headers present in a MIME part of a message.
 *
 * <p>This class is used primarily when an application wants to
 * retrieve specific attachments based on certain MIME headers and
 * values. This class will most likely be used by implementations of
 * {@code AttachmentPart} and other MIME dependent parts of the SAAJ
 * API.
 * @see SOAPMessage#getAttachments
 * @see AttachmentPart
 * @since 1.6
 */
public class MimeHeaders {
    private Vector<MimeHeader> headers;

   /**
    * Constructs a default {@code MimeHeaders} object initialized with
    * an empty {@code Vector} object.
    */
    public MimeHeaders() {
        headers = new Vector<>();
    }

    /**
     * Returns all of the values for the specified header as an array of
     * {@code String} objects.
     *
     * @param   name the name of the header for which values will be returned
     * @return a {@code String} array with all of the values for the
     *         specified header
     * @see #setHeader
     */
    public String[] getHeader(String name) {
        Vector<String> values = new Vector<>();

        for(int i = 0; i < headers.size(); i++) {
            MimeHeader hdr = headers.elementAt(i);
            if (hdr.getName().equalsIgnoreCase(name)
                && hdr.getValue() != null)
                values.addElement(hdr.getValue());
        }

        if (values.size() == 0)
            return null;

        String r[] = new String[values.size()];
        values.copyInto(r);
        return r;
    }

    /**
     * Replaces the current value of the first header entry whose name matches
     * the given name with the given value, adding a new header if no existing header
     * name matches. This method also removes all matching headers after the first one.
     * <P>
     * Note that RFC822 headers can contain only US-ASCII characters.
     *
     * @param   name a {@code String} with the name of the header for
     *          which to search
     * @param   value a {@code String} with the value that will replace the
     *          current value of the specified header
     *
     * @exception IllegalArgumentException if there was a problem in the
     * mime header name or the value being set
     * @see #getHeader
     */
    public void setHeader(String name, String value)
    {
        boolean found = false;

        if ((name == null) || name.equals(""))
            throw new IllegalArgumentException("Illegal MimeHeader name");

        for(int i = 0; i < headers.size(); i++) {
            MimeHeader hdr = headers.elementAt(i);
            if (hdr.getName().equalsIgnoreCase(name)) {
                if (!found) {
                    headers.setElementAt(new MimeHeader(hdr.getName(),
                                                        value), i);
                    found = true;
                }
                else
                    headers.removeElementAt(i--);
            }
        }

        if (!found)
            addHeader(name, value);
    }

    /**
     * Adds a {@code MimeHeader} object with the specified name and value
     * to this {@code MimeHeaders} object's list of headers.
     * <P>
     * Note that RFC822 headers can contain only US-ASCII characters.
     *
     * @param   name a {@code String} with the name of the header to
     *          be added
     * @param   value a {@code String} with the value of the header to
     *          be added
     *
     * @exception IllegalArgumentException if there was a problem in the
     * mime header name or value being added
     */
    public void addHeader(String name, String value)
    {
        if ((name == null) || name.equals(""))
            throw new IllegalArgumentException("Illegal MimeHeader name");

        int pos = headers.size();

        for(int i = pos - 1 ; i >= 0; i--) {
            MimeHeader hdr = headers.elementAt(i);
            if (hdr.getName().equalsIgnoreCase(name)) {
                headers.insertElementAt(new MimeHeader(name, value),
                                        i+1);
                return;
            }
        }
        headers.addElement(new MimeHeader(name, value));
    }

    /**
     * Remove all {@code MimeHeader} objects whose name matches the
     * given name.
     *
     * @param   name a {@code String} with the name of the header for
     *          which to search
     */
    public void removeHeader(String name) {
        for(int i = 0; i < headers.size(); i++) {
            MimeHeader hdr = headers.elementAt(i);
            if (hdr.getName().equalsIgnoreCase(name))
                headers.removeElementAt(i--);
        }
    }

    /**
     * Removes all the header entries from this {@code MimeHeaders} object.
     */
    public void removeAllHeaders() {
        headers.removeAllElements();
    }


    /**
     * Returns all the {@code MimeHeader}s in this {@code MimeHeaders} object.
     *
     * @return  an {@code Iterator} object over this {@code MimeHeaders}
     *          object's list of {@code MimeHeader} objects
     */
    public Iterator<MimeHeader> getAllHeaders() {
        return headers.iterator();
    }

    static class MatchingIterator implements Iterator<MimeHeader> {
        private final boolean match;
        private final Iterator<MimeHeader> iterator;
        private final String[] names;
        private MimeHeader nextHeader;

        MatchingIterator(String[] names, boolean match, Iterator<MimeHeader> i) {
            this.match = match;
            this.names = names;
            this.iterator = i;
        }

        private MimeHeader nextMatch() {
        next:
            while (iterator.hasNext()) {
                MimeHeader hdr = iterator.next();

                if (names == null)
                    return match ? null : hdr;

                for(int i = 0; i < names.length; i++)
                    if (hdr.getName().equalsIgnoreCase(names[i]))
                        if (match)
                            return hdr;
                        else
                            continue next;
                if (!match)
                    return hdr;
            }
            return null;
        }


        @Override
        public boolean hasNext() {
            if (nextHeader == null)
                nextHeader = nextMatch();
            return nextHeader != null;
        }

        @Override
        public MimeHeader next() {
            // hasNext should've prefetched the header for us,
            // return it.
            if (nextHeader != null) {
                MimeHeader ret = nextHeader;
                nextHeader = null;
                return ret;
            }
            if (hasNext())
                return nextHeader;
            return null;
        }

        @Override
        public void remove() {
            iterator.remove();
        }
    }


    /**
     * Returns all the {@code MimeHeader} objects whose name matches
     * a name in the given array of names.
     *
     * @param names an array of {@code String} objects with the names
     *         for which to search
     * @return  an {@code Iterator} object over the {@code MimeHeader}
     *          objects whose name matches one of the names in the given list
     */
    public Iterator<MimeHeader> getMatchingHeaders(String[] names) {
        return new MatchingIterator(names, true, headers.iterator());
    }

    /**
     * Returns all of the {@code MimeHeader} objects whose name does not
     * match a name in the given array of names.
     *
     * @param names an array of {@code String} objects with the names
     *         for which to search
     * @return  an {@code Iterator} object over the {@code MimeHeader}
     *          objects whose name does not match one of the names in the given list
     */
    public Iterator<MimeHeader> getNonMatchingHeaders(String[] names) {
        return new MatchingIterator(names, false, headers.iterator());
    }
}
