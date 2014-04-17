package org.swrlapi.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.swrlapi.exceptions.SWRLRuleException;
import org.swrlapi.ext.SWRLAPIOWLDataFactory;
import org.swrlapi.ext.SWRLAPIOWLOntology;
import org.swrlapi.ext.SWRLAPIRule;
import org.swrlapi.sqwrl.DefaultSQWRLQuery;
import org.swrlapi.sqwrl.SQWRLNames;
import org.swrlapi.sqwrl.SQWRLQuery;
import org.swrlapi.sqwrl.SQWRLResult;
import org.swrlapi.sqwrl.SQWRLResultGenerator;
import org.swrlapi.sqwrl.exceptions.SQWRLException;
import org.swrlapi.sqwrl.exceptions.SQWRLInvalidQueryNameException;

public class DefaultSWRLAPIOntologyProcessor implements SWRLAPIOntologyProcessor
{
	private final SWRLAPIOWLOntology swrlapiOWLOntology;

	private final HashMap<String, SWRLAPIRule> rules;
	private final HashMap<String, SQWRLQuery> queries;

	private final Set<OWLAxiom> assertedOWLAxioms; // All asserted OWL axioms extracted from the supplied ontology

	private final HashMap<IRI, OWLDeclarationAxiom> owlClassDeclarationAxioms;
	private final HashMap<IRI, OWLDeclarationAxiom> owlIndividualDeclarationAxioms;
	private final HashMap<IRI, OWLDeclarationAxiom> owlObjectPropertyDeclarationAxioms;
	private final HashMap<IRI, OWLDeclarationAxiom> owlDataPropertyDeclarationAxioms;
	private final HashMap<IRI, OWLDeclarationAxiom> owlAnnotationPropertyDeclarationAxioms;

	public DefaultSWRLAPIOntologyProcessor(SWRLAPIOWLOntology swrlapiOWLOntology)
	{
		this.swrlapiOWLOntology = swrlapiOWLOntology;

		this.rules = new HashMap<String, SWRLAPIRule>();
		this.queries = new HashMap<String, SQWRLQuery>();

		this.assertedOWLAxioms = new HashSet<OWLAxiom>();

		this.owlClassDeclarationAxioms = new HashMap<IRI, OWLDeclarationAxiom>();
		this.owlIndividualDeclarationAxioms = new HashMap<IRI, OWLDeclarationAxiom>();
		this.owlObjectPropertyDeclarationAxioms = new HashMap<IRI, OWLDeclarationAxiom>();
		this.owlDataPropertyDeclarationAxioms = new HashMap<IRI, OWLDeclarationAxiom>();
		this.owlAnnotationPropertyDeclarationAxioms = new HashMap<IRI, OWLDeclarationAxiom>();
	}

	@Override
	public void reset()
	{
		this.rules.clear();
		this.queries.clear();

		getOWLIRIResolver().reset();

		this.assertedOWLAxioms.clear();

		this.owlClassDeclarationAxioms.clear();
		this.owlObjectPropertyDeclarationAxioms.clear();
		this.owlDataPropertyDeclarationAxioms.clear();
		this.owlAnnotationPropertyDeclarationAxioms.clear();
		this.owlIndividualDeclarationAxioms.clear();
	}

	@Override
	public void processOntology() throws SQWRLException
	{
		reset();

		processSWRLRulesAndSQWRLQueries();
		processOWLAxioms();
	}

	@Override
	public SQWRLQuery getSQWRLQuery(String queryName) throws SQWRLException
	{
		if (!this.queries.containsKey(queryName))
			throw new SQWRLInvalidQueryNameException("invalid SQWRL query name " + queryName);

		return this.queries.get(queryName);
	}

	@Override
	public SWRLAPIRule getSWRLRule(String ruleName) throws SWRLRuleException
	{
		if (!this.rules.containsKey(ruleName))
			throw new SWRLRuleException("invalid rule name " + ruleName);

		return this.rules.get(ruleName);
	}

	@Override
	public int getNumberOfSWRLRules()
	{
		return this.rules.values().size();
	}

	@Override
	public int getNumberOfSQWRLQueries()
	{
		return this.queries.values().size();
	}

	@Override
	public Set<String> getSWRLRuleNames()
	{
		return new HashSet<String>(this.rules.keySet());
	}

	@Override
	public Set<String> getSQWRLQueryNames()
	{
		return new HashSet<String>(this.queries.keySet());
	}

	@Override
	public int getNumberOfOWLClassDeclarationAxioms()
	{
		return this.owlClassDeclarationAxioms.values().size();
	}

	@Override
	public int getNumberOfOWLIndividualDeclarationAxioms()
	{
		return this.owlIndividualDeclarationAxioms.values().size();
	}

	@Override
	public int getNumberOfOWLObjectPropertyDeclarationAxioms()
	{
		return this.owlObjectPropertyDeclarationAxioms.size();
	}

	@Override
	public int getNumberOfOWLDataPropertyDeclarationAxioms()
	{
		return this.owlDataPropertyDeclarationAxioms.size();
	}

	@Override
	public int getNumberOfOWLAxioms()
	{
		return this.assertedOWLAxioms.size();
	}

