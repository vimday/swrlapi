package org.swrlapi.factory;

import checkers.nullness.quals.NonNull;
import org.semanticweb.owlapi.model.OWLOntology;
import org.swrlapi.sqwrl.SQWRLQueryEngine;
import org.swrlapi.ui.model.SQWRLQueryEngineModel;

public class DefaultSQWRLQueryEngineModel extends DefaultSWRLRuleEngineModel implements SQWRLQueryEngineModel
{
  @NonNull private SQWRLQueryEngine queryEngine;

  public DefaultSQWRLQueryEngineModel(@NonNull OWLOntology ontology, @NonNull SQWRLQueryEngine sqwrlQueryEngine)
  {
    super(ontology, sqwrlQueryEngine);
    this.queryEngine = sqwrlQueryEngine;
  }

  protected void updateModel(@NonNull OWLOntology ontology, @NonNull SQWRLQueryEngine queryEngine)
  {
    this.queryEngine = queryEngine;
    super.updateModel(ontology, queryEngine);
  }

  @NonNull @Override public SQWRLQueryEngine getSQWRLQueryEngine()
  {
    return this.queryEngine;
  }
}