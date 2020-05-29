package com.bhadouriya.raml.tools;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.bhadouriya.raml.tools.AccreditorProcessor.install;
import static java.awt.FileDialog.LOAD;
import static java.awt.Font.*;
import static java.awt.GridBagConstraints.*;
import static java.io.File.separator;
import static org.apache.commons.lang.StringUtils.isEmpty;

public class AccreditorPanel extends JFrame implements ActionListener {

    private static final Logger LOGGER = Logger.getLogger(AccreditorPanel.class.getName());
    private static AccreditorPanel instance;
    JButton certImportBtn = new JButton();
    JButton certImportFileBtn = new JButton();
    JButton certDeleteBtn = new JButton();
    JTextField certImportText;
    JTextField certImportFileText;
    JTextField certDeleteText;

    boolean parent;

    private AccreditorPanel() {
        this.setTitle("Accreditor");
        this.ininUI();
    }

    private AccreditorPanel(boolean parent) {
        this();
        this.parent = parent;
    }

    public static AccreditorPanel getInstance(boolean parent) {
        if (null == instance) {
            instance = new AccreditorPanel(parent);
        } else {
            try {
                final String clipboardText = getClipboardContents();
                instance.certImportText.setText(clipboardText);
            } catch (final Exception e) {
                LOGGER.log(Level.SEVERE, "Instance", e);
            }
        }
        instance.setVisible(true);
        return instance;
    }

    private static Clipboard getSystemClipboard() {
        return Toolkit.getDefaultToolkit().getSystemClipboard();

    }