	@Override
	public Set<SWRLAPIRule> getSWRLRules()
	{
		return new HashSet<SWRLAPIRule>(this.rules.values());
	}

	@Override
	public Set<SQWRLQuery> getSQWRLQueries()
	{
		return new HashSet<SQWRLQuery>(this.queries.values());
	}

	@Override
	public Set<OWLAxiom> getOWLAxioms()
	{
		return Collections.unmodifiableSet(this.assertedOWLAxioms);
	}

	@Override
	public boolean hasAssertedOWLAxiom(OWLAxiom axiom)
	{
		return this.assertedOWLAxioms.contains(axiom);
	}

	public boolean isSQWRLQuery(String queryName)
	{
		return this.queries.containsKey(queryName);
	}

	/**
	 * Get the results from a previously executed SQWRL query.
	 */
	@Override
	public SQWRLResult getSQWRLResult(String queryName) throws SQWRLException
	{
		if (!this.queries.containsKey(queryName))
			throw new SQWRLInvalidQueryNameException(queryName);

		return this.queries.get(queryName).getResult();
	}

	/**
	 * Get the result generator for a SQWRL query.
	 */
	@Override
	public SQWRLResultGenerator getSQWRLResultGenerator(String queryName) throws SQWRLException
	{
		if (!this.queries.containsKey(queryName))
			throw new SQWRLInvalidQueryNameException(queryName);

		return this.queries.get(queryName).getResultGenerator();
	}

	/**
	 * Process currently supported OWL axioms. The processing consists of recording any OWL entities in the processed
	 * axioms (with an instance of the {@link OWLIRIResolver} class) and generating declaration axioms for these entities.
	 * <p>
	 * TODO The current approach is clunky. A better approach would be to walk the axioms with a visitor and record the
	 * entities and generate the declaration axioms.
	 */
	private void processOWLAxioms()
	{
		processOWLClassDeclarationAxioms();
		processOWLIndividualDeclarationAxioms();
		processOWLObjectPropertyDeclarationAxioms();
		processOWLDataPropertyDeclarationAxioms();
		processOWLAnnotationPropertyDeclarationAxioms();
		processOWLClassAssertionAxioms();
		processOWLObjectPropertyAssertionAxioms();
		processOWLDataPropertyAssertionAxioms();
		processOWLSameIndividualAxioms();
		processOWLDifferentIndividualsAxioms();
		processOWLSubClassOfAxioms();
		processOWLEquivalentClassesAxioms();
		processOWLSubObjectPropertyOfAxioms();
		processOWLSubDataPropertyOfAxioms();
		processOWLEquivalentDataPropertiesAxioms();
		processOWLEquivalentObjectPropertiesAxioms();
		processOWLTransitiveObjectPropertyAxioms();
		processOWLSymmetricObjectPropertyAxioms();
		processOWLFunctionalObjectPropertyAxioms();
		processOWLInverseFunctionalObjectPropertyAxioms();
		processOWLFunctionalDataPropertyAxioms();
		processOWLObjectPropertyDomainAxioms();
		processOWLDataPropertyDomainAxioms();
		processOWLObjectPropertyRangeAxioms();
		processOWLDataPropertyRangeAxioms();
		processOWLInverseObjectPropertiesAxioms();
		processOWLIrreflexiveObjectPropertyAxioms();
		processOWLAsymmetricObjectPropertyAxioms();
		processOWLDisjointObjectPropertiesAxioms();
		processOWLDisjointDataPropertiesAxioms();
	}

	private void processSWRLRulesAndSQWRLQueries() throws SQWRLException
	{
		for (SWRLAPIRule ruleOrQuery : getSWRLAPIOWLOntology().getSWRLAPIRules())
			processSWRLRuleOrSQWRLQuery(ruleOrQuery);
	}

	private void processSWRLRuleOrSQWRLQuery(SWRLAPIRule ruleOrQuery) throws SQWRLException
	{
		if (isSQWRLQuery(ruleOrQuery)) {
			boolean active = true; // TODO
			String comment = ""; // TODO
			SQWRLQuery query = new DefaultSQWRLQuery(ruleOrQuery.getRuleName(), ruleOrQuery.getBodyAtoms(),
					ruleOrQuery.getHeadAtoms(), getSWRLAPIOWLDataFactory(), getOWLIRIResolver(), active, comment);
			this.queries.put(ruleOrQuery.getRuleName(), query);
		} else {
			this.rules.put(ruleOrQuery.getRuleName(), ruleOrQuery);
			this.assertedOWLAxioms.add(ruleOrQuery); // A SWRL rule is a type of OWL axiom; a SQWRL query is not.
		}
	}

	private boolean isSQWRLQuery(SWRLAPIRule ruleOrQuery)
	{
		return !ruleOrQuery.getBuiltInAtomsFromHead(SQWRLNames.getSQWRLBuiltInNames()).isEmpty()
				|| !ruleOrQuery.getBuiltInAtomsFromBody(SQWRLNames.getSQWRLBuiltInNames()).isEmpty();
	}

