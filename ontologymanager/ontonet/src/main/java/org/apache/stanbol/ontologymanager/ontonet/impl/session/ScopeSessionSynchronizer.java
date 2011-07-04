package org.apache.stanbol.ontologymanager.ontonet.impl.session;

import org.apache.stanbol.ontologymanager.ontonet.api.ONManager;
import org.apache.stanbol.ontologymanager.ontonet.api.ontology.OntologyScope;
import org.apache.stanbol.ontologymanager.ontonet.api.ontology.OntologySpaceFactory;
import org.apache.stanbol.ontologymanager.ontonet.api.ontology.UnmodifiableOntologySpaceException;
import org.apache.stanbol.ontologymanager.ontonet.api.session.Session;
import org.apache.stanbol.ontologymanager.ontonet.api.session.SessionEvent;
import org.apache.stanbol.ontologymanager.ontonet.api.session.SessionListener;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.LoggerFactory;

public class ScopeSessionSynchronizer implements SessionListener {

    private ONManager manager;

    public ScopeSessionSynchronizer(ONManager manager) {
        // WARN do not use ONManager here, as it will most probably be
        // instantiated by it.
        this.manager = manager;
    }

    private void addSessionSpaces(IRI sessionId) {
        OntologySpaceFactory factory = manager.getOntologySpaceFactory();
        for (OntologyScope scope : manager.getScopeRegistry().getActiveScopes()) {
            try {
                scope.addSessionSpace(factory.createSessionOntologySpace(scope.getID()), sessionId);
            } catch (UnmodifiableOntologySpaceException e) {
                LoggerFactory.getLogger(getClass()).warn("Tried to add session to unmodifiable space ");
                continue;
            }
        }
    }

    @Override
    public void sessionChanged(SessionEvent event) {
        // System.err.println("Session " + event.getSession() + " has been "
        // + event.getOperationType());
        Session ses = event.getSession();
        switch (event.getOperationType()) {
            case CREATE:
                ses.addSessionListener(this);
                addSessionSpaces(ses.getID());
                break;
            case CLOSE:
                break;
            case KILL:
                ses.removeSessionListener(this);
                break;
            default:
                break;
        }
    }

}
