package org.apache.stanbol.reengineer.base.api.util;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;

public class ReengineerUriRefGenerator {

    // // Unused protected method that relies on a specific reengineer implementation
    // protected UriRef createTypedResource(MGraph mGraph, String resourceURI, UriRef type){
    // UriRef uriRef = new UriRef(resourceURI);
    // if(type != null){
    // mGraph.add(new TripleImpl(uriRef, DBS_L1.RDF_TYPE, type));
    // }
    //		
    // return uriRef;
    // }

    protected OWLClassAssertionAxiom createOWLClassAssertionAxiom(OWLDataFactory factory,
                                                                  IRI owlClassIRI,
                                                                  IRI individualIRI) {

        OWLClass owlClass = factory.getOWLClass(owlClassIRI);
        OWLNamedIndividual individual = factory.getOWLNamedIndividual(individualIRI);

        OWLClassAssertionAxiom classAssertion = factory.getOWLClassAssertionAxiom(owlClass, individual);

        return classAssertion;
    }

    protected OWLObjectPropertyAssertionAxiom createOWLObjectPropertyAssertionAxiom(OWLDataFactory factory,
                                                                                    IRI objectPropertyIRI,
                                                                                    IRI subjectIndividualIRI,
                                                                                    IRI objectIndividualIRI) {

        OWLObjectProperty objectProperty = factory.getOWLObjectProperty(objectPropertyIRI);
        OWLNamedIndividual subjectIndividual = factory.getOWLNamedIndividual(subjectIndividualIRI);
        OWLNamedIndividual objectIndividual = factory.getOWLNamedIndividual(objectIndividualIRI);

        return factory
                .getOWLObjectPropertyAssertionAxiom(objectProperty, subjectIndividual, objectIndividual);
    }

    protected OWLDataPropertyAssertionAxiom createOWLDataPropertyAssertionAxiom(OWLDataFactory factory,
                                                                                IRI dataPropertyIRI,
                                                                                IRI subjectIndividualIRI,
                                                                                int data) {

        OWLDataProperty dataProperty = factory.getOWLDataProperty(dataPropertyIRI);
        OWLNamedIndividual subjectIndividual = factory.getOWLNamedIndividual(subjectIndividualIRI);

        return factory.getOWLDataPropertyAssertionAxiom(dataProperty, subjectIndividual, data);
    }

    protected OWLDataPropertyAssertionAxiom createOWLDataPropertyAssertionAxiom(OWLDataFactory factory,
                                                                                IRI dataPropertyIRI,
                                                                                IRI subjectIndividualIRI,
                                                                                double data) {

        OWLDataProperty dataProperty = factory.getOWLDataProperty(dataPropertyIRI);
        OWLNamedIndividual subjectIndividual = factory.getOWLNamedIndividual(subjectIndividualIRI);

        return factory.getOWLDataPropertyAssertionAxiom(dataProperty, subjectIndividual, data);
    }

    protected OWLDataPropertyAssertionAxiom createOWLDataPropertyAssertionAxiom(OWLDataFactory factory,
                                                                                IRI dataPropertyIRI,
                                                                                IRI subjectIndividualIRI,
                                                                                float data) {

        OWLDataProperty dataProperty = factory.getOWLDataProperty(dataPropertyIRI);
        OWLNamedIndividual subjectIndividual = factory.getOWLNamedIndividual(subjectIndividualIRI);

        return factory.getOWLDataPropertyAssertionAxiom(dataProperty, subjectIndividual, data);
    }

    protected OWLDataPropertyAssertionAxiom createOWLDataPropertyAssertionAxiom(OWLDataFactory factory,
                                                                                IRI dataPropertyIRI,
                                                                                IRI subjectIndividualIRI,
                                                                                boolean data) {

        OWLDataProperty dataProperty = factory.getOWLDataProperty(dataPropertyIRI);
        OWLNamedIndividual subjectIndividual = factory.getOWLNamedIndividual(subjectIndividualIRI);

        return factory.getOWLDataPropertyAssertionAxiom(dataProperty, subjectIndividual, data);
    }

    protected OWLDataPropertyAssertionAxiom createOWLDataPropertyAssertionAxiom(OWLDataFactory factory,
                                                                                IRI dataPropertyIRI,
                                                                                IRI subjectIndividualIRI,
                                                                                String data) {

        OWLDataProperty dataProperty = factory.getOWLDataProperty(dataPropertyIRI);
        OWLNamedIndividual subjectIndividual = factory.getOWLNamedIndividual(subjectIndividualIRI);

        return factory.getOWLDataPropertyAssertionAxiom(dataProperty, subjectIndividual, data);
    }

}