	private void processOWLClassAssertionAxioms()
	{
		for (OWLClassAssertionAxiom axiom : getOWLClassAssertionAxioms()) {
			generateOWLIndividualDeclarationAxiomIfNecessary(axiom.getIndividual());
			this.assertedOWLAxioms.add(axiom);
		}
	}

	private void processOWLObjectPropertyAssertionAxioms()
	{
		for (OWLObjectPropertyAssertionAxiom axiom : getOWLObjectPropertyAssertionAxioms()) {
			generateOWLIndividualDeclarationAxiomIfNecessary(axiom.getSubject());
			generateOWLIndividualDeclarationAxiomIfNecessary(axiom.getObject());
			this.assertedOWLAxioms.add(axiom);
		}
	}

	private void processOWLDataPropertyAssertionAxioms()
	{
		for (OWLDataPropertyAssertionAxiom axiom : getOWLDataPropertyAssertionAxioms()) {
			generateOWLIndividualDeclarationAxiomIfNecessary(axiom.getSubject());
			this.assertedOWLAxioms.add(axiom);
		}
	}

	private void processOWLClassDeclarationAxioms()
	{
		for (OWLDeclarationAxiom axiom : getOWLClassDeclarationAxioms()) {
			OWLEntity cls = axiom.getEntity();
			this.owlClassDeclarationAxioms.put(cls.getIRI(), axiom);
			this.assertedOWLAxioms.add(axiom);
			recordOWLClass(cls);
		}
	}

	private void processOWLIndividualDeclarationAxioms()
	{
		for (OWLDeclarationAxiom axiom : getOWLIndividualDeclarationAxioms()) {
			OWLEntity individual = axiom.getEntity();
			this.owlIndividualDeclarationAxioms.put(individual.getIRI(), axiom);
			this.assertedOWLAxioms.add(axiom);
			recordOWLNamedIndividual(individual);
		}
	}

	private void processOWLObjectPropertyDeclarationAxioms()
	{
		for (OWLDeclarationAxiom axiom : getOWLObjectPropertyDeclarationAxioms()) {
			OWLEntity property = axiom.getEntity();
			this.owlObjectPropertyDeclarationAxioms.put(property.getIRI(), axiom);
			this.assertedOWLAxioms.add(axiom);
			recordOWLObjectProperty(property);
		}
	}

	private void processOWLDataPropertyDeclarationAxioms()
	{
		for (OWLDeclarationAxiom axiom : getOWLDataPropertyDeclarationAxioms()) {
			OWLEntity property = axiom.getEntity();

			this.owlDataPropertyDeclarationAxioms.put(property.getIRI(), axiom);
			this.assertedOWLAxioms.add(axiom);
			recordOWLDataProperty(property);
		}
	}

	private void processOWLAnnotationPropertyDeclarationAxioms()
	{
		for (OWLDeclarationAxiom axiom : getOWLAnnotationPropertyDeclarationAxioms()) {
			OWLEntity property = axiom.getEntity();

			this.owlAnnotationPropertyDeclarationAxioms.put(property.getIRI(), axiom);
			this.assertedOWLAxioms.add(axiom);
			recordOWLAnnotationProperty(property);
		}
	}

	private void processOWLSameIndividualAxioms()
	{
		Set<OWLSameIndividualAxiom> axioms = getOWLSameIndividualAxioms();
		for (OWLSameIndividualAxiom axiom : axioms) {
			for (OWLIndividual individual : axiom.getIndividuals())
				generateOWLIndividualDeclarationAxiomIfNecessary(individual);
		}
		this.assertedOWLAxioms.addAll(axioms);
	}

	private void processOWLDifferentIndividualsAxioms()
	{
		Set<OWLDifferentIndividualsAxiom> axioms = getOWLDifferentIndividualsAxioms();
		for (OWLDifferentIndividualsAxiom axiom : axioms) {
			for (OWLIndividual individual : axiom.getIndividuals())
				generateOWLIndividualDeclarationAxiomIfNecessary(individual);
		}
		this.assertedOWLAxioms.addAll(axioms);
	}

	private void processOWLSubClassOfAxioms()
	{
		Set<OWLSubClassOfAxiom> axioms = getOWLSubClassOfAxioms();
		for (OWLSubClassOfAxiom axiom : axioms) {
			generateOWLClassDeclarationAxiomIfNecessary(axiom.getSubClass());
			generateOWLClassDeclarationAxiomIfNecessary(axiom.getSuperClass());
		}
		this.assertedOWLAxioms.addAll(axioms);
	}

	private void processOWLEquivalentClassesAxioms()
	{
		Set<OWLEquivalentClassesAxiom> axioms = getOWLEquivalentClassesAxioms();
		for (OWLEquivalentClassesAxiom axiom : axioms) {
			for (OWLClass cls : axiom.getNamedClasses())
				generateOWLClassDeclarationAxiom(cls);
		}
		this.assertedOWLAxioms.addAll(axioms);
	}

