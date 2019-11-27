package org.swrlapi.ui.view.queries;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.swrlapi.sqwrl.SQWRLQueryEngine;
import org.swrlapi.sqwrl.SQWRLResult;
import org.swrlapi.sqwrl.exceptions.SQWRLException;
import org.swrlapi.sqwrl.exceptions.SQWRLInvalidQueryNameException;
import org.swrlapi.ui.model.SQWRLQueryEngineModel;
import org.swrlapi.ui.view.SWRLAPIView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @see org.swrlapi.sqwrl.SQWRLQueryEngine
 */
public class SQWRLQueryControlView extends JPanel implements SWRLAPIView
{
  private static final long serialVersionUID = 1L;

  private static final int VIEW_PREFERRED_WIDTH = 900;
  private static final int VIEW_PREFERRED_HEIGHT = 300;
  private static final int TOOLTIP_PREFERRED_WIDTH = 160;
  private static final int TOOLTIP_PREFERRED_HEIGHT = 30;
  private static final int CONSOLE_ROWS = 10;
  private static final int CONSOLE_COLUMNS = 80;
  private static final int MAXIMUM_OPEN_RESULT_VIEWS = 12;

  @NonNull private final SQWRLQueryEngineModel queryEngineModel;
  @NonNull private final SQWRLQuerySelector sqwrlQuerySelector;
  @NonNull private final JTextArea console;
  @NonNull private final JScrollPane consoleScrollPane;
  @NonNull private final Map<@NonNull String, SQWRLResultView> sqwrlResultViews = new HashMap<>();

  public SQWRLQueryControlView(@NonNull SQWRLQueryEngineModel queryEngineModel,
    @NonNull SQWRLQuerySelector sqwrlQuerySelector)
  {
    this.queryEngineModel = queryEngineModel;
    this.sqwrlQuerySelector = sqwrlQuerySelector;
    this.console = new JTextArea(CONSOLE_ROWS, CONSOLE_COLUMNS);
    this.consoleScrollPane = new JScrollPane(this.console);
  }

  @Override public void initialize()
  {
    setLayout(new BorderLayout());
    console.setLineWrap(true);
    console.setBackground(Color.WHITE);
    console.setEditable(false);
    consoleScrollPane.setPreferredSize(new Dimension(VIEW_PREFERRED_WIDTH, VIEW_PREFERRED_HEIGHT));
    add(BorderLayout.CENTER, consoleScrollPane);

    JPanel controlPanel = new JPanel(new FlowLayout());
    JButton runSQWRLQueryButton = createButton("运行", "Run a SQWRL query",
      new RunSQWRLQueryActionListener(this.console, this));
    controlPanel.add(runSQWRLQueryButton);
    add(BorderLayout.SOUTH, controlPanel);

    console.append("从上面的列表中选择一个SQWRL查询，然后按“运行”按钮.\n");
    console.append("如果所选查询生成结果，则该结果将显示在新的子选项卡中.\n\n");
    //console.append(
     // "The SWRLAPI supports an OWL profile called OWL 2 RL and uses an OWL 2 RL-based reasoner to perform querying.\n");
    //console.append("See the 'OWL 2 RL' subtab for more information on this reasoner.\n\n");
    //console.append("Executing queries in this tab does not modify the ontology.\n\n");
    console
      .append("正在使用 " + this.queryEngineModel.getSQWRLQueryEngine().getRuleEngineName() + " 进行推理查询请求.\n\n");
  }

  @Override public void update()
  {
    validate();
  }

