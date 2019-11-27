package org.swrlapi.ui.model;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.dataflow.qual.SideEffectFree;
import org.swrlapi.core.SWRLAPIRule;
import org.swrlapi.core.SWRLRuleEngine;
import org.swrlapi.core.SWRLRuleRenderer;
import org.swrlapi.ui.view.SWRLAPIView;

import javax.swing.table.AbstractTableModel;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * This class models a list of SWRL rules and SQWRL queries in an ontology for tabular display.
 *
 * @see org.swrlapi.ui.view.SWRLRulesTableView
 * @see org.swrlapi.ui.model.SWRLRuleEngineModel
 * @see org.swrlapi.core.SWRLAPIRule
 * @see org.swrlapi.sqwrl.SQWRLQuery
 */
public class SWRLRulesAndSQWRLQueriesTableModel extends AbstractTableModel implements SWRLAPIModel
{
  private static final long serialVersionUID = 1L;

  public enum ContentMode
  {
    RuleContentOnly, QueryContentOnly, RuleAndQueryContent
  }

  private static final String RULE_NAME_COLUMN_TITLE = "名称";
  private static final String RULE_TEXT_COLUMN_TITLE = "规则";
  private static final String QUERY_TEXT_COLUMN_TITLE = "查询";
  private static final String RULE_AND_QUERY_TEXT_COLUMN_TITLE = "主体";
  private static final String RULE_COMMENT_COLUMN_TITLE = "注释";

  @NonNull private SWRLRuleEngine swrlRuleEngine;
  @NonNull private SWRLRuleRenderer swrlRuleRenderer;
  @NonNull private final SortedMap<@NonNull String, @NonNull SWRLRuleModel> swrlRuleModels; // rule name -> SWRLRuleModel
  @NonNull private Optional<@NonNull SWRLAPIView> view = Optional.<@NonNull SWRLAPIView>empty();
  private ContentMode contentMode;

  private boolean isModified;

  public SWRLRulesAndSQWRLQueriesTableModel(@NonNull SWRLRuleEngine swrlRuleEngine)
  {
    this.swrlRuleEngine = swrlRuleEngine;
    this.swrlRuleRenderer = this.swrlRuleEngine.createSWRLRuleRenderer();
    this.swrlRuleModels = new TreeMap<>();
    this.isModified = false;
    this.contentMode = ContentMode.RuleContentOnly;
  }

  public void setView(@NonNull SWRLAPIView view)
  {
    this.view = Optional.of(view);
    updateRuleModels();
  }

  public void updateModel(@NonNull SWRLRuleEngine swrlRuleEngine)
  {
    this.swrlRuleEngine = swrlRuleEngine;
    this.swrlRuleRenderer = this.swrlRuleEngine.createSWRLRuleRenderer();
    this.swrlRuleModels.clear();
    this.isModified = false;

    updateView();
  }

