// package com.csms;

// import com.formdev.flatlaf.FlatLightLaf;
// import com.csms.entity.RoleName;
// import com.csms.entity.TableDashboardItem;
// import com.csms.view.operation.dashboard.TableDashboardPanel;

// import javax.swing.JFrame;
// import javax.swing.JOptionPane;
// import javax.swing.SwingUtilities;

// public class TableDashboardUITest {

//         public static void main(String[] args) {
//                 FlatLightLaf.setup();

//                 SwingUtilities.invokeLater(() -> {
//                         JFrame frame = new JFrame(
//                                         "CSMS - Waiter Dashboard");

//                         frame.setDefaultCloseOperation(
//                                         JFrame.EXIT_ON_CLOSE);

//                         TableDashboardPanel dashboardPanel = new TableDashboardPanel(
//                                         RoleName.WAITER,

//                                         TableDashboardUITest::openCreateOrder,

//                                         TableDashboardUITest::openOrderDetail);

//                         frame.setContentPane(
//                                         dashboardPanel);

//                         frame.setSize(1400, 850);
//                         frame.setLocationRelativeTo(null);
//                         frame.setVisible(true);
//                 });
//         }

//         private static void openCreateOrder(
//                         TableDashboardItem item) {
//                 JOptionPane.showMessageDialog(
//                                 null,
//                                 "Sẽ mở giao diện tạo đơn cho bàn "
//                                                 + item.getTableNumber(),
//                                 "Tạo đơn",
//                                 JOptionPane.INFORMATION_MESSAGE);
//         }

//         private static void openOrderDetail(
//                         TableDashboardItem item) {
//                 JOptionPane.showMessageDialog(
//                                 null,
//                                 "Sẽ mở chi tiết đơn "
//                                                 + item.getOrderId()
//                                                 + " của bàn "
//                                                 + item.getTableNumber(),
//                                 "Chi tiết bàn",
//                                 JOptionPane.INFORMATION_MESSAGE);
//         }
// }