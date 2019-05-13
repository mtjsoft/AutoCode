package main;

import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.JBColor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 生成kotlin MVP代码
 */
public class mvpCreate extends AnAction {
    private AnActionEvent event;
    private Project project;
    private JDialog jFrame;
    JTextField name;
    JTextField username;
    /**
     * 创建包名
     */
    JTextField packageName;
    /**
     * 单选类型按钮
     */
    JRadioButton baseActivityJB;
    JRadioButton baseFragmentJB;
    JRadioButton baseNotActivityJB;
    JRadioButton baseDataActivityJB;
    JRadioButton baseDataFragmentJB;
    JRadioButton baseRecycleViewActivityJB;
    JRadioButton baseRecycleViewFragmentJB;
    /*项目包名*/
    private String packagebase = "";
    private String modelPath = "";
    private String adapterPath = "";

    private enum CodeType {
        BaseActivity, BaseFragment, BaseNotActivity, BaseDataActivity, BaseDataFragment, BaseRecycleViewActivity, BaseRecycleViewFragment, Presenter, Contract
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here
        event = e;
        project = e.getProject();
        packagebase = readPackageName();
        initSelectView();
        project.getProjectFilePath();
    }

    private void initSelectView() {
        jFrame = new JDialog();// 定义一个窗体Container container = getContentPane();
        jFrame.setModal(true);
        Container container = jFrame.getContentPane();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        JPanel panelname1 = new JPanel();// /定义一个面板
        panelname1.setLayout(new GridLayout(1, 1));
        panelname1.setBorder(BorderFactory.createTitledBorder("创建包名"));
        packageName = new JTextField();
        packageName.setText("请输入包名用.隔开");
        packageName.addFocusListener(new MyFocusListener("请输入包名用.隔开", packageName));
        panelname1.add(packageName);

        JPanel panelname2 = new JPanel();// /定义一个面板
        panelname2.setLayout(new GridLayout(1, 1));
        panelname2.setBorder(BorderFactory.createTitledBorder("创建类名"));
        name = new JTextField();
        name.setText("请输入类名字");
        name.addFocusListener(new MyFocusListener("请输入类名字", name));
        panelname2.add(name);

        JPanel panelname3 = new JPanel();// /定义一个面板
        panelname3.setLayout(new GridLayout(1, 1));
        panelname3.setBorder(BorderFactory.createTitledBorder("注释作者"));
        username = new JTextField();
        username.setText("请输入注释的作者");
        username.addFocusListener(new MyFocusListener("请输入注释的作者", username));
        panelname3.add(username);

        container.add(panelname1);
        container.add(panelname2);
        container.add(panelname3);


        baseActivityJB = new JRadioButton("BaseActivity");// 定义一个单选按钮
        baseFragmentJB = new JRadioButton("BaseFragment");// 定义一个单选按钮
        baseNotActivityJB = new JRadioButton("BaseNotActivity");// 定义一个单选按钮
        baseDataActivityJB = new JRadioButton("BaseDataActivity");// 定义一个单选按钮
        baseDataFragmentJB = new JRadioButton("BaseDataFragment");// 定义一个单选按钮
        baseRecycleViewActivityJB = new JRadioButton("BaseRecycleViewActivity");// 定义一个单选按钮
        baseRecycleViewFragmentJB = new JRadioButton("BaseRecycleViewFragment");// 定义一个单选按钮

        baseActivityJB.setSelected(true);

        JPanel panel = new JPanel();// /定义一个面板

        panel.setBorder(BorderFactory.createTitledBorder("选择生成代码的类型"));// 定义一个面板的边框显示条
        panel.setLayout(new GridLayout(4, 2));// 定义排版，一行三列
        panel.add(baseActivityJB);// 加入组件
        panel.add(baseFragmentJB);// 加入组件
        panel.add(baseDataActivityJB);// 加入组件
        panel.add(baseDataFragmentJB);//
        panel.add(baseRecycleViewActivityJB);// 加入组件
        panel.add(baseRecycleViewFragmentJB);// 加入组件
        panel.add(baseNotActivityJB);// 加入组件

        ButtonGroup group = new ButtonGroup();
        group.add(baseActivityJB);
        group.add(baseFragmentJB);
        group.add(baseDataActivityJB);
        group.add(baseDataFragmentJB);
        group.add(baseRecycleViewActivityJB);
        group.add(baseRecycleViewFragmentJB);
        group.add(baseNotActivityJB);
        container.add(panel);// 加入面板

        JPanel menu = new JPanel();
        menu.setLayout(new GridLayout(1, 3));

        Button cancle = new Button();
        cancle.setLabel("取消");
        cancle.addActionListener(actionListener);
        cancle.setForeground(JBColor.RED);

        Button ok = new Button();
        ok.setLabel("确定");
        ok.addActionListener(actionListener);
        ok.setForeground(JBColor.GREEN);

        Button fuzhi = new Button();
        fuzhi.setLabel("光标位置生成Rx代码");
        fuzhi.addActionListener(actionListener);
        fuzhi.setForeground(JBColor.BLUE);

        menu.add(cancle);
        menu.add(ok);
        menu.add(fuzhi);
        container.add(menu);


        jFrame.setSize(450, 350);
        jFrame.setLocationRelativeTo(null);
        jFrame.setResizable(false);// 不可缩放
        Image myIconImage = Toolkit.getDefaultToolkit().createImage(this.getClass().getResource("/image/icon72.png"));
        jFrame.setIconImage(myIconImage);
        jFrame.setTitle("一键生成KotlinMVP代码");
        jFrame.setVisible(true);
    }


