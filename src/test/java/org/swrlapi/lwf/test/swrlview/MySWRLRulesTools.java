package org.swrlapi.lwf.test.swrlview;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.dataflow.qual.SideEffectFree;
import org.swrlapi.core.SWRLAPIRule;
import org.swrlapi.core.SWRLRuleEngine;
import org.swrlapi.core.SWRLRuleRenderer;
import org.swrlapi.ui.model.FileBackedSQWRLQueryEngineModel;
import org.swrlapi.ui.model.SWRLRulesAndSQWRLQueriesTableModel;
import org.swrlapi.ui.view.SWRLAPIView;
import org.swrlapi.ui.view.rules.SWRLRulesView;

import java.util.*;

public class MySWRLRulesTools {

    public enum ContentMode
    {
        RuleContentOnly, QueryContentOnly, RuleAndQueryContent
    }


    @NonNull private SWRLRuleEngine swrlRuleEngine;
    @NonNull private SWRLRuleRenderer swrlRuleRenderer;
    @NonNull private final SortedMap<@NonNull String, @NonNull SWRLRuleModel> swrlRuleModels; // rule name -> SWRLRuleModel
    @NonNull private Optional<@NonNull SWRLAPIView> view = Optional.<@NonNull SWRLAPIView>empty();
    private ContentMode contentMode;
    private boolean isModified;

    public MySWRLRulesTools(SWRLRuleEngine swrlRuleEngine){
        this.swrlRuleEngine = (SWRLRuleEngine) swrlRuleEngine;
        this.swrlRuleRenderer = this.swrlRuleEngine.createSWRLRuleRenderer();
        this.swrlRuleModels = new TreeMap<>();
        this.isModified = false;
        this.contentMode =ContentMode.RuleContentOnly;
    }


    private void updateRuleModels()
    {
        this.swrlRuleModels.clear();

        for (SWRLAPIRule swrlapiRule : this.swrlRuleEngine.getSWRLRules()) {
            String ruleName = swrlapiRule.getRuleName();
            SWRLRuleModel swrlRuleModel = new SWRLRuleModel(swrlapiRule);
            this.swrlRuleModels.put(ruleName, swrlRuleModel);
        }
    }
    public void updateView()
    {
        if (this.view.isPresent()) {
            updateRuleModels();
            this.view.get().update();
        }
    }
    public void setContentMode(ContentMode contentMode)
    {
        this.contentMode = contentMode;
        updateView();
    }
    public void updateModel(@NonNull SWRLRuleEngine swrlRuleEngine)
    {
        this.swrlRuleEngine = swrlRuleEngine;
        this.swrlRuleRenderer = this.swrlRuleEngine.createSWRLRuleRenderer();
        this.swrlRuleModels.clear();
        this.isModified = false;

        updateView();
    }
    @NonNull public Set<@NonNull SWRLRuleModel> getSWRLRuleModels()
    {
        return new HashSet<>(this.swrlRuleModels.values());
    }

    @NonNull public Set<@NonNull SWRLRuleModel> getSWRLRuleModels(boolean isActiveFlag)
    {
        Set<@NonNull SWRLRuleModel> swrlRuleModels = new HashSet<>();
        for (SWRLRuleModel swrlRuleModel : this.swrlRuleModels.values()) {
            if (swrlRuleModel.isActive() == isActiveFlag)
                swrlRuleModels.add(swrlRuleModel);
        }
        return swrlRuleModels;
    }

    public boolean hasSWRLRules()
    {
        return !this.swrlRuleModels.isEmpty();
    }

    @NonNull public String getSWRLRuleNameByIndex(int ruleIndex)
    {
        Optional<@NonNull SWRLRuleModel> swrlRuleModel = getSWRLRuleModelByIndex(ruleIndex);

        if (swrlRuleModel.isPresent())
            return swrlRuleModel.get().getRuleName();
        else
            return "<INVALID_INDEX>";
    }

    @NonNull public String getSWRLRuleTextByIndex(int ruleIndex)
    {
        Optional<@NonNull SWRLRuleModel> swrlRuleModel = getSWRLRuleModelByIndex(ruleIndex);

        if (swrlRuleModel.isPresent())
            return swrlRuleModel.get().getRuleText();
        else
            return "<INVALID_INDEX>";
    }

    @NonNull public String getSWRLRuleCommentByIndex(int ruleIndex)
    {
        Optional<@NonNull SWRLRuleModel> swrlRuleModel = getSWRLRuleModelByIndex(ruleIndex);

        if (swrlRuleModel.isPresent())
            return swrlRuleModel.get().getComment();
        else
            return "<INVALID_INDEX>";
    }
    @NonNull private SWRLRuleRenderer getSWRLRuleRenderer() { return this.swrlRuleRenderer; }

    @NonNull private Optional<@NonNull SWRLRuleModel> getSWRLRuleModelByIndex(int ruleIndex)
    {
        if (ruleIndex >= 0 && ruleIndex < this.swrlRuleModels.values().size())
            return Optional.of(((SWRLRuleModel)this.swrlRuleModels.values().toArray()[ruleIndex]));
        else
            return Optional.empty();
    }


    @Override public String toString()
    {
        return "SWRLRulesAndSQWRLQueriesTableModel{" +
                "swrlRuleEngine=" + swrlRuleEngine +
                ", swrlRuleModels=" + swrlRuleModels +
                ", view=" + view +
                ", contentMode=" + contentMode +
                ", isModified=" + isModified +
                '}';
    }
    public class SWRLRuleModel
    {
        @NonNull private final SWRLAPIRule rule;

        public SWRLRuleModel(@NonNull SWRLAPIRule rule)
        {
            this.rule = rule;
        }

        public void setActive(boolean active)
        {
            this.rule.setActive(active);
        }

        public boolean isActive()
        {
            return this.rule.isActive();
        }

        public boolean isSQWRLQuery()
        {
            return this.rule.isSQWRLQuery();
        }

        @NonNull public String getRuleText()
        {
            return getSWRLRuleRenderer().renderSWRLRule(this.rule);
        }

        @NonNull public String getRuleName()
        {
            return this.rule.getRuleName();
        }

        @NonNull public String getComment()
        {
            return this.rule.getComment();
        }

        @NonNull @SideEffectFree
        @Override public String toString()
        {
            return "(ruleName: " + getRuleName() + ", ruleText: " + getRuleText() + ", comment: " + getComment()
                    + ", active: " + isActive() + ")";
        }
    }
}