	private void processOWLSubObjectPropertyOfAxioms()
	{
		Set<OWLSubObjectPropertyOfAxiom> axioms = getOWLSubObjectPropertyOfAxioms();
		for (OWLSubObjectPropertyOfAxiom axiom : axioms) {
			generateOWLObjectPropertyDeclarationAxiomIfNecessary(axiom.getSubProperty());
			generateOWLObjectPropertyDeclarationAxiomIfNecessary(axiom.getSuperProperty());
		}
		this.assertedOWLAxioms.addAll(axioms);
	}

	private void processOWLSubDataPropertyOfAxioms()
	{
		Set<OWLSubDataPropertyOfAxiom> axioms = getOWLSubDataPropertyOfAxioms();
		for (OWLSubDataPropertyOfAxiom axiom : axioms) {
			generateOWLDataPropertyDeclarationAxiomIfNecessary(axiom.getSubProperty());
			generateOWLDataPropertyDeclarationAxiomIfNecessary(axiom.getSuperProperty());
		}
		this.assertedOWLAxioms.addAll(axioms);
	}

	private void processOWLTransitiveObjectPropertyAxioms()
	{
		Set<OWLTransitiveObjectPropertyAxiom> axioms = getOWLTransitiveObjectPropertyAxioms();
		for (OWLTransitiveObjectPropertyAxiom axiom : axioms)
			generateOWLObjectPropertyDeclarationAxiomIfNecessary(axiom.getProperty());
		this.assertedOWLAxioms.addAll(axioms);
	}

	private void processOWLSymmetricObjectPropertyAxioms()
	{
		Set<OWLSymmetricObjectPropertyAxiom> axioms = getOWLSymmetricObjectPropertyAxioms();
		for (OWLSymmetricObjectPropertyAxiom axiom : axioms)
			generateOWLObjectPropertyDeclarationAxiomIfNecessary(axiom.getProperty());
		this.assertedOWLAxioms.addAll(axioms);
	}

	private void processOWLFunctionalObjectPropertyAxioms()
	{
		Set<OWLFunctionalObjectPropertyAxiom> axioms = getOWLFunctionalObjectPropertyAxioms();
		for (OWLFunctionalObjectPropertyAxiom axiom : axioms)
			generateOWLObjectPropertyDeclarationAxiomIfNecessary(axiom.getProperty());
		this.assertedOWLAxioms.addAll(axioms);
	}

	private void processOWLInverseFunctionalObjectPropertyAxioms()
	{
		Set<OWLInverseFunctionalObjectPropertyAxiom> axioms = getOWLInverseFunctionalObjectPropertyAxioms();
		for (OWLInverseFunctionalObjectPropertyAxiom axiom : axioms)
			generateOWLObjectPropertyDeclarationAxiomIfNecessary(axiom.getProperty());
		this.assertedOWLAxioms.addAll(axioms);
	}

	private void processOWLFunctionalDataPropertyAxioms()
	{
		Set<OWLFunctionalDataPropertyAxiom> axioms = getOWLFunctionalDataPropertyAxioms();
		for (OWLFunctionalDataPropertyAxiom axiom : axioms)
			generateOWLDataPropertyDeclarationAxiomIfNecessary(axiom.getProperty());
		this.assertedOWLAxioms.addAll(axioms);
	}

	private void processOWLObjectPropertyDomainAxioms()
	{
		Set<OWLObjectPropertyDomainAxiom> axioms = getOWLObjectPropertyDomainAxioms();
		for (OWLObjectPropertyDomainAxiom axiom : axioms) {
			generateOWLObjectPropertyDeclarationAxiomIfNecessary(axiom.getProperty());
			generateOWLClassDeclarationAxiomIfNecessary(axiom.getDomain());
		}
		this.assertedOWLAxioms.addAll(axioms);
	}

	private void processOWLDataPropertyDomainAxioms()
	{
		Set<OWLDataPropertyDomainAxiom> axioms = getOWLDataPropertyDomainAxioms();
		for (OWLDataPropertyDomainAxiom axiom : axioms) {
			generateOWLDataPropertyDeclarationAxiomIfNecessary(axiom.getProperty());
			generateOWLClassDeclarationAxiomIfNecessary(axiom.getDomain());
		}
		this.assertedOWLAxioms.addAll(axioms);
	}

	private void processOWLObjectPropertyRangeAxioms()
	{
		Set<OWLObjectPropertyRangeAxiom> axioms = getOWLObjectPropertyRangeAxioms();
		for (OWLObjectPropertyRangeAxiom axiom : axioms) {
			generateOWLObjectPropertyDeclarationAxiomIfNecessary(axiom.getProperty());
			generateOWLClassDeclarationAxiomIfNecessary(axiom.getRange());
		}
		this.assertedOWLAxioms.addAll(axioms);
	}

	private void processOWLDataPropertyRangeAxioms()
	{
		Set<OWLDataPropertyRangeAxiom> axioms = getOWLDataPropertyRangeAxioms();
		for (OWLDataPropertyRangeAxiom axiom : axioms) {
			generateOWLDataPropertyDeclarationAxiomIfNecessary(axiom.getProperty());
		}
		this.assertedOWLAxioms.addAll(axioms);
	}

