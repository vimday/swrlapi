package org.swrlapi.lwf.test.swrlview;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.SystemOutDocumentTarget;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.SWRLRule;
import org.swrlapi.core.SWRLAPIRule;
import org.swrlapi.core.SWRLRuleEngine;
import org.swrlapi.core.SWRLRuleRenderer;
import org.swrlapi.factory.DefaultSWRLRuleAndQueryEngineFactory;
import org.swrlapi.factory.SWRLAPIFactory;
import org.swrlapi.sqwrl.SQWRLQueryEngine;
import org.swrlapi.ui.dialog.SWRLRuleEngineDialogManager;
import org.swrlapi.ui.model.FileBackedOntologyModel;
import org.swrlapi.ui.model.FileBackedSQWRLQueryEngineModel;
import org.swrlapi.ui.model.SWRLRulesAndSQWRLQueriesTableModel;
import org.swrlapi.ui.view.queries.SQWRLQueriesView;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.io.File;
import java.util.Optional;
import java.util.Set;

public class MySWRLRulesToolsTest {


    public static void main(String[] args) {

        Optional<@NonNull File> owlFile =
                Optional.of(new File("/home/perhaps/Documents/data/yyp/YP/YPtest.owl"));
        try {
            OWLOntologyManager owlOntologyManager= OWLManager.createOWLOntologyManager();
            OWLOntology ontology = owlFile.isPresent()?
                    owlOntologyManager.loadOntologyFromOntologyDocument(owlFile.get()):
                    owlOntologyManager.createOntology();

            SQWRLQueryEngine queryEngine= SWRLAPIFactory.createSQWRLQueryEngine(ontology);


            SWRLRuleRenderer swrlRuleRenderer = queryEngine.createSWRLRuleRenderer();

            for (SWRLAPIRule swrlapiRule:queryEngine.getSWRLRules()) {
                System.out.println(swrlapiRule.getRuleName() + swrlRuleRenderer.renderSWRLRule(swrlapiRule) + " " + swrlapiRule.getComment());
            }

            FileBackedSQWRLQueryEngineModel queryEngineModel = SWRLAPIFactory.createFileBackedSQWRLQueryEngineModel(queryEngine,owlFile);

            SWRLRulesAndSQWRLQueriesTableModel swrlRulesAndSQWRLQueriesTableModel
                    = queryEngineModel.getSWRLRulesTableModel();

            swrlRulesAndSQWRLQueriesTableModel.updateView();

            System.out.println(swrlRulesAndSQWRLQueriesTableModel);

            @NonNull Set<SWRLRulesAndSQWRLQueriesTableModel.@NonNull SWRLRuleModel> sss = swrlRulesAndSQWRLQueriesTableModel.getSWRLRuleModels();

            for(SWRLRulesAndSQWRLQueriesTableModel.@NonNull SWRLRuleModel ss:sss)
                System.out.println(ss);
//            MySWRLRulesTools mySWRLRulesTools=new MySWRLRulesTools(ruleEngine);
//
//
//
//            Set<MySWRLRulesTools.SWRLRuleModel> rules=mySWRLRulesTools.getSWRLRuleModels();
//            int i=1;
//            for(MySWRLRulesTools.SWRLRuleModel swrlRuleModel:rules)
//                System.out.println(i++);

        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
    }

}
