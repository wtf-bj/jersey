/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.internal.inject;

import javax.ws.rs.QueryParam;

import javax.inject.Provider;
import javax.inject.Singleton;

import org.glassfish.jersey.internal.inject.ExtractorException;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ParamException;
import org.glassfish.jersey.server.model.Parameter;

/**
 * Value supplier provider supporting the {@link QueryParam &#64;QueryParam} injection annotation.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@Singleton
final class QueryParamValueSupplierProvider extends AbstractValueSupplierProvider {

    /**
     * Injection constructor.
     *
     * @param mpep            multivalued map parameter extractor provider.
     * @param requestProvider request provider.
     */
    public QueryParamValueSupplierProvider(Provider<MultivaluedParameterExtractorProvider> mpep,
            Provider<ContainerRequest> requestProvider) {
        super(mpep, requestProvider, Parameter.Source.QUERY);
    }

    @Override
    public AbstractRequestDerivedValueSupplier<?> createValueSupplier(
            Parameter parameter,
            Provider<ContainerRequest> requestProvider) {

        String parameterName = parameter.getSourceName();
        if (parameterName == null || parameterName.length() == 0) {
            // Invalid query parameter name
            return null;
        }

        MultivaluedParameterExtractor e = get(parameter);
        if (e == null) {
            return null;
        }

        return new QueryParamValueSupplier(e, !parameter.isEncoded(), requestProvider);
    }

    private static final class QueryParamValueSupplier extends AbstractRequestDerivedValueSupplier<Object> {

        private final MultivaluedParameterExtractor<?> extractor;
        private final boolean decode;

        QueryParamValueSupplier(
                MultivaluedParameterExtractor<?> extractor,
                boolean decode,
                Provider<ContainerRequest> requestProvider) {
            super(requestProvider);
            this.extractor = extractor;
            this.decode = decode;
        }

        @Override
        public Object get() {
            try {
                return extractor.extract(getRequest().getUriInfo().getQueryParameters(decode));
            } catch (ExtractorException e) {
                throw new ParamException.QueryParamException(e.getCause(),
                        extractor.getName(), extractor.getDefaultValueString());
            }
        }
    }
}