	private void processOWLIrreflexiveObjectPropertyAxioms()
	{
		Set<OWLIrreflexiveObjectPropertyAxiom> axioms = getOWLIrreflexiveObjectPropertyAxioms();
		for (OWLIrreflexiveObjectPropertyAxiom axiom : axioms)
			generateOWLObjectPropertyDeclarationAxiomIfNecessary(axiom.getProperty());
		this.assertedOWLAxioms.addAll(axioms);
	}

	private void processOWLAsymmetricObjectPropertyAxioms()
	{
		Set<OWLAsymmetricObjectPropertyAxiom> axioms = getOWLAsymmetricObjectPropertyAxioms();
		for (OWLAsymmetricObjectPropertyAxiom axiom : axioms)
			generateOWLObjectPropertyDeclarationAxiomIfNecessary(axiom.getProperty());
		this.assertedOWLAxioms.addAll(axioms);
	}

	private void processOWLEquivalentObjectPropertiesAxioms()
	{
		Set<OWLEquivalentObjectPropertiesAxiom> axioms = getOWLEquivalentObjectPropertiesAxioms();
		for (OWLEquivalentObjectPropertiesAxiom axiom : axioms) {
			for (OWLObjectPropertyExpression property : axiom.getProperties())
				generateOWLObjectPropertyDeclarationAxiomIfNecessary(property);
		}
		this.assertedOWLAxioms.addAll(axioms);
	}

	private void processOWLEquivalentDataPropertiesAxioms()
	{
		Set<OWLEquivalentDataPropertiesAxiom> axioms = getOWLEquivalentDataPropertiesAxioms();
		for (OWLEquivalentDataPropertiesAxiom axiom : axioms) {
			for (OWLDataPropertyExpression property : axiom.getProperties())
				generateOWLDataPropertyDeclarationAxiomIfNecessary(property);
		}
		this.assertedOWLAxioms.addAll(axioms);
	}

	private void processOWLInverseObjectPropertiesAxioms()
	{
		Set<OWLInverseObjectPropertiesAxiom> axioms = getOWLInverseObjectPropertiesAxioms();
		for (OWLInverseObjectPropertiesAxiom axiom : axioms) {
			generateOWLObjectPropertyDeclarationAxiomIfNecessary(axiom.getFirstProperty());
			generateOWLObjectPropertyDeclarationAxiomIfNecessary(axiom.getSecondProperty());
		}
		this.assertedOWLAxioms.addAll(axioms);
	}

	private void processOWLDisjointObjectPropertiesAxioms()
	{
		Set<OWLDisjointObjectPropertiesAxiom> axioms = getOWLDisjointObjectPropertiesAxioms();
		for (OWLDisjointObjectPropertiesAxiom axiom : axioms) {
			for (OWLObjectPropertyExpression property : axiom.getProperties())
				generateOWLObjectPropertyDeclarationAxiomIfNecessary(property);
		}
		this.assertedOWLAxioms.addAll(axioms);
	}

	private void processOWLDisjointDataPropertiesAxioms()
	{
		Set<OWLDisjointDataPropertiesAxiom> axioms = getOWLDisjointDataPropertiesAxioms();
		for (OWLDisjointDataPropertiesAxiom axiom : axioms) {
			for (OWLDataPropertyExpression property : axiom.getProperties())
				generateOWLDataPropertyDeclarationAxiomIfNecessary(property);
		}
		this.assertedOWLAxioms.addAll(axioms);
	}

	private void generateOWLClassDeclarationAxiom(OWLClass cls)
	{
		if (!this.owlClassDeclarationAxioms.containsKey(cls.getIRI())) {
			OWLDeclarationAxiom axiom = getSWRLAPIOWLDataFactory().getOWLClassDeclarationAxiom(cls);
			this.owlClassDeclarationAxioms.put(cls.getIRI(), axiom);
			this.assertedOWLAxioms.add(axiom);
			recordOWLClass(cls);
		}
	}

	private void generateOWLClassDeclarationAxiomIfNecessary(OWLClassExpression classExpression)
	{
		if (classExpression instanceof OWLClass) {
			OWLClass cls = (OWLClass)classExpression;
			generateOWLClassDeclarationAxiom(cls);
		}
	}

	private void generateOWLIndividualDeclarationAxiomIfNecessary(OWLIndividual individual)
	{
		if (individual.isNamed()
				&& !this.owlIndividualDeclarationAxioms.containsKey(individual.asOWLNamedIndividual().getIRI())) {
			OWLDeclarationAxiom axiom = getSWRLAPIOWLDataFactory().getOWLIndividualDeclarationAxiom(
					individual.asOWLNamedIndividual());
			this.owlIndividualDeclarationAxioms.put(individual.asOWLNamedIndividual().getIRI(), axiom);
			this.assertedOWLAxioms.add(axiom);
			recordOWLNamedIndividual(individual.asOWLNamedIndividual());
		}
	}

