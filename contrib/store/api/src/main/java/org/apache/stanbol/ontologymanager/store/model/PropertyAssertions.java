/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.4-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.05.13 at 09:50:16 AM EEST 
//

package org.apache.stanbol.ontologymanager.store.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for anonymous complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="PropertyAssertion" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element ref="{model.rest.persistence.iks.srdc.com.tr}PropertyMetaInformation"/>
 *                   &lt;choice maxOccurs="unbounded">
 *                     &lt;element ref="{model.rest.persistence.iks.srdc.com.tr}IndividualMetaInformation"/>
 *                     &lt;element name="Literal" type="{model.rest.persistence.iks.srdc.com.tr}non_empty_string"/>
 *                   &lt;/choice>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"propertyAssertion"})
@XmlRootElement(name = "PropertyAssertions")
public class PropertyAssertions {

    @XmlElement(name = "PropertyAssertion")
    protected List<PropertyAssertions.PropertyAssertion> propertyAssertion;

    /**
     * Gets the value of the propertyAssertion property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification
     * you make to the returned list will be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the propertyAssertion property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getPropertyAssertion().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list {@link PropertyAssertions.PropertyAssertion }
     * 
     * 
     */
    public List<PropertyAssertions.PropertyAssertion> getPropertyAssertion() {
        if (propertyAssertion == null) {
            propertyAssertion = new ArrayList<PropertyAssertions.PropertyAssertion>();
        }
        return this.propertyAssertion;
    }

    /**
     * <p>
     * Java class for anonymous complex type.
     * 
     * <p>
     * The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element ref="{model.rest.persistence.iks.srdc.com.tr}PropertyMetaInformation"/>
     *         &lt;choice maxOccurs="unbounded">
     *           &lt;element ref="{model.rest.persistence.iks.srdc.com.tr}IndividualMetaInformation"/>
     *           &lt;element name="Literal" type="{model.rest.persistence.iks.srdc.com.tr}non_empty_string"/>
     *         &lt;/choice>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {"propertyMetaInformation", "individualMetaInformationOrLiteral"})
    public static class PropertyAssertion {

        @XmlElement(name = "PropertyMetaInformation", required = true)
        protected PropertyMetaInformation propertyMetaInformation;
        @XmlElements({@XmlElement(name = "Literal", type = String.class),
                      @XmlElement(name = "IndividualMetaInformation", type = IndividualMetaInformation.class)})
        protected List<Object> individualMetaInformationOrLiteral;

        /**
         * Gets the value of the propertyMetaInformation property.
         * 
         * @return possible object is {@link PropertyMetaInformation }
         * 
         */
        public PropertyMetaInformation getPropertyMetaInformation() {
            return propertyMetaInformation;
        }

        /**
         * Sets the value of the propertyMetaInformation property.
         * 
         * @param value
         *            allowed object is {@link PropertyMetaInformation }
         * 
         */
        public void setPropertyMetaInformation(PropertyMetaInformation value) {
            this.propertyMetaInformation = value;
        }

        /**
         * Gets the value of the individualMetaInformationOrLiteral property.
         * 
         * <p>
         * This accessor method returns a reference to the live list, not a snapshot. Therefore any
         * modification you make to the returned list will be present inside the JAXB object. This is why
         * there is not a <CODE>set</CODE> method for the individualMetaInformationOrLiteral property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * 
         * <pre>
         * getIndividualMetaInformationOrLiteral().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list {@link String }
         * {@link IndividualMetaInformation }
         * 
         * 
         */
        public List<Object> getIndividualMetaInformationOrLiteral() {
            if (individualMetaInformationOrLiteral == null) {
                individualMetaInformationOrLiteral = new ArrayList<Object>();
            }
            return this.individualMetaInformationOrLiteral;
        }

    }

}
