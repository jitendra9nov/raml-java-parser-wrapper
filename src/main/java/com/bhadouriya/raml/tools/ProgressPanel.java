package com.bhadouriya.raml.tools;

import com.bhadouriya.raml.efficacies.Efficacy;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static java.awt.Font.*;
import static java.awt.GridBagConstraints.*;
import static java.io.File.separator;
import static org.apache.commons.lang.StringUtils.*;

public class ProgressPanel extends JFrame implements ActionListener {

    private static final Logger LOGGER = Logger.getLogger(ProgressPanel.class.getName());
    private static final String ALL = "All";
    private static final String CUSTOM = "Custom";
    private static final String VERSION = "Ver: 0.0.3";
    private static final String REGEX = "^[a-zA-Z0-9-+/=_]{16,64}";
    private static ProgressPanel instance;
    JButton extractBtn = new JButton();
    JButton cancelBtn = new JButton();
    JTextField apiUriTxt;
    JTextField corrIdTxt;
    JPanel statusPane;
    JRadioButton allBtn;
    JRadioButton customBtn;
    JCheckBox ramlCbx;
    JCheckBox exptCbx;
    JProgressBar progressBar;
    JPanel progressPane;
    boolean parent;
    private ProgressProcessor progressProcessor;
    private boolean isCustom;
    private boolean isRaml;
    private boolean isExpt;

    private ProgressPanel() {
        super("Progress Pane");
        this.initUI();
    }

    private ProgressPanel(final boolean parent) {
        this();
        this.parent = parent;
    }