	private void generateOWLObjectPropertyDeclarationAxiomIfNecessary(OWLObjectPropertyExpression propertyExpression)
	{
		if (propertyExpression instanceof OWLObjectProperty) {
			OWLObjectProperty property = (OWLObjectProperty)propertyExpression;
			if (!this.owlObjectPropertyDeclarationAxioms.containsKey(property.getIRI())) {
				OWLDeclarationAxiom axiom = getSWRLAPIOWLDataFactory().getOWLObjectPropertyDeclarationAxiom(property);
				this.owlObjectPropertyDeclarationAxioms.put(property.getIRI(), axiom);
				this.assertedOWLAxioms.add(axiom);
				recordOWLObjectProperty(property);
			}
		}
	}

	private void generateOWLDataPropertyDeclarationAxiomIfNecessary(OWLDataPropertyExpression propertyExpression)
	{
		if (propertyExpression instanceof OWLDataProperty) {
			OWLDataProperty property = (OWLDataProperty)propertyExpression;
			if (!this.owlDataPropertyDeclarationAxioms.containsKey(property.getIRI())) {
				OWLDeclarationAxiom axiom = getSWRLAPIOWLDataFactory().getOWLDataPropertyDeclarationAxiom(property);
				this.owlDataPropertyDeclarationAxioms.put(property.getIRI(), axiom);
				this.assertedOWLAxioms.add(axiom);
				recordOWLDataProperty(property);
			}
		}
	}

	private Set<OWLSameIndividualAxiom> getOWLSameIndividualAxioms()
	{
		return getOWLOntology().getAxioms(AxiomType.SAME_INDIVIDUAL, true);
	}

	private Set<OWLDifferentIndividualsAxiom> getOWLDifferentIndividualsAxioms()
	{
		return getOWLOntology().getAxioms(AxiomType.DIFFERENT_INDIVIDUALS, true);
	}

	private Set<OWLSubObjectPropertyOfAxiom> getOWLSubObjectPropertyOfAxioms()
	{
		return getOWLOntology().getAxioms(AxiomType.SUB_OBJECT_PROPERTY, true);
	}

	private Set<OWLSubDataPropertyOfAxiom> getOWLSubDataPropertyOfAxioms()
	{
		return getOWLOntology().getAxioms(AxiomType.SUB_DATA_PROPERTY, true);
	}

	private Set<OWLEquivalentClassesAxiom> getOWLEquivalentClassesAxioms()
	{
		return getOWLOntology().getAxioms(AxiomType.EQUIVALENT_CLASSES, true);
	}

	private Set<OWLClassAssertionAxiom> getOWLClassAssertionAxioms()
	{
		return getOWLOntology().getAxioms(AxiomType.CLASS_ASSERTION, true);
	}

	private Set<OWLObjectPropertyAssertionAxiom> getOWLObjectPropertyAssertionAxioms()
	{
		return getOWLOntology().getAxioms(AxiomType.OBJECT_PROPERTY_ASSERTION, true);
	}

	private Set<OWLDataPropertyAssertionAxiom> getOWLDataPropertyAssertionAxioms()
	{
		return getOWLOntology().getAxioms(AxiomType.DATA_PROPERTY_ASSERTION, true);
	}

	private Set<OWLSubClassOfAxiom> getOWLSubClassOfAxioms()
	{
		return getOWLOntology().getAxioms(AxiomType.SUBCLASS_OF, true);
	}

	@SuppressWarnings("unused")
	private Set<OWLDisjointClassesAxiom> getOWLDisjointClassesAxioms()
	{
		return getOWLOntology().getAxioms(AxiomType.DISJOINT_CLASSES, true);
	}

	private Set<OWLEquivalentDataPropertiesAxiom> getOWLEquivalentDataPropertiesAxioms()
	{
		return getOWLOntology().getAxioms(AxiomType.EQUIVALENT_DATA_PROPERTIES, true);
	}

	private Set<OWLEquivalentObjectPropertiesAxiom> getOWLEquivalentObjectPropertiesAxioms()
	{
		return getOWLOntology().getAxioms(AxiomType.EQUIVALENT_OBJECT_PROPERTIES, true);
	}

	private Set<OWLDisjointDataPropertiesAxiom> getOWLDisjointDataPropertiesAxioms()
	{
		return getOWLOntology().getAxioms(AxiomType.DISJOINT_DATA_PROPERTIES, true);
	}

	private Set<OWLDisjointObjectPropertiesAxiom> getOWLDisjointObjectPropertiesAxioms()
	{
		return getOWLOntology().getAxioms(AxiomType.DISJOINT_OBJECT_PROPERTIES, true);
	}

	private Set<OWLObjectPropertyDomainAxiom> getOWLObjectPropertyDomainAxioms()
	{
		return getOWLOntology().getAxioms(AxiomType.OBJECT_PROPERTY_DOMAIN, true);
	}

	private Set<OWLDataPropertyDomainAxiom> getOWLDataPropertyDomainAxioms()
	{
		return getOWLOntology().getAxioms(AxiomType.DATA_PROPERTY_DOMAIN, true);
	}

	private Set<OWLObjectPropertyRangeAxiom> getOWLObjectPropertyRangeAxioms()
	{
		return getOWLOntology().getAxioms(AxiomType.OBJECT_PROPERTY_RANGE, true);
	}

	private Set<OWLDataPropertyRangeAxiom> getOWLDataPropertyRangeAxioms()
	{
		return getOWLOntology().getAxioms(AxiomType.DATA_PROPERTY_RANGE, true);
	}