  public void appendToConsole(@NonNull String text)
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        console.append(text);
        console.setCaretPosition(console.getDocument().getLength());
      }
    });
  }

  public void removeSQWRLResultView(@NonNull String queryName)
  {
    if (this.sqwrlResultViews.containsKey(queryName)) {
      SQWRLResultView sqwrlResultView = this.sqwrlResultViews.get(queryName);
      this.sqwrlResultViews.remove(queryName);
      getParent().remove(sqwrlResultView);
      ((JTabbedPane)getParent()).setSelectedIndex(0);
    }
  }

  public void removeAllSQWRLResultViews()
  {
    for (SQWRLResultView sqwrlResultView : this.sqwrlResultViews.values())
      getParent().remove(sqwrlResultView);
    this.sqwrlResultViews.clear();
  }

  @NonNull private JButton createButton(@NonNull String text, @NonNull String toolTipText,
    @NonNull ActionListener listener)
  {
    JButton button = new JButton(text);

    button.setToolTipText(toolTipText);
    button.setPreferredSize(new Dimension(TOOLTIP_PREFERRED_WIDTH, TOOLTIP_PREFERRED_HEIGHT));
    button.addActionListener(listener);

    return button;
  }

  @NonNull private SQWRLQueryEngine getSQWRLQueryEngine()
  {
    return this.queryEngineModel.getSQWRLQueryEngine();
  }

  private class ListenerBase
  {
    @NonNull protected final SQWRLQueryControlView sqwrlQueryControlView;
    @NonNull protected final JTextArea console;

    public ListenerBase(@NonNull JTextArea console, @NonNull SQWRLQueryControlView sqwrlQueryControlView)
    {
      this.console = console;
      this.sqwrlQueryControlView = sqwrlQueryControlView;
    }
  }

  private class RunSQWRLQueryActionListener extends ListenerBase implements ActionListener
  {
    public RunSQWRLQueryActionListener(@NonNull JTextArea console, @NonNull SQWRLQueryControlView sqwrlQueryControlView)
    {
      super(console, sqwrlQueryControlView);
    }

    @Override public void actionPerformed(@NonNull ActionEvent event)
    {
      runSQWRLQuery();
    }

    private void runSQWRLQuery()
    {
      Optional<@NonNull String> queryName = Optional.empty();

      if (SQWRLQueryControlView.this.sqwrlResultViews.size() == SQWRLQueryControlView.MAXIMUM_OPEN_RESULT_VIEWS) {
        appendToConsole(
          "最多" + SQWRLQueryControlView.MAXIMUM_OPEN_RESULT_VIEWS + " 个推理查询结果窗口能被同时展示 ");
        appendToConsole("请关闭一个窗口来进行这次推理查询任务.\n");
      } else {
        try {
          SQWRLQuerySelector querySelector = SQWRLQueryControlView.this.sqwrlQuerySelector;

          if (querySelector == null) {
            appendToConsole("错误! 没有查询任务被选中，请选择一个查询任务\n");
          } else {
            queryName = SQWRLQueryControlView.this.sqwrlQuerySelector.getSelectedQueryName();

            if (queryName.isPresent()) {
              long startTime = System.currentTimeMillis();
              SQWRLResult sqwrlResult = SQWRLQueryControlView.this.getSQWRLQueryEngine().runSQWRLQuery(queryName.get());

              if (sqwrlResult == null || sqwrlResult.getNumberOfRows() == 0)
                indicateEmptySQWRLResult(queryName.get());
              else
                displaySQWRLResult(queryName.get(), sqwrlResult, startTime);
            } else
              appendToConsole("没有选中推理查询任务！\n");
          }
        } catch (SQWRLInvalidQueryNameException e) {
          if (queryName.isPresent())
            appendToConsole(queryName.get() + " 不是一个有效的SQWRL推理查询任务.\n");
          else
            appendToConsole("没有选中SQWRL规则.\n");
        } catch (SQWRLException | RuntimeException e) {
          if (queryName.isPresent())
            appendToConsole(
              "发生异常，运行推理查询任务:" + queryName.get() + ": " + buildChainedErrorMessage(e) + "\n");
          else
            appendToConsole("发生异常，运行推理查询任务: " + buildChainedErrorMessage(e) + "\n");
        }
      }
    }

    private void indicateEmptySQWRLResult(@NonNull String queryName)
    {
      appendToConsole("SQWRL推理查询任务 " + queryName + " 并未出现任何与规则库中规则的冲突！ \n");

      if (SQWRLQueryControlView.this.sqwrlResultViews.containsKey(queryName)) {
        SQWRLResultView queryResultsView = SQWRLQueryControlView.this.sqwrlResultViews.get(queryName);
        SQWRLQueryControlView.this.sqwrlResultViews.remove(queryName);
        getParent().remove(queryResultsView);
      }
    }

    private void displaySQWRLResult(@NonNull String queryName, @NonNull SQWRLResult sqwrlResult, long startTime)
      throws SQWRLException
    {
      appendToConsole("请看 " + queryName + " 窗口来复现这个SQWRL推理查询结果.\n");
      appendToConsole("这次推理查询共耗时 " + (System.currentTimeMillis() - startTime) + " milliseconds. ");

      if (sqwrlResult.getNumberOfRows() == 1)
        appendToConsole("1 行推理查询结果已被返回在 "+queryName+" 窗口中.\n");
      else
        appendToConsole("" + sqwrlResult.getNumberOfRows() + " 行推理查询结果已被返回在 "+ queryName+" 窗口中.\n");

      SQWRLResultView sqwrlResultView;

      if (SQWRLQueryControlView.this.sqwrlResultViews.containsKey(queryName)) // Existing result tab found
        sqwrlResultView = SQWRLQueryControlView.this.sqwrlResultViews.get(queryName);
      else { // Create new result tab
        sqwrlResultView = new SQWRLResultView(SQWRLQueryControlView.this.queryEngineModel, queryName, sqwrlResult,
          this.sqwrlQueryControlView);
        sqwrlResultView.initialize();
        SQWRLQueryControlView.this.sqwrlResultViews.put(queryName, sqwrlResultView);
        ((JTabbedPane)getParent())
          .addTab(queryName, null, sqwrlResultView, "'" + queryName + "'的推理查询结果");
      }

      sqwrlResultView.validate();
      this.sqwrlQueryControlView.getParent().validate();
      this.console.validate();
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
}
