/*
 * Copyright 2013 JBoss Inc
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
package org.overlord.rtgov.ui.client.model;

import java.io.Serializable;
import java.util.List;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Models the list of application names.
 *
 */
@Portable
public class ApplicationListBean implements Serializable {

    private static final long serialVersionUID = ApplicationListBean.class.hashCode();

    private List<QName> appNames;

    /**
     * Constructor.
     */
    public ApplicationListBean() {
    }

    /**
     * @return the application names
     */
    public List<QName> getApplicationNames() {
        return appNames;
    }

    /**
     * @param appNames the application names to set
     */
    public void setApplicationNames(List<QName> appNames) {
        this.appNames = appNames;
    }

}