    private static String getClipboardContents() throws IOException, UnsupportedFlavorException {
        final Clipboard systemClipboard = getSystemClipboard();
        final DataFlavor dataFlavor = DataFlavor.stringFlavor;

        if (systemClipboard.isDataFlavorAvailable(dataFlavor)) {
            final Object text = systemClipboard.getData(dataFlavor);
            return (String) text;
        }
        return null;
    }

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                getInstance(false);
            }
        });
    }

    private void ininUI() {
        //setDefaultLookAndFeelDecorated(true);

        this.setIconImage(this.loadImage("cert.png"));
        //this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        String title = this.getTitle();

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Object[] options = {"Yes", "No"};

                //Ask for confirmation before termination the program.
                int option = JOptionPane.showOptionDialog(null, "Are you sure you want to close the '" + title + "' application?",
                        "Close Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);

                if (option == JOptionPane.YES_OPTION) {
                    if (AccreditorPanel.this.parent) {
                        AccreditorPanel.this.dispose();
                    } else {
                        System.exit(0);
                    }
                }
            }

        });

        this.setResizable(false);
        this.setSize(450, 130);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.certImportText = new JTextField(30) {

            @Override
            public JToolTip createToolTip() {
                final JToolTip toolTip = super.createToolTip();
                toolTip.setBackground(Color.BLACK);
                toolTip.setForeground(new Color(255, 255, 51));
                toolTip.setFont(new Font(DIALOG, BOLD + ITALIC, 10));
                return toolTip;
            }
        };
        this.certImportFileText = new JTextField(30) {

            @Override
            public JToolTip createToolTip() {
                final JToolTip toolTip = super.createToolTip();
                toolTip.setBackground(Color.BLACK);
                toolTip.setForeground(new Color(255, 255, 51));
                toolTip.setFont(new Font(DIALOG, BOLD + ITALIC, 10));
                return toolTip;
            }
        };
        this.certDeleteText = new JTextField(30) {

            @Override
            public JToolTip createToolTip() {
                final JToolTip toolTip = super.createToolTip();
                toolTip.setBackground(Color.BLACK);
                toolTip.setForeground(new Color(255, 255, 51));
                toolTip.setFont(new Font(DIALOG, BOLD + ITALIC, 10));
                return toolTip;
            }
        };
        this.createTabbedPane();
    }

    private void createTabbedPane() {
        final JTabbedPane tabbedPane = new JTabbedPane();
        final ImageIcon icon = null;

        final JComponent importCert = this.makeTextPane("Import From URL", this.certImportBtn, this.certImportText, true);
        tabbedPane.addTab("Import from URL", icon, importCert, "Import Missing Certificate");
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

        final JComponent importFileCert = this.makeTextPane("Import From File", this.certImportFileBtn, this.certImportFileText, true);
        tabbedPane.addTab("Import from File", icon, importFileCert, "Import Missing Certificate");
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

        final JComponent deleteCert = this.makeTextPane("Delete", this.certDeleteBtn, this.certDeleteText, true);
        tabbedPane.addTab("Delete Cert", icon, deleteCert, "Delete Existing Certificate");
        tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);

        tabbedPane.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(final FocusEvent e) {
                switch (tabbedPane.getSelectedIndex()) {
                    case 0:
                        AccreditorPanel.this.certImportText.grabFocus();
                        break;
                    case 1:
                        AccreditorPanel.this.certImportFileText.grabFocus();
                        break;
                    case 2:
                        AccreditorPanel.this.certDeleteText.grabFocus();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void focusLost(final FocusEvent e) {

            }
        });
        //Add tabbed pane o this panel
        this.add(tabbedPane, BorderLayout.CENTER);

        //The following line enables to use scrolling tabs.
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

    }

    protected JComponent makeTextPane(final String text, final JButton jButton, final JTextField jTextField, final boolean urlOnly) {
        final JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        final GridBagConstraints gridBagConstraints = new GridBagConstraints();

        jTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(final FocusEvent e) {
                jTextField.setBackground(new Color(255, 255, 10));
                jTextField.setFont(new Font(DIALOG, BOLD, 12));
                jTextField.setToolTipText("Please provide valid https URL (e.g. https://domain.com)" +
                        (!urlOnly ? " OR Alias Name" : ""));
            }

            @Override
            public void focusLost(final FocusEvent e) {

            }
        });

        jTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    jButton.doClick();
                }
            }
        });

        jTextField.addMouseListener(new MouseAdapter() {

            final int defaultTimeout = ToolTipManager.sharedInstance().getInitialDelay();

            final int defaultDismiss = ToolTipManager.sharedInstance().getDismissDelay();


            @Override
            public void mouseEntered(final MouseEvent e) {
                ToolTipManager.sharedInstance().setInitialDelay(0);
                ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
            }

            @Override
            public void mouseExited(final MouseEvent e) {
                ToolTipManager.sharedInstance().setInitialDelay(this.defaultTimeout);
                ToolTipManager.sharedInstance().setDismissDelay(this.defaultDismiss);
            }
        });

        final JLabel lblUrl = new JLabel("Delete".equals(text) ? "URL/Alias" : "URL:");
        lblUrl.setFont(new Font(DIALOG, BOLD, 12));
        lblUrl.setLabelFor(jTextField);

        jButton.setText(text);
        jButton.setMargin(new Insets(1, 1, 1, 1));
        jButton.setFont(new Font(DIALOG, BOLD, 12));
        jButton.setToolTipText(text + " Certificate");
        jButton.addActionListener(this);

        final KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_MASK);
        final InputMap inputMap = jButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        final ActionMap actionMap = jButton.getActionMap();

        inputMap.put(keyStroke, keyStroke.toString());


        actionMap.put(keyStroke.toString(), new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                jButton.doClick();
            }
        });

        final JPanel importPanel = new JPanel();

        importPanel.setLayout(new GridBagLayout());

        final GridBagConstraints bagConstraints = new GridBagConstraints();

        bagConstraints.anchor = NORTHWEST;
        bagConstraints.fill = HORIZONTAL;
        bagConstraints.insets = new Insets(1, 1, 1, 1);
        bagConstraints.ipadx = 1;
        bagConstraints.ipady = 1;

        bagConstraints.gridx = 0;
        bagConstraints.gridy = 0;
        importPanel.add(lblUrl, bagConstraints);

        bagConstraints.gridx = 1;
        bagConstraints.gridy = 0;
        bagConstraints.weightx = 25.0;
        bagConstraints.gridwidth = REMAINDER;
        importPanel.add(jTextField, bagConstraints);

        bagConstraints.gridx = 1;
        bagConstraints.gridy = 1;
        bagConstraints.fill = NONE;
        bagConstraints.anchor = LAST_LINE_END;
        importPanel.add(jButton, bagConstraints);

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

    private BufferedImage loadImage(final String imageName) {
        try {
            return ImageIO.read(this.getClass().getResource(separator + "artifacts" + separator + imageName));
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, imageName, e);
        }
        return null;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == this.certImportBtn) {
            this.importCertificate(false, this.certImportText, null);
        } else if (e.getSource() == this.certImportFileBtn) {
            final FileDialog fileDialog = new FileDialog(this, "Choose a certificate file", LOAD);
            fileDialog.setDirectory(System.getProperty("user.home") + "/Downloads");
            fileDialog.setFile("*.cer;*.crt;*.cert;*.txt");
            fileDialog.setVisible(true);
            final String fileName = fileDialog.getFile();
            final String fileDir = fileDialog.getDirectory();

            if (null != fileName) {
                LOGGER.log(Level.INFO, "File Chosen" + fileDir + fileName);
                this.importCertificate(false, this.certImportFileText, fileDir + fileName);
            }
        } else if (e.getSource() == this.certDeleteBtn) {
            this.importCertificate(true, this.certDeleteText, null);
        }
    }

    private void importCertificate(final boolean isDel, final JTextField cerText, final String certFilePath) {
        String url = null;

        try {
            url = cerText.getText();
            if (null != url) {
                url = url.trim();
                if (isEmpty(url)) {
                    throw new IllegalArgumentException("Please provide valid https URL (e.g. https://domain.com)" +
                            (isDel ? " OR Alias Name" : ""));
                }
                install(url, isDel, certFilePath, null, null);
                cerText.setText("");
                cerText.grabFocus();
            }


        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, (null != e.getMessage() ? e.getMessage() : "Unable to "
                            + (isDel ? "Delete " : "Import ")), (isEmpty(url) ? "URL " : (isDel ? "Delete " : "Import ")) + "Error",
                    JOptionPane.ERROR_MESSAGE, null);
            cerText.setText("");
            cerText.grabFocus();
            LOGGER.log(Level.WARNING, "Certificate", e);
        }
    }
}