    public static ProgressPanel getInstance(final boolean parent) {
        if (null == instance) {
            instance = new ProgressPanel(parent);
        }
        instance.setVisible(true);
        return instance;
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                getInstance(false);
            }
        });
    }

    private void initUI() {
        //setDefaultLookAndFeelDecorated(true);

        this.setIconImage(this.loadImage("cert.png"));
        //this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        final String title = this.getTitle();

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                final Object[] options = {"Yes", "No"};

                //Ask for confirmation before termination the program.
                final int option = JOptionPane.showOptionDialog(null, "Are you sure you want to close the '" + title + "' application?",
                        "Close Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);

                if (option == JOptionPane.YES_OPTION) {
                    if (ProgressPanel.this.parent) {
                        ProgressPanel.this.dispose();
                    } else {
                        System.exit(0);
                    }
                }
            }

        });

        this.statusPaneWithTimer();

        this.apiUriTxt = new JTextField(30) {

            @Override
            public JToolTip createToolTip() {
                JToolTip toolTip = super.createToolTip();
                toolTip.setBackground(Color.BLACK);
                toolTip.setForeground(new Color(255, 255, 51));
                toolTip.setFont(new Font(DIALOG, BOLD + ITALIC, 10));
                return toolTip;
            }
        };
        this.corrIdTxt = new JTextField(30) {

            @Override
            public JToolTip createToolTip() {
                JToolTip toolTip = super.createToolTip();
                toolTip.setBackground(Color.BLACK);
                toolTip.setForeground(new Color(255, 255, 51));
                toolTip.setFont(new Font(DIALOG, BOLD + ITALIC, 10));
                return toolTip;
            }
        };
        this.copyFromClipboard();

        this.createTabbedPane();
        this.setResizable(false);
        this.setSize(540, 210);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void statusPaneWithTimer() {
        //Create the Status area
        this.statusPane = new JPanel();
        this.statusPane.setPreferredSize(new Dimension(this.getWidth(), 15));
        this.statusPane.setLayout(new BoxLayout(this.statusPane, BoxLayout.LINE_AXIS));
        this.statusPane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        //create the progress area
        this.progressPane = new JPanel();
        this.progressPane.setPreferredSize(new Dimension(100, 14));
        this.progressPane.setLayout(new BoxLayout(this.progressPane, BoxLayout.LINE_AXIS));
        this.progressPane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        //Progress Bar
        this.progressBar = new JProgressBar(0, 1000);
        this.progressBar.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        this.progressBar.setStringPainted(false);
        this.progressBar.setIndeterminate(true);

        this.progressPane.add(this.progressBar);
        this.progressPane.add(new JLabel(" "));

        final JLabel label = new JLabel();
        final Font font = new Font("Arial", BOLD, 9);
        label.setFont(font);

        this.statusPane.add(label);

        final Timer timer = new Timer(1000, new ActionListener() {
            final SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");

            @Override
            public void actionPerformed(final ActionEvent e) {
                final String timeString = this.fmt.format(new Date());
                label.setText("    " + timeString + "    " + VERSION + "  ");
            }
        });
        timer.setInitialDelay(0);
        timer.start();

        this.statusPane.add(this.progressPane);

        this.add(this.statusPane, BorderLayout.PAGE_END);
        this.toggleButton(true);


    }


    private void createTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();
        ImageIcon icon = null;

        JComponent importCert = this.makeTextPane("Extricator", this.apiUriTxt, this.corrIdTxt);
        tabbedPane.addTab("                                                    Extricate Logs                                                    ", icon, importCert, "Extract All Logs");
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

        tabbedPane.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                switch (tabbedPane.getSelectedIndex()) {
                    case 0:
                        ProgressPanel.this.apiUriTxt.grabFocus();
                        //ProgressPanel.this.copyFromClipboard();
                        break;

                    default:
                        break;
                }
            }

            @Override
            public void focusLost(FocusEvent e) {

            }
        });
        //Add tabbed pane o this panel
        this.add(tabbedPane, BorderLayout.CENTER);

        //The following line enables to use scrolling tabs.
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

    }

    private void copyFromClipboard() {
        try {
            String clipboardText = this.getClipboardContents();
            if (isNotBlank(clipboardText) && isBlank(this.apiUriTxt.getText()) && this.isValidUrl(clipboardText)) {
                this.apiUriTxt.setText(clipboardText);
            } else if (isNotBlank(clipboardText) && isBlank(this.corrIdTxt.getText()) && Pattern.compile(REGEX).matcher(clipboardText).find()) {
                this.corrIdTxt.setText(clipboardText);
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Instance", e);
        }
    }

    private Clipboard getSystemClipboard() {
        return Toolkit.getDefaultToolkit().getSystemClipboard();

    }

    private String getClipboardContents() throws IOException, UnsupportedFlavorException {
        Clipboard systemClipboard = this.getSystemClipboard();
        DataFlavor dataFlavor = DataFlavor.stringFlavor;

        if (systemClipboard.isDataFlavorAvailable(dataFlavor)) {
            Object text = systemClipboard.getData(dataFlavor);
            return (String) text;
        }
        return null;
    }


    protected JComponent makeTextPane(String text, JTextField apiUriTxt, final JTextField corrIdTxt) {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints = new GridBagConstraints();

        this.addListners(text, this.extractBtn, apiUriTxt);
        this.addListners("Correlation-Id", this.extractBtn, corrIdTxt);

        this.extractBtn.setText("Extricate");
        this.extractBtn.setMargin(new Insets(1, 1, 1, 1));
        this.extractBtn.setFont(new Font(DIALOG, BOLD, 12));
        this.extractBtn.setToolTipText(text + " Logs");
        this.extractBtn.addActionListener(this);

        KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_MASK);
        InputMap inputMap = this.extractBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = this.extractBtn.getActionMap();

        inputMap.put(keyStroke, keyStroke.toString());

        actionMap.put(keyStroke.toString(), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ProgressPanel.this.extractBtn.doClick();
            }
        });


        //Stop
        this.cancelBtn.setText("Cancel");
        this.cancelBtn.setForeground(Color.RED);
        this.cancelBtn.setMargin(new Insets(1, 1, 1, 1));
        this.cancelBtn.setFont(new Font(DIALOG, BOLD, 12));
        this.cancelBtn.setToolTipText("Cancel");
        this.cancelBtn.addActionListener(this);

        KeyStroke keyStrokeCnl = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_MASK);
        InputMap inputMapCnl = this.cancelBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMapCnl = this.cancelBtn.getActionMap();
        inputMapCnl.put(keyStrokeCnl, keyStrokeCnl.toString());

        actionMapCnl.put(keyStroke.toString(), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ProgressPanel.this.cancelBtn.doClick();
            }
        });


        JLabel lblUrl = new JLabel("URL:");
        lblUrl.setFont(new Font(DIALOG, BOLD, 12));
        lblUrl.setLabelFor(apiUriTxt);

        JLabel lblId = new JLabel("Correlation-Id:");
        lblId.setFont(new Font(DIALOG, BOLD, 12));
        lblId.setLabelFor(corrIdTxt);


        JPanel importPanel = new JPanel();
        importPanel.setLayout(new GridBagLayout());

        GridBagConstraints bagConstraints = new GridBagConstraints();

        bagConstraints.anchor = NORTHWEST;
        bagConstraints.fill = HORIZONTAL;
        bagConstraints.insets = new Insets(1, 1, 1, 1);
        bagConstraints.ipadx = 1;
        bagConstraints.ipady = 1;

        bagConstraints.gridx = 0;
        bagConstraints.gridy = 0;
        bagConstraints.weightx = 35.0;
        importPanel.add(lblUrl, bagConstraints);

        bagConstraints.gridx = 1;
        bagConstraints.gridy = 0;
        bagConstraints.weightx = 25.0;
        bagConstraints.gridwidth = REMAINDER;
        importPanel.add(apiUriTxt, bagConstraints);

        bagConstraints.gridx = 0;
        bagConstraints.gridy = 1;
        bagConstraints.weightx = 35.0;
        importPanel.add(lblId, bagConstraints);

        bagConstraints.gridx = 1;
        bagConstraints.gridy = 1;
        bagConstraints.weightx = 25.0;
        bagConstraints.gridwidth = REMAINDER;
        importPanel.add(corrIdTxt, bagConstraints);


        final JPanel confPane = this.getConfigPane();
        bagConstraints.gridx = 1;
        bagConstraints.gridy = 2;
        bagConstraints.fill = NONE;
        bagConstraints.anchor = LAST_LINE_START;
        importPanel.add(confPane, bagConstraints);

        bagConstraints.gridx = 2;
        bagConstraints.gridy = 3;
        bagConstraints.fill = NONE;
        bagConstraints.anchor = LAST_LINE_START;
        importPanel.add(this.extractBtn, bagConstraints);

        bagConstraints.gridx = 3;
        bagConstraints.gridy = 3;
        bagConstraints.fill = NONE;
        bagConstraints.anchor = LAST_LINE_START;
        importPanel.add(this.cancelBtn, bagConstraints);

        importPanel.setBorder(new TitledBorder(null, "Details", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION,
                new Font(DIALOG, PLAIN, 12), null));


        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.anchor = NORTHWEST;
        gridBagConstraints.fill = HORIZONTAL;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        mainPanel.add(importPanel, gridBagConstraints);

        return mainPanel;

    }

    private JPanel getConfigPane() {
        final JPanel configPane = new JPanel();

        configPane.setLayout(new GridBagLayout());

        this.allBtn = new JRadioButton(ALL);
        this.allBtn.setMnemonic(KeyEvent.VK_A);
        this.allBtn.setSelected(true);
        this.allBtn.addActionListener(this);

        this.customBtn = new JRadioButton(CUSTOM);
        this.customBtn.setMnemonic(KeyEvent.VK_C);
        this.customBtn.setSelected(false);
        this.customBtn.addActionListener(this);

//Group radio

        final ButtonGroup group = new ButtonGroup();
        group.add(this.allBtn);
        group.add(this.customBtn);


        this.ramlCbx = new JCheckBox("Raml");
        this.ramlCbx.setMnemonic(KeyEvent.VK_R);
        this.ramlCbx.setSelected(false);
        this.ramlCbx.addActionListener(this);


        this.exptCbx = new JCheckBox("Exception");
        this.exptCbx.setMnemonic(KeyEvent.VK_E);
        this.exptCbx.setSelected(false);
        this.exptCbx.addActionListener(this);

        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = HORIZONTAL;
        constraints.insets = new Insets(1, 1, 1, 1);

        constraints.gridx = 0;
        constraints.gridy = 0;
        configPane.add(this.allBtn, constraints);

        constraints.gridx = 1;
        constraints.gridy = 0;
        configPane.add(this.customBtn, constraints);

        constraints.gridx = 2;
        constraints.gridy = 0;
        configPane.add(this.ramlCbx, constraints);

        constraints.gridx = 3;
        constraints.gridy = 0;
        configPane.add(this.exptCbx, constraints);

        return configPane;
    }

    private void addListners(final String text, final JButton btn, final JTextField mainText) {

        mainText.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                //ProgressPanel.this.copyFromClipboard();
                mainText.setBackground(new Color(255, 255, 210));
                mainText.setFont(new Font(DIALOG, BOLD, 12));
                mainText.setToolTipText("Extricator".equalsIgnoreCase(text) ? "Please provide valid https URL (e.g. https://domain.com)" : "Please provide valid Correlation-Id (e.g. hhjhj-513vhv-hjhj-hj)");
            }

            @Override
            public void focusLost(FocusEvent e) {

            }
        });


        mainText.addMouseListener(new MouseAdapter() {

            final int defaultTimeout = ToolTipManager.sharedInstance().getInitialDelay();

            final int defaultDismiss = ToolTipManager.sharedInstance().getDismissDelay();


            @Override
            public void mouseEntered(MouseEvent e) {
                ToolTipManager.sharedInstance().setInitialDelay(0);
                ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ToolTipManager.sharedInstance().setInitialDelay(this.defaultTimeout);
                ToolTipManager.sharedInstance().setDismissDelay(this.defaultDismiss);
            }
        });

        mainText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    btn.doClick();
                }
            }
        });
    }


    private BufferedImage loadImage(String imageName) {
        try {
            return ImageIO.read(this.getClass().getResource(separator + "artifacts" + separator + imageName));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, imageName, e);
        }
        return null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.extractBtn) {
            this.extricateLogs(this.apiUriTxt, this.corrIdTxt);
        } else if (e.getSource() == this.customBtn || e.getSource() == this.allBtn) {
            this.isCustom = this.customBtn.isSelected();
            this.isRaml = this.ramlCbx.isSelected();
            this.isExpt = this.exptCbx.isSelected();
            this.flipSepection(this.isCustom);
        } else if (e.getSource() == this.ramlCbx) {
            this.isRaml = this.ramlCbx.isSelected();
        } else if (e.getSource() == this.exptCbx) {
            this.isExpt = this.exptCbx.isSelected();
        } else if (e.getSource() == this.cancelBtn) {
            this.progressProcessor.timeout = 0;
            this.progressProcessor.executor.shutdownNow();
            this.toggleButton(true);
            this.apiUriTxt.setText("");
            this.corrIdTxt.setText("");
            this.apiUriTxt.grabFocus();
        }
    }

    private void toggleButton(final boolean toggle) {
        this.extractBtn.setVisible(toggle);
        this.extractBtn.setEnabled(toggle);
        this.cancelBtn.setVisible(!toggle);
        this.cancelBtn.setEnabled(!toggle);
        this.progressPane.setVisible(!toggle);
        this.progressBar.setVisible(!toggle);
        this.repaint();
        this.requestFocus();
    }

    private void flipSepection(final boolean isCustom) {
        this.allBtn.setSelected(isCustom);
        this.customBtn.setSelected(isCustom);
        this.ramlCbx.setEnabled(isCustom);
        this.exptCbx.setEnabled(isCustom);
        if (!isCustom) {
            this.ramlCbx.setSelected(isCustom);
            this.exptCbx.setSelected(isCustom);
        }
    }

    private void extricateLogs(JTextField apiUriTxt, JTextField corrIdTxt) {
        String url = isEmpty(apiUriTxt.getText()) ? null : apiUriTxt.getText().trim();
        String id = isEmpty(corrIdTxt.getText()) ? null : corrIdTxt.getText().trim();

        System.err.println("##############");
        System.err.println(url);
        System.err.println(id);
        try {
            if (null != url && null != id) {
                if (isBlank(url) || !this.isValidUrl(url)) {
                    throw new IllegalArgumentException("Please provide valid https URL (e.g. https://domain.com)");
                }
                if (isBlank(id) || !Pattern.compile(REGEX).matcher(id).find()) {
                    throw new IllegalArgumentException("Please provide valid Id (e.g. hjh7-hjfhj76-hfhf)");
                }
                this.toggleButton(false);
                this.progressProcessor = new ProgressProcessor(this);
                final String timestamp = this.getCurrentDate("yyyyMMddHHmmss");

                //All code inside Swingwoked Runs on a seperate thread
                final SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        try {
                            ProgressPanel.this.progressProcessor.extricateNow(timestamp, ProgressPanel.this.isCustom, ProgressPanel.this.isRaml, ProgressPanel.this.isExpt);
                            ProgressPanel.this.toggleButton(true);
                            if (ProgressPanel.this.progressProcessor.isPrinted()) {
                                JOptionPane.showMessageDialog(null,
                                        String.format("Logs are available for URL %s & Id %s \n and have been extracted to: \n%s", url, id,
                                                new File(ProgressProcessor.PATH + separator + timestamp).getCanonicalPath()),
                                        "Extricator Success",
                                        JOptionPane.INFORMATION_MESSAGE, null);
                            } else {
                                ProgressPanel.this.progressProcessor.timeout = 0;
                                JOptionPane.showMessageDialog(null,
                                        String.format("Logs are not available for URL %s & Id %s ", url, id),
                                        "Extricator Failure",
                                        JOptionPane.WARNING_MESSAGE, null);
                            }
                        } catch (final Exception e) {
                            if (!(e instanceof RejectedExecutionException)) {
                                ProgressPanel.this.handleException(apiUriTxt, corrIdTxt, url, id, e);
                            }
                        }
                        return ProgressPanel.this.progressProcessor.isPrinted();
                    }

                    @Override
                    public void done() {
                        apiUriTxt.setText("");
                        corrIdTxt.setText("");
                        apiUriTxt.grabFocus();
                    }
                };
                //Call SwingWorker from within the Swing Thread
                worker.execute();
            }

        } catch (final Exception e) {
            if (!(e instanceof RejectedExecutionException)) {
                this.handleException(apiUriTxt, corrIdTxt, url, id, e);
            }

        }
    }

    private void handleException(final JTextField apiUriTxt, final JTextField corrIdTxt, final String url, final String id, final Exception e) {
        LOGGER.log(Level.SEVERE, "Logger", e);
        final String message = (e instanceof IllegalArgumentException) ? e.getMessage() : "Unable to Generate Logs";
        JOptionPane.showMessageDialog(null, message, (isBlank(url) || !this.isValidUrl(url)) ? "URL " : ((isBlank(id) || !Pattern.compile(REGEX).matcher(id).find()) ? "Corr-Id " : "") + "Error",
                JOptionPane.ERROR_MESSAGE, null);

        if (isBlank(url) || !this.isValidUrl(url)) {
            apiUriTxt.setText("");
            apiUriTxt.grabFocus();
        } else if ((isBlank(id) || !Pattern.compile(REGEX).matcher(id).find())) {
            corrIdTxt.setText("");
            corrIdTxt.grabFocus();
        } else {
            apiUriTxt.setText("");
            corrIdTxt.setText("");
            apiUriTxt.grabFocus();
        }
        Efficacy.printToErr(e.getMessage());

    }

    private String getCurrentDate(final String yyyyMMddhhmmss) {
        final Calendar calendar = Calendar.getInstance();
        return new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'").format(calendar.getTime());
    }

    private boolean isValidUrl(final String url) {
        boolean isValidUrl = false;
        try {
            new URI(url).toURL();
            isValidUrl = true;
        } catch (final Exception e) {
            //LOGGER.log(Level.WARNING, "Invalid URL " + url, e);
        }

        return isValidUrl;
    }
}
