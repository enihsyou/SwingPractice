package zwei.ui.mediator;

import zwei.dao.JDBCUtilities;
import zwei.model.Student;
import zwei.model.Teacher;
import zwei.model.User;
import zwei.ui.UiHelper;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.function.Supplier;

/**
 * Created on 2018-12-11
 *
 * @author 九条涼果 chunxiang.huang@hypers.com
 */
public class SplashInterface extends JPanel implements UserInterface {

  private static final long serialVersionUID = 5251739093646189753L;

  /** 用户ID输入文本框 */
  private JTextField idField;
  /** 密码输入文本框 */
  private JTextField pwField;

  /** 选择登录类型为学生的单选框 */
  private JRadioButton stuRadioBtn;
  /** 选择登录类型为教师的单选框 */
  private JRadioButton teaRadioBtn;

  /** 登录按钮 */
  private JButton loginBtn;
  /** 注册按钮 */
  private JButton registerBtn;

  public SplashInterface() {
    createSelf();

    /*添加操作回调函数*/
    idField.addActionListener(this::enterOnIdField);
    pwField.addActionListener(this::clickLogin);
    loginBtn.addActionListener(this::clickLogin);
    registerBtn.addActionListener(this::clickRegister);
  }


  @Override
  public void showInFrame(JFrame parent) {
    UiHelper.onFrameCenter(parent, frame -> {
      frame.setContentPane(this);
      frame.getRootPane().setDefaultButton(loginBtn);
      frame.setTitle("用户登录");
    });
  }

  @SuppressWarnings("Duplicates")
  private void createSelf() {
    idField = new JTextField();
    pwField = new JPasswordField();
    JLabel idLabel = new JLabel(" ID:", JLabel.TRAILING);
    JLabel pwLabel = new JLabel("PWD:", JLabel.TRAILING);
    idLabel.setLabelFor(idField);
    pwLabel.setLabelFor(pwField);


    loginBtn = new JButton("登录");
    registerBtn = new JButton("注册");

    stuRadioBtn = new JRadioButton("学生");
    teaRadioBtn = new JRadioButton("教师");
    ButtonGroup buttonGroup = new ButtonGroup();
    buttonGroup.add(teaRadioBtn);
    buttonGroup.add(stuRadioBtn);
    stuRadioBtn.setSelected(true);

    JPanel topPanel = new JPanel();
    topPanel.add(stuRadioBtn);
    topPanel.add(teaRadioBtn);
    topPanel.setMaximumSize(topPanel.getPreferredSize());

    JPanel midPanel = new JPanel(new SpringLayout());
    midPanel.add(idLabel);
    midPanel.add(idField);
    midPanel.add(pwLabel);
    midPanel.add(pwField);
    SpringUtilities.makeCompactGrid(midPanel, 2, 2, 6, 6, 6, 6);

    JPanel lstPanel = new JPanel();
    lstPanel.add(registerBtn);
    lstPanel.add(loginBtn);

    setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    setBorder(paddingBorder);
    add(topPanel);
    add(midPanel);
    add(lstPanel);
  }

  /**
   * 处理在用户名输入框按回车的事件
   *
   * @see #idField
   */
  private void enterOnIdField(ActionEvent actionEvent) {
    if (pwField.getText().isEmpty()) {
      pwField.requestFocusInWindow();
    } else {
      clickLogin(actionEvent);
    }
  }

  /**
   * 处理在密码框按回车或者点击登录按钮的事件
   *
   * @see #loginBtn
   */
  private void clickLogin(ActionEvent actionEvent) {
    String inputId       = idField.getText();
    String inputPassword = pwField.getText();

    if (inputId == null || inputId.isEmpty()) {
      idField.requestFocusInWindow();
      return;
    }
    if (inputPassword == null || inputPassword.isEmpty()) {
      pwField.requestFocusInWindow();
      return;
    }

    User                    user;
    Supplier<UserInterface> Interface;
    if (stuRadioBtn.isSelected()) {
      user = JDBCUtilities.dbop(conn -> {return Student.retrieveOne(conn, inputId);});
      Interface = StudentInterface::new;
    } else if (teaRadioBtn.isSelected()) {
      user = JDBCUtilities.dbop(conn -> {return Teacher.retrieveOne(conn, inputId);});
      Interface = TeacherInterface::new;
    } else {
      // should not happen
      JOptionPane.showMessageDialog(this, "单选框未选中", "异常发生", JOptionPane.WARNING_MESSAGE);
      return;
    }

    if (user == null) {
      JOptionPane.showMessageDialog(this, "用户不存在", "登录失败", JOptionPane.WARNING_MESSAGE);
      return;
    }

    if (user.comparePassword(inputPassword)) {
      UserInterface panel = Interface.get();
      panel.putArgument("user", user);
      switchPanel(panel);
    } else {
      JOptionPane.showMessageDialog(this, "密码错误", "登录失败", JOptionPane.WARNING_MESSAGE);
    }
  }

  /**
   * 处理点击注册按钮的事件
   *
   * @see #registerBtn
   */
  private void clickRegister(ActionEvent actionEvent) {
    Class<? extends User> type;
    if (stuRadioBtn.isSelected()) {
      type = Student.class;
    } else if (teaRadioBtn.isSelected()) {
      type = Teacher.class;
    } else {
      JOptionPane.showMessageDialog(this, "单选框未选中", "异常发生", JOptionPane.WARNING_MESSAGE);
      return;
    }

    switchPanel(new RegisterInterface(type));
  }

  /**
   * Replace window's content panel to new user interface, take user to next step.
   */
  private void switchPanel(UserInterface Interface) {
    JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);
    // replace content panel
    setVisible(false);
    Interface.showInFrame(frame);
    // no need to pack
  }
}

