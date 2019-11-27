package org.swrlapi.ui.view.rules;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.swrlapi.core.SWRLRuleEngine;
import org.swrlapi.exceptions.SWRLRuleEngineException;
import org.swrlapi.ui.model.SWRLRuleEngineModel;
import org.swrlapi.ui.view.SWRLAPIView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SWRLRulesControlView extends JPanel implements SWRLAPIView
{
  private static final long serialVersionUID = 1L;

  private static final int VIEW_PREFERRED_WIDTH = 900;
  private static final int VIEW_PREFERRED_HEIGHT = 300;
  private static final int BUTTON_PREFERRED_WIDTH = 160;
  private static final int BUTTON_PREFERRED_HEIGHT = 30;
  private static final int CONSOLE_ROWS = 10;
  private static final int CONSOLE_COLUMNS = 80;

  @NonNull private final SWRLRuleEngineModel swrlRuleEngineModel;

  public SWRLRulesControlView(@NonNull SWRLRuleEngineModel swrlRuleEngineModel)
  {
    this.swrlRuleEngineModel = swrlRuleEngineModel;
  }

  @Override public void initialize()
  {
    String ruleEngineName = this.swrlRuleEngineModel.getSWRLRuleEngine().getRuleEngineName();
    String ruleEngineVersion = this.swrlRuleEngineModel.getSWRLRuleEngine().getRuleEngineVersion();

    JTextArea console = createConsole();
    JScrollPane scrollPane = new JScrollPane(console);
    scrollPane.setPreferredSize(new Dimension(VIEW_PREFERRED_WIDTH, VIEW_PREFERRED_HEIGHT));
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, scrollPane);

    JPanel buttonsPanel = new JPanel(new FlowLayout());
    JButton button = createButton("OWL+SWRL->" + ruleEngineName,
      "把SWRL规则及OWL本体支持加载进推理引擎",
      new ImportActionListener(this.swrlRuleEngineModel, console, this));
    buttonsPanel.add(button);
    button = createButton("进行推理分类任务" + ruleEngineName, "运行推理引擎",
      new RunActionListener(this.swrlRuleEngineModel, console, this));
    buttonsPanel.add(button);
    button = createButton(ruleEngineName + "->OWL", "把推理结果转换到OWL本体中",
      new ExportActionListener(this.swrlRuleEngineModel, console, this));
    buttonsPanel.add(button);
    add(BorderLayout.SOUTH, buttonsPanel);

    console.append("正在使用 " + ruleEngineName + " 规则推理引擎.\n\n");
    console.append("点击 'OWL+SWRL->" + ruleEngineName
      + "' 按钮 to transfer SWRL rules and relevant OWL knowledge to the rule engine.\n");
    console.append("Press the 'Run " + ruleEngineName + "' button to run the rule engine.\n");
    console.append("Press the '" + ruleEngineName
      + "->OWL' button to transfer the inferred rule engine knowledge to OWL knowledge.\n\n");
    console.append(
      "The SWRLAPI supports an OWL profile called OWL 2 RL and uses an OWL 2 RL-based reasoner to perform reasoning.\n");
    console.append("See the 'OWL 2 RL' sub-tab for more information on this reasoner.");
  }

  @Override public void update()
  {
    validate();
  }

  @NonNull private JButton createButton(@NonNull String text, @NonNull String toolTipText,
    @NonNull ActionListener listener)
  {
    JButton button = new JButton(text);

    button.setToolTipText(toolTipText);
    button.setPreferredSize(new Dimension(BUTTON_PREFERRED_WIDTH, BUTTON_PREFERRED_HEIGHT));
    button.addActionListener(listener);

    return button;
  }

  @NonNull private JTextArea createConsole()
  {
    JTextArea textArea = new JTextArea(CONSOLE_ROWS, CONSOLE_COLUMNS);

    textArea.setLineWrap(true);
    textArea.setBackground(Color.WHITE);
    textArea.setEditable(false);

    return textArea;
  }

  @NonNull private SWRLRuleEngine getSWRLRuleEngine()
  {
    return this.swrlRuleEngineModel.getSWRLRuleEngine();
  }

  private class ListenerBase
  {
    @NonNull protected final SWRLRuleEngineModel ruleEngineModel;
    @NonNull protected final JTextArea console;
    @NonNull protected final SWRLRulesControlView controlPanel;

    public ListenerBase(@NonNull SWRLRuleEngineModel ruleEngineModel, @NonNull JTextArea console,
      @NonNull SWRLRulesControlView controlPanel)
    {
      this.ruleEngineModel = ruleEngineModel;
      this.console = console;
      this.controlPanel = controlPanel;
    }

    protected SWRLRuleEngine getSWRLRuleEngine() { return this.ruleEngineModel.getSWRLRuleEngine(); }

    protected void clearConsole()
    {
      this.console.setText("");
    }

    protected void appendToConsole(@NonNull String text)
    {
      this.console.append(text);
    }
  }

  private class ImportActionListener extends ListenerBase implements ActionListener
  {
    public ImportActionListener(@NonNull SWRLRuleEngineModel ruleEngineModel, @NonNull JTextArea console,
      @NonNull SWRLRulesControlView controlPanel)
    {
      super(ruleEngineModel, console, controlPanel);
    }

    @Override public void actionPerformed(@NonNull ActionEvent event)
    {
      try {
        long startTime = System.currentTimeMillis();
        getSWRLRuleEngine().importAssertedOWLAxioms();

        clearConsole();
        appendToConsole("OWL axioms successfully transferred to rule engine.\n");
        appendToConsole(
          "Number of SWRL rules exported to rule engine: " + getSWRLRuleEngine().getNumberOfImportedSWRLRules() + "\n");
        appendToConsole("Number of OWL class declarations exported to rule engine: " + getSWRLRuleEngine()
          .getNumberOfAssertedOWLClassDeclarationAxioms() + "\n");
        appendToConsole("Number of OWL individual declarations exported to rule engine: " + getSWRLRuleEngine()
          .getNumberOfAssertedOWLIndividualDeclarationsAxioms() + "\n");
        appendToConsole("Number of OWL object property declarations exported to rule engine: " + getSWRLRuleEngine()
          .getNumberOfAssertedOWLObjectPropertyDeclarationAxioms() + "\n");
        appendToConsole("Number of OWL data property declarations exported to rule engine: " + getSWRLRuleEngine()
          .getNumberOfAssertedOWLDataPropertyDeclarationAxioms() + "\n");
        appendToConsole(
          "Total number of OWL axioms exported to rule engine: " + getSWRLRuleEngine().getNumberOfAssertedOWLAxioms()
            + "\n");
        appendToConsole("The transfer took " + (System.currentTimeMillis() - startTime) + " millisecond(s).\n");
        appendToConsole("Press the 'Run " + SWRLRulesControlView.this.getSWRLRuleEngine().getRuleEngineName()
          + "' button to run the rule engine.\n");
      } catch (SWRLRuleEngineException e) {
        appendToConsole("Exception importing SWRL rules and OWL knowledge: " + e.toString() + "\n");
      }
      this.controlPanel.getParent().validate();
    }
  }

  private class RunActionListener extends ListenerBase implements ActionListener
  {
    public RunActionListener(@NonNull SWRLRuleEngineModel ruleEngineModel, @NonNull JTextArea textArea,
      @NonNull SWRLRulesControlView controlPanel)
    {
      super(ruleEngineModel, textArea, controlPanel);
    }

    @Override public void actionPerformed(@NonNull ActionEvent event)
    {
      displayRunResults();
    }

    private void displayRunResults()
    {
      try {
        long startTime = System.currentTimeMillis();
        getSWRLRuleEngine().run();

        appendToConsole("Successful execution of rule engine.\n");
        appendToConsole("Number of inferred axioms: " + getSWRLRuleEngine().getNumberOfInferredOWLAxioms() + "\n");
        if (getSWRLRuleEngine().getNumberOfInjectedOWLAxioms() != 0)
          appendToConsole(
            "Number of axioms injected by built-ins: " + getSWRLRuleEngine().getNumberOfInjectedOWLAxioms() + "\n");
        appendToConsole("The process took " + (System.currentTimeMillis() - startTime) + " millisecond(s).\n");
        appendToConsole("Look at the 'Inferred Axioms' tab to see the inferred axioms.\n");
        appendToConsole("Press the '" + SWRLRulesControlView.this.getSWRLRuleEngine().getRuleEngineName()
          + "->OWL' button to translate the inferred axioms to OWL knowledge.\n");
      } catch (Exception e) {
        String errorMessage = buildChainedErrorMessage(e);
        appendToConsole("Exception running rule engine: " + errorMessage + "\n");
      }
      this.controlPanel.getParent().validate();
    }
  }

  @NonNull private String buildChainedErrorMessage(Throwable t)
  {
    String message = t.getMessage() != null ? t.getMessage() : "";

    Throwable currentThrowable = t;
    while (currentThrowable != null) {
      Throwable cause = currentThrowable.getCause();
      if (cause != null && cause.getMessage() != null)
        message += ": " + cause.getMessage();

      currentThrowable = cause;
    }
    return message;
  }

  private class ExportActionListener extends ListenerBase implements ActionListener
  {
    public ExportActionListener(@NonNull SWRLRuleEngineModel ruleEngineModel, @NonNull JTextArea textArea,
      @NonNull SWRLRulesControlView controlPanel)
    {
      super(ruleEngineModel, textArea, controlPanel);
    }

    @Override public void actionPerformed(@NonNull ActionEvent event)
    {
      displayExportResults();
    }

    private void displayExportResults()
    {
      try {
        long startTime = System.currentTimeMillis();
        getSWRLRuleEngine().exportInferredOWLAxioms();

        appendToConsole("Successfully transferred inferred axioms to OWL model.\n");
        appendToConsole("The process took " + (System.currentTimeMillis() - startTime) + " millisecond(s).\n");
      } catch (SWRLRuleEngineException e) {
        appendToConsole("Exception exporting knowledge to OWL: " + e.toString() + "\n");
      }
      this.controlPanel.getParent().validate();
    }
  }
}
