package com.boss.android.lite.plugin.file;

import com.boss.android.lite.plugin.utils.Icons;
import com.boss.android.lite.plugin.utils.LiteConfig;
import com.boss.android.lite.plugin.views.ListRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static com.intellij.openapi.ui.Messages.getInformationIcon;
import static com.intellij.openapi.ui.Messages.showMessageDialog;

public class LiteFileDialog extends JDialog {
    private JPanel contentPane;

    private JPanel editJPanel;
    private JList<FileListModel> fileList;
    private JCheckBox xmlCheckBox;
    private JTextField nameField;
    private JLabel iconLabel;
    private JCheckBox javaCheckBox;

    public LiteFileDialog() {
        setModal(true);
        setContentPane(contentPane);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }


            @Override
            public void windowLostFocus(WindowEvent e) {
                super.windowLostFocus(e);
                onCancel();
            }

        });
        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);



        xmlCheckBox.addChangeListener(e -> {
            nameField.requestFocusInWindow();
            nameField.requestFocus(true);
            nameField.requestFocus();
        });

        initTitle();
        initEdit();
        initList();
    }

    private void initList() {
        ListRenderer listRenderer = new ListRenderer();
        DefaultListModel<FileListModel> listModel = new DefaultListModel();
        listModel.addElement(new FileListModel(Icons.INSTANCE.getCreate_file(),"Lite", FileType.Lite));
        listModel.addElement(new FileListModel(Icons.INSTANCE.getCreate_file_1(),"Lite From Activity",FileType.LiteActivity));
        listModel.addElement(new FileListModel(Icons.INSTANCE.getCreate_file_2(),"Lite From Fragment",FileType.LiteFragment));
        listModel.addElement(new FileListModel(Icons.INSTANCE.getCreate_file_3(),"Lite From DialogFragment",FileType.LiteDialogFragment));

        int size = listModel.getSize();
        if(size > 8){
            size = 8;
        }
        fileList.setVisibleRowCount(size);
        fileList.setModel(listModel);
        fileList.setCellRenderer(listRenderer);
        fileList.addListSelectionListener(event -> {
            boolean valueIsAdjusting = event.getValueIsAdjusting();
            if(valueIsAdjusting){
                iconLabel.setIcon(fileList.getSelectedValue().getIcon());

                switch (fileList.getSelectedValue().getType()){
                    case LiteActivity:
                    case LiteFragment:
                    case LiteDialogFragment:
                        javaCheckBox.setEnabled(true);
                        xmlCheckBox.setEnabled(true);
                        break;
                    default:
                        javaCheckBox.setEnabled(false);
                        xmlCheckBox.setEnabled(false);
                        break;
                }
                nameField.requestFocus();
            }
        });

        fileList.setSelectedIndex(LiteConfig.Companion.getInstance().isPosition());

        switch (fileList.getSelectedValue().getType()){
            case LiteActivity:
            case LiteFragment:
            case LiteDialogFragment:
                javaCheckBox.setSelected(LiteConfig.Companion.getInstance().isJava());
                xmlCheckBox.setSelected(LiteConfig.Companion.getInstance().isXML());
                break;
            case Lite:
                javaCheckBox.setEnabled(false);
                xmlCheckBox.setEnabled(false);
                break;
            default:
                javaCheckBox.setEnabled(true);
                xmlCheckBox.setEnabled(false);
                break;
        }
    }

    private void initEdit() {
        iconLabel.setIcon(Icons.INSTANCE.getCreate_file_1());
        nameField.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        editJPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        Action action = new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
              run();
            }
        };
        nameField.addActionListener( action );

        fileList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                if(e.getKeyCode()  == KeyEvent.VK_ENTER){
                    run();
                }
            }
        });

        nameField.requestFocus();
    }
    private void run(){
        if(nameField.getText().trim().length() < 1){
            showMessageDialog(project, "Lite : lite name is empty.", "Lite", getInformationIcon());
            return;
        }
        boolean isSelect = xmlCheckBox.isSelected();
        boolean isJava = javaCheckBox.isSelected();
        new CrateFileCodeGenerate(project,psiDirectory,layoutDirectory,packageName,xmlPackageName,nameField.getText().trim(),fileList.getSelectedValue(),isSelect,isJava)
                .run(this);
    }

    private Point mouseClickPoint = null;
    private void initTitle() {
        contentPane.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                mouseClickPoint = e.getPoint();
            }
        });
        contentPane.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point newPoint = e.getLocationOnScreen();
                newPoint.translate(-mouseClickPoint.x, -mouseClickPoint.y); // Moves the point by given values from its location
                setLocation(newPoint);
            }
        });
    }


    public void onCancel() {
        // add your code here if necessary
        dispose();
    }


    public static void main(String[] args) {
        LiteFileDialog dialog = new LiteFileDialog();
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        System.exit(0);
    }

    private Project project;
    private PsiDirectory psiDirectory;
    private PsiDirectory layoutDirectory;

    private String packageName;
    private String xmlPackageName;

    public void setBasicInfo(@Nullable Project project, @NotNull PsiDirectory psiDirectory,  PsiDirectory layoutDirectory, @NotNull String packageName, @NotNull String xmlPackageName) {
        this.project = project;
        this.psiDirectory = psiDirectory;
        this.layoutDirectory = layoutDirectory;
        this.packageName = packageName;
        this.xmlPackageName = xmlPackageName;
    }
}