	private Set<OWLFunctionalObjectPropertyAxiom> getOWLFunctionalObjectPropertyAxioms()
	{
		return getOWLOntology().getAxioms(AxiomType.FUNCTIONAL_OBJECT_PROPERTY, true);
	}

	private Set<OWLFunctionalDataPropertyAxiom> getOWLFunctionalDataPropertyAxioms()
	{
		return getOWLOntology().getAxioms(AxiomType.FUNCTIONAL_DATA_PROPERTY, true);
	}

	private Set<OWLIrreflexiveObjectPropertyAxiom> getOWLIrreflexiveObjectPropertyAxioms()
	{
		return getOWLOntology().getAxioms(AxiomType.IRREFLEXIVE_OBJECT_PROPERTY, true);
	}

	private Set<OWLInverseFunctionalObjectPropertyAxiom> getOWLInverseFunctionalObjectPropertyAxioms()
	{
		return getOWLOntology().getAxioms(AxiomType.INVERSE_FUNCTIONAL_OBJECT_PROPERTY, true);
	}

	private Set<OWLTransitiveObjectPropertyAxiom> getOWLTransitiveObjectPropertyAxioms()
	{
		return getOWLOntology().getAxioms(AxiomType.TRANSITIVE_OBJECT_PROPERTY, true);
	}

	private Set<OWLSymmetricObjectPropertyAxiom> getOWLSymmetricObjectPropertyAxioms()
	{
		return getOWLOntology().getAxioms(AxiomType.SYMMETRIC_OBJECT_PROPERTY, true);
	}

	private Set<OWLAsymmetricObjectPropertyAxiom> getOWLAsymmetricObjectPropertyAxioms()
	{
		return getOWLOntology().getAxioms(AxiomType.ASYMMETRIC_OBJECT_PROPERTY, true);
	}

	private Set<OWLInverseObjectPropertiesAxiom> getOWLInverseObjectPropertiesAxioms()
	{
		return getOWLOntology().getAxioms(AxiomType.INVERSE_OBJECT_PROPERTIES, true);
	}

	@SuppressWarnings("unused")
	private Set<OWLNegativeDataPropertyAssertionAxiom> getOWLNegativeDataPropertyAssertionAxioms()
	{
		return getOWLOntology().getAxioms(AxiomType.NEGATIVE_DATA_PROPERTY_ASSERTION, true);
	}

	@SuppressWarnings("unused")
	private Set<OWLNegativeObjectPropertyAssertionAxiom> getOWLNegativeObjectPropertyAssertionAxioms()
	{
		return getOWLOntology().getAxioms(AxiomType.NEGATIVE_OBJECT_PROPERTY_ASSERTION, true);
	}

	@SuppressWarnings("unused")
	private Set<OWLReflexiveObjectPropertyAxiom> getOWLReflexiveObjectPropertyAxioms()
	{
		return getOWLOntology().getAxioms(AxiomType.REFLEXIVE_OBJECT_PROPERTY, true);
	}

	@SuppressWarnings("unused")
	private Set<OWLDisjointUnionAxiom> getOWLDisjointUnionAxioms()
	{
		return getOWLOntology().getAxioms(AxiomType.DISJOINT_UNION, true);
	}

	@SuppressWarnings("unused")
	private Set<OWLAnnotationAssertionAxiom> getOWLAnnotationAssertionAxioms()
	{
		return getOWLOntology().getAxioms(AxiomType.ANNOTATION_ASSERTION, true);
	}

	@SuppressWarnings("unused")
	private Set<OWLSubPropertyChainOfAxiom> getOWLSubPropertyChainOfAxioms()
	{
		return getOWLOntology().getAxioms(AxiomType.SUB_PROPERTY_CHAIN_OF, true);
	}

	@SuppressWarnings("unused")
	private Set<OWLHasKeyAxiom> getOWLHasKeyAxioms()
	{
		return getOWLOntology().getAxioms(AxiomType.HAS_KEY, true);
	}

	@SuppressWarnings("unused")
	private Set<OWLDatatypeDefinitionAxiom> getOWLDatatypeDefinitionAxioms()
	{
		return getOWLOntology().getAxioms(AxiomType.DATATYPE_DEFINITION, true);
	}

	@SuppressWarnings("unused")
	private Set<OWLAnnotationPropertyRangeAxiom> getOWLAnnotationPropertyRangeAxioms()
	{
		return getOWLOntology().getAxioms(AxiomType.ANNOTATION_PROPERTY_RANGE, true);
	}

	@SuppressWarnings("unused")
	private Set<OWLAnnotationPropertyDomainAxiom> getOWLAnnotationPropertyDomainAxioms()
	{
		return getOWLOntology().getAxioms(AxiomType.ANNOTATION_PROPERTY_DOMAIN, true);
	}

	@SuppressWarnings("unused")
	private Set<OWLSubAnnotationPropertyOfAxiom> getOWLSubAnnotationPropertyOfAxioms()
	{
		return getOWLOntology().getAxioms(AxiomType.SUB_ANNOTATION_PROPERTY_OF, true);
	}