    /**
     * 输入框获取焦点时，清空提示
     */
    class MyFocusListener implements FocusListener {
        String info;
        JTextField jtf;

        public MyFocusListener(String info, JTextField jtf) {
            this.info = info;
            this.jtf = jtf;
        }

        @Override
        public void focusGained(FocusEvent e) {//获得焦点的时候,清空提示文字
            String temp = jtf.getText();
            if (temp.equals(info)) {
                jtf.setText("");
            }
        }

        @Override
        public void focusLost(FocusEvent e) {//失去焦点的时候,判断如果为空,就显示提示文字
            String temp = jtf.getText();
            if (temp.equals("")) {
                jtf.setText(info);
            }
        }
    }

    /**
     * 读取包名
     *
     * @return
     */
    private String readPackageName() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(project.getBasePath() + "/App/src/main/AndroidManifest.xml");
            NodeList dogList = doc.getElementsByTagName("manifest");
            for (int i = 0; i < dogList.getLength(); i++) {
                Node dog = dogList.item(i);
                Element elem = (Element) dog;
                return elem.getAttribute("package");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private ActionListener actionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("取消")) {
                jFrame.dispose();
            } else if (e.getActionCommand().equals("光标位置生成Rx代码")) {
                String copyString = "val map = HashMap<String, Any>()\n" +
                        "        HttpRxObservable.getObservable(\n" +
                        "                ApiUtil.instance().apiObservable(\"user/login\", ApiUtil.POST, map, null),\n" +
                        "                mActivityProvider,\n" +
                        "                activityEvent\n" +
                        "        ).subscribe(object : HttpRxObserver<HttpResponse>() {\n" +
                        "            override fun onStart(d: Disposable) {\n" +
                        "                getView().showLoadingUI(\"正在登录...\", false)\n" +
                        "                log(\"开始了\")\n" +
                        "            }\n" +
                        "\n" +
                        "            override fun onError(e: ApiException) {\n" +
                        "                getView().hideLoadingUI()\n" +
                        "                toast(e.msg)\n" +
                        "            }\n" +
                        "\n" +
                        "            override fun onSuccess(response: HttpResponse) {\n" +
                        "                getView().hideLoadingUI()\n" +
                        "                log(\"成功了：\" + response.result)\n" +
                        "            }\n" +
                        "        })";

                int exitCode = Messages.showOkCancelDialog("确认在光标位置生成Rx代码吗？","提示",null);
                if (exitCode == Messages.OK) {
                    insetStringAfterOffset(event, copyString);
                    jFrame.dispose();
                }
            } else {
                if ("请输入包名用.隔开".equals(packageName.getText())) {
                    Messages.showErrorDialog(project, "请输入包名用.号隔开", "提示");
                } else if ("请输入类名字".equals(name.getText())) {
                    Messages.showErrorDialog(project, "请输入类名字", "提示");
                } else if ("请输入注释的作者".equals(username.getText())) {
                    Messages.showErrorDialog(project, "请输入注释的作者", "提示");
                } else {
                    jFrame.dispose();
                    clickCreateFile();
                    showNotification("implementation", "Success", "Mvp一键生成代码成功！");
                }
            }
        }
    };

    private void insetStringAfterOffset(AnActionEvent e, String copyString) {
        Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        CaretModel caretModel = editor.getCaretModel();
        int offset = caretModel.getOffset();
        WriteCommandAction.runWriteCommandAction(e.getProject(), new Runnable() {
            @Override
            public void run() {
                editor.getDocument().insertString(offset, copyString);
            }
        });
        showNotification("implementation", "Success", "Rx一键生成代码成功！");
    }


    /**
     * 将字符串复制到剪切板。
     */
    public static void setSysClipboardText(String writeMe) {
        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable tText = new StringSelection(writeMe);
        clip.setContents(tText, null);
    }


    private void clickCreateFile() {
        if (baseActivityJB.isSelected()) {
            createFiles(CodeType.Contract);
            createFiles(CodeType.Presenter);
            createFiles(CodeType.BaseActivity);
        }
        if (baseFragmentJB.isSelected()) {
            createFiles(CodeType.Contract);
            createFiles(CodeType.Presenter);
            createFiles(CodeType.BaseFragment);
        }
        if (baseNotActivityJB.isSelected()) {
            createFiles(CodeType.BaseNotActivity);
        }
        if (baseDataActivityJB.isSelected()) {
            createFiles(CodeType.Contract);
            createFiles(CodeType.Presenter);
            createFiles(CodeType.BaseDataActivity);
        }
        if (baseDataFragmentJB.isSelected()) {
            createFiles(CodeType.Contract);
            createFiles(CodeType.Presenter);
            createFiles(CodeType.BaseDataFragment);
        }
        if (baseRecycleViewActivityJB.isSelected()) {
            createFiles(CodeType.BaseRecycleViewActivity);
        }
        if (baseRecycleViewFragmentJB.isSelected()) {
            createFiles(CodeType.BaseRecycleViewFragment);
        }
    }


    /**
     * 创建文件
     */
    private void createFiles(CodeType codeType) {
        String filename = "";
        String content = "";

        String packagepath = packagebase.replace(".", "/");
        String apppath = project.getBasePath() + "/App/src/main/java/" + packagepath + "/" + packageName.getText().replace(".", "/");
        modelPath = project.getBasePath() + "/App/src/main/java/" + packagepath + "/model";
        adapterPath = project.getBasePath() + "/App/src/main/java/" + packagepath + "/adapter";
        switch (codeType) {
            case BaseActivity:
                filename = "TemplateActivity.txt";
                content = ReadFile(filename);
                // 1.通用流程,处理顶部注释
                content = dealFileTitle(content);
                //处理activity
                content = dealActivity(content);
                writetoFile(content, apppath, name.getText() + "Activity.kt");
                break;
            case BaseFragment:
                filename = "TemplateFragment.txt";
                content = ReadFile(filename);
                // 1.通用流程,处理顶部注释
                content = dealFileTitle(content);
                //处理fragment
                content = dealFragment(content);
                writetoFile(content, apppath, name.getText() + "Fragment.kt");
                break;
            case BaseNotActivity:
                filename = "TemplateNotActivity.txt";
                content = ReadFile(filename);
                // 1.通用流程,处理顶部注释
                content = dealFileTitle(content);
                //处理activity
                content = dealActivity(content);
                writetoFile(content, apppath, name.getText() + "Activity.kt");
                break;
            case BaseDataActivity:
                filename = "TemplateDataActivity.txt";
                content = ReadFile(filename);
                // 1.通用流程,处理顶部注释
                content = dealFileTitle(content);
                //处理activity
                content = dealActivity(content);
                writetoFile(content, apppath, name.getText() + "Activity.kt");
                break;
            case BaseDataFragment:
                filename = "TemplateDataFragment.txt";
                content = ReadFile(filename);
                // 1.通用流程,处理顶部注释
                content = dealFileTitle(content);
                //处理activity
                content = dealActivity(content);
                writetoFile(content, apppath, name.getText() + "Fragment.kt");
                break;
            case BaseRecycleViewActivity:
                //创建model
                filename = "TemplateModel.txt";
                content = ReadFile(filename);
                // 1.通用流程,处理顶部注释
                content = dealFileTitle(content);
                //处理
                content = dealActivity(content);
                writetoFile(content, modelPath, name.getText() + "Model.java");

                //创建model
                filename = "TemplateRecycleViewAdapter.txt";
                content = ReadFile(filename);
                // 1.通用流程,处理顶部注释
                content = dealFileTitle(content);
                //处理
                content = dealActivity(content);
                writetoFile(content, adapterPath, name.getText() + "Adapter.java");

                //创建Activity
                filename = "TemplateRecycleViewActivity.txt";
                content = ReadFile(filename);
                // 1.通用流程,处理顶部注释
                content = dealFileTitle(content);
                //处理activity
                content = dealActivity(content);
                writetoFile(content, apppath, name.getText() + "RecycleViewActivity.kt");
                break;
            case BaseRecycleViewFragment:
                //创建model
                filename = "TemplateModel.txt";
                content = ReadFile(filename);
                // 1.通用流程,处理顶部注释
                content = dealFileTitle(content);
                //处理
                content = dealFragment(content);
                writetoFile(content, modelPath, name.getText() + "Model.java");

                //创建model
                filename = "TemplateRecycleViewAdapter.txt";
                content = ReadFile(filename);
                // 1.通用流程,处理顶部注释
                content = dealFileTitle(content);
                //处理
                content = dealFragment(content);
                writetoFile(content, adapterPath, name.getText() + "Adapter.java");

                filename = "TemplateRecycleViewFragment.txt";
                content = ReadFile(filename);
                // 1.通用流程,处理顶部注释
                content = dealFileTitle(content);
                //处理fragment
                content = dealFragment(content);
                writetoFile(content, apppath, name.getText() + "RecycleViewFragment.kt");
                break;
            case Contract:
                filename = "TemplateContract.txt";
                content = ReadFile(filename);
                // 1.通用流程,处理顶部注释
                content = dealFileTitle(content);
                //处理fragment
                content = dealFragment(content);
                writetoFile(content, apppath, name.getText() + "Contract.kt");
                break;
            case Presenter:
                filename = "TemplatePresenter.txt";
                content = ReadFile(filename);
                // 1.通用流程,处理顶部注释
                content = dealFileTitle(content);
                //处理fragment
                content = dealFragment(content);
                writetoFile(content, apppath, name.getText() + "Presenter.kt");
                break;
        }

    }

    private String ReadFile(String filename) {
        InputStream in = null;
        in = this.getClass().getResourceAsStream("/Template/" + filename);
        String content = "";
        try {
            content = new String(readStream(in));
        } catch (Exception e) {
        }
        return content;
    }

    /**
     * 处理activity
     *
     * @param content
     * @return
     */
    private String dealActivity(String content) {
        content = content.replace("$name", name.getText());
        content = content.replace("$packagename", packagebase + "." + packageName.getText());
        content = content.replace("$modelpackagename", packagebase + ".model");
        content = content.replace("$adapterpackagename", packagebase + ".adapter");
        return content;
    }

    /**
     * 处理fragment
     *
     * @param content
     * @return
     */
    private String dealFragment(String content) {
        content = content.replace("$name", name.getText());
        content = content.replace("$packagename", packagebase + "." + packageName.getText());
        content = content.replace("$modelpackagename", packagebase + ".model");
        content = content.replace("$adapterpackagename", packagebase + ".adapter");
        return content;
    }

    /**
     * 处理
     *
     * @param content
     */
    private String dealFileTitle(String content) {
        content = content.replace("$author", username.getText());
        content = content.replace("$packagebase", packagebase + "." + packageName.getText());
        content = content.replace("$date", getNowDateShort());
        content = content.replace("$modelpackagename", packagebase + ".model");
        content = content.replace("$adapterpackagename", packagebase + ".adapter");
        return content;
    }

    public String getNowDateShort() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    /**
     * 写入文件
     *
     * @param content
     * @param filepath
     * @param filename
     */
    private void writetoFile(String content, String filepath, String filename) {
        try {
            File floder = new File(filepath);
            // if file doesnt exists, then create it
            if (!floder.exists()) {
                floder.mkdirs();
            }
            File file = new File(filepath + "/" + filename);
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public byte[] readStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[1024];
            int len = -1;
            while ((len = inStream.read(buffer)) != -1) {
                outSteam.write(buffer, 0, len);
                System.out.println(new String(buffer));
            }

        } catch (IOException e) {
        } finally {
            outSteam.close();
            inStream.close();
        }
        return outSteam.toByteArray();
    }

    private void showNotification(String displayId, String title, String message) {
        NotificationGroup noti = new NotificationGroup(displayId, NotificationDisplayType.BALLOON, true);
        noti.createNotification(
                title,
                message,
                NotificationType.INFORMATION,
                null
        ).notify(event.getProject());
    }
}