  public void setContentMode(ContentMode contentMode)
  {
    this.contentMode = contentMode;
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

  public boolean hasSWRLRule(@NonNull String ruleName)
  {
    return this.swrlRuleModels.containsKey(ruleName);
  }

  public boolean hasBeenModified()
  {
    return this.isModified;
  }

  public void clearModifiedStatus()
  {
    this.isModified = false;
  }

  public int getNumberOfColumns()
  {
    return this.contentMode == ContentMode.QueryContentOnly ? 3 : 4;
  }

  public boolean hasRuleActiveColumn()
  {
    return this.contentMode != ContentMode.QueryContentOnly;
  }

  public int getRuleActiveColumnNumber()
  {
    return this.contentMode == ContentMode.QueryContentOnly ? -1 : 0;
  }

  public int getRuleNameColumnNumber()
  {
    return this.contentMode == ContentMode.QueryContentOnly ? 0 : 1;
  }

  public int getRuleTextColumnNumber()
  {
    return this.contentMode == ContentMode.QueryContentOnly ? 1 : 2;
  }

  public int getRuleCommentColumnNumber()
  {
    return this.contentMode == ContentMode.QueryContentOnly ? 2 : 3;
  }

  @Override public int getRowCount()
  {
    return this.swrlRuleModels.size();
  }

  @Override public int getColumnCount()
  {
    return getNumberOfColumns();
  }

  @NonNull @Override public String getColumnName(int column)
  {
    if (column == getRuleNameColumnNumber())
      return RULE_NAME_COLUMN_TITLE;
    else if (column == getRuleTextColumnNumber()) {
      switch (this.contentMode) {
      case RuleContentOnly:
        return RULE_TEXT_COLUMN_TITLE;
      case QueryContentOnly:
        return QUERY_TEXT_COLUMN_TITLE;
      case RuleAndQueryContent:
        return RULE_AND_QUERY_TEXT_COLUMN_TITLE;
      default:
        return "<INVALID>";
      }
    } else if (column == getRuleCommentColumnNumber())
      return RULE_COMMENT_COLUMN_TITLE;
    else if (column == getRuleActiveColumnNumber())
      return "";
    else
      return "";
  }

  @NonNull @Override public Object getValueAt(int row, int column)
  {
    if ((row < 0 || row >= getRowCount()) || ((column < 0 || column >= getColumnCount())))
      return "<OUT OF BOUNDS>";
    else {
      SWRLRuleModel swrlRuleModel = (SWRLRuleModel)this.swrlRuleModels.values().toArray()[row];
      if (column == getRuleTextColumnNumber())
        return swrlRuleModel.getRuleText();
      else if (column == getRuleNameColumnNumber())
        return swrlRuleModel.getRuleName();
      else if (column == getRuleCommentColumnNumber())
        return swrlRuleModel.getComment();
      else if (column == getRuleActiveColumnNumber())
        return swrlRuleModel.isActive() && !swrlRuleModel.isSQWRLQuery();
      return ">INVALID COLUMN>";
    }
  }

  @Override public boolean isCellEditable(int rowIndex, int columnIndex)
  {
    if (hasRuleActiveColumn() && columnIndex == getRuleActiveColumnNumber()) {
      SWRLRuleModel swrlRuleModel = (SWRLRuleModel)this.swrlRuleModels.values().toArray()[rowIndex];
      return !swrlRuleModel.isSQWRLQuery();
    } else
      return false;
  }

  @Override public Class<?> getColumnClass(int columnIndex)
  {
    if (columnIndex == getRuleActiveColumnNumber()) {
      return Boolean.class;
    } else {
      return super.getColumnClass(columnIndex);
    }
  }

  @Override public void setValueAt(Object aValue, int rowIndex, int columnIndex)
  {
    if (columnIndex == getRuleActiveColumnNumber()) {
      ((SWRLRuleModel)this.swrlRuleModels.values().toArray()[rowIndex]).setActive((Boolean)aValue);
    } else {
      super.setValueAt(aValue, rowIndex, columnIndex);
    }
  }

  @Override public void updateView()
  {
    if (this.view.isPresent()) {
      updateRuleModels();
      this.view.get().update();
    }
  }

  @NonNull private SWRLRuleRenderer getSWRLRuleRenderer() { return this.swrlRuleRenderer; }

  @NonNull private Optional<@NonNull SWRLRuleModel> getSWRLRuleModelByIndex(int ruleIndex)
  {
    if (ruleIndex >= 0 && ruleIndex < this.swrlRuleModels.values().size())
      return Optional.of(((SWRLRuleModel)this.swrlRuleModels.values().toArray()[ruleIndex]));
    else
      return Optional.<@NonNull SWRLRuleModel>empty();
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

     @NonNull @SideEffectFree @Override public String toString()
    {
      return "(ruleName: " + getRuleName() + ", ruleText: " + getRuleText() + ", comment: " + getComment()
        + ", active: " + isActive() + ")";
    }
  }
}