	private Set<OWLDeclarationAxiom> getOWLClassDeclarationAxioms()
	{
		Set<OWLDeclarationAxiom> owlClassDeclarationAxioms = new HashSet<OWLDeclarationAxiom>();

		for (OWLDeclarationAxiom owlDeclarationAxiom : getOWLOntology().getAxioms(AxiomType.DECLARATION, true)) {
			if (owlDeclarationAxiom.getEntity().isOWLClass())
				owlClassDeclarationAxioms.add(owlDeclarationAxiom);
		}
		return owlClassDeclarationAxioms;
	}

	private Set<OWLDeclarationAxiom> getOWLIndividualDeclarationAxioms()
	{
		Set<OWLDeclarationAxiom> owlIndividualDeclarationAxioms = new HashSet<OWLDeclarationAxiom>();

		for (OWLDeclarationAxiom owlDeclarationAxiom : getOWLOntology().getAxioms(AxiomType.DECLARATION, true)) {
			if (owlDeclarationAxiom.getEntity().isOWLNamedIndividual())
				owlIndividualDeclarationAxioms.add(owlDeclarationAxiom);
		}
		return owlIndividualDeclarationAxioms;
	}

	private Set<OWLDeclarationAxiom> getOWLObjectPropertyDeclarationAxioms()
	{
		Set<OWLDeclarationAxiom> owlObjectPropertyDeclarationAxioms = new HashSet<OWLDeclarationAxiom>();

		for (OWLDeclarationAxiom owlDeclarationAxiom : getOWLOntology().getAxioms(AxiomType.DECLARATION, true)) {
			if (owlDeclarationAxiom.getEntity().isOWLObjectProperty())
				owlObjectPropertyDeclarationAxioms.add(owlDeclarationAxiom);
		}
		return owlObjectPropertyDeclarationAxioms;
	}

	private Set<OWLDeclarationAxiom> getOWLDataPropertyDeclarationAxioms()
	{
		Set<OWLDeclarationAxiom> owlDataPropertyDeclarationAxioms = new HashSet<OWLDeclarationAxiom>();

		for (OWLDeclarationAxiom owlDeclarationAxiom : getOWLOntology().getAxioms(AxiomType.DECLARATION, true)) {
			if (owlDeclarationAxiom.getEntity().isOWLDataProperty())
				owlDataPropertyDeclarationAxioms.add(owlDeclarationAxiom);
		}
		return owlDataPropertyDeclarationAxioms;
	}

	private Set<OWLDeclarationAxiom> getOWLAnnotationPropertyDeclarationAxioms()
	{
		Set<OWLDeclarationAxiom> owlAnnotationPropertyDeclarationAxioms = new HashSet<OWLDeclarationAxiom>();

		for (OWLDeclarationAxiom owlDeclarationAxiom : getOWLOntology().getAxioms(AxiomType.DECLARATION, true)) {
			if (owlDeclarationAxiom.getEntity().isOWLAnnotationProperty())
				owlAnnotationPropertyDeclarationAxioms.add(owlDeclarationAxiom);
		}
		return owlAnnotationPropertyDeclarationAxioms;
	}

	@SuppressWarnings("unused")
	private Set<OWLDeclarationAxiom> getOWLDatatypeDeclarationAxioms()
	{
		Set<OWLDeclarationAxiom> owlDatatypeDeclarationAxioms = new HashSet<OWLDeclarationAxiom>();

		for (OWLDeclarationAxiom owlDeclarationAxiom : getOWLOntology().getAxioms(AxiomType.DECLARATION, true)) {
			if (owlDeclarationAxiom.getEntity().isOWLDatatype())
				owlDatatypeDeclarationAxioms.add(owlDeclarationAxiom);
		}
		return owlDatatypeDeclarationAxioms;
	}

	private void recordOWLClass(OWLEntity cls)
	{
		getOWLIRIResolver().recordOWLClass(cls);
	}

	private void recordOWLNamedIndividual(OWLEntity individual)
	{
		getOWLIRIResolver().recordOWLNamedIndividual(individual);
	}

	private void recordOWLObjectProperty(OWLEntity property)
	{
		getOWLIRIResolver().recordOWLObjectProperty(property);
	}

	private void recordOWLDataProperty(OWLEntity property)
	{
		getOWLIRIResolver().recordOWLDataProperty(property);
	}

	private void recordOWLAnnotationProperty(OWLEntity property)
	{
		getOWLIRIResolver().recordOWLAnnotationProperty(property);
	}

	private SWRLAPIOWLOntology getSWRLAPIOWLOntology()
	{
		return this.swrlapiOWLOntology;
	}

	private OWLOntology getOWLOntology()
	{
		return getSWRLAPIOWLOntology().getOWLOntology();
	}

	private OWLIRIResolver getOWLIRIResolver()
	{
		return getSWRLAPIOWLOntology().getOWLIRIResolver();
	}

	private SWRLAPIOWLDataFactory getSWRLAPIOWLDataFactory()
	{
		return getSWRLAPIOWLOntology().getSWRLAPIOWLDataFactory();
	}
}